package com.IC.zohotaskswidget.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(

    @PrimaryKey
    val id: Int = 1,

    val zohoUserId: String,

    val name: String,

    val email: String
)