package dolly

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DollyResponsePdl(val ident: String, val person: DollyPerson)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DollyPerson(
    val doedsfall: List<Doedsfall>,
    val foedsel: List<Foedsel>,
    val navn: List<Navn>,
    val forelderBarnRelasjon: List<ForelderBarnRelasjon>,
    val sivilstand: List<Sivilstand>
)

data class ForelderBarnRelasjon(
    val relatertPersonsIdent: String,
    val barn: Boolean
)

data class Sivilstand(val type: String, val relatertVedSivilstand: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Doedsfall(val doedsdato: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Foedsel(val foedselsdato: String, val foedselsaar: Int)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Navn(val fornavn: String, val etternavn: String)
