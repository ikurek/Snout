package com.nightcode.snout

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
