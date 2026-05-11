package com.example.proyecto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.ui.theme.TextoGris
import com.example.proyecto.ui.theme.VerdePrincipal
import com.example.proyecto.ui.theme.BlancoPuro
import com.example.proyecto.ui.theme.NaranjaAcento
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch

@Composable
fun ResultadosScreen(navController: NavController, state: AlimentacionState) {
    val alimentosSnapshot = state.alimentos.toList()
    var recetas by remember { mutableStateOf<List<Receta>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }

    LaunchedEffect(alimentosSnapshot, state.objetivo) {
        if (alimentosSnapshot.isNotEmpty()) {
            cargando = true
            try {
                // USANDO LA LÓGICA GRATUITA
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-2 .5-flash")
                
                val prompt = """
                    Actúa como un nutricionista experto. Genera 3 recetas saludables en formato JSON.
                    Objetivo: ${state.objetivo.titulo} (${state.objetivo.descripcion}).
                    Ingredientes disponibles: ${alimentosSnapshot.joinToString(", ")}.
                    
                    El formato JSON debe ser una lista de objetos con esta estructura exacta:
                    [
                      {
                        "titulo": "nombre de la receta",
                        "descripcion": "pasos cortos",
                        "tiempo": "tiempo estimado",
                        "enfoque": "por qué es buena para el objetivo"
                      }
                    ]
                """.trimIndent()

                val response = model.generateContent(prompt)
                val jsonResponse = response.text ?: ""
                val nuevasRecetas = parseRecetasFromJson(jsonResponse)
                
                if (nuevasRecetas.isNotEmpty()) {
                    recetas = nuevasRecetas
                } else {
                    recetas = generarRecomendaciones(state.objetivo, alimentosSnapshot)
                }
                
                AuthRepository.saveProfile(state) { success, message ->
                    if (!success) state.mensaje = "Error al sincronizar: $message"
                }
                
            } catch (e: Exception) {
                state.mensaje = "Error IA: ${e.localizedMessage}"
                recetas = generarRecomendaciones(state.objetivo, alimentosSnapshot)
            } finally {
                cargando = false
            }
        } else {
            recetas = generarRecomendaciones(state.objetivo, alimentosSnapshot)
        }
    }

    AppScaffold(
        navController = navController,
        state = state,
        currentRoute = AppRoute.RESULTADO
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            ScreenHeader(
                titulo = "Tu dieta personalizada",
                subtitulo = "Basada en tu objetivo y en ${alimentosSnapshot.size} ingredientes."
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (cargando) {
                Text("Generando recetas con IA...", color = TextoGris, fontWeight = FontWeight.Medium)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(recetas) { receta ->
                        RecetaCard(receta)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = "Guardar cambios en Firestore",
                onClick = {
                    AuthRepository.saveProfile(state) { success, message ->
                        state.mensaje = message
                    }
                }
            )
            if (state.mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.mensaje,
                    color = if (state.mensaje.contains("error", ignoreCase = true)) Color.Red else VerdePrincipal,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = "Editar alimentos",
                onClick = { navController.navigate(AppRoute.ALIMENTOS.route) }
            )
        }
    }
}

fun parseRecetasFromJson(json: String): List<Receta> {
    return try {
        val cleaned = json.trim()
            .removePrefix("```json")
            .removeSuffix("```")
            .trim()
        
        val recetasList = mutableListOf<Receta>()
        val regex = Regex("""\{\s*"titulo":\s*"(.*?)",\s*"descripcion":\s*"(.*?)",\s*"tiempo":\s*"(.*?)",\s*"enfoque":\s*"(.*?)"\s*\}""", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(cleaned)
        
        for (match in matches) {
            recetasList.add(
                Receta(
                    titulo = match.groups[1]?.value ?: "",
                    descripcion = match.groups[2]?.value ?: "",
                    tiempo = match.groups[3]?.value ?: "",
                    enfoque = match.groups[4]?.value ?: ""
                )
            )
        }
        
        if (recetasList.isEmpty()) {
            val items = cleaned.removePrefix("[").removeSuffix("]").split("},")
            items.forEach { item ->
                val titulo = Regex("""\"titulo\":\s*\"(.*?)\"""").find(item)?.groupValues?.get(1) ?: ""
                val desc = Regex("""\"descripcion\":\s*\"(.*?)\"""").find(item)?.groupValues?.get(1) ?: ""
                val tiempo = Regex("""\"tiempo\":\s*\"(.*?)\"""").find(item)?.groupValues?.get(1) ?: ""
                val enfoque = Regex("""\"enfoque\":\s*\"(.*?)\"""").find(item)?.groupValues?.get(1) ?: ""
                if (titulo.isNotBlank()) {
                    recetasList.add(Receta(titulo, desc, tiempo, enfoque))
                }
            }
        }
        recetasList
    } catch (e: Exception) {
        emptyList()
    }
}
