package site.addzero.example

import site.addzero.kcp.transformoverload.annotations.OverloadTransform

@OverloadTransform
fun S.toT(): T = T(value)

@OverloadTransform
fun G.toR(): R = R(value)
