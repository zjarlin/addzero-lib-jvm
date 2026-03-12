package site.addzero.example

import org.babyfish.jimmer.Input
import site.addzero.kcp.transformoverload.annotations.OverloadTransform

@OverloadTransform
fun <E : Any> Input<E>.toEntityInput(): E = toEntity()
