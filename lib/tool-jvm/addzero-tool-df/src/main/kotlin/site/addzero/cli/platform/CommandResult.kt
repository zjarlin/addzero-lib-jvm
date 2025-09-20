package site.addzero.cli.platform

data class CommandResult(
    val exitCode: Int,
    val output: String,
    val error: String
){
    fun isError(): Boolean {
       return exitCode != 0
    }
    fun isSucces(): Boolean {
        return exitCode == 0
    }
}
