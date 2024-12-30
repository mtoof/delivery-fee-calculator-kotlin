package example.deliveryfee.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Constraint(validatedBy = [TimeStampValidator::class])
@Target(FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidUtcTimestamp(
    val message: String,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
