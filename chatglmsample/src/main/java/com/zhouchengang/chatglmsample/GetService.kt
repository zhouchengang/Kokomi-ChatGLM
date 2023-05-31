package com.zhouchengang.chatglmsample


import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface GetService {

    @POST("/")
    fun chat(
        @Body body: RequestBody
    ): Call<BaseBean>


}