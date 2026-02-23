package org.babyfish.jimmer.lsi.field

import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.field.hasAnnotation

/**
 *
 * @author ForteScarlet
 */
val LsiField.isId: Boolean get() = this.hasAnnotation("Id")
