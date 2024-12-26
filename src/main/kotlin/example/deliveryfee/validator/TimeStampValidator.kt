package example.deliveryfee.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.Instant
import java.time.format.DateTimeFormatter

class TimeStampValidator: ConstraintValidator<ValidUtcTimestamp, String> {
    override fun isValid(timeValue: String?, context: ConstraintValidatorContext?): Boolean {
        val parsedTime: Instant
        return try {
            val formatter = DateTimeFormatter.ISO_INSTANT
            parsedTime = Instant.from(timeValue?.let { formatter.parse(it) })
            val currentTime = Instant.now()
            parsedTime <= currentTime
        }
        catch (e: Exception) {
            false
        }
    }
}