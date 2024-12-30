package example.deliveryfee.dto

import example.deliveryfee.validator.ValidUtcTimestamp
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class Cart(
    @field:NotNull(message = "cart_value can't be null")
    @field:Min(1, message = "cart_value must be greater than zero")
    val cart_value: Int,

    @field:NotNull(message = "delivery_distance can't be null")
    @field:Min(1, message = "delivery_distance must be greater than zero")
    val delivery_distance: Int,

    @field:NotNull(message = "number_of_items can't be null")
    @field:Min(1, message = "number_of_items must be greater than zero")
    val number_of_items: Int,

    @field:NotNull(message = "time can't be null")
    @field:ValidUtcTimestamp(message = "Timestamp must be in UTC format and not exceed the current time",)
    val time: String
)