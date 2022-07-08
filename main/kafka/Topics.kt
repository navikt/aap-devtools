package kafka

import no.nav.aap.kafka.streams.Topic
import org.apache.kafka.common.serialization.Serdes

object Topics {
    val søker = Topic("aap.sokere.v1", Serdes.ByteArraySerde())
    val søknad = Topic("aap.soknad-sendt.v1", Serdes.ByteArraySerde())
    val inntekter = Topic("aap.inntekter.v1", Serdes.ByteArraySerde())
    val man11_2 = Topic("aap.manuell.11-2.v1", Serdes.ByteArraySerde())
    val man11_3 = Topic("aap.manuell.11-3.v1", Serdes.ByteArraySerde())
    val man11_4 = Topic("aap.manuell.11-4.v1", Serdes.ByteArraySerde())
    val man11_5 = Topic("aap.manuell.11-5.v1", Serdes.ByteArraySerde())
    val man11_6 = Topic("aap.manuell.11-6.v1", Serdes.ByteArraySerde())
    val man11_12 = Topic("aap.manuell.11-12.v1", Serdes.ByteArraySerde())
    val man11_29 = Topic("aap.manuell.11-29.v1", Serdes.ByteArraySerde())
    val man_beregn = Topic("aap.manuell.beregningsdato.v1", Serdes.ByteArraySerde())
    val vedtak = Topic("aap.vedtak.v1", Serdes.ByteArraySerde())
    val personopplysninger = Topic("aap.personopplysninger.v1", Serdes.ByteArraySerde())
    val mottakere = Topic("aap.mottakere.v1", Serdes.ByteArraySerde())
    val utbetalingsbehov = Topic("aap.utbetalingsbehov.v1", Serdes.ByteArraySerde())

    val all = listOf(
        søker,
        søknad,
        inntekter,
        man11_2,
        man11_3,
        man11_4,
        man11_5,
        man11_6,
        man11_12,
        man11_29,
        man_beregn,
        vedtak,
        personopplysninger,
        mottakere,
        utbetalingsbehov,
    ).associateBy { it.name }
}
