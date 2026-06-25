// File: app/src/main/java/com/IC/zohotaskswidget/repository/AuthRepository.kt

package com.IC.zohotaskswidget.repository

import android.util.Log
import com.IC.zohotaskswidget.api.RetrofitClient
import com.IC.zohotaskswidget.api.TokenResponse
import com.IC.zohotaskswidget.auth.TokenManager
import com.IC.zohotaskswidget.utils.Constants
import retrofit2.Response

/**
 * Repository for handling authentication operations.
 * Manages OAuth token exchange, refresh, and token storage.
 */
class AuthRepository(private val tokenManager: TokenManager) {

    companion object {
        private const val TAG = "AuthRepository"
        private const val GRANT_TYPE_CODE = "authorization_code"
        private const val GRANT_TYPE_REFRESH = "refresh_token"
    }

    // ========== TOKEN GENERATION ==========

    /**
     * Exchange authorization code for access token.
     * Called after user authorizes the app in Zoho's OAuth flow.
     *
     * @param authCode Authorization code from Zoho OAuth callback
     * @return Result containing TokenResponse or error message
     */
    suspend fun getAccessToken(authCode: String): Result<TokenResponse> {
        return try {
            Log.d(TAG, "Attempting to exchange auth code for access token")

            val response = RetrofitClient.getOAuthService().getAccessToken(
                grantType = GRANT_TYPE_CODE,
                clientId = Constants.CLIENT_ID,
                clientSecret = Constants.CLIENT_SECRET,
                redirectUri = Constants.REDIRECT_URI,
                code = authCode
            )

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!

                // Save tokens securely
                tokenManager.saveTokenResponse(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                    expiresIn = tokenResponse.expiresIn,
                    apiDomain = tokenResponse.apiDomain,
                    tokenType = tokenResponse.tokenType
                )

                // Reset CRM service to use new API domain
                RetrofitClient.resetCRMService()

                Log.d(TAG, "Successfully obtained access token")
                Log.d(TAG, "API Domain: ${tokenResponse.apiDomain}")

                Result.success(tokenResponse)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to get access token: HTTP ${response.code()}")
                Log.e(TAG, "Error body: $errorMessage")
                Result.failure(Exception("Failed to get access token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while getting access token", e)
            Result.failure(e)
        }
    }

    // ========== TOKEN REFRESH ==========

    /**
     * Refresh expired access token using refresh token.
     * Called when access token expires but refresh token is still valid.
     *
     * @return Result containing new TokenResponse or error message
     */
    suspend fun refreshAccessToken(): Result<TokenResponse> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                Log.e(TAG, "No refresh token available")
                return Result.failure(Exception("No refresh token available"))
            }

            Log.d(TAG, "Attempting to refresh access token")

            val response = RetrofitClient.getOAuthService().refreshAccessToken(
                grantType = GRANT_TYPE_REFRESH,
                clientId = Constants.CLIENT_ID,
                clientSecret = Constants.CLIENT_SECRET,
                refreshToken = refreshToken
            )

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!

                // Update tokens with new access token
                tokenManager.saveTokenResponse(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken ?: refreshToken,
                    expiresIn = tokenResponse.expiresIn,
                    apiDomain = tokenResponse.apiDomain,
                    tokenType = tokenResponse.tokenType
                )

                Log.d(TAG, "Successfully refreshed access token")
                Result.success(tokenResponse)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to refresh token: HTTP ${response.code()}")
                Log.e(TAG, "Error body: $errorMessage")

                // If refresh fails with 401, clear tokens and require re-login
                if (response.code() == 401) {
                    tokenManager.clearAllTokens()
                }

                Result.failure(Exception("Failed to refresh token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while refreshing access token", e)
            Result.failure(e)
        }
    }

    // ========== AUTHENTICATION STATE ==========

    /**
     * Check if user is currently authenticated.
     *
     * @return true if valid access token exists
     */
    fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }

    /**
     * Check if access token needs refresh.
     * Returns true if token is expired or about to expire (within 5 minutes).
     *
     * @return true if token should be refreshed
     */
    fun shouldRefreshToken(): Boolean {
        val timeUntilExpiry = tokenManager.getTimeUntilExpiry()
        val refreshThreshold = 5 * 60 * 1000 // 5 minutes in milliseconds
        return timeUntilExpiry < refreshThreshold
    }

    /**
     * Logout user by clearing all stored tokens.
     */
    fun logout() {
        Log.d(TAG, "Logging out user")
        tokenManager.clearAllTokens()
        RetrofitClient.resetCRMService()
    }

    /**
     * Get current access token if available.
     *
     * @return Access token or null if not authenticated
     */
    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    /**
     * Get API domain for current session.
     *
     * @return API domain URL
     */
    fun getApiDomain(): String {
        return tokenManager.getApiDomain()
    }

    /**
     * Get stored user ID.
     *
     * @return User ID or null
     */
    fun getUserId(): String? {
        return tokenManager.getUserId()
    }

    /**
     * Save user ID from user profile.
     *
     * @param userId User ID from Zoho
     */
    fun saveUserId(userId: String) {
        tokenManager.saveUserId(userId)
    }
}