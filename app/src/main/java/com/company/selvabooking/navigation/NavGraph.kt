package com.company.selvabooking.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.navigation.homeRoute
import com.company.selvabooking.navigation.mainDestination
import com.company.selvabooking.ui.admin.AdminAuditScreen
import com.company.selvabooking.ui.admin.AdminDashboardScreen
import com.company.selvabooking.ui.admin.AdminAdministradoresScreen
import com.company.selvabooking.ui.admin.AdminGerentesScreen
import com.company.selvabooking.ui.admin.AdminLimitedReviewsScreen
import com.company.selvabooking.ui.admin.AdminUsersScreen
import com.company.selvabooking.ui.admin.HotelReviewsScreen
import com.company.selvabooking.ui.admin.AdminHotelsScreen
import com.company.selvabooking.ui.admin.ManagerReviewsScreen
import com.company.selvabooking.ui.admin.AdminReservationsScreen
import com.company.selvabooking.ui.admin.AdminRoomsScreen
import com.company.selvabooking.ui.auth.ForgotPasswordScreen
import com.company.selvabooking.ui.auth.GerenteProfileCompletionScreen
import com.company.selvabooking.ui.auth.GuestAuthRequiredDialog
import com.company.selvabooking.ui.auth.LoginScreen
import com.company.selvabooking.ui.auth.RegisterScreen
import com.company.selvabooking.ui.client.BookingScreen
import com.company.selvabooking.ui.client.HotelDetailScreen
import com.company.selvabooking.ui.client.HomeScreen
import com.company.selvabooking.ui.client.MyReservationsScreen
import com.company.selvabooking.ui.client.PaymentScreen
import com.company.selvabooking.ui.client.SearchScreen
import com.company.selvabooking.ui.components.LoadingIndicator
import com.company.selvabooking.ui.navigation.DrawerController
import com.company.selvabooking.ui.navigation.DrawerNavItem
import com.company.selvabooking.ui.navigation.LocalDrawerController
import com.company.selvabooking.ui.navigation.guestDrawerItems
import com.company.selvabooking.ui.navigation.managerDrawerItems
import com.company.selvabooking.ui.navigation.SelvaNavigationDrawer
import com.company.selvabooking.ui.navigation.adminDrawerItemsFor
import com.company.selvabooking.ui.navigation.clientDrawerItems
import com.company.selvabooking.ui.profile.ProfileScreen
import com.company.selvabooking.ui.splash.SplashScreen
import com.company.selvabooking.ui.support.SupportScreen
import com.company.selvabooking.viewmodel.AdminAuditViewModel
import com.company.selvabooking.viewmodel.AdminAdministradoresViewModel
import com.company.selvabooking.viewmodel.AdminGerentesViewModel
import com.company.selvabooking.viewmodel.AdminDashboardViewModel
import com.company.selvabooking.viewmodel.AdminHotelReviewsViewModel
import com.company.selvabooking.viewmodel.AdminLimitedReviewsViewModel
import com.company.selvabooking.viewmodel.AdminHotelsViewModel
import com.company.selvabooking.viewmodel.AdminReservationsViewModel
import com.company.selvabooking.viewmodel.AdminUsersViewModel
import com.company.selvabooking.viewmodel.AuthViewModel
import com.company.selvabooking.viewmodel.BookingViewModel
import com.company.selvabooking.viewmodel.HomeViewModel
import com.company.selvabooking.viewmodel.HotelDetailViewModel
import com.company.selvabooking.viewmodel.ManagerReviewsViewModel
import com.company.selvabooking.viewmodel.MyReservationsViewModel
import com.company.selvabooking.viewmodel.PaymentViewModel
import com.company.selvabooking.viewmodel.ProfileViewModel
import com.company.selvabooking.viewmodel.SearchViewModel
import com.company.selvabooking.viewmodel.AdminRoomsViewModel
import com.company.selvabooking.viewmodel.viewModelFactory

