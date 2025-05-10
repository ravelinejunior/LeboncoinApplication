package com.raveline.leboncoinapplication.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response

class ImageInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val newRequest = original.newBuilder()
            .header("User-Agent", "LeboncoinApp/1.0")
            .build()
        val response = chain.proceed(newRequest)
        println("Request: ${newRequest.url}, Method: ${newRequest.method}, Headers: ${newRequest.headers}")
        println("Response: ${response.code}, Message: ${response.message}, Headers: ${response.headers}")

        return response
    }
}