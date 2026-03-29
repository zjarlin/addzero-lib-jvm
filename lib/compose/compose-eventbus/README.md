# compose-eventbus

Small in-process event bus for Compose and shared coroutine flows.

## What It Provides

- `EventBus.emit(event)` to publish events
- `EventBus.events<T>()` to observe a typed stream
- `EventBus.consumer<T> { ... }` for suspend consumers
- Optional bridge from `network-starter` HTTP error responses into the event bus

## Basic Usage

```kotlin
launch {
    EventBus.emit(UserLoggedIn("u-1"))
}

launch {
    EventBus.consumer<UserLoggedIn> {
        println(id)
    }
}
```

## HTTP Response Bridge

If this module and `network-starter` are both in the app, `EventBusHttpResponseHandler` is registered through Koin SPI.

That means failed `HttpResponse` events from the shared HTTP client can be consumed directly:

```kotlin
launch {
    EventBus.consumer<HttpResponse> {
        println(status)
    }
}
```

## Notes

- The bus is type-based. Different event classes use different flows.
- `MutableSharedFlow` is created lazily with `extraBufferCapacity = 64`.
