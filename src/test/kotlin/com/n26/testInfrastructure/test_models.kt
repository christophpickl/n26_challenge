package com.n26.testInfrastructure

import com.n26.Transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val Transaction.Companion.testInstance get() = Transaction(BigDecimal.ONE, LocalDateTime.parse("2018-10-06T20:00:00")!!)

data class TestTransactionDto(
    val amount: String,
    val timestamp: String
) {
    companion object {
        val dateFormatter = DateTimeFormatter.ISO_DATE_TIME!!
        val instance = TestTransactionDto(
            amount = "12.34",
            timestamp = "2018-07-17T09:59:51.312Z"
        )
    }

    fun copyWithTimestamp(timestamp: LocalDateTime) = copy(
        timestamp = dateFormatter.format(timestamp)
    )
}

data class TestStatisticsDto(
    val sum: String,
    val avg: String,
    val max: String,
    val min: String,
    val count: Long
) {
    companion object {
        val empty get() = TestStatisticsDto("0.00", "0.00", "0.00", "0.00", 0L)
    }
}
