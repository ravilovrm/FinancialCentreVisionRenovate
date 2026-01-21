package com.example.myapplication.util

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.random.Random

interface VkApi {
    @GET("messages.send")
    suspend fun sendMessage(
        @Query("access_token") token: String,
        @Query("peer_id") peerId: String,
        @Query("message") message: String,
        @Query("random_id") randomId: Int = Random.nextInt(),
        @Query("v") v: String = "5.131"
    )
}

object VkManager {
    private const val TOKEN = "vk1.a.Nct1a6Wzusb44SiIDyC_IeJmOEB7CbWhuErnOxZJRGYFFEjLHVKXuITqe2PDh3ExP0J6pw1uw7KIfoKFI3IVbqpiitEnx_izQPdL7Pyu1DiGmjOOcDD3cpQvsyYDYa81XLQtB36JmXAWxR5onJ17Q_ggWRiEKtIIhElaTz7pKXeYzRx1h-ABWHqvtGINuHmrmAQhYmUKcWm7SLZG3Fol9Q"
    
    // ВСТАВЬ СВОЙ ЦИФРОВОЙ ID ВК ТУТ
    private const val MY_USER_ID = "435064930" 

    private val api = Retrofit.Builder()
        .baseUrl("https://api.vk.com/method/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(VkApi::class.java)

    suspend fun sendEstimate(area: Double, price: Double) {
        val msg = "🏠 VisionRenovate: Новый расчет!\n📏 Площадь: $area м²\n💰 Итого: $price руб."
        safeSend(msg)
    }

    suspend fun callHelp() {
        safeSend("🆘 Пользователю нужна помощь с расчетами в приложении!")
    }

    private suspend fun safeSend(msg: String) {
        try { api.sendMessage(TOKEN, MY_USER_ID, msg) } catch (e: Exception) { e.printStackTrace() }
    }
}