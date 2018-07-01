package serg.denis.taranenko.googlemapstesttask.data.persistance.dao.polylines

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import serg.denis.taranenko.googlemapstesttask.data.persistance.models.Route

@Dao
public interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoute(route: Route)

    @Query("SELECT * FROM Routes")
    fun getAllRoutes(): Single<List<Route>>
}