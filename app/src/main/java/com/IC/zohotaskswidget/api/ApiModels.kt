// File: app/src/main/java/com/IC/zohotaskswidget/api/ApiModels.kt

package com.IC.zohotaskswidget.api

import com.google.gson.annotations.SerializedName

// ========== OAUTH MODELS ==========

/**
 * Response model for OAuth token generation.
 * Returned after successful authorization code exchange.
 */
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

/**
 * Error response model for API failures.
 */
data class ErrorResponse(
    @SerializedName("code")
    val code: Int?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("status")
    val status: String?
)

// ========== TASK MODELS ==========

/**
 * Response wrapper for task list API calls.
 */
data class TaskListResponse(
    @SerializedName("data")
    val data: List<TaskData>?,

    @SerializedName("info")
    val info: ResponseInfo?,

    @SerializedName("code")
    val code: Int?
)

/**
 * Individual task data model.
 */
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
/**
 * Task owner information.
 */
data class OwnerInfo(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?
)

/**
 * Response information metadata.
 */
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

/**
 * Single task details response.
 */
data class TaskDetailsResponse(
    @SerializedName("data")
    val data: List<TaskData>?,

    @SerializedName("code")
    val code: Int?
)

/**
 * Request model for updating task status.
 */
data class UpdateTaskRequest(
    @SerializedName("data")
    val data: List<UpdateTaskData>
)

/**
 * Task data for update operations.
 */
data class UpdateTaskData(
    @SerializedName("id")
    val id: String,

    @SerializedName("Status")
    val status: String
)

// ========== USER MODELS ==========

/**
 * User profile information response.
 */
data class UserProfileResponse(
    @SerializedName("users")
    val users: List<UserData>?,

    @SerializedName("code")
    val code: Int?
)

/**
 * Individual user data.
 */
data class UserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String?,

    @SerializedName("email")
    val email: String?
)