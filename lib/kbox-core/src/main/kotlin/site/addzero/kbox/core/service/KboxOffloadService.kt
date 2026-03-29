package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxLargeFileCandidate
import site.addzero.kbox.core.model.KboxOffloadRecord
import site.addzero.kbox.core.model.KboxOffloadResult
import site.addzero.kbox.core.model.KboxRemoteStorageGateway
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.support.KboxDefaults
import java.io.File

@Single
class KboxOffloadService(
    private val gateway: KboxRemoteStorageGateway,
    private val remotePathService: KboxRemotePathService,
    private val historyStore: KboxHistoryStore,
) {
    fun validate(
        settings: KboxSettings,
    ) {
        val normalized = KboxDefaults.normalize(settings)
        require(normalized.ssh.enabled) {
            "SSH 未启用"
        }
        require(normalized.ssh.host.isNotBlank()) {
            "SSH 主机不能为空"
        }
        require(normalized.ssh.username.isNotBlank()) {
            "SSH 用户名不能为空"
        }
        gateway.validate(normalized.ssh)
    }

    fun offload(
        settings: KboxSettings,
        candidates: List<KboxLargeFileCandidate>,
        deleteLocalSource: Boolean = true,
    ): KboxOffloadResult {
        val normalized = KboxDefaults.normalize(settings)
        validate(normalized)
        val uploaded = mutableListOf<KboxOffloadRecord>()
        val skipped = mutableListOf<String>()
        candidates.forEach { candidate ->
            val sourceFile = File(candidate.sourcePath)
            if (!sourceFile.isFile) {
                skipped += "源文件不存在：${candidate.sourcePath}"
                return@forEach
            }
            val remoteAbsolutePath = remotePathService.remoteAbsolutePath(
                config = normalized.ssh,
                relativePath = candidate.remoteRelativePath,
            )
            gateway.uploadFile(
                localFile = sourceFile,
                remoteAbsolutePath = remoteAbsolutePath,
                config = normalized.ssh,
            )
            if (deleteLocalSource) {
                check(sourceFile.delete()) {
                    "远端上传成功，但本地源文件删除失败：${sourceFile.absolutePath}"
                }
            }
            uploaded += KboxOffloadRecord(
                sourcePath = candidate.sourcePath,
                remotePath = remoteAbsolutePath,
                remoteRelativePath = candidate.remoteRelativePath,
                sizeBytes = candidate.sizeBytes,
                deletedLocalSource = deleteLocalSource,
                offloadedAtMillis = System.currentTimeMillis(),
            )
        }
        historyStore.appendOffloadHistory(uploaded)
        return KboxOffloadResult(
            uploaded = uploaded,
            skipped = skipped,
        )
    }
}
