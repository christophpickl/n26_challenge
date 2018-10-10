package com.n26

import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import javax.annotation.PreDestroy

@Configuration
class TransactionsRepositoryConfiguration {
    @Bean
    fun transactionsRepository(
        watch: Watch,
        @Value("\${n26.maxTransactionAgeInSeconds}")
        maxTransactionAgeInSeconds: Long,
        @Value("\${n26.artificialDelay}")
        artificalDelay: Boolean,
        @Value("\${n26.transactionThreadCount}")
        transactionThreadCount: Int,
        @Value("\${n26.threadShutdownTimeoutInSeconds}")
        threadShutdownTimeoutInSeconds: Long
    ): TransactionsRepository =
        AsyncTransactionsRepository(
            TransactionsRepositoryImpl(
                watch, maxTransactionAgeInSeconds, artificalDelay
            ),
            transactionThreadCount, threadShutdownTimeoutInSeconds)

}

interface TransactionsRepository {
    fun add(transaction: Transaction)
    fun all(): List<Transaction>
    fun removeOutdated(): List<Transaction>
    fun clear()
}

class AsyncTransactionsRepository(
    private val delegate: TransactionsRepository,
    private val transactionThreadCount: Int,
    private val threadShutdownTimeoutInSeconds: Long
) : TransactionsRepository by delegate {

    private val log = logger {}
    private val executor = Executors.newFixedThreadPool(transactionThreadCount)

    init {
        log.info { "transactionThreadCount=$transactionThreadCount" }
    }

    override fun add(transaction: Transaction) {
        executor.submit {
            synchronized(delegate) {
                delegate.removeOutdated()
                delegate.add(transaction)
            }
        }
    }

    @PreDestroy
    fun stopExecutors() {
        log.debug { "Shutting down executors ..." }
        executor.shutdown()
        executor.awaitTermination(threadShutdownTimeoutInSeconds, SECONDS)
        log.debug { "Shut down complete." }
    }
}

class TransactionsRepositoryImpl(
    private val watch: Watch,
    private val maxTransactionAgeInSeconds: Long,
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
        log.debug { "clear()" }
        transactions.clear()
    }

}
