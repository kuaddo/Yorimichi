package jp.shiita.yorimichi.data

data class DirectionResult(
        val routes: List<Route>,
        val geocodedWaypoints: List<GeocodedWaypoint>,
        val status: String
) {
    data class Route(
            val bounds: Viewport,
            val legs: List<Leg>,
            val summary: String,
            val copyrights: String,
            val overviewPolyline: Polyline,
            val warnings: List<String>,
            val waypointOrder: List<Int>
    )

    data class GeocodedWaypoint(
            val geocoderStatus: String,
            val placeId: String,
            val types: List<String>
    )

    data class Leg(
            val distance: Value,
            val duration: Value,
            val startLocation: Location,
            val endLocation: Location,
            val startAddress: String,
            val endAddress: String,
            val steps: List<Step>
    )

    data class Step(
            val distance: Value,
            val duration: Value,
            val startLocation: Location,
            val endLocation: Location,
            val travelMode: String,
            val polyline: Polyline,
            val htmlInstructions: String
    )

    data class Value(
            val value: Int,
            val text: String
    )

    data class Polyline(
            val points: String
    )
}