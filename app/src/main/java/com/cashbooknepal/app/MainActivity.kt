package com.cashbooknepal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cashbooknepal.app.navigation.AppNavigation
import com.cashbooknepal.app.ui.theme.CashBookNepalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashBookNepalTheme {
                AppNavigation()
            }
        }
    }
}