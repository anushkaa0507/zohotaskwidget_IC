package com.IC.zohotaskswidget.api

data class TaskResponse(
    val data: List<Task>
)

data class Task(
    val id: String,
    val Subject: String?,
    val Status: String?,
    val Due_Date: String?
)