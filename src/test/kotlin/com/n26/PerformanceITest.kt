package com.n26

import com.n26.testInfrastructure.TestRestService
import com.n26.testInfrastructure.TestTransactionDto
import com.n26.testInfrastructure.nowUTC
import mu.KotlinLogging.logger
import org.assertj.core.api.Assertions.assertThat
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
    "n26.maxTransactionAgeInSeconds=600",
    "n26.artificialDelay=true"
])
class PerformanceITest {

    @Autowired
    private lateinit var rest: TestRestService
    private val log = logger {}
    private val transaction = TestTransactionDto.instance

    @Test
    fun `Given artificial delay enabled When sending a bunch of requests Then execution time is linear growing`() {
        val chunkSize = 50
        val timesNeeded = sendRequestsAndMeasureTimeNeededInMs(
            requestCount = chunkSize * 3,
            threadPoolCount = 20
        )

        val parts = timesNeeded.asSequence().chunked(50).map { it.average() }.toList()
        assertThat(parts[0]).isLessThan(parts[1])
        assertThat(parts[1]).isLessThan(parts[2])
    }

    private fun sendRequestsAndMeasureTimeNeededInMs(requestCount: Int, threadPoolCount: Int): List<Long> {
        val executor = Executors.newFixedThreadPool(threadPoolCount)
        val timesNeeded = mutableListOf<Long>()
        val moduloDivisor = requestCount / 10

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
        return timesNeeded
    }

}
