package com.raveline.leboncoinapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.raveline.leboncoinapplication.presentation.navigation.AppNavHost
import com.raveline.leboncoinapplication.ui.theme.LeboncoinApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeboncoinApplicationTheme {
                AppNavHost(rememberNavController())
            }
        }
    }
}