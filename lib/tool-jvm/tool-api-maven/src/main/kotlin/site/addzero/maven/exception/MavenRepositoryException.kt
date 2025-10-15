package site.addzero.maven.exception

/**
 * Maven仓库操作异常
 *
 * @author zjarlin
 * @since 2025/10/15
 */
open class MavenRepositoryException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

/**
 * Maven仓库连接异常
 */
class MavenRepositoryConnectionException : MavenRepositoryException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Maven解析异常
 */
class MavenRepositoryParseException : MavenRepositoryException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}