private val clientDrawerRoutes = setOf(
    Routes.CLIENT_HOME,
    Routes.CLIENT_SEARCH,
    Routes.CLIENT_RESERVATIONS,
    Routes.CLIENT_PROFILE,
    Routes.SUPPORT
)

private val guestDrawerRoutes = setOf(
    Routes.CLIENT_HOME,
    Routes.CLIENT_SEARCH,
    Routes.SUPPORT
)

private val adminDrawerRouteBases = setOf(
    Routes.ADMIN_DASHBOARD,
    Routes.ADMIN_ADMINISTRADORES,
    Routes.ADMIN_GERENTES,
    Routes.ADMIN_HOTELS,
    Routes.ADMIN_RESERVATIONS,
    Routes.ADMIN_AUDIT,
    Routes.ADMIN_USERS,
    Routes.ADMIN_REVIEWS,
    Routes.ADMIN_PROFILE,
    Routes.SUPPORT
)

private fun isAdminDrawerRoute(route: String): Boolean =
    Routes.routeBase(route) in adminDrawerRouteBases

private val managerDrawerRoutes = setOf(
    Routes.MANAGER_HOTELS,
    Routes.MANAGER_RESERVATIONS,
    Routes.MANAGER_REVIEWS,
    Routes.MANAGER_PROFILE,
    Routes.SUPPORT
)

private fun NavHostController.navigateToMainTab(route: String, rootRoute: String) {
    if (route == rootRoute) {
        if (!popBackStack(rootRoute, inclusive = false)) {
            navigate(rootRoute) {
                launchSingleTop = true
            }
        }
        return
    }
    navigate(route) {
        popUpTo(rootRoute) { inclusive = false }
        launchSingleTop = true
    }
}

