import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kafka.KafkaMock
import kafka.KafkaResult
import kafka.Topics
import no.nav.aap.kafka.vanilla.KafkaConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class DevtoolsTest {

    @Test
    fun `has liveness`() {
        testApplication {
            environment { config = envVars }
            application { server(KafkaMock()) }
            val response = client.get("actuator/live")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `has readiness`() {
        testApplication {
            environment { config = envVars }
            application { server(KafkaMock()) }
            val response = client.get("actuator/ready")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `can fetch configured topics`() {
        testApplication {
            environment { config = envVars }
            application { server(KafkaMock()) }
            val client = createClient { install(ContentNegotiation) { jackson() } }
            val response = client.get("/topics")

            val expected = setOf(
                "aap.sokere.v1",
                "aap.soknad-sendt.v1",
                "aap.sokere.v1",
                "aap.soknad-sendt.v1",
                "aap.inntekter.v1",
                "aap.manuell.11-2.v1",
                "aap.manuell.11-3.v1",
                "aap.manuell.11-4.v1",
                "aap.manuell.11-5.v1",
                "aap.manuell.11-6.v1",
                "aap.manuell.11-12.v1",
                "aap.manuell.11-29.v1",
                "aap.manuell.beregningsdato.v1",
                "aap.vedtak.v1",
                "aap.personopplysninger.v1",
                "aap.mottakere.v1",
                "aap.utbetalingsbehov.v1",
            )
            assertEquals(expected, response.body<Set<String>>())
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    private fun assertNoException(): (RecordMetadata, Exception?) -> Unit = { _, err -> assertNull(err) }

    @Test
    fun `can get latest records from topic`() {
        val kafka = KafkaMock()
        val kafkaConfig = KafkaConfig("mock://kafka", null, null)
        kafka.createProducer(kafkaConfig, Topics.s??ker).use { producer ->
            val record = ProducerRecord(Topics.s??ker.name, 6, "123", "Hello".encodeToByteArray())
            producer.send(record, assertNoException())
        }

        testApplication {
            environment { config = envVars }
            application { server(kafka) }
            val client = createClient { install(ContentNegotiation) { jackson() } }
            val response = client.get("/topic/aap.sokere.v1/latest")

            val expected = KafkaResult(
                topic = Topics.s??ker.name,
                key = "123",
                value = "Hello".encodeToByteArray(),
                partition = 6,
                offset = 0,
                timestamp = -1,
            )

            val actual = response.body<List<KafkaResult>>()

            assertEquals(1, actual.size)
            assertEquals(listOf(expected), actual)
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }


    @Test
    fun `can get empty list when no records found`() {
        testApplication {
            environment { config = envVars }
            application { server(KafkaMock()) }
            val client = createClient { install(ContentNegotiation) { jackson() } }
            val response = client.get("/topic/aap.sokere.v1/latest")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<KafkaResult>(), response.body<List<KafkaResult>>())
        }
    }

    @Test
    fun `can get earliest records from topic`() {
        val kafka = KafkaMock()
        val kafkaConfig = KafkaConfig("mock://kafka", null, null)
        kafka.createProducer(kafkaConfig, Topics.s??ker).use { producer ->
            val record = ProducerRecord(Topics.s??ker.name, 6, "123", "Hello".encodeToByteArray())
            producer.send(record, assertNoException())
        }

        testApplication {
            environment { config = envVars }
            application { server(kafka) }
            val client = createClient { install(ContentNegotiation) { jackson() } }
            val response = client.get("/topic/aap.sokere.v1/earliest")

            val expected = KafkaResult(
                topic = Topics.s??ker.name,
                key = "123",
                value = "Hello".encodeToByteArray(),
                partition = 6,
                offset = 0,
                timestamp = -1,
            )

            val actual = response.body<List<KafkaResult>>()
            assertEquals(1, actual.size)
            assertEquals(listOf(expected), actual)
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `can fetch a specific record from topic`() {
        val kafka = KafkaMock()
        val kafkaConfig = KafkaConfig("mock://kafka", null, null)
        kafka.createProducer(kafkaConfig, Topics.s??ker).use { producer ->
            val record = ProducerRecord(Topics.s??ker.name, 6, "123", "Hello".encodeToByteArray())
            producer.send(record, assertNoException())
        }

        testApplication {
            environment { config = envVars }
            application { server(kafka) }
            val client = createClient { install(ContentNegotiation) { jackson() } }
            val response = client.get("/topic/aap.sokere.v1/6/0")

            val expected = KafkaResult(
                topic = Topics.s??ker.name,
                key = "123",
                value = "Hello".encodeToByteArray(),
                partition = 6,
                offset = 0,
                timestamp = -1,
            )

            val actual = response.body<KafkaResult>()
            assertEquals(expected, actual)
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `can delete s??ker`() {
        testApplication {
            environment { config = envVars }
            application { server(KafkaMock()) }
            val client = createClient { install(ContentNegotiation) { jackson() } }
            client.delete("/s??ker/333")
            val response = client.get("/topic/aap.sokere.v1/latest")

            val expected = KafkaResult(
                topic = Topics.s??ker.name,
                key = "333",
                value = null,
                partition = 11, // default in mock
                offset = 0,
                timestamp = -1,
            )

            val actual = response.body<List<KafkaResult>>()
            assertEquals(listOf(expected), actual)
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `can delete s??knad`() {
        testApplication {
            environment { config = envVars }
            application { server(KafkaMock()) }
            val client = createClient { install(ContentNegotiation) { jackson() } }
            client.delete("/s??knad/555")
            val response = client.get("/topic/aap.soknad-sendt.v1/latest")

            val expected = KafkaResult(
                topic = Topics.s??knad.name,
                key = "555",
                value = null,
                partition = 11, // default in mock
                offset = 0,
                timestamp = -1,
            )

            val actual = response.body<List<KafkaResult>>()
            assertEquals(listOf(expected), actual)
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `can post s??knad`() {
        val jsonS??knad = """
        {
          "f??dselsdato": "1977-01-04"
        }
        """.encodeToByteArray()

        testApplication {
            environment { config = envVars }
            application { server(KafkaMock()) }
            val client = createClient { install(ContentNegotiation) { jackson() } }
            client.post("/s??knad/333") {
                setBody(jsonS??knad)
                contentType(ContentType.Application.Json)
            }

            val response = client.get("/topic/aap.soknad-sendt.v1/latest")

            val expected = KafkaResult(
                topic = Topics.s??knad.name,
                key = "333",
                value = jsonS??knad,
                partition = 11, // default in mock
                offset = 0,
                timestamp = -1,
            )

            val actual = response.body<List<KafkaResult>>()
            assertEquals(listOf(expected), actual)
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }
}

private val envVars = MapApplicationConfig(
    "KAFKA_BROKERS" to "mock://kafka",
    "KAFKA_TRUSTSTORE_PATH" to "",
    "KAFKA_KEYSTORE_PATH" to "",
    "KAFKA_CREDSTORE_PASSWORD" to "",
)
