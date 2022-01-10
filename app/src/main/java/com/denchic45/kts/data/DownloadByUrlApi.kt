package com.denchic45.kts.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface DownloadByUrlApi {

    @GET
    suspend operator fun invoke(@Url fileUrl: String): Response<ResponseBody>
}