@Composable
fun SelvaNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.SPLASH

    var drawerOpen by remember { mutableStateOf(false) }
    var showGuestAuthDialog by remember { mutableStateOf(false) }
    var guestAuthMessage by remember { mutableStateOf("") }

    val showClientDrawer = authState.isAuthenticated &&
        authState.currentUser?.rol == UserRole.CLIENTE &&
        currentRoute in clientDrawerRoutes

    val showGuestDrawer = !authState.isAuthenticated && currentRoute in guestDrawerRoutes

    val showAdminDrawer = authState.isAuthenticated &&
        authState.currentUser?.rol?.hasAdminPanelAccess() == true &&
        isAdminDrawerRoute(currentRoute)

    val showManagerDrawer = authState.isAuthenticated &&
        authState.currentUser?.rol == UserRole.GERENTE_HOTEL &&
        currentRoute in managerDrawerRoutes

    val drawerItems = when {
        showAdminDrawer -> adminDrawerItemsFor(authState.currentUser!!.rol)
        showManagerDrawer -> managerDrawerItems
        showClientDrawer -> clientDrawerItems
        showGuestDrawer -> guestDrawerItems
        else -> emptyList()
    }

    val drawerUser = if (showGuestDrawer) null else authState.currentUser

    val showDrawer = drawerItems.isNotEmpty()

    val drawerController = DrawerController(
        open = { drawerOpen = true },
        close = { drawerOpen = false },
        showMenuButton = showDrawer
    )

    LaunchedEffect(currentRoute) {
        drawerOpen = false
    }

    val homeRoute = authState.currentUser?.homeRoute() ?: Routes.CLIENT_HOME

    fun openGuestAuthDialog(message: String, pendingAction: PendingAuthAction) {
        authViewModel.setPendingAuthAction(pendingAction)
        guestAuthMessage = message
        showGuestAuthDialog = true
    }

    fun handlePostAuthNavigation() {
        val user = authState.currentUser
        when (val pending = authViewModel.consumePendingAuthAction()) {
            is PendingAuthAction.HotelDetail -> {
                if (user?.rol == UserRole.CLIENTE) {
                    while (true) {
                        val route = navController.currentBackStackEntry?.destination?.route
                        if (route == Routes.LOGIN ||
                            route == Routes.REGISTER ||
                            route == Routes.FORGOT_PASSWORD
                        ) {
                            if (!navController.popBackStack()) break
                        } else {
                            break
                        }
                    }
                    val onHotelDetail = navController.currentBackStackEntry
                        ?.destination
                        ?.route
                        ?.startsWith("hotel_detail/") == true
                    if (!onHotelDetail) {
                        navController.navigate(Routes.hotelDetail(pending.hotelId)) {
                            popUpTo(Routes.CLIENT_HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                } else {
                    navController.navigate(user?.mainDestination() ?: Routes.CLIENT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            is PendingAuthAction.Book -> {
                if (user?.rol == UserRole.CLIENTE) {
                    navController.navigate(Routes.booking(pending.hotelId, pending.roomId)) {
                        popUpTo(Routes.CLIENT_HOME)
                    }
                } else {
                    navController.navigate(user?.mainDestination() ?: Routes.CLIENT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            is PendingAuthAction.Payment -> {
                if (user?.rol == UserRole.CLIENTE) {
                    navController.navigate(Routes.payment(pending.reservationId)) {
                        popUpTo(Routes.CLIENT_HOME)
                    }
                } else {
                    navController.navigate(user?.mainDestination() ?: Routes.CLIENT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            PendingAuthAction.Reservations -> {
                if (user?.rol == UserRole.CLIENTE) {
                    navController.navigateToMainTab(Routes.CLIENT_RESERVATIONS, Routes.CLIENT_HOME)
                } else {
                    navController.navigate(user?.mainDestination() ?: Routes.CLIENT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            PendingAuthAction.Profile -> {
                if (user?.rol == UserRole.CLIENTE) {
                    navController.navigateToMainTab(Routes.CLIENT_PROFILE, Routes.CLIENT_HOME)
                } else {
                    navController.navigate(user?.mainDestination() ?: Routes.CLIENT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            null -> {
                val destination = authState.currentUser?.mainDestination() ?: Routes.CLIENT_HOME
                navController.navigate(destination) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
    }

    fun handleDrawerItemClick(item: DrawerNavItem) {
        drawerOpen = false
        if (item.isLogout) {
            authViewModel.logout()
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true
            }
            return
        }
        val route = item.route ?: return
        if (route == Routes.REGISTER && !authState.isAuthenticated) {
            navController.navigate(Routes.LOGIN) {
                launchSingleTop = true
            }
            navController.navigate(Routes.REGISTER)
            return
        }
        if (!authState.isAuthenticated && route in setOf(Routes.CLIENT_RESERVATIONS, Routes.CLIENT_PROFILE)) {
            openGuestAuthDialog(
                message = "Inicia sesión o crea una cuenta para acceder a esta sección.",
                pendingAction = if (route == Routes.CLIENT_RESERVATIONS) {
                    PendingAuthAction.Reservations
                } else {
                    PendingAuthAction.Profile
                }
            )
            return
        }
        navController.navigateToMainTab(route, homeRoute)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalDrawerController provides drawerController) {
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.SPLASH) {
                    SplashScreen(
                        sessionReady = authState.isSessionReady,
                        onNavigate = {
                            val destination = if (authState.isAuthenticated && authState.currentUser != null) {
                                authState.currentUser!!.mainDestination()
                            } else {
                                Routes.LOGIN
                            }
                            navController.navigate(destination) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.LOGIN) {
                    LaunchedEffect(authState.isSessionReady, authState.isAuthenticated, authState.currentUser) {
                        if (authState.isSessionReady &&
                            authState.isAuthenticated &&
                            authState.currentUser != null
                        ) {
                            navController.navigate(authState.currentUser!!.mainDestination()) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    }
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = {
                            navController.navigate(Routes.REGISTER)
                        },
                        onNavigateToForgotPassword = {
                            navController.navigate(Routes.FORGOT_PASSWORD)
                        },
                        onLoginSuccess = { handlePostAuthNavigation() },
                        onContinueAsGuest = {
                            navController.navigate(Routes.CLIENT_HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.REGISTER) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onRegisterSuccess = { handlePostAuthNavigation() }
                    )
                }

                composable(Routes.FORGOT_PASSWORD) {
                    ForgotPasswordScreen(
                        viewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.GERENTE_COMPLETE_PROFILE) {
                    val user = authState.currentUser
                    LaunchedEffect(user?.id, user?.perfilCompleto) {
                        when {
                            user == null || user.rol != UserRole.GERENTE_HOTEL -> {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            }
                            user.perfilCompleto -> {
                                navController.navigate(Routes.MANAGER_HOTELS) {
                                    popUpTo(Routes.GERENTE_COMPLETE_PROFILE) { inclusive = true }
                                }
                            }
                        }
                    }
                    if (user == null || user.rol != UserRole.GERENTE_HOTEL || user.perfilCompleto) {
                        LoadingIndicator()
                        return@composable
                    }
                    GerenteProfileCompletionScreen(
                        viewModel = authViewModel,
                        user = user,
                        onProfileCompleted = {
                            navController.navigate(Routes.MANAGER_HOTELS) {
                                popUpTo(Routes.GERENTE_COMPLETE_PROFILE) { inclusive = true }
                            }
                        },
                        onSkipForNow = {
                            navController.navigate(Routes.MANAGER_HOTELS) {
                                popUpTo(Routes.GERENTE_COMPLETE_PROFILE) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.CLIENT_HOME) {
                    val homeViewModel: HomeViewModel = viewModel()
                    HomeScreen(
                        viewModel = homeViewModel,
                        onHotelClick = { navController.navigate(Routes.hotelDetail(it)) },
                        onSearchClick = {
                            navController.navigateToMainTab(Routes.CLIENT_SEARCH, Routes.CLIENT_HOME)
                        },
                        onReservationsClick = {
                            if (!authState.isAuthenticated) {
                                openGuestAuthDialog(
                                    message = "Inicia sesión o crea una cuenta para ver tus reservas.",
                                    pendingAction = PendingAuthAction.Reservations
                                )
                            } else {
                                navController.navigateToMainTab(
                                    Routes.CLIENT_RESERVATIONS,
                                    Routes.CLIENT_HOME
                                )
                            }
                        }
                    )
                }

                composable(Routes.CLIENT_SEARCH) {
                    val searchViewModel: SearchViewModel = viewModel()
                    SearchScreen(
                        viewModel = searchViewModel,
                        onHotelClick = { navController.navigate(Routes.hotelDetail(it)) }
                    )
                }

                composable(Routes.CLIENT_RESERVATIONS) {
                    val reservationsViewModel: MyReservationsViewModel = viewModel()
                    MyReservationsScreen(viewModel = reservationsViewModel)
                }

                composable(Routes.CLIENT_PROFILE) {
                    val profileViewModel: ProfileViewModel = viewModel()
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onUserUpdated = { user ->
                            val previousRole = authState.currentUser?.rol
                            authViewModel.updateCurrentUser(user)
                            when {
                                previousRole == UserRole.CLIENTE && user.rol.hasAdminPanelAccess() -> {
                                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                                        popUpTo(Routes.CLIENT_HOME) { inclusive = true }
                                    }
                                }
                                previousRole == UserRole.CLIENTE && user.rol == UserRole.GERENTE_HOTEL -> {
                                    navController.navigate(Routes.MANAGER_HOTELS) {
                                        popUpTo(Routes.CLIENT_HOME) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Routes.SUPPORT) {
                    SupportScreen()
                }

                composable(
                    route = Routes.HOTEL_DETAIL,
                    arguments = listOf(navArgument("hotelId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val hotelId = backStackEntry.arguments?.getString("hotelId") ?: return@composable
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val currentUser = authState.currentUser
                    val detailViewModel: HotelDetailViewModel = viewModel(
                        key = "${hotelId}_${currentUser?.id ?: "guest"}",
                        factory = viewModelFactory {
                            HotelDetailViewModel(
                                application = app,
                                hotelId = hotelId,
                                userId = currentUser?.id,
                                userName = currentUser?.nombre
                            )
                        }
                    )
                    HotelDetailScreen(
                        viewModel = detailViewModel,
                        onBack = { navController.popBackStack() },
                        onBook = { hId, rId ->
                            if (!authState.isAuthenticated) {
                                openGuestAuthDialog(
                                    message = "Inicia sesión o crea una cuenta para reservar este hotel.",
                                    pendingAction = PendingAuthAction.HotelDetail(hId)
                                )
                            } else {
                                navController.navigate(Routes.booking(hId, rId))
                            }
                        }
                    )
                }

                composable(
                    route = Routes.BOOKING,
                    arguments = listOf(
                        navArgument("hotelId") { type = NavType.StringType },
                        navArgument("roomId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val hotelId = backStackEntry.arguments?.getString("hotelId") ?: return@composable
                    val roomId = backStackEntry.arguments?.getString("roomId") ?: return@composable
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val bookingViewModel: BookingViewModel = viewModel(
                        factory = viewModelFactory { BookingViewModel(app, hotelId, roomId) }
                    )
                    BookingScreen(
                        viewModel = bookingViewModel,
                        onBack = { navController.popBackStack() },
                        onBookingComplete = { reservationId ->
                            if (!authState.isAuthenticated) {
                                openGuestAuthDialog(
                                    message = "Inicia sesión o crea una cuenta para continuar con el pago.",
                                    pendingAction = PendingAuthAction.Payment(reservationId)
                                )
                            } else {
                                navController.navigate(Routes.payment(reservationId)) {
                                    popUpTo(Routes.CLIENT_HOME)
                                }
                            }
                        },
                        onRequireAuth = {
                            openGuestAuthDialog(
                                message = "Inicia sesión o crea una cuenta para confirmar tu reserva.",
                                pendingAction = PendingAuthAction.Book(hotelId, roomId)
                            )
                        }
                    )
                }

                composable(
                    route = Routes.PAYMENT,
                    arguments = listOf(navArgument("reservationId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val reservationId = backStackEntry.arguments?.getString("reservationId") ?: return@composable
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val paymentViewModel: PaymentViewModel = viewModel(
                        factory = viewModelFactory { PaymentViewModel(app, reservationId) }
                    )
                    PaymentScreen(
                        viewModel = paymentViewModel,
                        onBack = { navController.popBackStack() },
                        onPaymentSuccess = {
                            navController.navigate(Routes.CLIENT_RESERVATIONS) {
                                popUpTo(Routes.CLIENT_HOME)
                            }
                        },
                        onRequireAuth = {
                            openGuestAuthDialog(
                                message = "Inicia sesión o crea una cuenta para ingresar tu método de pago.",
                                pendingAction = PendingAuthAction.Payment(reservationId)
                            )
                        }
                    )
                }

                composable(Routes.ADMIN_DASHBOARD) {
                    val dashboardViewModel: AdminDashboardViewModel = viewModel()
                    AdminDashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToReservations = {
                            navController.navigateToMainTab(Routes.ADMIN_RESERVATIONS, Routes.ADMIN_DASHBOARD)
                        },
                        onNavigateToHotels = {
                            navController.navigateToMainTab(
                                Routes.adminHotels(Routes.AdminHotelsFilter.ALL),
                                Routes.ADMIN_DASHBOARD
                            )
                        },
                        onNavigateToGerentes = {
                            navController.navigateToMainTab(Routes.ADMIN_GERENTES, Routes.ADMIN_DASHBOARD)
                        },
                        onNavigateToAdministradores = {
                            navController.navigateToMainTab(Routes.ADMIN_ADMINISTRADORES, Routes.ADMIN_DASHBOARD)
                        },
                        onNavigateToUsers = {
                            navController.navigateToMainTab(Routes.ADMIN_USERS, Routes.ADMIN_DASHBOARD)
                        },
                        onNavigateToAudit = {
                            navController.navigateToMainTab(Routes.ADMIN_AUDIT, Routes.ADMIN_DASHBOARD)
                        }
                    )
                }

                composable(Routes.ADMIN_AUDIT) {
                    val auditViewModel: AdminAuditViewModel = viewModel()
                    AdminAuditScreen(viewModel = auditViewModel)
                }

                composable(Routes.ADMIN_USERS) {
                    val usersViewModel: AdminUsersViewModel = viewModel()
                    AdminUsersScreen(viewModel = usersViewModel)
                }

                composable(Routes.ADMIN_REVIEWS) {
                    val reviewsViewModel: AdminLimitedReviewsViewModel = viewModel()
                    AdminLimitedReviewsScreen(viewModel = reviewsViewModel)
                }

                composable(Routes.ADMIN_ADMINISTRADORES) {
                    val administradoresViewModel: AdminAdministradoresViewModel = viewModel()
                    AdminAdministradoresScreen(viewModel = administradoresViewModel)
                }

                composable(Routes.ADMIN_GERENTES) {
                    val gerentesViewModel: AdminGerentesViewModel = viewModel()
                    AdminGerentesScreen(viewModel = gerentesViewModel)
                }

                composable(
                    route = "${Routes.ADMIN_HOTELS}?hotelsFilter={hotelsFilter}",
                    arguments = listOf(
                        navArgument("hotelsFilter") {
                            type = NavType.StringType
                            defaultValue = Routes.AdminHotelsFilter.ALL
                        }
                    )
                ) { backStackEntry ->
                    val hotelsFilter = backStackEntry.arguments
                        ?.getString("hotelsFilter")
                        ?: Routes.AdminHotelsFilter.ALL
                    val hotelsViewModel: AdminHotelsViewModel = viewModel()
                    LaunchedEffect(hotelsFilter) {
                        hotelsViewModel.applyIncomingHotelsFilter(hotelsFilter)
                    }
                    AdminHotelsScreen(
                        viewModel = hotelsViewModel,
                        onManageRooms = { hotelId, hotelName ->
                            navController.navigate(Routes.adminRooms(hotelId, hotelName))
                        },
                        onViewReviews = { hotelId, hotelName ->
                            navController.navigate(Routes.adminHotelReviews(hotelId, hotelName))
                        }
                    )
                }

                composable(
                    route = Routes.ADMIN_HOTEL_REVIEWS,
                    arguments = listOf(
                        navArgument("hotelId") { type = NavType.StringType },
                        navArgument("hotelName") {
                            type = NavType.StringType
                            defaultValue = ""
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val hotelId = backStackEntry.arguments?.getString("hotelId") ?: return@composable
                    val hotelName = backStackEntry.arguments?.getString("hotelName").orEmpty()
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val reviewsViewModel: AdminHotelReviewsViewModel = viewModel(
                        factory = viewModelFactory {
                            AdminHotelReviewsViewModel(app, hotelId, hotelName)
                        }
                    )
                    val uiState by reviewsViewModel.uiState.collectAsState()
                    HotelReviewsScreen(
                        uiState = uiState,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Routes.ADMIN_ROOMS,
                    arguments = listOf(
                        navArgument("hotelId") { type = NavType.StringType },
                        navArgument("hotelName") {
                            type = NavType.StringType
                            defaultValue = ""
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val hotelId = backStackEntry.arguments?.getString("hotelId") ?: return@composable
                    val hotelName = backStackEntry.arguments?.getString("hotelName").orEmpty()
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val roomsViewModel: AdminRoomsViewModel = viewModel(
                        factory = viewModelFactory {
                            AdminRoomsViewModel(app, hotelId, hotelName)
                        }
                    )
                    AdminRoomsScreen(
                        viewModel = roomsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.ADMIN_RESERVATIONS) { backStackEntry ->
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val currentUser = authState.currentUser
                    val canManageReservations = currentUser?.rol == UserRole.SUPER_ADMIN
                    val adminReservationsViewModel: AdminReservationsViewModel = viewModel(
                        key = Routes.ADMIN_RESERVATIONS,
                        factory = viewModelFactory {
                            AdminReservationsViewModel(
                                app,
                                initialCanManageReservations = canManageReservations,
                                initialRoleResolved = currentUser != null
                            )
                        }
                    )
                    LaunchedEffect(backStackEntry.id) {
                        adminReservationsViewModel.onScreenVisible()
                    }
                    AdminReservationsScreen(viewModel = adminReservationsViewModel)
                }

                composable(Routes.MANAGER_HOTELS) {
                    val hotelsViewModel: AdminHotelsViewModel = viewModel()
                    AdminHotelsScreen(
                        viewModel = hotelsViewModel,
                        onManageRooms = { hotelId, hotelName ->
                            navController.navigate(Routes.adminRooms(hotelId, hotelName))
                        }
                    )
                }

                composable(Routes.MANAGER_RESERVATIONS) { backStackEntry ->
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val reservationsViewModel: AdminReservationsViewModel = viewModel(
                        key = Routes.MANAGER_RESERVATIONS,
                        factory = viewModelFactory {
                            AdminReservationsViewModel(
                                app,
                                forceGerenteMode = true,
                                initialRoleResolved = true
                            )
                        }
                    )
                    LaunchedEffect(backStackEntry.id) {
                        reservationsViewModel.onScreenVisible()
                    }
                    AdminReservationsScreen(viewModel = reservationsViewModel)
                }

                composable(Routes.MANAGER_REVIEWS) {
                    val reviewsViewModel: ManagerReviewsViewModel = viewModel()
                    ManagerReviewsScreen(viewModel = reviewsViewModel)
                }

                composable(Routes.MANAGER_PROFILE) {
                    val profileViewModel: ProfileViewModel = viewModel()
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onUserUpdated = { user ->
                            val previousRole = authState.currentUser?.rol
                            authViewModel.updateCurrentUser(user)
                            if (previousRole == UserRole.GERENTE_HOTEL && user.rol == UserRole.CLIENTE) {
                                navController.navigate(Routes.CLIENT_HOME) {
                                    popUpTo(Routes.MANAGER_HOTELS) { inclusive = true }
                                }
                            }
                        },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Routes.ADMIN_PROFILE) {
                    val profileViewModel: ProfileViewModel = viewModel()
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onUserUpdated = { user ->
                            val previousRole = authState.currentUser?.rol
                            authViewModel.updateCurrentUser(user)
                            when {
                                previousRole?.hasAdminPanelAccess() == true && user.rol == UserRole.CLIENTE -> {
                                    navController.navigate(Routes.CLIENT_HOME) {
                                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                                    }
                                }
                                previousRole == UserRole.CLIENTE && user.rol.hasAdminPanelAccess() -> {
                                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                                        popUpTo(Routes.CLIENT_HOME) { inclusive = true }
                                    }
                                }
                                previousRole == UserRole.GERENTE_HOTEL && user.rol == UserRole.CLIENTE -> {
                                    navController.navigate(Routes.CLIENT_HOME) {
                                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

        if (showDrawer) {
            SelvaNavigationDrawer(
                isOpen = drawerOpen,
                onDismiss = { drawerOpen = false },
                user = drawerUser,
                currentRoute = currentRoute,
                items = drawerItems,
                onItemClick = ::handleDrawerItemClick
            )
        }

        if (showGuestAuthDialog) {
            GuestAuthRequiredDialog(
                message = guestAuthMessage,
                onDismiss = { showGuestAuthDialog = false },
                onLogin = {
                    showGuestAuthDialog = false
                    navController.navigate(Routes.LOGIN)
                },
                onRegister = {
                    showGuestAuthDialog = false
                    if (navController.currentBackStackEntry?.destination?.route != Routes.LOGIN) {
                        navController.navigate(Routes.LOGIN) {
                            launchSingleTop = true
                        }
                    }
                    navController.navigate(Routes.REGISTER)
                }
            )
        }
    }
}
