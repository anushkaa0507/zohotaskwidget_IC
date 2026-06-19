package com.IC.zohotaskswidget.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey
    val id: String,

    val subject: String,

    val status: String?,

    val priority: String?,

    val dueDate: String?,

    val ownerName: String?
)