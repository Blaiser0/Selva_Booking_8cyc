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
import com.company.selvabooking.ui.admin.AdminDashboardScreen
import com.company.selvabooking.ui.admin.AdminHotelsScreen
import com.company.selvabooking.ui.admin.AdminRequestsScreen
import com.company.selvabooking.ui.admin.AdminReservationsScreen
import com.company.selvabooking.ui.admin.AdminRoomsScreen
import com.company.selvabooking.ui.auth.ForgotPasswordScreen
import com.company.selvabooking.ui.auth.LoginScreen
import com.company.selvabooking.ui.auth.RegisterScreen
import com.company.selvabooking.ui.client.BookingScreen
import com.company.selvabooking.ui.client.HotelDetailScreen
import com.company.selvabooking.ui.client.HomeScreen
import com.company.selvabooking.ui.client.MyReservationsScreen
import com.company.selvabooking.ui.client.PaymentScreen
import com.company.selvabooking.ui.client.SearchScreen
import com.company.selvabooking.ui.navigation.DrawerController
import com.company.selvabooking.ui.navigation.DrawerNavItem
import com.company.selvabooking.ui.navigation.LocalDrawerController
import com.company.selvabooking.ui.navigation.SelvaNavigationDrawer
import com.company.selvabooking.ui.navigation.adminDrawerItems
import com.company.selvabooking.ui.navigation.clientDrawerItems
import com.company.selvabooking.ui.profile.ProfileScreen
import com.company.selvabooking.ui.splash.SplashScreen
import com.company.selvabooking.ui.support.SupportScreen
import com.company.selvabooking.viewmodel.AdminDashboardViewModel
import com.company.selvabooking.viewmodel.AdminHotelsViewModel
import com.company.selvabooking.viewmodel.AdminRequestsViewModel
import com.company.selvabooking.viewmodel.AdminReservationsViewModel
import com.company.selvabooking.viewmodel.AuthViewModel
import com.company.selvabooking.viewmodel.BookingViewModel
import com.company.selvabooking.viewmodel.HomeViewModel
import com.company.selvabooking.viewmodel.HotelDetailViewModel
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

private val adminDrawerRoutes = setOf(
    Routes.ADMIN_DASHBOARD,
    Routes.ADMIN_REQUESTS,
    Routes.ADMIN_HOTELS,
    Routes.ADMIN_RESERVATIONS,
    Routes.ADMIN_PROFILE,
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
        popUpTo(rootRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
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

    val showClientDrawer = authState.isAuthenticated &&
        authState.currentUser?.rol == UserRole.CLIENTE &&
        currentRoute in clientDrawerRoutes

    val showAdminDrawer = authState.isAuthenticated &&
        authState.currentUser?.rol == UserRole.ADMINISTRADOR &&
        currentRoute in adminDrawerRoutes

    val drawerItems = when {
        showAdminDrawer -> adminDrawerItems
        showClientDrawer -> clientDrawerItems
        else -> emptyList()
    }

    val showDrawer = drawerItems.isNotEmpty()

    val drawerController = DrawerController(
        open = { drawerOpen = true },
        close = { drawerOpen = false },
        showMenuButton = showDrawer
    )

    LaunchedEffect(currentRoute) {
        drawerOpen = false
    }

    val homeRoute = if (authState.currentUser?.rol == UserRole.ADMINISTRADOR) {
        Routes.ADMIN_DASHBOARD
    } else {
        Routes.CLIENT_HOME
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
                        onNavigateToLogin = {
                            val destination = when {
                                authState.isAuthenticated && authState.currentUser?.rol == UserRole.ADMINISTRADOR ->
                                    Routes.ADMIN_DASHBOARD
                                authState.isAuthenticated ->
                                    Routes.CLIENT_HOME
                                else -> Routes.LOGIN
                            }
                            navController.navigate(destination) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.LOGIN) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = {
                            navController.navigate(Routes.REGISTER)
                        },
                        onNavigateToForgotPassword = {
                            navController.navigate(Routes.FORGOT_PASSWORD)
                        },
                        onLoginSuccess = {
                            val destination = if (authState.currentUser?.rol == UserRole.ADMINISTRADOR) {
                                Routes.ADMIN_DASHBOARD
                            } else {
                                Routes.CLIENT_HOME
                            }
                            navController.navigate(destination) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = { navController.popBackStack() },
                        onRegisterSuccess = {
                            navController.navigate(Routes.CLIENT_HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.FORGOT_PASSWORD) {
                    ForgotPasswordScreen(
                        viewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() }
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
                            navController.navigateToMainTab(Routes.CLIENT_RESERVATIONS, Routes.CLIENT_HOME)
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
                            if (previousRole == UserRole.CLIENTE && user.rol == UserRole.ADMINISTRADOR) {
                                navController.navigate(Routes.ADMIN_DASHBOARD) {
                                    popUpTo(Routes.CLIENT_HOME) { inclusive = true }
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
                        onBook = { hId, rId -> navController.navigate(Routes.booking(hId, rId)) }
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
                            navController.navigate(Routes.payment(reservationId)) {
                                popUpTo(Routes.CLIENT_HOME)
                            }
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
                        }
                    )
                }

                composable(Routes.ADMIN_DASHBOARD) {
                    val dashboardViewModel: AdminDashboardViewModel = viewModel()
                    AdminDashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToRequests = {
                            navController.navigateToMainTab(Routes.ADMIN_REQUESTS, Routes.ADMIN_DASHBOARD)
                        }
                    )
                }

                composable(Routes.ADMIN_REQUESTS) {
                    val requestsViewModel: AdminRequestsViewModel = viewModel()
                    AdminRequestsScreen(viewModel = requestsViewModel)
                }

                composable(Routes.ADMIN_HOTELS) {
                    val hotelsViewModel: AdminHotelsViewModel = viewModel()
                    AdminHotelsScreen(
                        viewModel = hotelsViewModel,
                        onManageRooms = { hotelId ->
                            navController.navigate(Routes.adminRooms(hotelId))
                        }
                    )
                }

                composable(
                    route = Routes.ADMIN_ROOMS,
                    arguments = listOf(navArgument("hotelId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val hotelId = backStackEntry.arguments?.getString("hotelId") ?: return@composable
                    val app = LocalContext.current.applicationContext as android.app.Application
                    val roomsViewModel: AdminRoomsViewModel = viewModel(
                        factory = viewModelFactory { AdminRoomsViewModel(app, hotelId) }
                    )
                    AdminRoomsScreen(
                        viewModel = roomsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.ADMIN_RESERVATIONS) {
                    val adminReservationsViewModel: AdminReservationsViewModel = viewModel()
                    AdminReservationsScreen(viewModel = adminReservationsViewModel)
                }

                composable(Routes.ADMIN_PROFILE) {
                    val profileViewModel: ProfileViewModel = viewModel()
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onUserUpdated = { user ->
                            val previousRole = authState.currentUser?.rol
                            authViewModel.updateCurrentUser(user)
                            if (previousRole == UserRole.ADMINISTRADOR && user.rol == UserRole.CLIENTE) {
                                navController.navigate(Routes.CLIENT_HOME) {
                                    popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                                }
                            } else if (previousRole == UserRole.CLIENTE && user.rol == UserRole.ADMINISTRADOR) {
                                navController.navigate(Routes.ADMIN_DASHBOARD) {
                                    popUpTo(Routes.CLIENT_HOME) { inclusive = true }
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
                user = authState.currentUser,
                currentRoute = currentRoute,
                items = drawerItems,
                onItemClick = ::handleDrawerItemClick
            )
        }
    }
}
