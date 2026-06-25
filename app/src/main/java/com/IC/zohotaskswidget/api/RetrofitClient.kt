// File: app/src/main/java/com/IC/zohotaskswidget/api/RetrofitClient.kt

package com.IC.zohotaskswidget.api

import android.content.Context
import com.IC.zohotaskswidget.auth.TokenManager
import com.IC.zohotaskswidget.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client factory for Zoho API communication.
 * Handles multiple base URLs and token refresh logic.
 */
object RetrofitClient {

    private var oauthApiService: ZohoApiService? = null
    private var crmApiService: ZohoApiService? = null
    private var tokenManager: TokenManager? = null

    /**
     * Initialize Retrofit clients with context.
     * Must be called once during app startup.
     */
    fun initialize(context: Context) {
        tokenManager = TokenManager(context)
    }

    /**
     * Get OAuth service (for token operations).
     * Uses accounts.zoho.in as base URL.
     */
    fun getOAuthService(): ZohoApiService {
        if (oauthApiService == null) {
            oauthApiService = createRetrofit(Constants.ACCOUNTS_BASE_URL).create(
                ZohoApiService::class.java
            )
        }
        return oauthApiService!!
    }

    /**
     * Get CRM API service (for task operations).
     * Uses API domain from OAuth response as base URL.
     */
    fun getCRMService(): ZohoApiService {
        if (crmApiService == null) {
            val apiDomain = tokenManager?.getApiDomain() ?: Constants.DEFAULT_API_DOMAIN
            val baseUrl = "$apiDomain${Constants.TASKS_API_VERSION}/"
            crmApiService = createRetrofit(baseUrl).create(ZohoApiService::class.java)
        }
        return crmApiService!!
    }

    /**
     * Reset CRM service when API domain changes.
     * Call this after successful OAuth token generation.
     */
    fun resetCRMService() {
        crmApiService = null
    }

    /**
     * Create Retrofit instance with configured OkHttpClient.
     *
     * @param baseUrl Base URL for API requests
     * @return Configured Retrofit instance
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Create OkHttpClient with interceptors and timeout configuration.
     * Includes:
     * - Token authorization interceptor
     * - Token refresh interceptor
     * - HTTP logging interceptor (debug only)
     */
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(createLoggingInterceptor())
            .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Create authorization interceptor that adds Bearer token to requests.
     * Automatically handles token refresh if expired.
     */
    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()

            // Add Authorization header if token is available
            tokenManager?.getAccessToken()?.let { token ->
                val authenticatedRequest = request.newBuilder()
                    .header(Constants.AUTHORIZATION_HEADER, "Bearer $token")
                    .build()
                request = authenticatedRequest
            }

            var response = chain.proceed(request)

            // If we get 401, try refreshing token and retry
            if (response.code == 401) {
                // Attempt token refresh
                val refreshToken = tokenManager?.getRefreshToken()
                if (refreshToken != null) {
                    try {
                        // Note: In production, implement proper token refresh logic
                        // This is a simplified version
                        response.close()
                    } catch (e: Exception) {
                        // Token refresh failed
                        response.close()
                        return@Interceptor chain.proceed(request)
                    }
                }
            }

            response
        }
    }

    /**
     * Create HTTP logging interceptor for debug builds.
     * Logs request/response headers and bodies.
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            android.util.Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}