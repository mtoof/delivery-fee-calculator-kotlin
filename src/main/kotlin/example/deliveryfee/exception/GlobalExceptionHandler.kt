package example.deliveryfee.exception

import com.fasterxml.jackson.databind.JsonMappingException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(err: HttpMessageNotReadableException): ResponseEntity<Map<String, String>> {
        val errorDetails = mutableMapOf<String, String>()

        errorDetails["error"] = "Invalid input data"

        val cause = err.cause
        if (cause is JsonMappingException){
            val fieldPath = cause.path?.joinToString("."){it.fieldName ?: "[Unknown]"}

            errorDetails["field"] = fieldPath ?: "Unknown field"
            errorDetails["message"] = cause.originalMessage ?: "Invalid data"
        }
        else{
            errorDetails["message"] = err.message ?: "Error processing input"
        }
        return ResponseEntity(errorDetails, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(e: MethodArgumentNotValidException): ResponseEntity<String> {
        return ResponseEntity.badRequest().body("Invalid input data: ${e.fieldError?.defaultMessage}")
    }
}
