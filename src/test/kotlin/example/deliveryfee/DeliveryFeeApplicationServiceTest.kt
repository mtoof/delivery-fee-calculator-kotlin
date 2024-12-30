package example.deliveryfee

import example.deliveryfee.service.DeliveryFeeService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZonedDateTime
import kotlin.test.assertEquals


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class DeliveryFeeApplicationServiceTest() {

    @Autowired
    lateinit var deliveryFeeService: DeliveryFeeService

    @Test
    fun `test small order surcharge`(){
        val surcharge = deliveryFeeService.calculateCartValueSurcharge(790)

        assertEquals(210, surcharge)
    }

    @Test
    fun `test delivery distance surcharge`(){
        val surcharge: Int = deliveryFeeService.calculateDeliveryDistanceFee(2150)
        //2e for first 1000m + 1e x for every extra less than equal 500m -> 2 + (1 x 3) = 5 euro
        assertEquals(500, surcharge)
    }

    @Test
    fun `test zero for four items`() {
        val surcharge: Int = deliveryFeeService.calculateNumberOfItemsFee(4)
        //Example 1: If the number of items is 4, no extra surcharge
        assertEquals(0, surcharge)
    }

    @Test
    fun `test for ten items`() {
        val surcharge: Int = deliveryFeeService.calculateNumberOfItemsFee(10)
        //Example 3: If the number of items is 10, 3€ surcharge (6 x 50 cents) is added
        assertEquals(300, surcharge)
    }

    @Test
    fun `test for bulk fee surcharge for thirteen items`() {
        val surcharge: Int = deliveryFeeService.calculateNumberOfItemsFee(13)
        //Example 4: If the number of items is 13, 5,70€ surcharge is added ((9 * 50 cents) + 1,20€)
        assertEquals(570, surcharge)
    }

    @Test
    fun `test RushHour`(){
        val surcharge: Int = deliveryFeeService.isRushHour(
            ZonedDateTime.parse("2024-12-06T15:10:00Z")
            , 500)
        // deliveryFee * 1.2x
        assertEquals(600, surcharge)
    }
}