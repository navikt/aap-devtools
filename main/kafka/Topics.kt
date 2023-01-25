package kafka

import no.nav.aap.kafka.streams.Topic
import org.apache.kafka.common.serialization.Serdes

object Topics {
    val søker = Topic("aap.sokere.v1", Serdes.ByteArraySerde())
    val søknad = Topic("aap.soknad-sendt.v1", Serdes.ByteArraySerde())

    val vedtak = Topic("aap.vedtak.v1", Serdes.ByteArraySerde())
    val mottakere = Topic("aap.mottakere.v1", Serdes.ByteArraySerde())
    val meldeplikt = Topic("aap.meldeplikt.v1", Serdes.ByteArraySerde())

    private val inntekter = Topic("aap.inntekter.v1", Serdes.ByteArraySerde())
    private val man11_2 = Topic("aap.manuell.11-2.v1", Serdes.ByteArraySerde())
    private val man11_3 = Topic("aap.manuell.11-3.v1", Serdes.ByteArraySerde())
    private val man11_4 = Topic("aap.manuell.11-4.v1", Serdes.ByteArraySerde())
    private val man11_5 = Topic("aap.manuell.11-5.v1", Serdes.ByteArraySerde())
    private val man11_6 = Topic("aap.manuell.11-6.v1", Serdes.ByteArraySerde())
    private val man11_19 = Topic("aap.manuell.11-19.v1", Serdes.ByteArraySerde())
    private val man11_29 = Topic("aap.manuell.11-29.v1", Serdes.ByteArraySerde())
    private val man22_13 = Topic("aap.manuell.22-13.v1", Serdes.ByteArraySerde())
    private val kval11_2 = Topic("aap.kvalitetssikring.11-2.v1", Serdes.ByteArraySerde())
    private val kval11_3 = Topic("aap.kvalitetssikring.11-3.v1", Serdes.ByteArraySerde())
    private val kval11_4 = Topic("aap.kvalitetssikring.11-4.v1", Serdes.ByteArraySerde())
    private val kval11_5 = Topic("aap.kvalitetssikring.11-5.v1", Serdes.ByteArraySerde())
    private val kval11_6 = Topic("aap.kvalitetssikring.11-6.v1", Serdes.ByteArraySerde())
    private val kval11_19 = Topic("aap.kvalitetssikring.11-19.v1", Serdes.ByteArraySerde())
    private val kval11_29 = Topic("aap.kvalitetssikring.11-29.v1", Serdes.ByteArraySerde())
    private val kval22_13 = Topic("aap.kvalitetssikring.22-13.v1", Serdes.ByteArraySerde())
    private val medlem = Topic("aap.medlem.v1", Serdes.ByteArraySerde())
    private val personopplysninger = Topic("aap.personopplysninger.v1", Serdes.ByteArraySerde())
    private val utbetalingsbehov = Topic("aap.utbetalingsbehov.v1", Serdes.ByteArraySerde())
    private val sykepengedager = Topic("aap.sykepengedager.v1", Serdes.ByteArraySerde())
    private val sykepengedagerInfotrygd = Topic("aap.sykepengedager.infotrygd.v1", Serdes.ByteArraySerde())
    private val sykepengedagerInfotrygdQ1 = Topic("aap.sykepengedager.infotrygd-q1.v1", Serdes.ByteArraySerde())

    operator fun get(name: String) = all.single { it.name == name }

    val all by lazy {
        listOf(
            søker,
            søknad,
            inntekter,
            man11_2,
            man11_3,
            man11_4,
            man11_5,
            man11_6,
            man11_19,
            man11_29,
            man22_13,
            kval11_2,
            kval11_3,
            kval11_4,
            kval11_5,
            kval11_6,
            kval11_19,
            kval11_29,
            kval22_13,
            vedtak,
            medlem,
            personopplysninger,
            mottakere,
            utbetalingsbehov,
            sykepengedager,
            sykepengedagerInfotrygd,
            sykepengedagerInfotrygdQ1,
            meldeplikt
        )
    }
}
