package site.addzero.kbox.ssh

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxRemoteStorageGateway
import site.addzero.kbox.core.model.KboxSshAuthMode
import site.addzero.kbox.core.model.KboxSshConfig
import java.io.File

@Single
class SshjKboxRemoteStorageGateway : KboxRemoteStorageGateway {
    override fun validate(config: KboxSshConfig) {
        withClient(config) {
            // 连接和认证成功即可视为可用。
        }
    }

    override fun uploadFile(
        localFile: File,
        remoteAbsolutePath: String,
        config: KboxSshConfig,
    ) {
        require(localFile.isFile) {
            "待上传文件不存在：${localFile.absolutePath}"
        }
        withClient(config) { client ->
            client.newSFTPClient().use { sftp ->
                val normalizedRemotePath = normalizeRemotePath(remoteAbsolutePath)
                val remoteParent = normalizedRemotePath.substringBeforeLast('/', "")
                if (remoteParent.isNotBlank()) {
                    sftp.mkdirs(remoteParent)
                }
                sftp.put(localFile.absolutePath, normalizedRemotePath)
            }
        }
    }

    private fun withClient(
        config: KboxSshConfig,
        action: (SSHClient) -> Unit,
    ) {
        require(config.host.isNotBlank()) {
            "SSH 主机不能为空"
        }
        require(config.username.isNotBlank()) {
            "SSH 用户名不能为空"
        }
        val client = SSHClient()
        if (config.strictHostKeyChecking) {
            val knownHosts = File(System.getProperty("user.home").orEmpty(), ".ssh/known_hosts")
            check(knownHosts.isFile) {
                "启用严格主机校验时未找到 known_hosts：${knownHosts.absolutePath}"
            }
            client.loadKnownHosts(knownHosts)
        } else {
            client.addHostKeyVerifier(PromiscuousVerifier())
        }
        client.connect(config.host, config.port)
        try {
            authenticate(client, config)
            action(client)
        } finally {
            client.close()
        }
    }

    private fun authenticate(
        client: SSHClient,
        config: KboxSshConfig,
    ) {
        when (config.authMode) {
            KboxSshAuthMode.PASSWORD -> {
                require(config.password.isNotBlank()) {
                    "密码认证模式下 password 不能为空"
                }
                client.authPassword(config.username, config.password)
            }

            KboxSshAuthMode.PRIVATE_KEY -> {
                require(config.privateKeyPath.isNotBlank()) {
                    "私钥认证模式下 privateKeyPath 不能为空"
                }
                val keyProvider = if (config.privateKeyPassphrase.isBlank()) {
                    client.loadKeys(config.privateKeyPath)
                } else {
                    client.loadKeys(
                        config.privateKeyPath,
                        config.privateKeyPassphrase,
                    )
                }
                client.authPublickey(config.username, keyProvider)
            }
        }
    }

    private fun normalizeRemotePath(
        path: String,
    ): String {
        return path.replace('\\', '/')
            .replace(Regex("/{2,}"), "/")
    }
}
