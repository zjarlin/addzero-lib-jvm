fun main() {
    // 测试第一个实现
    println("=== 第一个实现 ===")
    println("helloWorld -> ${"helloWorld".toKebabCaseFirstImpl()}")
    println("HelloWorld -> ${"HelloWorld".toKebabCaseFirstImpl()}")
    println("hello_world -> ${"hello_world".toKebabCaseFirstImpl()}")
    println("hello world -> ${"hello world".toKebabCaseFirstImpl()}")
    println("HELLO_WORLD -> ${"HELLO_WORLD".toKebabCaseFirstImpl()}")
    println("'' -> ${"".toKebabCaseFirstImpl()}")
    println("A -> ${"A".toKebabCaseFirstImpl()}")
    println("aBc -> ${"aBc".toKebabCaseFirstImpl()}")
    println("XMLHttpRequest -> ${"XMLHttpRequest".toKebabCaseFirstImpl()}")

    // 测试第二个实现
    println("\n=== 第二个实现 ===")
    println("helloWorld -> ${"helloWorld".toKebabCaseSecondImpl()}")
    println("HelloWorld -> ${"HelloWorld".toKebabCaseSecondImpl()}")
    println("hello_world -> ${"hello_world".toKebabCaseSecondImpl()}")
    println("hello space -> ${"hello space".toKebabCaseSecondImpl()}")
    println("HELLO_WORLD -> ${"HELLO_WORLD".toKebabCaseSecondImpl()}")
    println("'' -> ${"".toKebabCaseSecondImpl()}")
    println("A -> ${"A".toKebabCaseSecondImpl()}")
    println("aBc -> ${"aBc".toKebabCaseSecondImpl()}")
    println("XMLHttpRequest -> ${"XMLHttpRequest".toKebabCaseSecondImpl()}")
}

// 为第一个实现创建一个单独的函数用于测试
fun String.toKebabCaseFirstImpl(): String {
    if (isEmpty()) return this

    // 使用正则表达式处理驼峰命名和下划线分隔
    return replace(Regex("(?<!^)([A-Z])"), "-$1")
        // 将下划线和空格替换为连字符
        .replace(Regex("[_\\s]+"), "-")
        // 转换为小写
        .lowercase()
        // 移除多余的连字符
        .replace(Regex("-{2,}"), "-")
        // 移除开头和结尾的连字符
        .trim('-')
}

// 为第二个实现创建一个单独的函数用于测试
fun String.toKebabCaseSecondImpl(): String {
    return this.replace(Regex("([a-z])([A-Z])"), "$1-$2").lowercase()
}