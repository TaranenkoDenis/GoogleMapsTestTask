package serg.denis.taranenko.googlemapstesttask.testActivity3

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    private companion object {
        lateinit var retrofit: Retrofit
            private set
    }

    fun getClient(baseUrl: String): Retrofit {
        Log.d("MyTag", "instance = $retrofit")

        if (retrofit == null)
            retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

        return retrofit
    }

}