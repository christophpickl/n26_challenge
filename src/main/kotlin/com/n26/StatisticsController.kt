package com.n26

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.text.DecimalFormat

@RestController
@RequestMapping(value = ["/statistics"], produces = [MediaType.APPLICATION_JSON_VALUE])
class StatisticsController(
    private val service: StatisticsService
) {

    private val formatter = ThreadLocal.withInitial { DecimalFormat("0.00") }

    @GetMapping
    fun getStatistics() =
        service.calculateStatistics().toStatisticsDto()

    private fun Statistics.toStatisticsDto() = StatisticsDto(
        sum = sum.toFormattedString(),
        avg = avg.toFormattedString(),
        max = max.toFormattedString(),
        min = min.toFormattedString(),
        count = count
    )

    private fun BigDecimal.toFormattedString() =
        formatter.get().format(this)

}

/** All BigDecimal values always contain exactly two decimal places and use `HALF_ROUND_UP` rounding. */
data class StatisticsDto(
    val sum: String,
    val avg: String,
    val max: String,
    val min: String,
    /** Specifying the total number of transactions that happened in the last 60 seconds. */
    val count: Long
)
