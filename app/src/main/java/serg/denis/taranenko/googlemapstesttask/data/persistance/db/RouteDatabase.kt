package serg.denis.taranenko.googlemapstesttask.data.persistance.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import serg.denis.taranenko.googlemapstesttask.data.net.models.route.Polyline
import serg.denis.taranenko.googlemapstesttask.data.persistance.dao.polylines.PointsConverter
import serg.denis.taranenko.googlemapstesttask.data.persistance.dao.polylines.RouteDao
import serg.denis.taranenko.googlemapstesttask.data.persistance.models.Route

@Database(entities = [(Route::class)], version = 3)
@TypeConverters(PointsConverter::class)
abstract class RouteDatabase: RoomDatabase(){

    abstract fun getRouteDao(): RouteDao

    companion object {
        @Volatile
        private var I: RouteDatabase? = null

        fun getInstance(context: Context): RouteDatabase {
            if (I == null) {
                synchronized(RouteDatabase::class.java) {
                    if (I == null) {
                        I = Room.databaseBuilder(context.applicationContext,
                                RouteDatabase::class.java, "route_database")
                                .build()
                    }
                }
            }
            return I!!
        }
    }
}