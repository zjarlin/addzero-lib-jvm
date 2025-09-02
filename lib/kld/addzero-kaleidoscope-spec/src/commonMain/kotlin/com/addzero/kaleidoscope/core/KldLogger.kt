package com.addzero.kaleidoscope.core

interface KldLogger {
       /**
     * 记录信息日志
     *
     * @param message 日志消息
     * @param element 相关元素（可选）
     */
    fun info(message: String, element: Any? = null)

    /**
     * 记录警告日志
     *
     * @param message 日志消息
     * @param element 相关元素（可选）
     */
    fun warn(message: String, element: Any? = null)

    /**
     * 记录错误日志
     *
     * @param message 日志消息
     * @param element 相关元素（可选）
     */
    fun error(message: String, element: Any? = null)


}
