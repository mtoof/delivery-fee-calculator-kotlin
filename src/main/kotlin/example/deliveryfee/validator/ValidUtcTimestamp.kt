package example.deliveryfee.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Constraint(validatedBy = [TimeStampValidator::class])
@Target(FIELD, PROPERTY, ANNOTATION_CLASS, VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidUtcTimestamp(
    val message: String,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
