package site.addzero.email.exception

/**
 * 邮件发送相关的自定义异常类
 */
class EmailException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}