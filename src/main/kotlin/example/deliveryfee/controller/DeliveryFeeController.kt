package example.deliveryfee.controller

import example.deliveryfee.dto.Cart
import example.deliveryfee.dto.DeliveryFeeResponse
import example.deliveryfee.service.DeliveryFeeService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@RestController
class DeliveryFeeController(
    private val deliveryFeeService: DeliveryFeeService
) {

    @PostMapping("/cart")
    fun handleDeliveryFee(@Valid @RequestBody cart: Cart): ResponseEntity<DeliveryFeeResponse>{

        val cart_value: Int = cart.cart_value
        val delivery_distance: Int = cart.delivery_distance
        val items: Int = cart.number_of_items
        val time: ZonedDateTime = ZonedDateTime.parse(cart.time)

        val deliveryFee: Int = deliveryFeeService.calculateDeliveryFee(cart_value, delivery_distance, items, time)
        return ResponseEntity.ok(DeliveryFeeResponse(deliveryFee))
    }
}