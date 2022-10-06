package dolly

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import no.nav.aap.ktor.client.AzureConfig
import no.nav.aap.ktor.client.HttpClientAzureAdTokenProvider
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.LocalDate
import java.util.*

data class DollyConfig(
    val url: URI,
    val scope: String,
)

private val log = LoggerFactory.getLogger(DollyClient::class.java)
private val secureLog = LoggerFactory.getLogger("secureLog")

class DollyClient(private val dollyConfig: DollyConfig, azureConfig: AzureConfig) {
    private val tokenProvider = HttpClientAzureAdTokenProvider(azureConfig, dollyConfig.scope)

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout)
        install(HttpRequestRetry)
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) = secureLog.info(message)
            }
        }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
    }

    suspend fun hentBrukere(): List<DollyResponsePerson> {
        val token = tokenProvider.getToken()
        val callId = callId
        val brukereForGruppe = httpClient.get(dollyConfig.url.toURL()) {
            url {
                appendPathSegments("gruppe", "4946")
            }
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", callId)
            header("Nav-Consumer-Id", "aap_oppgavestyring")
            bearerAuth(token)
        }.body<DollyResponseGrupper>()

        val identer = brukereForGruppe.identer.map { it.ident }
        val url = "${dollyConfig.url.toURL()}/pdlperson/identer?identer=${identer.joinToString(",")}"

        val brukerlisteJson = httpClient.get(url) {
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", callId)
            header("Nav-Consumer-Id", "aap_oppgavestyring")
            bearerAuth(token)
        }.body<DollyResponsePdl>()

        return brukerlisteJson.data.hentPersonBolk.map { DollyResponsePerson(
            fødselsnummer = it.ident,
            navn = "${it.person.navn.first().fornavn} ${it.person.navn.first().etternavn}",
            fødselsdato = it.person.foedsel.first().foedselsdato
        ) }
    }

    private val callId: String get() = UUID.randomUUID().toString().also { log.info("calling dolly with call-id $it") }
}

data class DollyResponsePerson(
    val fødselsnummer: String,
    val navn: String,
    val fødselsdato: LocalDate
)