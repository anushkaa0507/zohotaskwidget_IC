package com.IC.zohotaskswidget.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oauth_tokens")
data class OAuthTokenEntity(

    @PrimaryKey
    val id: Int = 1,

    val accessToken: String,

    val refreshToken: String,

    val expiresAt: Long
)