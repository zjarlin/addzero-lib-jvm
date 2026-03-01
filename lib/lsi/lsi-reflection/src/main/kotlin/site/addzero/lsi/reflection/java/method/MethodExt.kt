package site.addzero.lsi.reflection.java.method

import site.addzero.lsi.reflection.anno.fieldComment
import java.lang.reflect.Method


fun Method.comment(): String? = this.annotations.fieldComment()
