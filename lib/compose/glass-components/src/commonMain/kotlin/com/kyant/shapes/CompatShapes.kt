package com.kyant.shapes

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

fun RoundedRectangle(cornerRadius: Dp): Shape = RoundedCornerShape(cornerRadius)

fun Capsule(): Shape = RoundedCornerShape(percent = 50)
