package dolly

import com.fasterxml.jackson.databind.DeserializationFeature
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
import no.nav.aap.ktor.client.AzureAdTokenProvider
import no.nav.aap.ktor.client.AzureConfig
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDate
import java.util.*

data class DollyConfig(
    val url: URL,
    val scope: String,
)

private val log = LoggerFactory.getLogger(DollyClient::class.java)
private val secureLog = LoggerFactory.getLogger("secureLog")

class DollyClient(private val dollyConfig: DollyConfig, azureConfig: AzureConfig) {
    private val tokenProvider = AzureAdTokenProvider(azureConfig, dollyConfig.scope)

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
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

    suspend fun hentBrukere(gruppeId: String): List<DollyResponsePerson> {
        val token = tokenProvider.getClientCredentialToken()
        val callId = callId

        val gruppeResponse = httpClient.get("${dollyConfig.url}/gruppe/$gruppeId/page/0") {
            parameter("pageSize", 20)
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", callId)
            header("Nav-Consumer-Id", "aap_oppgavestyring")
            bearerAuth(token)
        }

        return if (gruppeResponse.status.isSuccess()) {
            val identer = gruppeResponse
                .body<DollyResponseGrupper>()
                .identer
                .map(DollyIdent::ident)
                .joinToString(",")

            val personerResponse = httpClient.get("${dollyConfig.url}/pdlperson/identer?identer=$identer") {
                    accept(ContentType.Application.Json)
                    header("Nav-Call-Id", callId)
                    header("Nav-Consumer-Id", "aap_oppgavestyring")
                    bearerAuth(token)
                }

            if (personerResponse.status.isSuccess()) {
                personerResponse.body<DollyResponsePdl>().data.hentPersonBolk.map {
                    DollyResponsePerson(
                        fødselsnummer = it.ident,
                        navn = "${it.person?.navn?.first()?.fornavn} ${it.person?.navn?.first()?.etternavn}",
                        fødselsdato = it.person?.foedsel?.first()?.foedselsdato ?: LocalDate.now().minusYears(30)
                    )
                }
            } else {
                secureLog.error("Feilet mot dolly ved henting av personer fra dolly: $personerResponse")
                emptyList()
            }
        } else {
            secureLog.error("Fikk ikke response ved henting av gruppe $gruppeId fra dolly: $gruppeResponse")
            emptyList()
        }
    }

    private val callId: String get() = UUID.randomUUID().toString().also { log.info("calling dolly with call-id $it") }
}

data class DollyResponsePerson(
    val fødselsnummer: String,
    val navn: String,
    val fødselsdato: LocalDate
)
