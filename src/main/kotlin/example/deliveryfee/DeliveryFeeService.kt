package example.deliveryfee

import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.ceil

@Service
class DeliveryFeeService {

    //{"cart_value": 790, "delivery_distance": 2235, "number_of_items": 4, "time": "2024-01-15T13:00:00Z"}

    companion object {
        private const val CART_VALUE_THRESHOLD = 1000 //10euro
        private const val BASE_DELIVERY_FEE = 200 //2euro
        private const val DISTANCE_THRESHOLD = 1000 //1000meter
        private const val ADDITIONAL_DISTANCE_FEE = 100 //1euro
        private const val EXTRA_DISTANCE_UNIT = 500 //500meter
        private const val ITEM_THRESHOLD = 4
        private const val ADDITIONAL_ITEM_FEE = 50
        private const val BULK_ITEM_FEE = 120
        private const val BULK_ITEM_THRESHOLD = 12
        private const val RUSH_HOUR = 1.2
        private const val MAX_DELIVERY_FEE = 1500
        private const val FREE_DELIVERY_THRESHOLD = 20000
        private val START_RUSH_HOUR = LocalTime.of(15, 0)
        private val END_RUSH_HOUR = LocalTime.of(19, 0)


    }

    fun calculateDeliveryFee(
        cartValue: Int,
        deliveryDistance: Int,
        itemCount: Int,
        timeStamp: ZonedDateTime
    ): Int {
        var deliveryFee: Int = 0

        //If the cart value is less than 10€,
        // a small order surcharge is added to the delivery price.
        // The surcharge is the difference between the cart value and 10€.
        // For example if the cart value is 8.90€, the surcharge will be 1.10€.

        val cartValueSurcharge: Int = calculateCartValueSurcharge(cartValue)

        val deliveryDistanceFee: Int = calculateDeliveryDistanceFee(deliveryDistance)

        val extraItemsFee: Int = calculateNumberOfItemsFee(itemCount)

        deliveryFee = cartValueSurcharge + deliveryDistanceFee + extraItemsFee

        deliveryFee = isRushHour(timeStamp, deliveryFee)

        deliveryFee = deliveryFee.coerceAtMost(MAX_DELIVERY_FEE)

        if (cartValue >= FREE_DELIVERY_THRESHOLD)
            deliveryFee = 0

        //The delivery fee can never be more than 15€, including possible surcharges.
        //The delivery is free (0€) when the cart value is equal or more than 200€.

        return deliveryFee
    }

    fun calculateCartValueSurcharge(cartValue: Int): Int {
        return if (cartValue < CART_VALUE_THRESHOLD) CART_VALUE_THRESHOLD - cartValue else 0
    }

    fun calculateDeliveryDistanceFee(deliveryDistance: Int): Int {
        //A delivery fee for the first 1000 meters (=1km) is 2€. If the delivery distance is longer than that,
        // 1€ is added for every additional 500 meters that the courier needs to travel before reaching the destination.
        // Even if the distance would be shorter than 500 meters, the minimum fee is always 1€.
        //Example 1: If the delivery distance is 1499 meters, the delivery fee is: 2€ base fee + 1€ for the additional 500 m => 3€
        //Example 2: If the delivery distance is 1500 meters, the delivery fee is: 2€ base fee + 1€ for the additional 500 m => 3€
        //Example 3: If the delivery distance is 1501 meters, the delivery fee is: 2€ base fee + 1€ for the first 500 m + 1€ for the second 500 m => 4€

        val extraDistance: Int = if (deliveryDistance > DISTANCE_THRESHOLD) deliveryDistance - DISTANCE_THRESHOLD else 0
        val extraUnit = ceil(extraDistance / EXTRA_DISTANCE_UNIT.toDouble()).toInt()
        return BASE_DELIVERY_FEE + (extraUnit * ADDITIONAL_DISTANCE_FEE)
    }

    fun calculateNumberOfItemsFee(itemCount: Int): Int {
        //If the number of items is five or more, an additional 50 cent surcharge is added for each item above
        // and including the fifth item.
        // An extra "bulk" fee applies for more than 12 items of 1,20€

        //Example 1: If the number of items is 4, no extra surcharge
        //Example 2: If the number of items is 5, 50 cents surcharge is added
        //Example 3: If the number of items is 10, 3€ surcharge (6 x 50 cents) is added
        //Example 4: If the number of items is 13, 5,70€ surcharge is added ((9 * 50 cents) + 1,20€)
        //Example 5: If the number of items is 14, 6,20€ surcharge is added ((10 * 50 cents) + 1,20€)
        //The delivery fee can never be more than 15€, including possible surcharges.

        val itemsSurcharge: Int = if (itemCount <= ITEM_THRESHOLD) 0 else (itemCount - ITEM_THRESHOLD) * ADDITIONAL_ITEM_FEE
        val bulkSurcharge = if (itemCount > BULK_ITEM_THRESHOLD) BULK_ITEM_FEE else 0
        return itemsSurcharge + bulkSurcharge
    }

    fun isRushHour(timeStamp: ZonedDateTime, deliveryFee: Int): Int {
        //During the Friday rush, 3 - 7 PM, the delivery fee (the total fee including possible surcharges) will be multiplied by 1.2x.
        // However, the fee still cannot be more than the max (15€).
        // Considering timezone, for simplicity,
        // use UTC as a timezone in backend solutions (so Friday rush is 3 - 7 PM UTC).
        val day = timeStamp.dayOfWeek
        val time = timeStamp.toLocalTime()
        return if (day == DayOfWeek.FRIDAY &&
            (time.equals(START_RUSH_HOUR) || time.isAfter(START_RUSH_HOUR))
            && (time.equals(END_RUSH_HOUR) || time.isBefore(END_RUSH_HOUR)))
                (deliveryFee * RUSH_HOUR).toInt()
        else
            deliveryFee
    }
}