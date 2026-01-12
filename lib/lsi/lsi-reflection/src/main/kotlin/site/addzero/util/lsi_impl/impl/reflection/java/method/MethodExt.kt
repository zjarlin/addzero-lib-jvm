package site.addzero.util.lsi_impl.impl.reflection.java.method

import site.addzero.util.lsi_impl.impl.reflection.anno.fieldComment
import java.lang.reflect.Method


fun Method.comment(): String? = this.annotations.fieldComment()
