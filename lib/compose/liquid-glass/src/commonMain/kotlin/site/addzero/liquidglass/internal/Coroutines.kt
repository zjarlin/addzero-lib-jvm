package site.addzero.liquidglass.internal

import androidx.compose.runtime.withFrameNanos

internal suspend fun awaitFrame() {
    withFrameNanos { }
}
