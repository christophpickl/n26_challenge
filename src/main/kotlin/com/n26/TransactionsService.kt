package com.n26

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TransactionsService(
    private val repo: TransactionsRepository,
    private val watch: Watch,
    @Value("\${n26.maxTransactionAgeInSeconds}")
    private val maxTransactionAgeInSeconds: Long
) {

    fun create(transaction: Transaction): CreationResult {
        val now = watch.now()
        if (transaction.timestamp.isBefore(now.minusSeconds(maxTransactionAgeInSeconds))) {
            return CreationResult.TOO_OLD
        }
        if (transaction.timestamp.isAfter(now)) {
            return CreationResult.IN_FUTURE
        }

        repo.add(transaction)

        return CreationResult.OK
    }

    fun deleteAll() {
        repo.clear()
    }

}

enum class CreationResult {
    OK,
    TOO_OLD,
    IN_FUTURE
}

data class Transaction(
    val amount: BigDecimal,
    val timestamp: LocalDateTime
) {
    companion object
}
