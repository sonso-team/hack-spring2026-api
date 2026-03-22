package ru.sonso.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import ru.sonso.dto.ErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.resolve(ex.statusCode.value()) ?: HttpStatus.INTERNAL_SERVER_ERROR
        return ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    error = status.reasonPhrase,
                    message = ex.reason ?: status.reasonPhrase,
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(error = HttpStatus.BAD_REQUEST.reasonPhrase, message = ex.message ?: "Invalid request"))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> = ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse(error = HttpStatus.NOT_FOUND.reasonPhrase, message = ex.message ?: "Resource not found"))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class, HttpMessageNotReadableException::class)
    fun handleBadRequest(ex: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse(error = HttpStatus.BAD_REQUEST.reasonPhrase, message = ex.message ?: "Invalid request"))

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", ex)
        return ResponseEntity
            .status(500)
            .body(ErrorResponse(error = "Internal Server Error", message = "Unexpected server error"))
    }
}
