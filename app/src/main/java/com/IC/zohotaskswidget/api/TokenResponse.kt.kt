package com.IC.zohotaskswidget.api

data class TokenResponse(
    val access_token: String,
    val refresh_token: String?,
    val expires_in: Long,
    val api_domain: String?,
    val token_type: String?
)