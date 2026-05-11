package com.example.proyecto

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.ui.theme.VerdePrincipal
import com.example.proyecto.ui.theme.VerdeOscuro
import com.example.proyecto.ui.theme.TextoGris
import com.example.proyecto.ui.theme.BlancoPuro
import com.example.proyecto.ui.theme.FondoClaro

@Composable
fun LoginScreen(navController: NavController, state: AlimentacionState) {
    var esLogin by remember { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize().background(VerdeOscuro)) {
        // Decoración de fondo (simulando las líneas de la imagen A)
        Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            // Aquí se podrían añadir formas abstractas
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlancoPuro, RoundedCornerShape(32.dp))
                    .padding(32.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo de la aplicación desde drawable
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )

                    Text(
                        text = if (esLogin) "Bienvenido" else "Registro",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextoGris,
                        fontWeight = FontWeight.Bold
                    )

                    if (!esLogin) {
                        AppTextField(
                            value = state.nombre,
                            onValueChange = { state.nombre = it },
                            label = "Nombre"
                        )
                    }
                    AppTextField(
                        value = state.email,
                        onValueChange = { state.email = it },
                        label = "Email"
                    )
                    AppTextField(
                        value = state.password,
                        onValueChange = { state.password = it },
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true
                    )

                    PrimaryButton(
                        text = if (esLogin) "Iniciar Sesión" else "Crear Cuenta",
                        onClick = {
                            val context = navController.context
                            if (state.email.isBlank() || state.password.isBlank() || (!esLogin && state.nombre.isBlank())) {
                                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_LONG).show()
                                return@PrimaryButton
                            }
                            if (esLogin) {
                                AuthRepository.signIn(state.email, state.password) { success, message ->
                                    if (success) {
                                        AuthRepository.loadProfile { profile ->
                                            if (profile != null) {
                                                state.nombre = profile.nombre
                                                state.peso = profile.peso
                                                state.altura = profile.altura
                                                state.edad = profile.edad
                                                state.objetivo = Objetivo.valueOf(profile.objetivo)
                                                state.alimentos.clear()
                                                state.alimentos.addAll(profile.alimentos)
                                            }
                                            navController.navigate(AppRoute.INICIO.route)
                                        }
                                    } else {
                                        val errorMsg = when (message) {
                                            "ERROR_INVALID_EMAIL", "ERROR_USER_NOT_FOUND" -> "falla correo"
                                            "ERROR_WRONG_PASSWORD" -> "falla contraseña"
                                            else -> "contraseña o correo incorrecta"
                                        }
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                AuthRepository.signUp(state.email, state.password, state.nombre) { success, message ->
                                    if (success) {
                                        navController.navigate(AppRoute.INICIO.route)
                                    } else {
                                        Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )

                    TextButton(onClick = { esLogin = !esLogin }) {
                        Text(
                            text = if (esLogin) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Entra",
                            color = VerdePrincipal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
