package com.company.selvabooking.repository

import android.net.Uri
import com.company.selvabooking.data.SampleData
import com.company.selvabooking.data.firebase.FirebaseAuthService
import com.company.selvabooking.data.firebase.FirestoreService
import com.company.selvabooking.data.firebase.StorageService
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.utils.AdminUtils
import com.company.selvabooking.utils.ValidationUtils
import com.company.selvabooking.utils.AuthErrorUtils
import com.company.selvabooking.utils.Constants
import com.company.selvabooking.utils.HotelFormOptions
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

    private suspend fun applySuperAdminOnLogin(user: User): User {
        if (!AdminUtils.isSuperAdminEmail(user.email)) return user
        if (user.puedeAlternarRol && user.rol == UserRole.CLIENTE) return user
        if (user.rol == UserRole.SUPER_ADMIN) {
            if (!user.puedeAlternarRol) {
                val updated = user.copy(puedeAlternarRol = true)
                firestoreService.updateUser(updated)
                return updated
            }
            return user
        }
        val superAdminUser = user.copy(
            rol = UserRole.SUPER_ADMIN,
            puedeAlternarRol = true
        )
        firestoreService.updateUser(superAdminUser)
        return superAdminUser
    }

    private suspend fun resolveUserAfterAuth(
        firebaseUser: FirebaseUser,
        fallbackEmail: String
    ): Result<User> {
        return firestoreService.getUser(firebaseUser.uid).fold(
            onSuccess = { user -> Result.success(applySuperAdminOnLogin(user)) },
            onFailure = {
                val email = firebaseUser.email ?: fallbackEmail
                val isSuperAdmin = AdminUtils.isSuperAdminEmail(email)
                val recoveredUser = User(
                    id = firebaseUser.uid,
                    nombre = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                        ?: email.substringBefore("@"),
                    email = email,
                    rol = if (isSuperAdmin) UserRole.SUPER_ADMIN else UserRole.CLIENTE,
                    puedeAlternarRol = isSuperAdmin
                )
                firestoreService.createUser(recoveredUser).fold(
                    onSuccess = { Result.success(applySuperAdminOnLogin(recoveredUser)) },
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

    private suspend fun requireAdminPanelAccess(): Result<User> {
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        return if (user.rol.hasAdminPanelAccess()) {
            Result.success(user)
        } else {
            Result.failure(Exception("No tienes permisos de administrador"))
        }
    }

    private suspend fun requireSuperAdmin(): Result<User> {
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        return if (user.rol == UserRole.SUPER_ADMIN) {
            Result.success(user)
        } else {
            Result.failure(Exception("No tienes permisos de super administrador"))
        }
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
        password: String,
        telefono: String = ""
    ): Result<User> {
        val normalizedNombre = nombre.trim()
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()
        val normalizedTelefono = telefono.trim()
        val authResult = authService.signUp(normalizedEmail, normalizedPassword)
        return authResult.fold(
            onSuccess = { firebaseUser ->
                val isSuperAdmin = AdminUtils.isSuperAdminEmail(normalizedEmail)
                val user = User(
                    id = firebaseUser.uid,
                    nombre = normalizedNombre,
                    email = normalizedEmail,
                    telefono = normalizedTelefono,
                    rol = if (isSuperAdmin) UserRole.SUPER_ADMIN else UserRole.CLIENTE,
                    puedeAlternarRol = isSuperAdmin
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
        if (user.rol.hasAdminPanelAccess()) {
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
        if (!user.rol.hasAdminPanelAccess() && user.rol != UserRole.GERENTE_HOTEL) {
            return Result.failure(Exception("Ya estás en modo cliente"))
        }
        val updatedUser = user.copy(
            rol = UserRole.CLIENTE,
            puedeAlternarRol = true,
            rolAlternativo = user.rol.value,
            solicitudAdmin = ""
        )
        return updateUserProfile(updatedUser)
    }

    suspend fun switchToGerenteRole(): Result<User> {
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        if (user.rol == UserRole.GERENTE_HOTEL) {
            return Result.failure(Exception("Ya estás en modo encargado del hotel"))
        }
        if (!UserRole.matchesEncargadoHotel(user.rolAlternativo)) {
            return Result.failure(Exception("No tienes permiso para activar el modo encargado del hotel"))
        }
        val updatedUser = user.copy(
            rol = UserRole.GERENTE_HOTEL,
            puedeAlternarRol = true,
            rolAlternativo = "",
            solicitudAdmin = ""
        )
        return updateUserProfile(updatedUser)
    }

    suspend fun switchToAdminRole(): Result<User> {
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        if (user.rol.hasAdminPanelAccess()) {
            return Result.failure(Exception("Ya estás en modo administrador"))
        }
        val targetRole = when {
            user.rolAlternativo == UserRole.SUPER_ADMIN.value -> UserRole.SUPER_ADMIN
            user.rolAlternativo == UserRole.ADMINISTRADOR.value -> UserRole.ADMINISTRADOR
            user.puedeAlternarRol && AdminUtils.isSuperAdminEmail(user.email) -> UserRole.SUPER_ADMIN
            user.puedeAlternarRol -> UserRole.ADMINISTRADOR
            else -> return Result.failure(Exception("No tienes permiso para activar el modo administrador"))
        }
        val updatedUser = user.copy(
            rol = targetRole,
            puedeAlternarRol = true,
            rolAlternativo = "",
            solicitudAdmin = ""
        )
        return updateUserProfile(updatedUser)
    }

    suspend fun approveAdminRequest(userId: String): Result<User> {
        requireSuperAdmin().getOrElse { return Result.failure(it) }
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
        requireSuperAdmin().getOrElse { return Result.failure(it) }
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

    fun getGerentesHotelFlow(): Flow<List<User>> {
        return firestoreService.getGerentesHotelFlow()
    }

    fun getAdministradoresFlow(): Flow<List<User>> {
        return firestoreService.getAdministradoresFlow()
    }

    fun getAdministratorAccountsFlow(): Flow<List<User>> {
        return firestoreService.getAdministratorAccountsFlow()
    }

    fun getAllUsersFlow(): Flow<List<User>> = firestoreService.getAllUsersFlow()

    suspend fun createGerenteHotel(email: String, password: String): Result<User> {
        val creator = requireAdminPanelAccess().getOrElse { return Result.failure(it) }
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()
        val authResult = authService.createUserWithoutSigningIn(normalizedEmail, normalizedPassword)
        return authResult.fold(
            onSuccess = { firebaseUser ->
                val user = User(
                    id = firebaseUser.uid,
                    nombre = normalizedEmail.substringBefore("@"),
                    email = normalizedEmail,
                    rol = UserRole.GERENTE_HOTEL,
                    puedeAlternarRol = true,
                    perfilCompleto = false,
                    creadoPorAdminId = creator.administratorOwnerId.orEmpty()
                )
                firestoreService.createUser(user).fold(
                    onSuccess = { Result.success(user) },
                    onFailure = { firestoreError ->
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

    suspend fun createAdministrador(email: String, password: String): Result<User> {
        requireSuperAdmin().getOrElse { return Result.failure(it) }
        val normalizedEmail = email.trim()
        if (AdminUtils.isSuperAdminEmail(normalizedEmail)) {
            return Result.failure(Exception("Este correo está reservado para SuperAdmin"))
        }
        val normalizedPassword = password.trim()
        val authResult = authService.createUserWithoutSigningIn(normalizedEmail, normalizedPassword)
        return authResult.fold(
            onSuccess = { firebaseUser ->
                val user = User(
                    id = firebaseUser.uid,
                    nombre = normalizedEmail.substringBefore("@"),
                    email = normalizedEmail,
                    rol = UserRole.ADMINISTRADOR,
                    puedeAlternarRol = true,
                    perfilCompleto = true
                )
                firestoreService.createUser(user).fold(
                    onSuccess = { Result.success(user) },
                    onFailure = { firestoreError ->
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

    suspend fun updateAdministrador(adminId: String, nombre: String): Result<User> {
        requireSuperAdmin().getOrElse { return Result.failure(it) }
        val admin = firestoreService.getUser(adminId).getOrElse { return Result.failure(it) }
        if (!isAdministradorAccount(admin)) {
            return Result.failure(Exception("El usuario no es administrador"))
        }
        if (AdminUtils.isSuperAdminEmail(admin.email)) {
            return Result.failure(Exception("No se puede modificar la cuenta SuperAdmin"))
        }
        val normalizedNombre = nombre.trim()
        if (!ValidationUtils.isValidName(normalizedNombre)) {
            return Result.failure(Exception("Nombre muy corto"))
        }
        val updatedUser = admin.copy(nombre = normalizedNombre)
        return updateUserProfile(updatedUser)
    }

    suspend fun sendAdministradorPasswordReset(adminId: String): Result<Unit> {
        requireSuperAdmin().getOrElse { return Result.failure(it) }
        val admin = firestoreService.getUser(adminId).getOrElse { return Result.failure(it) }
        if (!isAdministradorAccount(admin)) {
            return Result.failure(Exception("El usuario no es administrador"))
        }
        if (AdminUtils.isSuperAdminEmail(admin.email)) {
            return Result.failure(Exception("No se puede restablecer la contraseña de SuperAdmin"))
        }
        return sendPasswordResetEmail(admin.email)
    }

    suspend fun deleteAdministrador(adminId: String): Result<Unit> {
        val currentUser = requireSuperAdmin().getOrElse { return Result.failure(it) }
        if (currentUser.id == adminId) {
            return Result.failure(Exception("No puede eliminar su propia cuenta"))
        }
        val admin = firestoreService.getUser(adminId).getOrElse { return Result.failure(it) }
        if (!isAdministradorAccount(admin)) {
            return Result.failure(Exception("El usuario no es administrador"))
        }
        if (AdminUtils.isSuperAdminEmail(admin.email) || admin.rol == UserRole.SUPER_ADMIN) {
            return Result.failure(Exception("No se puede eliminar la cuenta SuperAdmin"))
        }
        return firestoreService.deleteUser(adminId)
    }

    private fun isAdministradorAccount(user: User): Boolean =
        user.rol == UserRole.ADMINISTRADOR ||
            user.rolAlternativo == UserRole.ADMINISTRADOR.value

    suspend fun deleteGerenteHotel(gerenteId: String): Result<Unit> {
        val currentUser = requireAdminPanelAccess().getOrElse { return Result.failure(it) }
        val gerente = firestoreService.getUser(gerenteId).getOrElse { return Result.failure(it) }
        val isGerenteAccount = gerente.rol == UserRole.GERENTE_HOTEL ||
            UserRole.matchesEncargadoHotel(gerente.rolAlternativo)
        if (!isGerenteAccount) {
            return Result.failure(Exception("El usuario no es encargado del hotel"))
        }
        if (currentUser.rol == UserRole.ADMINISTRADOR && gerente.creadoPorAdminId != currentUser.id) {
            return Result.failure(Exception("No puede eliminar encargados creados por otro administrador"))
        }
        val hotels = firestoreService.getHotelsByOwner(gerenteId).getOrElse { return Result.failure(it) }
        hotels.forEach { hotel ->
            firestoreService.deleteHotel(hotel.id).getOrElse { return Result.failure(it) }
        }
        return firestoreService.deleteUser(gerenteId)
    }

    suspend fun completeGerenteProfile(
        nombre: String,
        password: String,
        confirmPassword: String
    ): Result<User> {
        val user = getCurrentUserData().getOrElse { return Result.failure(it) }
        if (user.rol != UserRole.GERENTE_HOTEL) {
            return Result.failure(Exception("Solo encargados del hotel pueden completar este perfil"))
        }
        if (!ValidationUtils.isValidName(nombre)) {
            return Result.failure(Exception("Nombre muy corto"))
        }
        if (!ValidationUtils.isValidPassword(password)) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        if (!ValidationUtils.passwordsMatch(password, confirmPassword)) {
            return Result.failure(Exception("Las contraseñas no coinciden"))
        }
        authService.updatePassword(password.trim()).getOrElse { return Result.failure(it) }
        val updatedUser = user.copy(
            nombre = nombre.trim(),
            perfilCompleto = true
        )
        return updateUserProfile(updatedUser)
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
            firestoreService.migrateRoomStockFields()
            firestoreService.migrateGerenteCreatorAssignments()
            firestoreService.migrateOneHotelPerGerente()
            migrateOrphanHotelsWithStaff()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun migrateOrphanHotelsWithStaff(): Result<Unit> {
        return try {
            var hotels = firestoreService.getAllHotels().getOrElse { return Result.failure(it) }
            val users = firestoreService.getAllUsers()
                .getOrElse { return Result.failure(it) }
                .toMutableList()

            val orphanHotels = hotels
                .filter { it.propietarioId.isBlank() }
                .sortedWith(compareBy({ it.nombre.lowercase() }, { it.id }))

            if (orphanHotels.isEmpty()) return Result.success(Unit)

            var number = nextMigrationStaffNumber(users)

            for (hotel in orphanHotels) {
                var assigned = false
                var attempts = 0
                while (!assigned && attempts < 500) {
                    attempts++
                    val adminEmail = migrationAdminEmail(number)
                    val encargadoEmail = migrationEncargadoEmail(number)

                    val admin = ensureMigrationAdministrador(adminEmail, users)
                        .getOrElse { return Result.failure(it) }

                    ensureMigrationEncargado(
                        email = encargadoEmail,
                        adminId = admin.id,
                        users = users,
                        hotels = hotels
                    ).fold(
                        onSuccess = { encargado ->
                            firestoreService.updateHotel(hotel.copy(propietarioId = encargado.id))
                                .getOrElse { return Result.failure(it) }
                            hotels = hotels.map { current ->
                                if (current.id == hotel.id) {
                                    current.copy(propietarioId = encargado.id)
                                } else {
                                    current
                                }
                            }
                            assigned = true
                            number++
                        },
                        onFailure = {
                            number++
                        }
                    )
                }
                if (!assigned) {
                    return Result.failure(
                        Exception("No se pudo asignar encargado al hotel ${hotel.nombre}")
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureMigrationAdministrador(
        email: String,
        users: MutableList<User>
    ): Result<User> {
        findUserByEmail(users, email)?.let { return Result.success(it) }
        return createMigrationAuthUser(email).fold(
            onSuccess = { firebaseUser ->
                val user = User(
                    id = firebaseUser.uid,
                    nombre = email.substringBefore("@"),
                    email = email,
                    rol = UserRole.ADMINISTRADOR,
                    puedeAlternarRol = true,
                    perfilCompleto = true
                )
                firestoreService.createUser(user).fold(
                    onSuccess = {
                        users.add(user)
                        Result.success(user)
                    },
                    onFailure = { Result.failure(it) }
                )
            },
            onFailure = { error ->
                reloadUserByEmail(email, users)?.let { return Result.success(it) }
                Result.failure(Exception(AuthErrorUtils.toUserMessage(error)))
            }
        )
    }

    private suspend fun ensureMigrationEncargado(
        email: String,
        adminId: String,
        users: MutableList<User>,
        hotels: List<Hotel>
    ): Result<User> {
        val existing = findUserByEmail(users, email)
        if (existing != null) {
            val ownedHotels = hotels.count { hotel -> hotel.propietarioId == existing.id }
            if (ownedHotels >= HotelFormOptions.MAX_HOTELS_PER_GERENTE) {
                return Result.failure(Exception("Encargado ya tiene hotel asignado"))
            }
            if (existing.creadoPorAdminId != adminId) {
                val updated = existing.copy(creadoPorAdminId = adminId, perfilCompleto = true)
                firestoreService.updateUser(updated).getOrElse { return Result.failure(it) }
                users.replaceAll { if (it.id == updated.id) updated else it }
                return Result.success(updated)
            }
            return Result.success(existing)
        }
        return createMigrationAuthUser(email).fold(
            onSuccess = { firebaseUser ->
                val user = User(
                    id = firebaseUser.uid,
                    nombre = email.substringBefore("@"),
                    email = email,
                    rol = UserRole.GERENTE_HOTEL,
                    puedeAlternarRol = true,
                    perfilCompleto = true,
                    creadoPorAdminId = adminId
                )
                firestoreService.createUser(user).fold(
                    onSuccess = {
                        users.add(user)
                        Result.success(user)
                    },
                    onFailure = { Result.failure(it) }
                )
            },
            onFailure = { error ->
                reloadUserByEmail(email, users)?.let { user ->
                    val ownedHotels = hotels.count { hotel -> hotel.propietarioId == user.id }
                    if (ownedHotels >= HotelFormOptions.MAX_HOTELS_PER_GERENTE) {
                        return Result.failure(Exception("Encargado ya tiene hotel asignado"))
                    }
                    return Result.success(user)
                }
                Result.failure(Exception(AuthErrorUtils.toUserMessage(error)))
            }
        )
    }

    private suspend fun createMigrationAuthUser(email: String): Result<FirebaseUser> =
        authService.createUserWithoutSigningIn(email, Constants.MIGRATION_STAFF_PASSWORD)

    private suspend fun reloadUserByEmail(email: String, users: MutableList<User>): User? {
        val refreshed = firestoreService.getAllUsers().getOrNull().orEmpty()
        val match = refreshed.find { it.email.equals(email, ignoreCase = true) } ?: return null
        if (users.none { it.id == match.id }) {
            users.add(match)
        }
        return match
    }

    private fun findUserByEmail(users: List<User>, email: String): User? =
        users.find { it.email.equals(email, ignoreCase = true) }

    private fun migrationEncargadoEmail(number: Int): String =
        "encargado$number${Constants.MIGRATION_ENCARGADO_EMAIL_DOMAIN}"

    private fun migrationAdminEmail(number: Int): String =
        "administrador$number${Constants.MIGRATION_ADMIN_EMAIL_DOMAIN}"

    private fun nextMigrationStaffNumber(users: List<User>): Int {
        val encargadoPattern = Regex("^encargado(\\d+)@gmail\\.com$", RegexOption.IGNORE_CASE)
        val adminPattern = Regex("^administrador(\\d+)@gmail\\.com$", RegexOption.IGNORE_CASE)
        val numbers = users.mapNotNull { user ->
            encargadoPattern.find(user.email)?.groupValues?.get(1)?.toIntOrNull()
                ?: adminPattern.find(user.email)?.groupValues?.get(1)?.toIntOrNull()
        }
        return (numbers.maxOrNull() ?: 0) + 1
    }
}
