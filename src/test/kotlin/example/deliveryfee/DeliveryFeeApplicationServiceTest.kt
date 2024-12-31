package example.deliveryfee

import example.deliveryfee.service.DeliveryFeeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals


class DeliveryFeeApplicationServiceTest() {
    lateinit var deliveryFeeService: DeliveryFeeService

    @BeforeEach
    fun setUp(){
        deliveryFeeService = DeliveryFeeService()
    }

    @Test
    fun `test small order surcharge`(){
        val surcharge = deliveryFeeService.calculateCartValueSurcharge(790)
        assertEquals(210, surcharge) //10e-7.9 = 2.10e
    }

    @Test
    fun `test delivery distance surcharge`(){
        val surcharge: Int = deliveryFeeService.calculateDeliveryDistanceFee(2150)
        assertEquals(500, surcharge)//2e for first 1000m + 1e x for every extra less than equal 500m -> 2 + (1 x 3) = 5 euro
    }

    @Test
    fun `test zero for four items`() {
        val surcharge: Int = deliveryFeeService.calculateNumberOfItemsFee(4)
        assertEquals(0, surcharge) //If the number of items is 4, no extra surcharge
    }

    @Test
    fun `test for ten items`() {
        val surcharge: Int = deliveryFeeService.calculateNumberOfItemsFee(10)
        assertEquals(300, surcharge) //If the number of items is 10, 3€ surcharge (6 x 50 cents) is added
    }

    @Test
    fun `test for bulk fee surcharge for more than 12 items`() {
        val surcharge: Int = deliveryFeeService.calculateNumberOfItemsFee(13)
        assertEquals(570, surcharge) //If the number of items is 13, 5,70€ surcharge is added ((9 * 50 cents) + 1,20€)
    }

    @Test
    fun `test isRushHour`(){
        val surcharge: Int = deliveryFeeService.isRushHour(
            ZonedDateTime.parse("2024-12-06T15:10:00Z")
            , 500)
        assertEquals(600, surcharge) // deliveryFee * 1.2x
    }

    @Test
    fun `test for 200 euro cart_value`() {
        val deliveryFee: Int = deliveryFeeService.calculateDeliveryFee(
            20000,
            10000,
            20,
            timeStamp = ZonedDateTime.parse("2024-12-06T15:10:00Z")
        )
        assertEquals(0, deliveryFee)
    }

    @Test
    fun `test for max delivery fee`() {
        val deliveryFee: Int = deliveryFeeService.calculateDeliveryFee(
            790,
            4100,
            20,
            timeStamp = ZonedDateTime.parse("2024-12-06T15:10:00Z")
        )
        assertEquals(1500, deliveryFee)// 210+900+920 = (2030 * 1.2x) > 1500
    }
}