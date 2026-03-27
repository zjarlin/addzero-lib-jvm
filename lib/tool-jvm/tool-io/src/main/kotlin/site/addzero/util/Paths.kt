package site.addzero.util

import java.io.File

private const val USER_HOME_PROPERTY = "user.home"
private const val TEMP_DIR_PROPERTY = "java.io.tmpdir"
private const val OS_NAME_PROPERTY = "os.name"
private const val LOCAL_APP_DATA_ENV = "LOCALAPPDATA"
private const val APP_DATA_ENV = "APPDATA"
private const val XDG_DATA_HOME_ENV = "XDG_DATA_HOME"

object PathUtil {
    fun userHomeDir(): File {
        return File(requiredSystemProperty(USER_HOME_PROPERTY))
    }

    fun tempDir(): File {
        return File(requiredSystemProperty(TEMP_DIR_PROPERTY))
    }

    fun appDataDir(
        appName: String,
        createDirectories: Boolean = false,
    ): File {
        return appDataDir(
            appName = appName,
            createDirectories = createDirectories,
            osName = System.getProperty(OS_NAME_PROPERTY).orEmpty(),
            userHome = userHomeDir(),
            localAppData = environmentValue(LOCAL_APP_DATA_ENV),
            appData = environmentValue(APP_DATA_ENV),
            xdgDataHome = environmentValue(XDG_DATA_HOME_ENV),
        )
    }


    fun child(
        parent: File,
        vararg names: String,
        createDirectories: Boolean = false,
    ): File {
        val resolved = names.fold(parent) { current, name ->
            require(name.isNotBlank()) {
                "子路径不能为空"
            }
            File(current, name)
        }
        return materializeDirectory(resolved, createDirectories)
    }

    internal fun appDataDir(
        appName: String,
        createDirectories: Boolean,
        osName: String,
        userHome: File,
        localAppData: String?,
        appData: String?,
        xdgDataHome: String?,
    ): File {
        val normalizedAppName = appName.trim()
        require(normalizedAppName.isNotEmpty()) {
            "应用名不能为空"
        }

        val resolvedDirectory = when {
            osName.contains("Mac", ignoreCase = true) -> {
                child(userHome, "Library", "Application Support", normalizedAppName)
            }

            osName.contains("Windows", ignoreCase = true) -> {
                val baseDir = localAppData
                    ?.takeIf { it.isNotBlank() }
                    ?: appData?.takeIf { it.isNotBlank() }
                    ?: userHome.absolutePath
                File(baseDir, normalizedAppName)
            }

            else -> {
                val baseDir = xdgDataHome?.takeIf { it.isNotBlank() }
                    ?: child(userHome, ".local", "share").absolutePath
                File(baseDir, normalizedAppName)
            }
        }
        return materializeDirectory(resolvedDirectory, createDirectories)
    }

    private fun materializeDirectory(
        directory: File,
        createDirectories: Boolean,
    ): File {
        if (!createDirectories) {
            return directory
        }
        return directory.ensureDirectory()
    }

    private fun propertyValue(name: String): String? {
        if (name.isBlank()) {
            return null
        }
        return System.getProperty(name)?.takeIf { it.isNotBlank() }
    }

    private fun environmentValue(name: String): String? {
        if (name.isBlank()) {
            return null
        }
        return System.getenv(name)?.takeIf { it.isNotBlank() }
    }

    private fun requiredSystemProperty(name: String): String {
        val value = System.getProperty(name)?.trim()
        check(!value.isNullOrEmpty()) {
            "系统属性缺失：$name"
        }
        return value
    }
}
