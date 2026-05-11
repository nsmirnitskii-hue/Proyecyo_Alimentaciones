package com.example.proyecto

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.ui.theme.VerdePrincipal
import com.example.proyecto.ui.theme.VerdeOscuro
import com.example.proyecto.ui.theme.NaranjaAcento
import com.example.proyecto.ui.theme.FondoClaro
import com.example.proyecto.ui.theme.BlancoPuro
import com.example.proyecto.ui.theme.TextoGris
import com.example.proyecto.ui.theme.GrisSuave
import kotlinx.coroutines.launch

@Stable
class AlimentacionState {
    var nombre by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var peso by mutableStateOf("")
    var altura by mutableStateOf("")
    var edad by mutableStateOf("")
    var objetivo by mutableStateOf(Objetivo.MANTENER)
    val alimentos: SnapshotStateList<String> = mutableStateListOf()
    var mensaje by mutableStateOf("")
    var cargando by mutableStateOf(false)
}

@Composable
fun rememberAppState(): AlimentacionState = remember { AlimentacionState() }

enum class Objetivo(val titulo: String, val descripcion: String, val tono: String) {
    ADELGAZAR("Adelgazar", "Comidas ligeras y saciantes", "Deficit suave"),
    MANTENER("Mantenerse", "Equilibrio diario sin complicaciones", "Balance estable"),
    FORMA("Ponerse en forma", "Mas proteina y energia limpia", "Rendimiento")
}

data class Receta(
    val titulo: String,
    val descripcion: String,
    val tiempo: String,
    val enfoque: String
)

enum class AppRoute(val route: String, val title: String) {
    INICIO("inicio", "Inicio"),
    INFO("info", "Tu informacion"),
    ALIMENTOS("alimentos", "Alimentos"),
    RESULTADO("resultado", "Tu dieta")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavController,
    state: AlimentacionState,
    currentRoute: AppRoute,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) {
        ScreenBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text(text = currentRoute.title, color = BlancoPuro, fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = VerdePrincipal,
                            titleContentColor = BlancoPuro,
                            navigationIconContentColor = BlancoPuro,
                            actionIconContentColor = BlancoPuro
                        ),
                        navigationIcon = {
                            TextButton(onClick = { scope.launch { drawerState.open() } }) {
                                Text(text = "Menu", color = BlancoPuro, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            ) { paddingValues ->
                content(paddingValues)
            }
        }
    }
}

@Composable
fun DrawerContent(currentRoute: AppRoute, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Fondo siempre blanco
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(60.dp).padding(bottom = 8.dp)
        )
        Text(
            text = "Alimentación",
            style = MaterialTheme.typography.headlineMedium,
            color = TextoGris, 
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Navega entre secciones",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoGris.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        val drawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = VerdePrincipal,
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = Color.White,
            unselectedTextColor = TextoGris,
            selectedIconColor = Color.White,
            unselectedIconColor = TextoGris
        )

        NavigationDrawerItem(
            label = { Text(AppRoute.INICIO.title) },
            selected = currentRoute == AppRoute.INICIO,
            onClick = { onNavigate(AppRoute.INICIO.route) },
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            label = { Text(AppRoute.INFO.title) },
            selected = currentRoute == AppRoute.INFO,
            onClick = { onNavigate(AppRoute.INFO.route) },
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            label = { Text(AppRoute.ALIMENTOS.title) },
            selected = currentRoute == AppRoute.ALIMENTOS,
            onClick = { onNavigate(AppRoute.ALIMENTOS.route) },
            colors = drawerItemColors
        )
        NavigationDrawerItem(
            label = { Text(AppRoute.RESULTADO.title) },
            selected = currentRoute == AppRoute.RESULTADO,
            onClick = { onNavigate(AppRoute.RESULTADO.route) },
            colors = drawerItemColors
        )
    }
}

@Composable
fun ScreenBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        content()
    }
}

