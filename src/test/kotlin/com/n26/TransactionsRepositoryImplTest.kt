package com.n26

import com.n26.testInfrastructure.testInstance
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class TransactionsRepositoryImplTest {

    @Mock
    private lateinit var watch: Watch
    private lateinit var repo: TransactionsRepository

    private val transactionMaxOldInSeconds = 60L
    private val date = LocalDateTime.parse("2018-10-06T20:00:00")!!
    private val transaction = Transaction.testInstance

    @Before
    fun `init repo`() {
        repo = TransactionsRepositoryImpl(watch, transactionMaxOldInSeconds, artificalDelay = false)
    }

    @Test
    fun `When get all Then return empty`() {
        val transactions = repo.all()

        assertThat(transactions).isEmpty()
    }

    @Test
    fun `Given a transaction When get all Then return that transaction`() {
        repo.add(transaction)

        val transactions = repo.all()

        assertThat(transactions).containsExactly(transaction)
    }

    @Test
    fun `Given in time transaction When remove outdated Then nothing is removed`() {
        repo.add(transaction.copy(timestamp = date))
        whenever(watch.now()).thenReturn(date)

        val removed = repo.removeOutdated()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `Given just in time transaction When remove outdated Then nothing is removed`() {
        repo.add(transaction.copy(timestamp = date))
        whenever(watch.now()).thenReturn(date.plusSeconds(transactionMaxOldInSeconds))

        val removed = repo.removeOutdated()

        assertThat(removed).isEmpty()
    }

    @Test
    fun `Given outdated transaction When remove outdated Then get that transaction is removed`() {
        repo.add(transaction.copy(timestamp = date))
        whenever(watch.now()).thenReturn(date.plusSeconds(transactionMaxOldInSeconds + 1))

        val removed = repo.removeOutdated()

        assertThat(removed).isEqualTo(listOf(transaction))
    }

    @Test
    fun `Given a transaction When clear transactions Then the repo is empty`() {
        repo.add(transaction)

        repo.clear()

        assertThat(repo.all()).isEmpty()
    }

}
