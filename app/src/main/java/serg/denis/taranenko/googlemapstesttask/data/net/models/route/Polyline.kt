package serg.denis.taranenko.googlemapstesttask.data.net.models.route

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Polyline(
    val points: String
)