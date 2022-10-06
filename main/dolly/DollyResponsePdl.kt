package dolly

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class DollyResponsePdl(val data: DollyResponseData)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DollyResponseData(val hentPersonBolk: List<DollyPersonBolk>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DollyPersonBolk(val ident: String, val person: DollyPerson)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DollyPerson(
    val navn: List<Navn>,
    val foedsel: List<Fødsel>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Navn(val fornavn: String, val etternavn: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Fødsel(val foedselsdato: LocalDate)