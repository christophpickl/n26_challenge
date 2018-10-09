package com.n26.testInfrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import java.net.URI

@Service
class TestRestService(
    val mapper: ObjectMapper
) {

    // made optional in order to get the provided integration tests to work (which obviously don't use spring-boot-test)
    @Autowired(required = false)
    private lateinit var restTemplate: TestRestTemplate

    fun request(
        method: HttpMethod,
        path: String,
        body: Any? = null
    ): ResponseEntity<String> =
        restTemplate.exchange(RequestEntity(
            body,
            LinkedMultiValueMap<String, String>().apply {
                add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                if (body != null) {
                    add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                }
            },
            method,
            URI.create(path)
        ))

    final inline fun <reified T : Any> requestFor(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        expectedStatusCode: HttpStatus = HttpStatus.OK
    ): T {
        val response = request(method, path, body)
        assertThat(response.statusCode).describedAs("Response was: $response").isEqualTo(expectedStatusCode)
        return mapper.readValue(response.body!!)
    }

}
