package com.example.proyecto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.ui.theme.TextoGris
import com.example.proyecto.ui.theme.BlancoPuro
import com.example.proyecto.ui.theme.VerdePrincipal
import com.example.proyecto.ui.theme.NaranjaAcento

@Composable
fun InicioScreen(navController: NavController, state: AlimentacionState) {
    AppScaffold(
        navController = navController,
        state = state,
        currentRoute = AppRoute.INICIO
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ScreenHeader(
                titulo = "Bienvenido a Alimentacion",
                subtitulo = "Genera platos con los ingredientes que ya tienes, segun tu objetivo."
            )
            HeroIllustration()
            Card(
                colors = CardDefaults.cardColors(containerColor = BlancoPuro),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Como funciona",
                        style = MaterialTheme.typography.titleLarge,
                        color = VerdePrincipal,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1. Completa tu informacion y objetivo.\n" +
                            "2. Escribe lo que tienes en casa.\n" +
                            "3. Recibe una dieta creada para ti.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoGris.copy(alpha = 0.8f)
                    )
                }
            }
            PrimaryButton(
                text = "Completar mi informacion",
                onClick = { navController.navigate(AppRoute.INFO.route) }
            )
        }
    }
}