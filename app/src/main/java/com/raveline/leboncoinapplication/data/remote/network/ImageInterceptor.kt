package com.raveline.leboncoinapplication.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response

class ImageInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val newRequest = original.newBuilder()
            .header("User-Agent", "LeboncoinApp/1.0")
            .build()
        return chain.proceed(newRequest)
    }
}