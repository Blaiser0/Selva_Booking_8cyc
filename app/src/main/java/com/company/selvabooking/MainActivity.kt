package com.company.selvabooking

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.company.selvabooking.navigation.SelvaNavGraph
import com.company.selvabooking.ui.theme.SelvaBookingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.parseColor("#FAF9F6"),
                AndroidColor.parseColor("#FAF9F6")
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.parseColor("#FAF9F6"),
                AndroidColor.parseColor("#FAF9F6")
            )
        )
        setContent {
            SelvaBookingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SelvaNavGraph()
                }
            }
        }
    }
}
