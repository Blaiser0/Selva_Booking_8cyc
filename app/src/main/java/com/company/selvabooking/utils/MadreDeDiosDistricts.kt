package com.company.selvabooking.utils

object MadreDeDiosDistricts {

    private val districtNames = listOf(
        "Tambopata",
        "Las Piedras",
        "Laberinto",
        "Inambari",
        "Manu",
        "Huepetuhe",
        "Madre de Dios",
        "Fitzcarrald",
        "Tahuamanu",
        "Iberia",
        "Iñapari"
    )

    val districts: List<String> = districtNames.sortedWith(
        compareBy(java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es-PE"))) { it }
    )

    private val postalCodesByDistrict: Map<String, List<String>> = mapOf(
        "Tambopata" to listOf("17000", "17001"),
        "Las Piedras" to listOf("17100"),
        "Laberinto" to listOf("17400"),
        "Inambari" to listOf("17501"),
        "Manu" to listOf("17700"),
        "Huepetuhe" to listOf("17550", "17551"),
        "Madre de Dios" to listOf("17600", "17601"),
        "Fitzcarrald" to listOf("17800"),
        "Tahuamanu" to listOf("17200"),
        "Iberia" to listOf("17250", "17251"),
        "Iñapari" to listOf("17300")
    )

    fun postalCodesFor(district: String): List<String> =
        postalCodesByDistrict[district].orEmpty()

    fun isValidDistrict(district: String): Boolean =
        district in postalCodesByDistrict

    fun isValidPostalCode(district: String, postalCode: String): Boolean =
        postalCodesFor(district).contains(postalCode)

    fun requiresPostalCodeSelection(district: String): Boolean =
        postalCodesFor(district).size > 1

    fun resolvePostalCodeOnDistrictChange(district: String, currentPostalCode: String): String {
        val codes = postalCodesFor(district)
        return when {
            codes.isEmpty() -> ""
            codes.size == 1 -> codes.first()
            currentPostalCode in codes -> currentPostalCode
            else -> ""
        }
    }
}
