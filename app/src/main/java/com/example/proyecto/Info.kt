package com.example.proyecto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.ui.theme.TextoGris
import com.example.proyecto.ui.theme.VerdePrincipal
import com.example.proyecto.ui.theme.BlancoPuro
import com.example.proyecto.ui.theme.NaranjaAcento

@Composable
fun InfoScreen(navController: NavController, state: AlimentacionState) {
    AppScaffold(
        navController = navController,
        state = state,
        currentRoute = AppRoute.INFO
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            ScreenHeader(
                titulo = "Tu informacion",
                subtitulo = "Necesitamos unos datos basicos para personalizar tu dieta."
            )
            AppTextField(
                value = state.peso,
                onValueChange = { state.peso = it },
                label = "Peso (kg)",
                keyboardType = KeyboardType.Number
            )
            AppTextField(
                value = state.altura,
                onValueChange = { state.altura = it },
                label = "Altura (cm)",
                keyboardType = KeyboardType.Number
            )
            AppTextField(
                value = state.edad,
                onValueChange = { state.edad = it },
                label = "Edad",
                keyboardType = KeyboardType.Number
            )
            Text(
                text = "Objetivo principal",
                style = MaterialTheme.typography.titleLarge,
                color = TextoGris,
                fontWeight = FontWeight.Bold
            )
            ObjetivoCard(
                objetivo = Objetivo.ADELGAZAR,
                selected = state.objetivo == Objetivo.ADELGAZAR,
                onClick = { state.objetivo = Objetivo.ADELGAZAR }
            )
            ObjetivoCard(
                objetivo = Objetivo.MANTENER,
                selected = state.objetivo == Objetivo.MANTENER,
                onClick = { state.objetivo = Objetivo.MANTENER }
            )
            ObjetivoCard(
                objetivo = Objetivo.FORMA,
                selected = state.objetivo == Objetivo.FORMA,
                onClick = { state.objetivo = Objetivo.FORMA }
            )
            Spacer(modifier = Modifier.height(4.dp))
            PrimaryButton(
                text = "Guardar mis datos",
                onClick = {
                    if (state.peso.isBlank() || state.altura.isBlank() || state.edad.isBlank()) {
                        state.mensaje = "Introduce peso, altura y edad"
                    } else {
                        AuthRepository.saveProfile(state) { success, message ->
                            state.mensaje = message
                        }
                    }
                }
            )
            PrimaryButton(
                text = "Siguiente: Mis alimentos",
                onClick = {
                    if (state.peso.isBlank() || state.altura.isBlank() || state.edad.isBlank()) {
                        state.mensaje = "Introduce peso, altura y edad antes de continuar"
                    } else {
                        AuthRepository.saveProfile(state) { success, _ ->
                            navController.navigate(AppRoute.ALIMENTOS.route)
                        }
                    }
                }
            )
            if (state.mensaje.isNotBlank()) {
                Text(
                    text = state.mensaje, 
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}