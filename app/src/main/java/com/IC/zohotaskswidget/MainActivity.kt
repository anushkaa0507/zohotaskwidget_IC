package com.IC.zohotaskswidget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.IC.zohotaskswidget.ui.theme.ZohoTasksWidgetTheme
import com.IC.zohotaskswidget.utils.Constants

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle OAuth callback if app was opened by Zoho
        handleOAuthIntent(intent)

        enableEdgeToEdge()

        setContent {
            ZohoTasksWidgetTheme {
                LoginScreen {
                    openZohoLogin()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {

        val uri = intent?.data

        Log.d("ZOHO_CALLBACK", "URI = $uri")

        uri?.let {

            val authCode = it.getQueryParameter("code")

            Log.d("ZOHO_CALLBACK", "CODE = $authCode")

            if (authCode != null) {
                Log.d("ZOHO_AUTH", "Authorization Code: $authCode")
            }
        }
    }

    private fun openZohoLogin() {
        val url =
            "${Constants.ACCOUNTS_BASE_URL}/oauth/v2/auth" +
                    "?scope=ZohoCRM.modules.ALL,offline_access" +
                    "&client_id=${Constants.CLIENT_ID}" +
                    "&response_type=code" +
                    "&access_type=offline" +
                    "&redirect_uri=${Constants.REDIRECT_URI}"
        Log.d("ZOHO_URL", url)
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }
}

@Composable
fun LoginScreen(onLoginClick: () -> Unit) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Zoho CRM Tasks Widget",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onLoginClick
        ) {
            Text("Login with Zoho")
        }
    }
}