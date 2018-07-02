package serg.denis.taranenko.googlemapstesttask.data.net.apis

import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.ResponseGeoPlaces
import serg.denis.taranenko.googlemapstesttask.data.net.models.places.PlaceDetailsResult
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Response
import serg.denis.taranenko.googlemapstesttask.API_KEY
import serg.denis.taranenko.googlemapstesttask.data.net.models.route.ResponseRoute
import java.io.IOException
import okhttp3.logging.HttpLoggingInterceptor




interface GeoApi {

    @GET("place/autocomplete/json")
    fun autocomplete(
            @Query("input") input: String,
            @Query("types") types: String,
            @Query("location") location: String,
            @Query("radius") radius: Int
    ): Observable<ResponseGeoPlaces>

    @GET("place/autocomplete/json")
    fun autocomplete(
            @Query("input") input: String
    ): Observable<ResponseGeoPlaces>

    @GET("place/details/json")
    fun details(@Query("placeid") placeId: String): Observable<PlaceDetailsResult>

    @GET("directions/json")
    fun getRoute(
            @Query("origin") origins: String,
            @Query("destination") destinations: String
            ): Observable<ResponseRoute>

    @GET("directions/json")
    fun getRoute(
            @Query("origin") origins: String,
            @Query("destination") destinations: String,
            @Query("waypoints") waypoints: String
    ): Observable<ResponseRoute>

    /**
     * Factory class for convenient creation of the Api Service interface
     */
    object Factory {

        fun create(baseUrl: String): GeoApi {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(GoogleApiInterceptor())
                    .addInterceptor(logging)
                    .build()

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .baseUrl(baseUrl)
                    .build()

            return retrofit.create(GeoApi::class.java)
        }
    }

    private class GoogleApiInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(@NonNull chain: Interceptor.Chain): Response {
            var request = chain.request()
            val url = request.url().newBuilder().addQueryParameter("key", API_KEY).build()
            request = request.newBuilder().url(url).build()
            return chain.proceed(request)
        }
    }
}