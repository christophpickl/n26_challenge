package com.n26

import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class StatisticsServiceTest {

    @Mock
    private lateinit var repo: TransactionsRepository
    private lateinit var service: StatisticsService

    private val anyDateTime = LocalDateTime.parse("2018-10-06T20:00:00")!!

    @Before
    fun `init service`() {
        service = StatisticsService(repo)
    }

    @Test
    fun `Given no transactions When calculate statistics Then return empty statistics`() {
        whenever(repo.all()).thenReturn(emptyList())

        val statistics = service.calculateStatistics()

        assertThat(statistics).isEqualTo(Statistics.empty)
    }

    @Test
    fun `Given single transaction When calculate statistics Then return statistics with the transaction's amount`() {
        val transaction = transactionWithAmount("1.00")
        whenever(repo.all()).thenReturn(listOf(transaction))

        val statistics = service.calculateStatistics()

        assertThat(statistics).isEqualTo(Statistics(
            sum = transaction.amount,
            avg = transaction.amount,
            max = transaction.amount,
            min = transaction.amount,
            count = 1L
        ))
    }

    @Test
    fun `Given some transactions When calculate statistics Then return proper statistics`() {
        whenever(repo.all()).thenReturn(transactionsWithAmounts("5.00", "3.00", "3.00"))

        val statistics = service.calculateStatistics()

        assertThat(statistics).isEqualTo(Statistics(
            sum = BigDecimal("11.00"),
            avg = BigDecimal("3.67"),
            max = BigDecimal("5.00"),
            min = BigDecimal("3.00"),
            count = 3L
        ))
    }

    private fun transactionsWithAmounts(vararg amounts: String) =
        amounts.map { transactionWithAmount(it) }

    private fun transactionWithAmount(amount: String) =
        Transaction(BigDecimal(amount), anyDateTime)

}
