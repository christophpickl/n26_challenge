package com.n26.testInfrastructure

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.Clock
import java.time.LocalDateTime

fun nowUTC() = LocalDateTime.now(Clock.systemUTC())!!

fun <SELF : AbstractAssert<SELF, ResponseEntity<String>>> AbstractAssert<SELF, ResponseEntity<String>>.hasStatusCode(expectedStatusCode: HttpStatus) {
    satisfies {
        assertThat(it.statusCode)
            .describedAs("Status code mismatch! Response body was: <<<${it.body}>>>")
            .isEqualTo(expectedStatusCode)
    }
}
