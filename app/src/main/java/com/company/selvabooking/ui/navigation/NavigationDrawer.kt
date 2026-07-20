package com.company.selvabooking.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.navigation.Routes
import com.company.selvabooking.ui.theme.CreamSurfaceVariant
import com.company.selvabooking.ui.theme.DarkText
import com.company.selvabooking.ui.theme.ForestGreen
import com.company.selvabooking.ui.theme.ForestGreenDark
import com.company.selvabooking.ui.theme.LightText
import com.company.selvabooking.ui.theme.LogoBackground
import com.company.selvabooking.ui.theme.TropicalGreen

data class DrawerNavItem(
    val route: String?,
    val label: String,
    val icon: ImageVector,
    val subtitle: String? = null,
    val sectionTitle: String? = null,
    val isLogout: Boolean = false
)

val clientDrawerItems = listOf(
    DrawerNavItem(
        route = Routes.CLIENT_HOME,
        label = "Inicio",
        icon = Icons.Outlined.Home,
        subtitle = "Explora hoteles destacados",
        sectionTitle = "Menú principal"
    ),
    DrawerNavItem(
        route = Routes.CLIENT_SEARCH,
        label = "Buscar",
        icon = Icons.Outlined.Search,
        subtitle = "Compara precios y ofertas"
    ),
    DrawerNavItem(
        route = Routes.CLIENT_RESERVATIONS,
        label = "Mis Reservas",
        icon = Icons.Default.Bookmark,
        subtitle = "Consulta tus viajes activos"
    ),
    DrawerNavItem(
        route = Routes.CLIENT_PROFILE,
        label = "Mi Cuenta",
        icon = Icons.Outlined.Person,
        subtitle = "Perfil, foto y preferencias",
        sectionTitle = "Tu cuenta"
    ),
    DrawerNavItem(
        route = Routes.SUPPORT,
        label = "Soporte y Ayuda",
        icon = Icons.AutoMirrored.Filled.HelpOutline,
        subtitle = "Preguntas frecuentes y contacto"
    ),
    DrawerNavItem(
        route = null,
        label = "Cerrar Sesión",
        icon = Icons.AutoMirrored.Filled.Logout,
        subtitle = "Salir de tu cuenta",
        sectionTitle = "Sesión",
        isLogout = true
    )
)

val adminDrawerItems = listOf(
    DrawerNavItem(
        route = Routes.ADMIN_DASHBOARD,
        label = "Dashboard",
        icon = Icons.Default.Dashboard,
        subtitle = "Resumen general del sistema",
        sectionTitle = "Administración"
    ),
    DrawerNavItem(
        route = Routes.ADMIN_REQUESTS,
        label = "Solicitudes",
        icon = Icons.Default.AdminPanelSettings,
        subtitle = "Aprobar accesos de admin"
    ),
    DrawerNavItem(
        route = Routes.ADMIN_HOTELS,
        label = "Hoteles",
        icon = Icons.Default.Hotel,
        subtitle = "Gestionar alojamientos"
    ),
    DrawerNavItem(
        route = Routes.ADMIN_RESERVATIONS,
        label = "Reservas",
        icon = Icons.AutoMirrored.Filled.List,
        subtitle = "Ver y administrar reservas"
    ),
    DrawerNavItem(
        route = Routes.ADMIN_PROFILE,
        label = "Mi Cuenta",
        icon = Icons.Outlined.Person,
        subtitle = "Perfil y configuración",
        sectionTitle = "Tu cuenta"
    ),
    DrawerNavItem(
        route = Routes.SUPPORT,
        label = "Soporte y Ayuda",
        icon = Icons.AutoMirrored.Filled.HelpOutline,
        subtitle = "Asistencia y documentación"
    ),
    DrawerNavItem(
        route = null,
        label = "Cerrar Sesión",
        icon = Icons.AutoMirrored.Filled.Logout,
        subtitle = "Salir de tu cuenta",
        sectionTitle = "Sesión",
        isLogout = true
    )
)

data class DrawerController(
    val open: () -> Unit,
    val close: () -> Unit,
    val showMenuButton: Boolean
)

val LocalDrawerController = compositionLocalOf<DrawerController?> { null }

@Composable
fun DrawerMenuIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Abrir menú",
            tint = ForestGreen
        )
    }
}

@Composable
fun SelvaNavigationDrawer(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    user: User?,
    currentRoute: String,
    items: List<DrawerNavItem>,
    onItemClick: (DrawerNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(220)) + slideInHorizontally(tween(280)) { -it / 3 },
        exit = fadeOut(tween(180)) + slideOutHorizontally(tween(240)) { -it / 3 },
        modifier = modifier
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.48f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() }
            )

            BoxWithConstraints(Modifier.fillMaxSize()) {
                val panelWidth = maxWidth * 0.78f
                val panelHeight = maxHeight * 0.94f

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .width(panelWidth)
                        .height(panelHeight)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                            ambientColor = Color.Black.copy(alpha = 0.2f),
                            spotColor = Color.Black.copy(alpha = 0.28f)
                        ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = LogoBackground
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DrawerBrandHeader(onDismiss = onDismiss)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            DrawerUserCard(user = user)
                            Spacer(modifier = Modifier.height(20.dp))

                            items.forEach { item ->
                                if (item.sectionTitle != null) {
                                    DrawerSectionTitle(title = item.sectionTitle)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                val selected = item.route != null && currentRoute == item.route
                                DrawerNavRow(
                                    item = item,
                                    selected = selected,
                                    onClick = { onItemClick(item) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            DrawerFooter()
                        }

                        DrawerHomeIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerBrandHeader(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ForestGreen, ForestGreenDark)
                ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Selva Booking",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LightText
                )
                Text(
                    text = "Tu selva, tu destino",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightText.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar menú",
                    tint = LightText
                )
            }
        }
    }
}

@Composable
private fun DrawerUserCard(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!user?.fotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user?.fotoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(ForestGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¡Hola!",
                    style = MaterialTheme.typography.labelMedium,
                    color = ForestGreen.copy(alpha = 0.7f)
                )
                Text(
                    text = user?.nombre?.ifBlank { "Usuario" } ?: "Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user?.email?.ifBlank { "tu_email@ejemplo.com" } ?: "tu_email@ejemplo.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkText.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                DrawerRoleBadge(role = user?.rol?.value ?: "Cliente")
            }
        }
    }
}

@Composable
private fun DrawerRoleBadge(role: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TropicalGreen.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = ForestGreen
        )
    }
}

@Composable
private fun DrawerSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = ForestGreen.copy(alpha = 0.55f),
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun DrawerNavRow(
    item: DrawerNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        selected -> CreamSurfaceVariant
        item.isLogout -> ForestGreen.copy(alpha = 0.06f)
        else -> Color.Transparent
    }
    val contentColor = if (item.isLogout) ForestGreen.copy(alpha = 0.9f) else ForestGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selected) ForestGreen.copy(alpha = 0.14f)
                    else ForestGreen.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) DarkText else contentColor
            )
            item.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkText.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun DrawerFooter() {
    HorizontalDivider(color = CreamSurfaceVariant)
    Spacer(modifier = Modifier.height(14.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Selva Booking v1.0",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = ForestGreen.copy(alpha = 0.7f)
        )
        Text(
            text = "Reserva hoteles en la selva peruana",
            style = MaterialTheme.typography.bodySmall,
            color = DarkText.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun DrawerHomeIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ForestGreen.copy(alpha = 0.3f))
        )
    }
}
