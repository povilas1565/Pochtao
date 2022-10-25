package com.example.pochtao.controllers
import com.example.pochtao.interfaces.RetrofitService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// Class for creating Retrofit instances and providing them to the rest of the application
@Singleton
class RetrofitController {

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/povilas1565/povilas1565.github.io/main/kolesnikovmail.json")
        .addConverterFactory(GsonConverterFactory.create())
        .build() // create the Retrofit instance

    val service = retrofit.create(RetrofitService::class.java) // create the service
}
