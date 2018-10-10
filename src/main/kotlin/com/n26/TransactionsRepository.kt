package com.n26

import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

interface TransactionsRepository {
    fun add(transaction: Transaction)
    fun all(): List<Transaction>
    fun removeOutdated(): List<Transaction>
    fun clear()
}

@Repository
class TransactionsRepositoryImpl(
    private val watch: Watch,
    @Value("\${n26.maxTransactionAgeInSeconds}")
    private val maxTransactionAgeInSeconds: Long,
    @Value("\${n26.artificialDelay}")
    private val artificalDelay: Boolean
) : TransactionsRepository {

    private val log = logger {}
    private val transactions = mutableListOf<Transaction>()

    init {
        log.info { "maxTransactionAgeInSeconds=$maxTransactionAgeInSeconds, artificalDelay=$artificalDelay" }
    }

    override fun add(transaction: Transaction) {
        transactions += transaction
    }

    override fun all() = transactions

    override fun removeOutdated(): List<Transaction> {
        val maxAge = watch.now().minusSeconds(maxTransactionAgeInSeconds)
        val toBeRemoved = transactions.filter { it.timestamp.isBefore(maxAge) }
        if (artificalDelay) {
            Thread.sleep(transactions.size.toLong())
        }
        transactions -= toBeRemoved
        return toBeRemoved
    }

    override fun clear() {
        transactions.clear()
    }

}
