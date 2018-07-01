package serg.denis.taranenko.googlemapstesttask.data.persistance.models

import android.arch.persistence.room.*
import serg.denis.taranenko.googlemapstesttask.data.persistance.dao.polylines.PointsConverter

@Entity(tableName = "Routes")
data class Route(@ColumnInfo(name = "NameOfRout")
                 val name: String,
                 @ColumnInfo(name = "PolylinePoints")
                 val polylinePoints: List<String>) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var id: Long = 0

    override fun toString(): String {
        return "id = $id; name = $name"
    }
}