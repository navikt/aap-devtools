package dolly

data class DollyResponseGrupper(
    val navn: String,
    val antallIdenter: Int,
    val identer: List<DollyIdent>
)

data class DollyIdent(
    val ident: String,
)
