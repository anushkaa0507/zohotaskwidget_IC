package com.IC.zohotaskswidget.api

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String?,

    @SerializedName("expires_in")
    val expiresIn: Long,

    @SerializedName("api_domain")
    val apiDomain: String?,

    @SerializedName("token_type")
    val tokenType: String?,

    @SerializedName("scope")
    val scope: String?
)

data class ErrorResponse(
    @SerializedName("code")
    val code: Int?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("status")
    val status: String?
)

data class TaskListResponse(
    @SerializedName("data")
    val data: List<TaskData>?,

    @SerializedName("info")
    val info: ResponseInfo?,

    @SerializedName("code")
    val code: Int?
)

data class TaskData(
    @SerializedName("id")
    val id: String,

    @SerializedName("Subject")
    val subject: String?,

    @SerializedName("Status")
    val status: String?,

    @SerializedName("Priority")
    val priority: String?,

    @SerializedName("Due_Date")
    val dueDate: String?,

    @SerializedName("Owner")
    val owner: OwnerInfo?,

    @SerializedName("Created_Time")
    val createdTime: String?,

    @SerializedName("Modified_Time")
    val modifiedTime: String?,

    @SerializedName("\$se_module")
    val module: String?
)

data class OwnerInfo(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?
)

data class ResponseInfo(
    @SerializedName("per_page")
    val perPage: Int?,

    @SerializedName("count")
    val count: Int?,

    @SerializedName("page")
    val page: Int?,

    @SerializedName("has_more")
    val hasMore: Boolean?
)

data class TaskDetailsResponse(
    @SerializedName("data")
    val data: List<TaskData>?,

    @SerializedName("code")
    val code: Int?
)

data class UpdateTaskRequest(
    @SerializedName("data")
    val data: List<UpdateTaskData>
)

data class UpdateTaskData(
    @SerializedName("id")
    val id: String,

    @SerializedName("Status")
    val status: String
)

data class CreateTaskRequest(
    @SerializedName("data")
    val data: List<CreateTaskData>
)

data class CreateTaskData(
    @SerializedName("Subject")
    val subject: String,

    @SerializedName("Priority")
    val priority: String? = null,

    @SerializedName("Due_Date")
    val dueDate: String? = null,

    @SerializedName("Status")
    val status: String? = "Not Started"
)

data class UserProfileResponse(
    @SerializedName("users")
    val users: List<UserData>?,

    @SerializedName("code")
    val code: Int?
)

data class UserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String?
)