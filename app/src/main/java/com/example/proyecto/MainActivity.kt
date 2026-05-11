package com.example.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.ui.theme.ProyectoTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            ProyectoTheme {
                AlimentacionApp()
            }
        }
    }
}

@Composable
fun AlimentacionApp() {
    val navController = rememberNavController()
    val state = rememberAppState()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, state) }
        composable("inicio") { InicioScreen(navController, state) }
        composable("info") { InfoScreen(navController, state) }
        composable("alimentos") { AlimentosScreen(navController, state) }
        composable("resultado") { ResultadosScreen(navController, state) }
    }
}