package example.deliveryfee

import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@RestController
@Validated
class DeliveryFeeController(
    private val deliveryFeeCalculatorService: DeliveryFeeCalculatorService
) {

    @PostMapping("/cart")
    fun handleDeliveryFee(@RequestBody cart: Cart): ResponseEntity<DeliveryFeeResponse>{

        val cart_value: Int = cart.cart_value
        val delivery_distance: Int = cart.delivery_distance
        val items: Int = cart.number_of_items
        val time: ZonedDateTime = ZonedDateTime.parse(cart.time)

        val deliveryFee: Int = deliveryFeeCalculatorService.calculateDeliveryFee(cart_value, delivery_distance, items, time)
        return ResponseEntity.ok(DeliveryFeeResponse(deliveryFee))
    }
}