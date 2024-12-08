package example.deliveryfee

import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Cart(
    val cart_value: Int,
    val delivery_distance: Int,
    val number_of_items: Int,
    val time: String
    ) {
    init {
        require(cart_value > 0) {"cart_value must be greater than 0"}
        require(delivery_distance > 0) {"delivery_distance must be greater than 0"}
        require(number_of_items > 0) {"number_of_items must be greater than 0"}
        val parsedTime: Instant
        try {
            val formatter = DateTimeFormatter.ISO_INSTANT
            parsedTime = Instant.from(formatter.parse(time))
        }
        catch (e: Exception) {
            throw IllegalArgumentException("Invalid time format. Must be in UTC ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z').")
        }
        val currentTime = Instant.now()
        require (parsedTime <= currentTime) {"The timestamp cannot exceed the current datetime."}
    }
}
