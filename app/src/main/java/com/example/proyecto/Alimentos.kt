package com.example.proyecto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.ui.theme.TextoGris
import com.example.proyecto.ui.theme.VerdePrincipal
import com.example.proyecto.ui.theme.BlancoPuro
import com.example.proyecto.ui.theme.NaranjaAcento
import com.example.proyecto.ui.theme.GrisSuave

@Composable
fun AlimentosScreen(navController: NavController, state: AlimentacionState) {
    AppScaffold(
        navController = navController,
        state = state,
        currentRoute = AppRoute.ALIMENTOS
    ) { paddingValues ->
        var nuevoAlimento by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(
                titulo = "Tus alimentos disponibles",
                subtitulo = "Escribe lo que tienes en casa. Aparecera en la lista."
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = nuevoAlimento,
                    onValueChange = { nuevoAlimento = it },
                    label = { Text("Alimento") },
                    colors = textFieldColors()
                )
                PrimaryButton(
                    text = "Anadir",
                    onClick = {
                        val limpio = nuevoAlimento.trim()
                        if (limpio.isNotEmpty()) {
                            state.alimentos.add(limpio)
                            nuevoAlimento = ""
                        }
                    },
                    modifier = Modifier.height(56.dp)
                )
            }
            HorizontalDivider(color = GrisSuave)
            Text(
                text = "Lista de alimentos",
                style = MaterialTheme.typography.titleLarge,
                color = TextoGris,
                fontWeight = FontWeight.Bold
            )
            if (state.alimentos.isEmpty()) {
                EmptyStateCard(
                    titulo = "Lista vacia",
                    detalle = "Agrega varios ingredientes para una dieta mas precisa."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.alimentos) { item ->
                        ListaItem(text = item, onRemove = { state.alimentos.remove(item) })
                    }
                }
            }
            PrimaryButton(
                text = "Crear dieta",
                onClick = { navController.navigate(AppRoute.RESULTADO.route) }
            )
        }
    }
}