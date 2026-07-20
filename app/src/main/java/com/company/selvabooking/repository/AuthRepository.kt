package com.company.selvabooking.repository

import android.net.Uri
import com.company.selvabooking.data.SampleData
import com.company.selvabooking.data.firebase.FirebaseAuthService
import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.data.firebase.StorageService
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.utils.AdminUtils
import com.company.selvabooking.utils.AuthErrorUtils
import com.company.selvabooking.utils.Constants
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val authService: FirebaseAuthService = FirebaseAuthService(),
    private val firestoreService: FirestoreService = FirestoreService(),
    private val storageService: StorageService = StorageService()
) {
    val currentUser: FirebaseUser?
        get() = authService.currentUser

    val isLoggedIn: Boolean
        get() = authService.isLoggedIn

    fun authStateFlow(): Flow<FirebaseUser?> = authService.authStateFlow()

    private suspend fun applyDesignatedAdminOnLogin(user: User): User {
        if (!AdminUtils.isDesignatedAdmin(user.nombre)) return user
        if (user.puedeAlternarRol && user.rol == UserRole.CLIENTE) return user
        if (user.rol == UserRole.ADMINISTRADOR) {
            if (!user.puedeAlternarRol) {
                val updated = user.copy(puedeAlternarRol = true)
                firestoreService.updateUser(updated)
                return updated
            }
            return user
        }
        val adminUser = user.copy(
            rol = UserRole.ADMINISTRADOR,
            puedeAlternarRol = true
        )
        firestoreService.updateUser(adminUser)
        return adminUser
    }

    private suspend fun resolveUserAfterAuth(
        firebaseUser: FirebaseUser,
        fallbackEmail: String
    ): Result<User> {
        return firestoreService.getUser(firebaseUser.uid).fold(
            onSuccess = { user -> Result.success(applyDesignatedAdminOnLogin(user)) },
            onFailure = {
                val recoveredUser = User(
                    id = firebaseUser.uid,
                    nombre = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                        ?: fallbackEmail.substringBefore("@"),
                    email = firebaseUser.email ?: fallbackEmail,
                    rol = if (AdminUtils.isDesignatedAdmin(
                            firebaseUser.displayName ?: fallbackEmail.substringBefore("@")
                        )
                    ) {
                        UserRole.ADMINISTRADOR
                    } else {
                        UserRole.CLIENTE
                    },
                    puedeAlternarRol = AdminUtils.isDesignatedAdmin(
                        firebaseUser.displayName ?: fallbackEmail.substringBefore("@")
                    )
                )
                firestoreService.createUser(recoveredUser).fold(
                    onSuccess = { Result.success(applyDesignatedAdminOnLogin(recoveredUser)) },
                    onFailure = { firestoreError ->
                        Result.failure(
                            Exception(
                                AuthErrorUtils.toUserMessage(firestoreError)
                            )
                        )
                    }
                )
            }
        )
    }

    suspend fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()
        val authResult = authService.signIn(normalizedEmail, normalizedPassword)
        return authResult.fold(
            onSuccess = { firebaseUser ->
                resolveUserAfterAuth(firebaseUser, normalizedEmail)
            },
            onFailure = { Result.failure(Exception(AuthErrorUtils.toUserMessage(it))) }
        )
    }

    suspend fun register(
        nombre: String,
        email: String,
        password: String
    ): Result<User> {
        val normalizedNombre = nombre.trim()
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()
        val authResult = authService.signUp(normalizedEmail, normalizedPassword)
        return authResult.fold(
            onSuccess = { firebaseUser ->
                val isDesignated = AdminUtils.isDesignatedAdmin(normalizedNombre)
                val user = User(
                    id = firebaseUser.uid,
                    nombre = normalizedNombre,
                    email = normalizedEmail,
                    rol = if (isDesignated) UserRole.ADMINISTRADOR else UserRole.CLIENTE,
                    puedeAlternarRol = isDesignated
                )
                firestoreService.createUser(user).fold(
                    onSuccess = { Result.success(user) },
                    onFailure = { firestoreError ->
                        authService.deleteCurrentUser()
                        Result.failure(
                            Exception(
                                "No se pudo guardar el perfil: ${AuthErrorUtils.toUserMessage(firestoreError)}"
                            )
                        )
                    }
                )
            },
            onFailure = { Result.failure(Exception(AuthErrorUtils.toUserMessage(it))) }
        )
    }

    suspend fun getCurrentUserData(): Result<User> {
        val uid = currentUser?.uid ?: return Result.failure(Exception("No hay sesión activa"))
        return firestoreService.getUser(uid)
    }

    suspend fun updateUserProfile(user: User): Result<User> {
        return firestoreService.updateUser(user).fold(
            onSuccess = { Result.success(user) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun uploadProfilePhoto(uri: Uri): Result<User> {
        val uid = currentUser?.uid ?: return Result.failure(Exception("No hay sesión activa"))
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        return storageService.uploadProfileImage(uri, uid).fold(
            onSuccess = { photoUrl ->
                val updatedUser = user.copy(fotoUrl = photoUrl)
                updateUserProfile(updatedUser)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun requestAdminAccess(): Result<User> {
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        if (user.rol == UserRole.ADMINISTRADOR) {
            return Result.failure(Exception("Ya tienes acceso de administrador"))
        }
        if (user.hasPendingAdminRequest) {
            return Result.failure(Exception("Ya tienes una solicitud pendiente"))
        }
        val updatedUser = user.copy(solicitudAdmin = Constants.ADMIN_REQUEST_PENDING)
        return updateUserProfile(updatedUser)
    }

    suspend fun switchToClientRole(): Result<User> {
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        if (user.rol != UserRole.ADMINISTRADOR) {
            return Result.failure(Exception("Ya estás en modo cliente"))
        }
        val updatedUser = user.copy(
            rol = UserRole.CLIENTE,
            puedeAlternarRol = true,
            solicitudAdmin = ""
        )
        return updateUserProfile(updatedUser)
    }

    suspend fun switchToAdminRole(): Result<User> {
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        if (user.rol == UserRole.ADMINISTRADOR) {
            return Result.failure(Exception("Ya estás en modo administrador"))
        }
        if (!user.puedeAlternarRol) {
            return Result.failure(Exception("No tienes permiso para activar el modo administrador"))
        }
        val updatedUser = user.copy(
            rol = UserRole.ADMINISTRADOR,
            puedeAlternarRol = true,
            solicitudAdmin = ""
        )
        return updateUserProfile(updatedUser)
    }

    suspend fun approveAdminRequest(userId: String): Result<User> {
        if (getCurrentUserData().getOrNull()?.rol != UserRole.ADMINISTRADOR) {
            return Result.failure(Exception("No tienes permisos de administrador"))
        }
        val user = firestoreService.getUser(userId).getOrElse { return Result.failure(it) }
        if (!user.hasPendingAdminRequest) {
            return Result.failure(Exception("Esta solicitud ya no está pendiente"))
        }
        val updatedUser = user.copy(
            rol = UserRole.ADMINISTRADOR,
            puedeAlternarRol = true,
            solicitudAdmin = ""
        )
        return updateUserProfile(updatedUser)
    }

    suspend fun rejectAdminRequest(userId: String): Result<User> {
        if (getCurrentUserData().getOrNull()?.rol != UserRole.ADMINISTRADOR) {
            return Result.failure(Exception("No tienes permisos de administrador"))
        }
        val user = firestoreService.getUser(userId).getOrElse { return Result.failure(it) }
        if (!user.hasPendingAdminRequest) {
            return Result.failure(Exception("Esta solicitud ya no está pendiente"))
        }
        val updatedUser = user.copy(solicitudAdmin = Constants.ADMIN_REQUEST_REJECTED)
        return updateUserProfile(updatedUser)
    }

    fun getPendingAdminRequestsFlow(): Flow<List<User>> {
        return firestoreService.getPendingAdminRequestsFlow()
    }

    fun logout() {
        authService.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authService.sendPasswordResetEmail(email.trim()).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(Exception(AuthErrorUtils.toUserMessage(it))) }
        )
    }

    suspend fun seedSampleDataIfNeeded(): Result<Unit> {
        return try {
            val hotels = firestoreService.getAllHotels().getOrElse { emptyList() }
            if (hotels.isEmpty()) {
                SampleData.hotels.forEach { hotel ->
                    val hotelIdResult = firestoreService.createHotel(hotel)
                    hotelIdResult.onSuccess { hotelId ->
                        val rooms = SampleData.roomsForHotel(hotelId, hotel.nombre)
                        rooms.forEach { room ->
                            firestoreService.createRoom(room.copy(hotelId = hotelId))
                        }
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
