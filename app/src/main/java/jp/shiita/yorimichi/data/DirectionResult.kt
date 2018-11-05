package jp.shiita.yorimichi.data

import com.google.android.gms.maps.model.LatLng

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
    ) {
        private var _routes: List<LatLng>? = null
        val routes: List<LatLng>
            get() = _routes ?: decodePolyline(points)

        private fun decodePolyline(encoded: String): List<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng

                val p = LatLng(lat.toDouble() / 1E5,
                        lng.toDouble() / 1E5)
                poly.add(p)
            }

            return poly
        }
    }

    data class GeocodedWaypoint(
            val geocoderStatus: String,
            val placeId: String,
            val types: List<String>
    )
}