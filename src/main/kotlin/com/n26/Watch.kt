package com.n26

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

/** In order to control randomness for tests. */
interface Watch {
    fun now(): LocalDateTime
}

@Service
class UtcWatch : Watch {
    override fun now() = LocalDateTime.now(Clock.systemUTC())!!
}
