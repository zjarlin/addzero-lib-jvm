package com.example.demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform