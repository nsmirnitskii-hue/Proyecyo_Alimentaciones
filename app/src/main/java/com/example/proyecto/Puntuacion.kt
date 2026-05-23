package com.example.proyecto

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.proyecto.ui.theme.TextoGris
import com.example.proyecto.ui.theme.VerdePrincipal
import com.example.proyecto.ui.theme.BlancoPuro
import com.example.proyecto.ui.theme.GrisSuave
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PuntuacionScreen(navController: NavController, state: AlimentacionState) {
    val context = navController.context
    var puntuacion by remember { mutableStateOf(0) }
    var comentario by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var enviando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var historial by remember { mutableStateOf<List<PuntuacionData>>(emptyList()) }
    var puntuacionSeleccionada by remember { mutableStateOf<PuntuacionData?>(null) }

    // Cargar historial al iniciar
    LaunchedEffect(Unit) {
        AuthRepository.getPuntuaciones { lista ->
            historial = lista
        }
    }

    if (puntuacionSeleccionada != null) {
        DetallePuntuacionDialog(
            item = puntuacionSeleccionada!!,
            onDismiss = { puntuacionSeleccionada = null },
            onDelete = {
                AuthRepository.deletePuntuacion(puntuacionSeleccionada!!.id) { success ->
                    if (success) {
                        puntuacionSeleccionada = null
                        AuthRepository.getPuntuaciones { historial = it }
                    }
                }
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localUri = guardarImagenLocal(context, it)
            imageUri = localUri
        }
    }

    AppScaffold(
        navController = navController,
        state = state,
        currentRoute = AppRoute.PUNTUACION
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenHeader(
                titulo = "Puntúa tu plato",
                subtitulo = "Comparte el resultado de tu receta con la comunidad."
            )

            // Selector de Imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { launcher.launch("image/*") },
                colors = CardDefaults.cardColors(containerColor = GrisSuave.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Imagen de la comida",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📸", fontSize = 40.sp)
                            Text("Toca para subir foto", color = TextoGris.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // Sistema de Estrellas
            Text(
                text = "¿Qué te ha parecido?",
                style = MaterialTheme.typography.titleLarge,
                color = TextoGris,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { index ->
                    Icon(
                        imageVector = if (index <= puntuacion) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (index <= puntuacion) Color(0xFFFFB300) else TextoGris.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { puntuacion = index }
                    )
                }
            }

            // Comentario
            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Añade un comentario...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors()
            )

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    color = if (mensaje.contains("Error")) Color.Red else VerdePrincipal,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botón de Envío
            PrimaryButton(
                text = if (enviando) "Enviando..." else "Publicar Puntuación",
                onClick = {
                    if (puntuacion == 0) {
                        mensaje = "Error: Por favor selecciona una puntuación"
                        return@PrimaryButton
                    }
                    enviando = true
                    AuthRepository.savePuntuacion(
                        puntuacion = puntuacion,
                        comentario = comentario,
                        imagenUri = imageUri?.toString() ?: "",
                        onComplete = { success, msg ->
                            enviando = false
                            mensaje = msg
                            if (success) {
                                // Limpiar campos y recargar historial
                                puntuacion = 0
                                comentario = ""
                                imageUri = null
                                AuthRepository.getPuntuaciones { historial = it }
                            }
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GrisSuave)

            // Sección de Historial
            Text(
                text = "Tus puntuaciones anteriores",
                style = MaterialTheme.typography.titleLarge,
                color = TextoGris,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            if (historial.isEmpty()) {
                Text(
                    text = "Aún no has puntuado ninguna receta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoGris.copy(alpha = 0.6f)
                )
            } else {
                historial.forEach { item ->
                    PuntuacionItem(
                        item = item, 
                        onVerDetalle = { puntuacionSeleccionada = item }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun guardarImagenLocal(context: Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "puntuacion_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePuntuacionDialog(item: PuntuacionData, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("Borrar", color = Color.Red.copy(alpha = 0.7f))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = VerdePrincipal, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = "Detalle de tu plato",
                style = MaterialTheme.typography.titleLarge,
                color = TextoGris,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Imagen en grande
                if (item.imagenUri.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imagenUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Estrellas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < item.puntuacion) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (index < item.puntuacion) Color(0xFFFFB300) else TextoGris.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Comentario
                if (item.comentario.isNotEmpty()) {
                    Surface(
                        color = GrisSuave.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.comentario,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoGris,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Fecha
                item.timestamp?.toDate()?.let { date ->
                    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault())
                    Text(
                        text = sdf.format(date),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextoGris.copy(alpha = 0.5f)
                    )
                }
            }
        },
        containerColor = BlancoPuro,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun PuntuacionItem(item: PuntuacionData, onVerDetalle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen miniatura
            if (item.imagenUri.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(item.imagenUri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(GrisSuave.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🥘", fontSize = 24.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < item.puntuacion) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (index < item.puntuacion) Color(0xFFFFB300) else TextoGris.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val date = item.timestamp?.toDate()
                    if (date != null) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text(
                            text = sdf.format(date),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoGris.copy(alpha = 0.5f)
                        )
                    }
                }
                if (item.comentario.isNotEmpty()) {
                    Text(
                        text = item.comentario,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoGris
                    )
                }
            }

            // Botón de info para ver detalles
            IconButton(onClick = onVerDetalle) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ver detalles",
                    tint = VerdePrincipal
                )
            }
        }
    }
}
