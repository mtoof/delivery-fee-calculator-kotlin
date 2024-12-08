package example.deliveryfee

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.BeforeEach
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
    private lateinit var deliveryFeeCalculatorService: DeliveryFeeCalculatorService

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var calculatorService: DeliveryFeeCalculatorService

    @BeforeEach
    fun setUp() {
        calculatorService = DeliveryFeeCalculatorService()
    }

    @Test
    fun shouldReturnTheSameCart() {
        val cart = Cart(790, 2235, 4, "2024-01-15T13:00:00Z")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assert(response.statusCode == HttpStatus.OK)
        val documentContext :DocumentContext = JsonPath.parse(response.body)
        val value = documentContext.read<Int>("$.delivery_fee")
        assert(value==210)
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

        mockMvc.perform(
            MockMvcRequestBuilders.post("/cart")
            .contentType("application/json")
            .content(cartJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("cart_value must be greater than 0"))
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

        mockMvc.perform(
            MockMvcRequestBuilders.post("/cart")
                .contentType("application/json")
                .content(cartJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("delivery_distance must be greater than 0"))
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

        mockMvc.perform(
            MockMvcRequestBuilders.post("/cart")
                .contentType("application/json")
                .content(cartJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("number_of_items must be greater than 0"))
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

        mockMvc.perform(
            MockMvcRequestBuilders.post("/cart")
                .contentType("application/json")
                .content(cartJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("The timestamp cannot exceed the current datetime."))
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

        mockMvc.perform(
            MockMvcRequestBuilders.post("/cart")
                .contentType("application/json")
                .content(cartJson))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().string("Invalid time format. Must be in UTC ISO 8601 format (yyyy-MM-dd'T'HH:mm:ss'Z')."))
    }

    @Test
    fun `test small order surcharge`(){
        val surcharge = calculatorService.calculateDeliveryFee(790,
            1000,
            4,
            ZonedDateTime.parse("2026-12-07T13:00:00Z"))

        assertEquals(410, surcharge)
    }
}
