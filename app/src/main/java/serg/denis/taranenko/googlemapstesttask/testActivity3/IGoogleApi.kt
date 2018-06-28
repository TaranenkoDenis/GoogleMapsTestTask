package serg.denis.taranenko.googlemapstesttask.testActivity3

import retrofit2.Call
import retrofit2.http.GET

interface IGoogleApi {
    @GET
    fun getDataFromGoogleApi(url: String): Call<String>
}