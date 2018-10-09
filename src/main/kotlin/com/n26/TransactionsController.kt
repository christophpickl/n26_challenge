package com.n26

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RestController
@RequestMapping(value = ["/transactions"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TransactionsController(
    private val service: TransactionsService
) {

    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTransaction(
        @RequestBody transactionDto: TransactionDto
    ): ResponseEntity<Any> {
        val transaction = transactionDto.toTransaction() ?: return ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY)
        val result = service.create(transaction)
        return result.toResponseEntity()
    }

    @DeleteMapping
    fun deleteTransactions(): ResponseEntity<Any> {
        service.deleteAll()
        return ResponseEntity(NO_CONTENT)
    }

    private fun TransactionDto.toTransaction(): Transaction? {
        return Transaction(
            amount = amount.toBigDecimalOrNull() ?: return null,
            timestamp = timestamp.toLocalDateTimeOrNull() ?: return null
        )
    }

    private fun String.toLocalDateTimeOrNull() =
        try {
            LocalDateTime.parse(this, dateTimeFormatter)
        } catch (e: DateTimeParseException) {
            null
        }

    private fun CreationResult.toResponseEntity() =
        ResponseEntity<Any>(when (this) {
            CreationResult.OK -> HttpStatus.CREATED
            CreationResult.TOO_OLD -> HttpStatus.NO_CONTENT
            CreationResult.IN_FUTURE -> HttpStatus.UNPROCESSABLE_ENTITY
        })

}

data class TransactionDto(
    /** String of arbitrary length that is parsable as a BigDecimal. */
    val amount: String,
    /** ISO 8601 format YYYY-MM-DDThh:mm:ss.sssZ in the UTC timezone (this is not the current timestamp). */
    val timestamp: String
)
