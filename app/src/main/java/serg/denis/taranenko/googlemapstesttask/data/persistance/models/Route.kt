package serg.denis.taranenko.googlemapstesttask.data.persistance.models

import android.arch.persistence.room.*
import serg.denis.taranenko.googlemapstesttask.data.persistance.dao.polylines.PointsConverter

@Entity(tableName = "Routes")
data class Route(@ColumnInfo(name = "NameOfRout")
                 val name: String,
                 @ColumnInfo(name = "Origin")
                 val origin: String,
                 @ColumnInfo(name = "Destination")
                 val destination: String,
                 @ColumnInfo(name = "WayPoints")
                 val waypoints: String) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var id: Long = 0

    override fun toString(): String {
        return "$name"
    }
}