package dolly

import java.time.LocalDate

data class DollyResponsePdl(val data: DollyResponseData)

data class DollyResponseData(val hentPersonBolk: List<DollyPersonBolk>)

data class DollyPersonBolk(val ident: String, val person: DollyPerson)

data class DollyPerson(
    val navn: List<Navn>,
    val foedsel: List<Fødsel>
)

data class Navn(val fornavn: String, val etternavn: String)

data class Fødsel(val foedselsdato: LocalDate)