@Composable
fun ScreenHeader(titulo: String, subtitulo: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VerdePrincipal, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = BlancoPuro,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = BlancoPuro.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HeroIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(BlancoPuro, RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
                .background(VerdePrincipal.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(70.dp).alpha(0.6f)
            )
        }
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .background(NaranjaAcento.copy(alpha = 0.2f), CircleShape)
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(50.dp).padding(bottom = 8.dp)
            )
            Text(
                text = "IA + Cocina",
                style = MaterialTheme.typography.titleLarge,
                color = TextoGris
            )
            Text(
                text = "Tu guia diaria",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoGris.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextoGris.copy(alpha = 0.6f)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        colors = textFieldColors()
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = VerdePrincipal,
            contentColor = BlancoPuro
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.height(54.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ToggleChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(
        targetValue = if (selected) VerdePrincipal else Color.Transparent,
        label = "toggle-bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else TextoGris,
        label = "toggle-content"
    )
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = background,
        border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.35f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = contentColor
        )
    }
}

@Composable
fun ObjetivoCard(objetivo: Objetivo, selected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) VerdePrincipal else Color.Transparent,
        label = "card-border"
    )
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BlancoPuro else BlancoPuro.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = objetivo.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = TextoGris,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = objetivo.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoGris.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = objetivo.tono,
                style = MaterialTheme.typography.labelLarge,
                color = VerdePrincipal,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyStateCard(titulo: String, detalle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.titleLarge, color = TextoGris, fontWeight = FontWeight.Bold)
            Text(text = detalle, style = MaterialTheme.typography.bodyMedium, color = TextoGris.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ListaItem(text: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BlancoPuro,
        shadowElevation = 1.dp,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge, color = TextoGris, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onRemove) {
                Text(text = "Quitar", color = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun RecetaCard(receta: Receta) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlancoPuro),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoPill(text = receta.tiempo)
                Text(
                    text = "Receta Saludable",
                    style = MaterialTheme.typography.labelMedium,
                    color = VerdePrincipal.copy(alpha = 0.6f)
                )
            }

            Text(
                text = receta.titulo,
                style = MaterialTheme.typography.headlineSmall,
                color = TextoGris,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = receta.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoGris.copy(alpha = 0.8f)
            )

            // Sección de enfoque mejorada para textos largos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VerdePrincipal.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(VerdePrincipal, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Beneficio nutricional",
                        style = MaterialTheme.typography.labelLarge,
                        color = VerdePrincipal,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = receta.enfoque,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoGris.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun InfoPill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = VerdePrincipal.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = VerdePrincipal,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = BlancoPuro,
    unfocusedContainerColor = BlancoPuro,
    focusedTextColor = TextoGris,
    unfocusedTextColor = TextoGris,
    cursorColor = VerdePrincipal,
    focusedIndicatorColor = VerdePrincipal,
    unfocusedIndicatorColor = GrisSuave,
    focusedLabelColor = VerdePrincipal,
    unfocusedLabelColor = TextoGris.copy(alpha = 0.6f)
)

fun generarRecomendaciones(objetivo: Objetivo, alimentos: List<String>): List<Receta> {
    val base = if (alimentos.isEmpty()) {
        listOf("verduras", "legumbres", "huevo")
    } else {
        alimentos
    }
    val ingredientes = base.take(4).joinToString(", ")
    val enfoque = when (objetivo) {
        Objetivo.ADELGAZAR -> "Ligero y saciante"
        Objetivo.MANTENER -> "Balance diario"
        Objetivo.FORMA -> "Alta proteina"
    }
    return listOf(
        Receta(
            titulo = "Bowl inteligente $enfoque",
            descripcion = "Combina $ingredientes con especias suaves y una base de hoja verde.",
            tiempo = "15-20 min",
            enfoque = enfoque
        ),
        Receta(
            titulo = "Salteado rapido",
            descripcion = "Saltea $ingredientes con aceite de oliva y termina con limon.",
            tiempo = "12 min",
            enfoque = "Cocina express"
        ),
        Receta(
            titulo = "Plato unico mediterraneo",
            descripcion = "Mezcla $ingredientes con grano integral y hierbas frescas.",
            tiempo = "25 min",
            enfoque = "Equilibrio completo"
        )
    )
}