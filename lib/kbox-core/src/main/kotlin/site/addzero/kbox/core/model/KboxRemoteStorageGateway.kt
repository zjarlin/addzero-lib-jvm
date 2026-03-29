package site.addzero.kbox.core.model

import java.io.File

interface KboxRemoteStorageGateway {
    fun validate(config: KboxSshConfig)

    fun uploadFile(
        localFile: File,
        remoteAbsolutePath: String,
        config: KboxSshConfig,
    )
}
