package com.IC.zohotaskswidget.api

import retrofit2.Response
import retrofit2.http.*

interface ZohoApiService {

    @FormUrlEncoded
    @POST("oauth/v2/token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code") code: String
    ): Response<TokenResponse>

    @FormUrlEncoded
    @POST("oauth/v2/token")
    suspend fun refreshAccessToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("refresh_token") refreshToken: String
    ): Response<TokenResponse>

    @GET("crm/v7/Tasks")
    suspend fun getTasks(
        @Header("Authorization") authorization: String
    ): Response<TaskResponse>
}