// File: app/src/main/java/com/IC/zohotaskswidget/auth/TokenManager.kt

package com.IC.zohotaskswidget.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.IC.zohotaskswidget.utils.Constants

/**
 * Secure token management using Android Keystore.
 * Handles storage and retrieval of OAuth tokens and related metadata.
 */
class TokenManager(context: Context) {

    companion object {
        private const val PREF_NAME = "zoho_secure_prefs"
        private const val ACCESS_TOKEN = "access_token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val EXPIRES_AT = "expires_at"
        private const val API_DOMAIN = "api_domain"
        private const val USER_ID = "user_id"
        private const val TOKEN_TYPE = "token_type"
    }

    /**
     * Initialize MasterKey for encryption.
     * Uses AES256_GCM for encryption.
     */
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    /**
     * EncryptedSharedPreferences for secure token storage.
     */
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ========== ACCESS TOKEN METHODS ==========

    /**
     * Save access token to secure storage.
     *
     * @param token Access token from OAuth response
     */
    fun saveAccessToken(token: String) {
        prefs.edit().putString(ACCESS_TOKEN, token).apply()
    }

    /**
     * Retrieve stored access token.
     *
     * @return Access token or null if not stored
     */
    fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN, null)
    }

    /**
     * Clear stored access token.
     */
    fun clearAccessToken() {
        prefs.edit().remove(ACCESS_TOKEN).apply()
    }

    // ========== REFRESH TOKEN METHODS ==========

    /**
     * Save refresh token to secure storage.
     *
     * @param token Refresh token from OAuth response
     */
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(REFRESH_TOKEN, token).apply()
    }

    /**
     * Retrieve stored refresh token.
     *
     * @return Refresh token or null if not stored
     */
    fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN, null)
    }

    /**
     * Clear stored refresh token.
     */
    fun clearRefreshToken() {
        prefs.edit().remove(REFRESH_TOKEN).apply()
    }

    // ========== TOKEN EXPIRY METHODS ==========

    /**
     * Save token expiration timestamp.
     *
     * @param expiryTime Unix timestamp when token expires
     */
    fun saveExpiryTime(expiryTime: Long) {
        prefs.edit().putLong(EXPIRES_AT, expiryTime).apply()
    }

    /**
     * Retrieve token expiration timestamp.
     *
     * @return Unix timestamp of token expiry, or 0 if not set
     */
    fun getExpiryTime(): Long {
        return prefs.getLong(EXPIRES_AT, 0)
    }

    /**
     * Check if access token is expired.
     *
     * @return true if token is expired or no expiry time set
     */
    fun isTokenExpired(): Boolean {
        val expiryTime = getExpiryTime()
        return expiryTime == 0L || System.currentTimeMillis() >= expiryTime
    }

    /**
     * Get time until token expiration in milliseconds.
     *
     * @return Milliseconds until expiry, or 0 if already expired
     */
    fun getTimeUntilExpiry(): Long {
        val expiryTime = getExpiryTime()
        val currentTime = System.currentTimeMillis()
        return if (expiryTime > currentTime) expiryTime - currentTime else 0
    }

    // ========== API DOMAIN METHODS ==========

    /**
     * Save API domain from OAuth response.
     * Example: https://www.zohoapis.in
     *
     * @param domain API domain URL
     */
    fun saveApiDomain(domain: String) {
        prefs.edit().putString(API_DOMAIN, domain).apply()
    }

    /**
     * Retrieve stored API domain.
     *
     * @return API domain or default if not stored
     */
    fun getApiDomain(): String {
        return prefs.getString(API_DOMAIN, null) ?: Constants.DEFAULT_API_DOMAIN
    }

    /**
     * Clear stored API domain.
     */
    fun clearApiDomain() {
        prefs.edit().remove(API_DOMAIN).apply()
    }

    // ========== TOKEN TYPE METHODS ==========

    /**
     * Save token type (usually "Bearer").
     *
     * @param type Token type
     */
    fun saveTokenType(type: String) {
        prefs.edit().putString(TOKEN_TYPE, type).apply()
    }

    /**
     * Retrieve stored token type.
     *
     * @return Token type or "Bearer" if not set
     */
    fun getTokenType(): String {
        return prefs.getString(TOKEN_TYPE, null) ?: "Bearer"
    }

    // ========== USER ID METHODS ==========

    /**
     * Save Zoho user ID.
     *
     * @param userId User ID from Zoho
     */
    fun saveUserId(userId: String) {
        prefs.edit().putString(USER_ID, userId).apply()
    }

    /**
     * Retrieve stored user ID.
     *
     * @return User ID or null if not stored
     */
    fun getUserId(): String? {
        return prefs.getString(USER_ID, null)
    }

    /**
     * Clear stored user ID.
     */
    fun clearUserId() {
        prefs.edit().remove(USER_ID).apply()
    }

    // ========== AUTHENTICATION STATE METHODS ==========

    /**
     * Check if user is authenticated (tokens are stored).
     *
     * @return true if both access and refresh tokens are stored
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }

    /**
     * Clear all stored tokens and authentication data.
     * Call this when user logs out.
     */
    fun clearAllTokens() {
        prefs.edit().apply {
            remove(ACCESS_TOKEN)
            remove(REFRESH_TOKEN)
            remove(EXPIRES_AT)
            remove(API_DOMAIN)
            remove(USER_ID)
            remove(TOKEN_TYPE)
        }.apply()
    }

    /**
     * Save complete token response from OAuth.
     * Convenience method to save all token data at once.
     *
     * @param accessToken Access token
     * @param refreshToken Refresh token (optional)
     * @param expiresIn Token validity duration in seconds
     * @param apiDomain API domain from response
     * @param tokenType Token type (usually "Bearer")
     */
    fun saveTokenResponse(
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long,
        apiDomain: String?,
        tokenType: String? = "Bearer"
    ) {
        saveAccessToken(accessToken)
        refreshToken?.let { saveRefreshToken(it) }
        saveExpiryTime(System.currentTimeMillis() + (expiresIn * 1000))
        apiDomain?.let { saveApiDomain(it) }
        saveTokenType(tokenType ?: "Bearer")
    }
}