package com.n26

import com.n26.testInfrastructure.TestRestService
import com.n26.testInfrastructure.TestTransactionDto
import com.n26.testInfrastructure.hasStatusCode
import com.n26.testInfrastructure.nowUTC
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TransactionsControllerITest {

    @Autowired
    private lateinit var rest: TestRestService
    @Autowired
    private lateinit var repo: TransactionsRepository
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Value("\${n26.maxTransactionAgeInSeconds}")
    private lateinit var transactionMaxAgeInSeconds: java.lang.Long

    private val transaction = TestTransactionDto.instance

    @Test
    fun `When create valid transaction Then return 201 CREATED`() {
        val response = rest.request(POST, "/transactions",
            transaction.copyWithTimestamp(nowUTC()))

        assertThat(response).hasStatusCode(CREATED)
    }

    @Test
    fun `When create transaction older than max age Then return 204 NO CONTENT`() {
        val response = rest.request(POST, "/transactions",
            transaction.copyWithTimestamp(nowUTC().minusSeconds(transactionMaxAgeInSeconds.toLong() + 1)))

        assertThat(response).hasStatusCode(NO_CONTENT)
    }

    @Test
    fun `When create transaction by invalid JSON Then return 400 BAD REQUEST`() {
        val response = rest.request(POST, "/transactions",
            """{ "invalid": "json" }""")

        assertThat(response).hasStatusCode(BAD_REQUEST)
    }

    @Test
    fun `When create future transaction Then return 422 UNPROCESSABLE ENTITY`() {
        val response = rest.request(POST, "/transactions",
            transaction.copyWithTimestamp(nowUTC().plusMinutes(1)))

        assertThat(response).hasStatusCode(UNPROCESSABLE_ENTITY)
    }

    @Test
    fun `When create transaction with unparsable amount Then return 422 UNPROCESSABLE ENTITY`() {
        val response = rest.request(POST, "/transactions",
            transaction.copy(amount = "invalidAmount"))

        assertThat(response).hasStatusCode(UNPROCESSABLE_ENTITY)
    }

    @Test
    fun `When create transaction with unparsable timestamp Then return 422 UNPROCESSABLE ENTITY`() {
        val response = rest.request(POST, "/transactions",
            transaction.copy(timestamp = "invalidTimestamp"))

        assertThat(response).hasStatusCode(UNPROCESSABLE_ENTITY)
    }

    @Test
    fun `Given a transaction When delete transactions Then return 204 NO CONTENT and repo is empty`() {
        val response = rest.request(DELETE, "/transactions",
            transaction.copyWithTimestamp(nowUTC()))

        assertThat(response).hasStatusCode(NO_CONTENT)
        assertThat(repo.all()).isEmpty()
    }

}
