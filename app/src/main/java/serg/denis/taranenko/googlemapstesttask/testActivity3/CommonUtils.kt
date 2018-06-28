package serg.denis.taranenko.googlemapstesttask.testActivity3

const val baseUrl = "https://googleapis.com"

public fun getGoogleApi(): IGoogleApi{
    return RetrofitClient().getClient(baseUrl)
            .create(IGoogleApi::class.java)
}