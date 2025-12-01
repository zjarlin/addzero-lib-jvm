package site.addzero.util.ssh

data class SshResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0

    fun getOutputOrThrow(): String {
        if (!isSuccess) {
            throw SshExecutionException("命令执行失败, exitCode=$exitCode, stderr=$stderr")
        }
        return stdout
    }
}

class SshExecutionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class SshConnectionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class SshFileTransferException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
