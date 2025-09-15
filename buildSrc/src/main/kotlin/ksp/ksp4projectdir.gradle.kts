import util.generateProjectDirConfigMap


plugins {
    id("com.google.devtools.ksp")
}

val projectDirConfigMap = generateProjectDirConfigMap()

ksp {
    projectDirConfigMap.forEach { (_, moduleConfig) ->
        moduleConfig.forEach { (key, value) ->
            arg(key, value)
        }
    }
}
