package com.powerfulboy.app.network

import com.powerfulboy.app.network.model.OFFSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("cgi/search.pl")
    suspend fun searchFoods(
        @Query("search_terms") query: String,
        @Query("search_simple") searchSimple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 25,
        @Query("fields") fields: String = "id,product_name,brands,nutriments"
    ): OFFSearchResponse
}
