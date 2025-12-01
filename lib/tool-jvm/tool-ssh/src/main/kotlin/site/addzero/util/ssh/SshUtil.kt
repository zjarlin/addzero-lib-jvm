@file:JvmName("SshUtil")

package site.addzero.util.ssh

import kotlin.jvm.JvmName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.IOUtils
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.Response
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.sftp.SFTPException
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FileSystemFile
import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object SshUtil {

    fun connect(config: SshConfig): SshSession = SshSession(config)

    inline fun <T> use(config: SshConfig, block: (SshSession) -> T): T {
        return SshSession(config).use(block)
    }

    fun executeSync(config: SshConfig, command: String): SshResult {
        return use(config) { session ->
            session.executeSync(command)
        }
    }

    fun executeStream(config: SshConfig, command: String): Flow<String> {
        return flow {
            SshSession(config).use { session ->
                session.executeStreamInternal(command).collect { emit(it) }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun uploadFile(config: SshConfig, localPath: String, remotePath: String) {
        use(config) { session ->
            session.uploadFile(localPath, remotePath)
        }
    }

    fun downloadFile(config: SshConfig, remotePath: String, localPath: String) {
        use(config) { session ->
            session.downloadFile(remotePath, localPath)
        }
    }
}

class SshSession(private val config: SshConfig) : Closeable {
    private val client: SSHClient = SSHClient().apply {
        addHostKeyVerifier(PromiscuousVerifier())
        connectTimeout = config.connectTimeoutMs
        timeout = config.readTimeoutMs
    }

    private var connected = false

    private fun ensureConnected() {
        if (connected) return
        try {
            client.connect(config.host, config.port)
            when {
                config.privateKeyPath != null -> {
                    val expandedPath = config.privateKeyPath.replace("~", System.getProperty("user.home"))
                    val keyProvider = if (config.privateKeyPassphrase != null) {
                        client.loadKeys(expandedPath, config.privateKeyPassphrase)
                    } else {
                        client.loadKeys(expandedPath)
                    }
                    client.authPublickey(config.username, keyProvider)
                }
                config.password != null -> {
                    client.authPassword(config.username, config.password)
                }
            }
            connected = true
        } catch (e: Exception) {
            throw SshConnectionException("连接SSH服务器失败: ${config.host}:${config.port}", e)
        }
    }

    fun executeSync(command: String): SshResult {
        ensureConnected()
        var session: Session? = null
        try {
            session = client.startSession()
            val cmd = session.exec(command)
            val stdout = IOUtils.readFully(cmd.inputStream).toString("UTF-8")
            val stderr = IOUtils.readFully(cmd.errorStream).toString("UTF-8")
            cmd.join(config.readTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
            val exitCode = cmd.exitStatus ?: -1
            return SshResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            throw SshExecutionException("执行命令失败: $command", e)
        } finally {
            session?.close()
        }
    }

    fun executeStream(command: String): Flow<String> {
        return executeStreamInternal(command).flowOn(Dispatchers.IO)
    }

    internal fun executeStreamInternal(command: String): Flow<String> = flow {
        ensureConnected()
        var session: Session? = null
        try {
            session = client.startSession()
            val cmd = session.exec(command)
            BufferedReader(InputStreamReader(cmd.inputStream, Charsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    emit(line!!)
                }
            }
            cmd.join(config.readTimeoutMs.toLong(), TimeUnit.MILLISECONDS)
            val exitCode = cmd.exitStatus ?: -1
            if (exitCode != 0) {
                val stderr = IOUtils.readFully(cmd.errorStream).toString("UTF-8")
                throw SshExecutionException("命令执行失败, exitCode=$exitCode, stderr=$stderr")
            }
        } finally {
            session?.close()
        }
    }

    fun uploadFile(localPath: String, remotePath: String) {
        ensureConnected()
        val expandedLocalPath = localPath.replace("~", System.getProperty("user.home"))
        val localFile = File(expandedLocalPath)
        if (!localFile.exists()) {
            throw SshFileTransferException("本地文件不存在: $expandedLocalPath")
        }
        var sftp: SFTPClient? = null
        try {
            sftp = client.newSFTPClient()
            val normalizedRemotePath = normalizeRemotePath(remotePath)
            val remoteHintIsDirectory = remotePath.trim().endsWith("/") || normalizedRemotePath.isEmpty()
            val remoteExistsAsDirectory = remotePathExistsAsDirectory(sftp, normalizedRemotePath)
            val treatRemoteAsDirectory = remoteHintIsDirectory || remoteExistsAsDirectory
            if (localFile.isDirectory) {
                val targetDirectory = if (treatRemoteAsDirectory) {
                    appendRemotePath(normalizedRemotePath, localFile.name)
                } else {
                    normalizedRemotePath
                }
                uploadDirectory(sftp, localFile, targetDirectory)
            } else {
                val remoteFilePath = if (treatRemoteAsDirectory) {
                    appendRemotePath(normalizedRemotePath, localFile.name)
                } else {
                    normalizedRemotePath
                }
                ensureRemoteParentDirectories(sftp, remoteFilePath)
                sftp.put(FileSystemFile(localFile), remoteFilePath)
            }
        } catch (e: SshFileTransferException) {
            throw e
        } catch (e: Exception) {
            throw SshFileTransferException("上传文件失败: $localPath -> $remotePath", e)
        } finally {
            sftp?.close()
        }
    }

    private fun uploadDirectory(sftp: SFTPClient, localDir: File, remotePath: String) {
        ensureRemoteDirectory(sftp, remotePath)
        localDir.listFiles()?.forEach { file ->
            val remoteFilePath = appendRemotePath(remotePath, file.name)
            if (file.isDirectory) {
                uploadDirectory(sftp, file, remoteFilePath)
            } else {
                sftp.put(FileSystemFile(file), remoteFilePath)
            }
        }
    }

    private fun normalizeRemotePath(remotePath: String): String {
        val trimmed = remotePath.trim()
        if (trimmed.isEmpty()) return ""
        return if (trimmed == "/") "/" else trimmed.trimEnd('/')
    }

    private fun appendRemotePath(base: String, child: String): String {
        if (child.isEmpty()) return base
        if (base.isEmpty()) return child
        if (base == "/") return "/$child"
        return "$base/$child"
    }

    private fun remotePathExistsAsDirectory(sftp: SFTPClient, remotePath: String): Boolean {
        if (remotePath.isBlank()) return false
        return try {
            sftp.stat(remotePath).type == net.schmizz.sshj.sftp.FileMode.Type.DIRECTORY
        } catch (_: Exception) {
            false
        }
    }

    private fun ensureRemoteParentDirectories(sftp: SFTPClient, remoteFilePath: String) {
        val lastSlash = remoteFilePath.lastIndexOf('/')
        if (lastSlash <= 0) {
            if (lastSlash == 0) {
                ensureRemoteDirectory(sftp, "/")
            }
            return
        }
        val parent = remoteFilePath.substring(0, lastSlash)
        ensureRemoteDirectory(sftp, parent)
    }

    private fun ensureRemoteDirectory(sftp: SFTPClient, remoteDirectory: String) {
        val trimmed = remoteDirectory.trim()
        if (trimmed.isEmpty()) return
        if (trimmed == "/") {
            verifyDirectoryNode(sftp, "/")
            return
        }
        val normalized = trimmed.trimEnd('/')
        val absolute = normalized.startsWith("/")
        val segments = normalized.split("/").filter { it.isNotEmpty() }
        var current = if (absolute) "/" else ""
        segments.forEach { segment ->
            current = when {
                current == "/" -> "/$segment"
                current.isEmpty() -> segment
                else -> "$current/$segment"
            }
            verifyDirectoryNode(sftp, current, createWhenMissing = true)
        }
    }

    private fun verifyDirectoryNode(
        sftp: SFTPClient,
        path: String,
        createWhenMissing: Boolean = false
    ) {
        try {
            val attrs = sftp.stat(path)
            if (attrs.type != net.schmizz.sshj.sftp.FileMode.Type.DIRECTORY) {
                throw SshFileTransferException("远程路径存在但不是目录: $path")
            }
        } catch (e: SFTPException) {
            if (!createWhenMissing || e.statusCode != Response.StatusCode.NO_SUCH_FILE) {
                throw e
            }
            try {
                sftp.mkdir(path)
            } catch (mkdirException: SFTPException) {
                if (mkdirException.statusCode != Response.StatusCode.FAILURE) {
                    throw mkdirException
                }
                val attrs = sftp.stat(path)
                if (attrs.type != net.schmizz.sshj.sftp.FileMode.Type.DIRECTORY) {
                    throw SshFileTransferException("远程路径存在但不是目录: $path")
                }
            }
        }
    }

    fun downloadFile(remotePath: String, localPath: String) {
        ensureConnected()
        val expandedLocalPath = localPath.replace("~", System.getProperty("user.home"))
        val localFile = File(expandedLocalPath)
        localFile.parentFile?.mkdirs()
        var sftp: SFTPClient? = null
        try {
            sftp = client.newSFTPClient()
            val attrs = sftp.stat(remotePath)
            if (attrs.type == net.schmizz.sshj.sftp.FileMode.Type.DIRECTORY) {
                localFile.mkdirs()
                downloadDirectory(sftp, remotePath, localFile)
            } else {
                sftp.get(remotePath, FileSystemFile(localFile))
            }
        } catch (e: SshFileTransferException) {
            throw e
        } catch (e: Exception) {
            throw SshFileTransferException("下载文件失败: $remotePath -> $localPath", e)
        } finally {
            sftp?.close()
        }
    }

    private fun downloadDirectory(sftp: SFTPClient, remotePath: String, localDir: File) {
        sftp.ls(remotePath)
            .filter { it.name != "." && it.name != ".." }
            .forEach { entry ->
                val remoteFilePath = "$remotePath/${entry.name}"
                val localFile = File(localDir, entry.name)
                if (entry.isDirectory) {
                    localFile.mkdirs()
                    downloadDirectory(sftp, remoteFilePath, localFile)
                } else {
                    sftp.get(remoteFilePath, FileSystemFile(localFile))
                }
            }
    }

    override fun close() {
        if (connected) {
            try {
                client.disconnect()
            } catch (_: Exception) { }
            connected = false
        }
    }
}
