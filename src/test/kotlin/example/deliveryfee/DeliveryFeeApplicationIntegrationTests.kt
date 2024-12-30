package example.deliveryfee

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import example.deliveryfee.dto.Cart
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class DeliveryFeeApplicationIntegrationTests {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

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
    fun shouldReturnZeroDeliveryFee() {
        val cart = Cart(200000, 2235, 4, "2024-01-15T13:00:00Z")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        val documentContext: DocumentContext = JsonPath.parse(response.body)
        val value: Int = documentContext.read("$.delivery_fee")
        assertEquals(0, value)
    }

    @Test
    fun shouldReturnErrorForZeroCartValue() {
        val cart = Cart(0, 2235, 4, "2024-01-15T13:00:00Z")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body?.contains("Invalid input data: cart_value must be greater than zero") == true)
    }

    @Test
    fun shouldReturnErrorForZeroDeliveryDistance() {
        val cart = Cart(790, 0, 4, "2024-01-15T13:00:00Z")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body?.contains("Invalid input data: delivery_distance must be greater than zero") == true)
    }

    @Test
    fun shouldReturnErrorForZeroNumberOfItems() {
        val cart = Cart(790, 2235, 0, "2024-01-15T13:00:00Z")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue(response.body?.contains("Invalid input data: number_of_items must be greater than zero") == true)
    }

    @Test
    fun shouldReturnErrorForExceedingCurrentTime() {
        val cart = Cart(790, 2235, 4, "2026-12-07T13:00:00Z")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid input data: Timestamp must be in UTC format and not exceed the current time", response.body)

    }

    @Test
    fun shouldReturnErrorForInvalidTimeZone() {
        val cart = Cart(790, 2235, 4, "2026-12-07T13:00:00+2:00")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid input data: Timestamp must be in UTC format and not exceed the current time", response.body)
    }

    @Test
    fun shouldCalculateCorrectlyForRushHour() {
        val cart = Cart(790, 1000, 4, "2024-12-06T15:10:00Z")
        val response: ResponseEntity<String> = restTemplate.postForEntity("/cart", cart, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        val documentContext: DocumentContext = JsonPath.parse(response.body)
        val value: Int = documentContext.read("$.delivery_fee")
        assertEquals(492, value)
    }

}