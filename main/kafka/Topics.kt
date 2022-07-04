package kafka

import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic
import org.apache.kafka.common.serialization.Serdes
import java.time.LocalDate
import kotlin.random.Random

internal object Topics {
    val søker = Topic("aap.sokere.v1", Serdes.ByteArraySerde())
    val søknad = Topic("aap.soknad-sendt.v1", JsonSerde.jackson<SøknadKafkaDto>())
}

internal data class SøknadKafkaDto(
    val fødselsdato: LocalDate = LocalDate.now().minusYears(Random.nextLong(18, 62))
)
