package serg.denis.taranenko.googlemapstesttask.data.persistance.dao.polylines

import android.arch.persistence.room.TypeConverter
import java.util.*

public class PointsConverter {

    @TypeConverter
    public fun fromPoints(points: List<String>): String {

        val sb = StringBuilder()

        for (p in points){
            sb.append("$p|")
        }

        return sb.toString()
    }

    @TypeConverter
    public fun toPoints(data: String): List<String> {
        return data.split("|")
    }
}