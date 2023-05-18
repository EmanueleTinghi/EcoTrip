package it.unipi.dii.masss_project.database

data class AggregateResults (
    val city: String = "",
    val travelDistances: Map<String, Map<String, Int>> = mapOf(
        "range(<1km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
        "range(1-5km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
        "range(5-10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0),
        "range(>10km)" to mapOf("bus" to 0, "car" to 0, "train" to 0, "walking" to 0)
    )
)