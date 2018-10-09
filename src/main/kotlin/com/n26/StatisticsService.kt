package com.n26

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP

@Service
class StatisticsService(
    private val repo: TransactionsRepository
) {

    fun calculateStatistics(): Statistics {
        val transactions = synchronized(repo) {
            repo.removeOutdated()
            repo.all()
        }

        if (transactions.isEmpty()) {
            return Statistics.empty
        }

        return doTheMath(transactions.map { it.amount })
    }

    private fun doTheMath(amounts: List<BigDecimal>): Statistics {
        assert(amounts.isNotEmpty())
        val sum = amounts.fold(BigDecimal.ZERO) { x, y -> x.add(y) }
        return Statistics(
            sum = sum,
            avg = sum.divide(BigDecimal(amounts.size), 2, HALF_UP),
            max = amounts.max()!!,
            min = amounts.min()!!,
            count = amounts.size.toLong()
        )
    }

}

data class Statistics(
    val sum: BigDecimal,
    val avg: BigDecimal,
    val max: BigDecimal,
    val min: BigDecimal,
    val count: Long
) {
    companion object {
        val empty = Statistics(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L)
    }
}
