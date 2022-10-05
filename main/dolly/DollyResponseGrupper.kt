package dolly

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DollyResponseGrupper(
    val navn: String,
    val antallIdenter: Int,
    val identer: List<DollyIdent>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DollyIdent(
    val ident: String,
)
