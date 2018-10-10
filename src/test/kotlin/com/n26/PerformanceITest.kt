package com.n26

import com.n26.testInfrastructure.TestRestService
import com.n26.testInfrastructure.TestTransactionDto
import com.n26.testInfrastructure.nowUTC
import mu.KotlinLogging.logger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpMethod.POST
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.system.measureTimeMillis

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = [
    "n26.maxTransactionAgeInSeconds=300",
    "n26.artificialDelay=true"
])
class PerformanceITest {

    @Autowired
    private lateinit var rest: TestRestService
    private val log = logger {}
    private val transaction = TestTransactionDto.instance

    @Test
    fun `Given artificial delay enabled When sending a bunch of requests Then execution time is constant`() {
        val chunkSize = 100
        val chunkCount = 3
        val validDeviationOfAverageResponseTimeInPercent = 80.0

        val timesNeeded = sendRequestsAndMeasureTimeNeededInMs(
            requestCount = chunkSize * chunkCount,
            threadPoolCount = 20
        )

        val averageResponsePerChunk = timesNeeded.asSequence().chunked(chunkSize).map { it.average() }.toList()
        (1 until averageResponsePerChunk.size).forEach {
            assertThat(averageResponsePerChunk[it - 1])
                .describedAs("Expected average of previous chunk to be similar than next chunk.")
                .isCloseTo(averageResponsePerChunk[it], Percentage.withPercentage(validDeviationOfAverageResponseTimeInPercent))
            // for linear execution time:
//            assertThat(averageResponsePerChunk[it - 1])
//                .describedAs("Expected average of previous chunk to be lower than next chunk.")
//                .isLessThan(averageResponsePerChunk[it])
        }
    }

    private fun sendRequestsAndMeasureTimeNeededInMs(requestCount: Int, threadPoolCount: Int): List<Long> {
        val executor = Executors.newFixedThreadPool(threadPoolCount)
        val timesNeeded = mutableListOf<Long>()
        val moduloDivisor = requestCount / 10

        1.rangeTo(50).forEach {
            // throw away first 50 requests, as spring needs some warm-up first
            rest.request(POST, "/transactions", transaction.copyWithTimestamp(nowUTC()))
        }

        1.rangeTo(requestCount).forEach { nr ->
            executor.submit {
                if (nr % moduloDivisor == 0) {
                    log.info { "Executing request $nr of $requestCount" }
                }
                timesNeeded += measureTimeMillis {
                    rest.request(POST, "/transactions", transaction.copyWithTimestamp(nowUTC()))
                }
            }
        }

        executor.shutdown()
        executor.awaitTermination(5L, MINUTES)
        @Suppress("UselessCallOnCollection") // ignore race condition
        return timesNeeded.filterNotNull()
    }

}
