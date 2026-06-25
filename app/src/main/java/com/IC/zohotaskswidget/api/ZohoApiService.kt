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

    @GET("Tasks")
    suspend fun getTasks(
        @Header("Authorization") authorization: String,
        @Query("fields") fields: String = "Subject,Status,Due_Date,Priority",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): Response<TaskListResponse>

    @GET("Tasks")
    suspend fun getTasksByStatus(
        @Header("Authorization") authorization: String,
        @Query("fields") fields: String = "Subject,Status,Due_Date,Priority",
        @Query("criteria") criteria: String
    ): Response<TaskListResponse>

    @GET("Tasks/{id}")
    suspend fun getTaskDetails(
        @Path("id") taskId: String,
        @Header("Authorization") authorization: String,
        @Query("fields") fields: String = "Subject,Status,Due_Date,Priority"
    ): Response<TaskDetailsResponse>

    @PUT("Tasks")
    suspend fun updateTask(
        @Header("Authorization") authorization: String,
        @Body updateRequest: UpdateTaskRequest
    ): Response<TaskListResponse>

    @PUT("Tasks/{id}")
    suspend fun updateTaskStatus(
        @Path("id") taskId: String,
        @Header("Authorization") authorization: String,
        @Body statusUpdate: UpdateTaskRequest
    ): Response<TaskDetailsResponse>

    @GET("Users")
    suspend fun getUserProfile(
        @Header("Authorization") authorization: String
    ): Response<UserProfileResponse>
}