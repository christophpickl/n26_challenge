package com.n26

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
    private val maxTransactionAgeInSeconds: Long
) : TransactionsRepository {

    private val transactions = mutableListOf<Transaction>()

    override fun add(transaction: Transaction) {
        transactions += transaction
    }

    override fun all() = transactions

    override fun removeOutdated(): List<Transaction> {
        val maxAge = watch.now().minusSeconds(maxTransactionAgeInSeconds)
        val toBeRemoved = all().filter { it.timestamp.isBefore(maxAge) }
        transactions -= toBeRemoved
        return toBeRemoved
    }

    override fun clear() {
        transactions.clear()
    }

}
