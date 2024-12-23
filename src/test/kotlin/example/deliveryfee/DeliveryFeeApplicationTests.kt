package example.deliveryfee

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZonedDateTime
import kotlin.test.assertEquals

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
class DeliveryFeeApplicationTests {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var deliveryFeeService: DeliveryFeeService

    private fun assertBadRequest(payloadJson: String, expectedMessage: String){
        mockMvc.perform(
            MockMvcRequestBuilders.post("/cart")
                .contentType("application/json")
                .content(payloadJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string(expectedMessage))
    }

    private fun assertIsOkRequest(payloadJson: String, @Suppress("SameParameterValue") expectedMessage: String){
        mockMvc.perform(
            MockMvcRequestBuilders.post("/cart")
                .contentType("application/json")
                .content(payloadJson))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string(expectedMessage))
    }

    @Test
    fun shouldCalculateCorrectly() {
        val cart = Cart(790, 2235, 4, "2024-01-15T13:00:00Z")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assert(response.statusCode == HttpStatus.OK)
        val documentContext: DocumentContext = JsonPath.parse(response.body)
        val value: Int = documentContext.read("$.delivery_fee")
        assertEquals(710, value)
    }

    @Test
    fun shouldReturnErrorForZeroCartValue() {
        val cartJson = """
            {
                "cart_value": 0,
                "delivery_distance": 2235,
                "number_of_items": 4,
                "time": "2024-01-15T13:00:00Z"
            }
        """.trimIndent()

        assertBadRequest(cartJson,"cart_value must be greater than 0")
    }

    @Test
    fun shouldReturnErrorForZeroDeliveryDistance() {
        val cartJson = """
            {
                "cart_value": 790,
                "delivery_distance": 0,
                "number_of_items": 4,
                "time": "2024-01-15T13:00:00Z"
            }
        """.trimIndent()

        assertBadRequest(cartJson,"delivery_distance must be greater than 0")
    }

    @Test
    fun shouldReturnErrorForZeroNumberOfItems() {
        val cartJson = """
            {
                "cart_value": 790,
                "delivery_distance": 2235,
                "number_of_items": 0,
                "time": "2024-01-15T13:00:00Z"
            }
        """.trimIndent()

        assertBadRequest(cartJson,"number_of_items must be greater than 0")
    }

    @Test
    fun shouldReturnErrorForExceedingCurrentTime() {
        val cartJson = """
            {
                "cart_value": 790,
                "delivery_distance": 2235,
                "number_of_items": 4,
                "time": "2026-12-07T13:00:00Z"
            }
        """.trimIndent()

        assertBadRequest(cartJson,"The timestamp cannot exceed the current datetime.")
    }

    @Test
    fun shouldReturnErrorForInvalidTimeZone() {
        val cartJson = """
            {
                "cart_value": 790,
                "delivery_distance": 2235,
                "number_of_items": 4,
                "time": "2026-12-07T13:00:00+2:00"
            }
        """.trimIndent()

        @Suppress("SpellCheckingInspection")
        assertBadRequest(cartJson,"Invalid time format. Must be in UTC ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z').")
    }

    @Test
    fun shouldCalculateCorrectlyForRushHour(){
        val cartJson = """
                {
                    "cart_value": 790,
                    "delivery_distance": 1000,
                    "number_of_items": 4,
                    "time": "2024-12-06T15:10:00Z"
                }
            """.trimIndent()
        assertIsOkRequest(cartJson, "{\"delivery_fee\":492}")
    }
    //TEST SERVICE LAYER FUNCTIONS

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
    fun `test for bulk fee thirteen items`() {
        val surcharge: Int = deliveryFeeService.calculateNumberOfItemsFee(13)
        //Example 4: If the number of items is 13, 5,70€ surcharge is added ((9 * 50 cents) + 1,20€)
        assertEquals(570, surcharge)
    }

    @Test
    fun `test RushHour`(){
        val surcharge: Int = deliveryFeeService.isRushHour(ZonedDateTime.parse("2024-12-06T15:10:00Z")
            , 500)
        // deliveryFee * 1.2x
        assertEquals(600, surcharge)
    }

}
