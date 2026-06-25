package com.IC.zohotaskswidget.utils

object Constants {

    // OAuth
    const val CLIENT_ID = "1000.DDIQRDGN1Z0YHLCWKPUYX99U4ORNXQ"
    const val CLIENT_SECRET = "9c1b6daad5f57965fc3be78bafe6d7dded53b4daa4"
    const val REDIRECT_URI = "com.ic.zohotaskswidget://oauth"
    const val ACCOUNTS_BASE_URL = "https://accounts.zoho.in"

    const val OAUTH_SCOPE =
        "ZohoCRM.modules.all,ZohoCRM.settings.READ,offline_access"

    // API
    const val API_BASE_URL = "https://tasks.zoho.com/api/v1/"
    const val DEFAULT_API_DOMAIN = "https://www.zohoapis.in"
    const val CRM_API_VERSION = "/crm/v6"
    const val TASKS_API_VERSION = "/crm/v7"

    // Database
    const val DATABASE_NAME = "zoho_tasks_widget_db"
    const val DATABASE_VERSION = 1

    // Preferences
    const val SECURE_PREFS_NAME = "zoho_secure_prefs"
    const val API_DOMAIN_PREF = "api_domain"
    const val USER_ID_PREF = "user_id"

    // Sync
    const val SYNC_INTERVAL_MINUTES = 15L
    const val TASK_SYNC_WORKER_TAG = "task_sync_worker"

    // Timeouts
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    // Endpoints
    const val TOKEN_ENDPOINT = "/oauth/v2/token"
    const val TASKS_ENDPOINT = "/Tasks"
    const val TASK_DETAILS_ENDPOINT = "/Tasks/{id}"

    // Headers
    const val AUTHORIZATION_HEADER = "Authorization"
    const val CONTENT_TYPE_HEADER = "Content-Type"
    const val JSON_CONTENT_TYPE = "application/json"
}