package com.example.myapplication.util

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Url

// 1. Интерфейс API Яндекса
interface YandexApi {
    // Получить ссылку для загрузки
    @GET("v1/disk/resources/upload")
    suspend fun getUploadLink(
        @Header("Authorization") auth: String,
        @Query("path") path: String,
        @Query("overwrite") overwrite: Boolean = true
    ): UploadLinkResponse

    // Сама загрузка файла (по полученной ссылке)
    @PUT
    suspend fun uploadFile(
        @Url url: String,
        @retrofit2.http.Body file: okhttp3.RequestBody
    ): ResponseBody
}

data class UploadLinkResponse(val href: String, val method: String)

// 2. Менеджер
object YandexManager {
    // ВСТАВЬ СЮДА ТОКЕН, КОТОРЫЙ ТЫ СКОПИРОВАЛ В БРАУЗЕРЕ (ШАГ 1)
    private const val TOKEN = "y0__xDNv7T_Axjh2DEgvNKw6hUwla3Q4wesL_wqU1SWrXN0FgtSoFG1UBEP7g" 

    private val api = Retrofit.Builder()
        .baseUrl("https://cloud-api.yandex.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YandexApi::class.java)

    // Функция загрузки PDF
    suspend fun uploadPdf(fileName: String, pdfBytes: ByteArray): Boolean {
        return try {
            // 1. Просим у Яндекса ссылку: "Куда залить файл?"
            val response = api.getUploadLink("OAuth $TOKEN", path = "disk:/$fileName")
            val uploadUrl = response.href

            // 2. Отправляем байты файла
            val requestBody = pdfBytes.toRequestBody("application/pdf".toMediaTypeOrNull())
            api.uploadFile(uploadUrl, requestBody)
            
            Log.d("Yandex", "Файл $fileName успешно загружен!")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}