package sample

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send

fun Route.registerSmokeNativeKtorRoutes() {
    webSocket("/ws/echo") {
        for (frame in incoming) {
            if (frame is Frame.Text) {
                send(Frame.Text("echo:${frame.readText()}"))
            }
        }
    }
}
