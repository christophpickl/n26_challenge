package com.n26

import com.n26.testInfrastructure.TestRestService
import com.n26.testInfrastructure.TestStatisticsDto
import com.n26.testInfrastructure.TestTransactionDto
import com.n26.testInfrastructure.hasStatusCode
import com.n26.testInfrastructure.nowUTC
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StatisticsControllerITest {

    @Autowired
    private lateinit var rest: TestRestService

    @Before
    fun `reset state`() {
        rest.request(DELETE, "/transactions")
    }

    @Test
    fun `When get statistics Then return 200 OK`() {
        val response = rest.request(GET, "/statistics")

        assertThat(response).hasStatusCode(OK)
    }

    @Test
    fun `When get statistics Then return empty statistics`() {
        val statistics = rest.requestFor<TestStatisticsDto>(GET, "/statistics")

        assertThat(statistics).isEqualTo(TestStatisticsDto.empty)
    }

    @Test
    fun `Given one valid transaction When get statistics Then return statistics for that single transaction`() {
        createValidTransaction(amount = "1.00", timestamp = nowUTC())
        val statistics = rest.requestFor<TestStatisticsDto>(GET, "/statistics")

        assertThat(statistics).isEqualTo(TestStatisticsDto(
            sum = "1.00",
            avg = "1.00",
            max = "1.00",
            min = "1.00",
            count = 1L
        ))
    }

    private fun createValidTransaction(amount: String, timestamp: LocalDateTime) {
        val response = rest.request(POST, "/transactions", TestTransactionDto(
            amount = amount,
            timestamp = TestTransactionDto.dateFormatter.format(timestamp)
        ))
        assertThat(response).hasStatusCode(CREATED)
    }

}
