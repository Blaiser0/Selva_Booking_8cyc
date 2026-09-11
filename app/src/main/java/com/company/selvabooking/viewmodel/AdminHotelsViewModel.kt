package com.company.selvabooking.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.AuditAction
import com.company.selvabooking.domain.model.AuditEntityType
import com.company.selvabooking.domain.model.AuditLog
import com.company.selvabooking.domain.model.Hotel
import com.company.selvabooking.domain.model.Room
import com.company.selvabooking.domain.model.User
import com.company.selvabooking.domain.model.UserRole
import com.company.selvabooking.repository.AuditRepository
import com.company.selvabooking.repository.AuthRepository
import com.company.selvabooking.repository.HotelRepository
import com.company.selvabooking.repository.ResenaRepository
import com.company.selvabooking.repository.RoomRepository
import com.company.selvabooking.navigation.Routes
import com.company.selvabooking.utils.AdminDataScope
import com.company.selvabooking.utils.Constants
import com.company.selvabooking.utils.HotelFormOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdministratorFilterOption(
    val id: String,
    val label: String
)

data class AdminHotelsUiState(
    val isLoading: Boolean = true,
    val hotels: List<Hotel> = emptyList(),
    val totalHotelCount: Int = 0,
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val editingHotelId: String? = null,
    val nombre: String = "",
    val ciudad: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val estrellas: Int = 3,
    val precioMinimo: String = "",
    val calificacion: String = "4.0",
    val selectedServicios: List<String> = emptyList(),
    val ciudadError: String? = null,
    val categoriaError: String? = null,
    val serviciosError: String? = null,
    val ubicacion: String = "",
    val destacado: Boolean = false,
    val oferta: Boolean = false,
    val imagenes: List<String> = emptyList(),
    val isGerenteMode: Boolean = false,
    val isSuperAdminMode: Boolean = false,
    val isLimitedAdminMode: Boolean = false,
    val calificacionLocked: Boolean = false,
    val ownerLabels: Map<String, String> = emptyMap(),
    val administratorLabelsByGerenteId: Map<String, String> = emptyMap(),
    val administratorFilterOptions: List<AdministratorFilterOption> = emptyList(),
    val selectedAdministratorFilterId: String = "",
    val selectedAdministratorFilterLabel: String = "Todos los administradores",
    val activeListFilterLabel: String? = null
) {
    val canCreateHotel: Boolean
        get() = isSuperAdminMode || (isGerenteMode && hotels.size < HotelFormOptions.MAX_HOTELS_PER_GERENTE)

    val canManageHotels: Boolean
        get() = isSuperAdminMode || isGerenteMode

    val showOwnerInfo: Boolean
        get() = isSuperAdminMode || isLimitedAdminMode
}

class AdminHotelsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val FILTER_ALL_ADMINS = ""
        const val FILTER_UNASSIGNED_ADMIN = "__unassigned__"
    }

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository
    private val resenaRepository: ResenaRepository =
        (application as SelvaBookingApplication).resenaRepository
    private val auditRepository: AuditRepository =
        (application as SelvaBookingApplication).auditRepository

    private var currentUserId: String = ""
    private var currentUser: User? = null
    private var isGerenteMode: Boolean = false
    private var isSuperAdminMode: Boolean = false
    private var allHotels: List<Hotel> = emptyList()
    private var allGerentes: List<User> = emptyList()
    private var allAdministradores: List<User> = emptyList()
    private var selectedAdministratorFilterId: String = FILTER_ALL_ADMINS
    private var hotelsListFilter: HotelsListFilter = HotelsListFilter.ALL
    private var isLimitedAdminMode: Boolean = false

    private enum class HotelsListFilter {
        ALL,
        WITHOUT_OWNER
    }

    private val _uiState = MutableStateFlow(AdminHotelsUiState())
    val uiState: StateFlow<AdminHotelsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull() ?: return@launch
            currentUserId = user.id
            currentUser = user
            isGerenteMode = user.rol == UserRole.GERENTE_HOTEL
            isSuperAdminMode = user.rol == UserRole.SUPER_ADMIN
            isLimitedAdminMode = user.rol == UserRole.ADMINISTRADOR
            _uiState.update {
                it.copy(
                    isGerenteMode = isGerenteMode,
                    isSuperAdminMode = isSuperAdminMode,
                    isLimitedAdminMode = isLimitedAdminMode,
                    calificacionLocked = isGerenteMode,
                    calificacion = if (isGerenteMode) Constants.DEFAULT_GERENTE_HOTEL_RATING.toString()
                    else it.calificacion
                )
            }
            val hotelsFlow = when {
                isGerenteMode -> hotelRepository.getHotelsByOwnerFlow(user.id)
                isLimitedAdminMode -> combine(
                    authRepository.getGerentesHotelFlow(),
                    hotelRepository.getHotelsFlow()
                ) { gerentes, hotels ->
                    AdminDataScope.hotelsForAdmin(user.id, gerentes, hotels)
                }
                else -> hotelRepository.getHotelsFlow()
            }
            if (isSuperAdminMode || isLimitedAdminMode) {
                launch {
                    authRepository.getGerentesHotelFlow().collect { gerentes ->
                        allGerentes = gerentes
                        val visibleGerentes = if (isLimitedAdminMode) {
                            AdminDataScope.gerentesCreatedBy(user.id, gerentes)
                        } else {
                            gerentes
                        }
                        val labels = visibleGerentes.associate { gerente ->
                            gerente.id to formatOwnerLabel(gerente)
                        }
                        _uiState.update {
                            it.copy(ownerLabels = labels)
                        }
                        if (isSuperAdminMode) {
                            syncAdministratorUiState()
                        } else {
                            _uiState.update {
                                it.copy(
                                    administratorLabelsByGerenteId = buildAdministratorLabelsByGerente(
                                        gerentes,
                                        allAdministradores
                                    )
                                )
                            }
                        }
                    }
                }
            }
            if (isSuperAdminMode) {
                launch {
                    authRepository.getAdministratorAccountsFlow().collect { administradores ->
                        allAdministradores = administradores
                        syncAdministratorUiState()
                    }
                }
            }
            hotelsFlow.collect { hotels ->
                allHotels = hotels
                if (isSuperAdminMode) {
                    applyAdministratorFilter()
                } else {
                    applyScopedHotelsFilter()
                }
            }
        }
    }

    fun applyIncomingHotelsFilter(filterKey: String) {
        when (filterKey) {
            Routes.AdminHotelsFilter.WITHOUT_OWNER -> {
                hotelsListFilter = HotelsListFilter.WITHOUT_OWNER
                selectedAdministratorFilterId = FILTER_ALL_ADMINS
            }
            Routes.AdminHotelsFilter.WITHOUT_ADMIN -> {
                hotelsListFilter = HotelsListFilter.ALL
                selectedAdministratorFilterId = FILTER_UNASSIGNED_ADMIN
            }
            else -> {
                hotelsListFilter = HotelsListFilter.ALL
                selectedAdministratorFilterId = FILTER_ALL_ADMINS
            }
        }
        if (isSuperAdminMode) {
            val label = _uiState.value.administratorFilterOptions
                .find { it.id == selectedAdministratorFilterId }?.label
                ?: "Todos los administradores"
            _uiState.update {
                it.copy(
                    selectedAdministratorFilterId = selectedAdministratorFilterId,
                    selectedAdministratorFilterLabel = label
                )
            }
            applyAdministratorFilter()
        } else if (isLimitedAdminMode) {
            applyScopedHotelsFilter()
        }
    }

    fun updateAdministratorFilter(filterId: String) {
        if (!isSuperAdminMode) return
        selectedAdministratorFilterId = filterId
        val label = _uiState.value.administratorFilterOptions
            .find { it.id == filterId }?.label ?: "Todos los administradores"
        _uiState.update {
            it.copy(
                selectedAdministratorFilterId = filterId,
                selectedAdministratorFilterLabel = label
            )
        }
        applyAdministratorFilter()
    }

    private fun syncAdministratorUiState() {
        if (!isSuperAdminMode) return
        val resolvedAdministradores = AdminDataScope.resolveAdministratorAccounts(
            allAdministradores,
            allGerentes
        )
        val options = buildAdministratorFilterOptions(resolvedAdministradores)
        val selectedLabel = options.find { it.id == selectedAdministratorFilterId }?.label
            ?: options.first().label
        _uiState.update {
            it.copy(
                administratorFilterOptions = options,
                selectedAdministratorFilterId = selectedAdministratorFilterId,
                selectedAdministratorFilterLabel = selectedLabel,
                administratorLabelsByGerenteId = buildAdministratorLabelsByGerente(
                    allGerentes,
                    resolvedAdministradores
                )
            )
        }
        applyAdministratorFilter()
    }

    private fun applyAdministratorFilter() {
        if (!isSuperAdminMode) return
        var filtered = when (selectedAdministratorFilterId) {
            FILTER_ALL_ADMINS -> allHotels
            FILTER_UNASSIGNED_ADMIN -> AdminDataScope.hotelsWithoutAdministrator(allGerentes, allHotels)
            else -> AdminDataScope.hotelsForAdmin(
                selectedAdministratorFilterId,
                allGerentes,
                allHotels
            )
        }
        filtered = applyOwnerListFilter(filtered)
        _uiState.update {
            it.copy(
                isLoading = false,
                hotels = filtered,
                totalHotelCount = allHotels.size,
                activeListFilterLabel = resolveActiveListFilterLabel()
            )
        }
    }

    private fun applyScopedHotelsFilter() {
        val filtered = applyOwnerListFilter(allHotels)
        _uiState.update {
            it.copy(
                isLoading = false,
                hotels = filtered,
                totalHotelCount = allHotels.size,
                activeListFilterLabel = resolveActiveListFilterLabel()
            )
        }
    }

    private fun applyOwnerListFilter(hotels: List<Hotel>): List<Hotel> =
        when (hotelsListFilter) {
            HotelsListFilter.WITHOUT_OWNER -> hotels.filter { it.propietarioId.isBlank() }
            HotelsListFilter.ALL -> hotels
        }

    private fun resolveActiveListFilterLabel(): String? = when {
        hotelsListFilter == HotelsListFilter.WITHOUT_OWNER -> "Sin encargado"
        isSuperAdminMode && selectedAdministratorFilterId == FILTER_UNASSIGNED_ADMIN ->
            "Sin administrador vinculado"
        else -> null
    }

    private fun buildAdministratorFilterOptions(administradores: List<User>): List<AdministratorFilterOption> {
        val sortedAdmins = administradores.sortedWith(
            compareBy({ it.nombre.lowercase() }, { it.email.lowercase() })
        )
        return buildList {
            add(AdministratorFilterOption(FILTER_ALL_ADMINS, "Todos los administradores"))
            sortedAdmins.forEach { admin ->
                add(AdministratorFilterOption(admin.id, formatAdminFilterLabel(admin)))
            }
            add(AdministratorFilterOption(FILTER_UNASSIGNED_ADMIN, "Sin administrador vinculado"))
        }
    }

    private fun buildAdministratorLabelsByGerente(
        gerentes: List<User>,
        administradores: List<User>
    ): Map<String, String> {
        val adminById = administradores.associateBy { it.id }
        return gerentes.associate { gerente ->
            gerente.id to formatAdminLabel(adminById[gerente.creadoPorAdminId])
        }
    }

    fun loadHotelForEdit(hotel: Hotel) {
        _uiState.update {
            it.copy(
                editingHotelId = hotel.id,
                nombre = hotel.nombre,
                ciudad = hotel.ciudad,
                direccion = hotel.direccion,
                descripcion = hotel.descripcion,
                categoria = hotel.categoria,
                estrellas = hotel.estrellas,
                precioMinimo = hotel.precioMinimo.toString(),
                calificacion = hotel.effectiveBaseRating().toString(),
                selectedServicios = hotel.servicios,
                ubicacion = hotel.ubicacion,
                destacado = hotel.destacado,
                oferta = hotel.oferta,
                imagenes = hotel.imagenes
            )
        }
    }

    fun clearForm() {
        _uiState.update {
            AdminHotelsUiState(
                hotels = it.hotels,
                totalHotelCount = it.totalHotelCount,
                isLoading = false,
                isGerenteMode = it.isGerenteMode,
                isSuperAdminMode = it.isSuperAdminMode,
                isLimitedAdminMode = it.isLimitedAdminMode,
                ownerLabels = it.ownerLabels,
                administratorLabelsByGerenteId = it.administratorLabelsByGerenteId,
                administratorFilterOptions = it.administratorFilterOptions,
                selectedAdministratorFilterId = it.selectedAdministratorFilterId,
                selectedAdministratorFilterLabel = it.selectedAdministratorFilterLabel,
                calificacionLocked = it.calificacionLocked,
                calificacion = if (it.isGerenteMode) {
                    Constants.DEFAULT_GERENTE_HOTEL_RATING.toString()
                } else {
                    "4.0"
                }
            )
        }
    }

    fun updateNombre(v: String) = _uiState.update { it.copy(nombre = v) }
    fun updateCiudad(v: String) = _uiState.update { it.copy(ciudad = v, ciudadError = null) }
    fun updateDireccion(v: String) = _uiState.update { it.copy(direccion = v) }
    fun updateDescripcion(v: String) = _uiState.update { it.copy(descripcion = v) }
    fun updateCategoria(v: String) = _uiState.update { it.copy(categoria = v, categoriaError = null) }
    fun updateEstrellas(v: Int) = _uiState.update { it.copy(estrellas = v) }
    fun updatePrecioMinimo(v: String) = _uiState.update { it.copy(precioMinimo = v) }
    fun updateCalificacion(v: String) {
        if (!_uiState.value.calificacionLocked) {
            _uiState.update { it.copy(calificacion = v) }
        }
    }
    fun setSelectedServicios(servicios: List<String>) {
        _uiState.update {
            it.copy(selectedServicios = servicios, serviciosError = null)
        }
    }

    fun toggleServicio(servicio: String) {
        _uiState.update { state ->
            val updated = if (servicio in state.selectedServicios) {
                state.selectedServicios - servicio
            } else {
                state.selectedServicios + servicio
            }
            state.copy(selectedServicios = updated, serviciosError = null)
        }
    }
    fun updateUbicacion(v: String) = _uiState.update { it.copy(ubicacion = v) }
    fun updateDestacado(v: Boolean) = _uiState.update { it.copy(destacado = v) }
    fun updateOferta(v: Boolean) = _uiState.update { it.copy(oferta = v) }

    fun removeImage(index: Int) {
        _uiState.update { state ->
            state.copy(imagenes = state.imagenes.filterIndexed { i, _ -> i != index })
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            hotelRepository.uploadHotelImage(uri).fold(
                onSuccess = { url ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            imagenes = it.imagenes + url
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun saveHotel() {
        if (_uiState.value.isLimitedAdminMode) {
            _uiState.update { it.copy(error = "Los administradores solo pueden consultar hoteles") }
            return
        }
        val state = _uiState.value
        val ciudadError = when {
            state.ciudad.isBlank() -> "Seleccione una ciudad"
            !HotelFormOptions.isValidCity(state.ciudad) -> "Ciudad no válida"
            else -> null
        }
        val categoriaError = when {
            state.categoria.isBlank() -> "Seleccione una categoría"
            !HotelFormOptions.isValidCategory(state.categoria) -> "Categoría no válida"
            else -> null
        }
        val serviciosError = if (state.selectedServicios.isEmpty()) {
            "Seleccione al menos un servicio"
        } else {
            null
        }

        if (state.nombre.isBlank() || ciudadError != null || categoriaError != null || serviciosError != null) {
            _uiState.update {
                it.copy(
                    error = if (state.nombre.isBlank()) "Nombre es obligatorio" else null,
                    ciudadError = ciudadError,
                    categoriaError = categoriaError,
                    serviciosError = serviciosError
                )
            }
            return
        }
        viewModelScope.launch {
            if (isGerenteMode && state.editingHotelId == null) {
                val ownedCount = hotelRepository.getHotelsByOwner(currentUserId)
                    .getOrDefault(emptyList())
                    .size
                if (ownedCount >= HotelFormOptions.MAX_HOTELS_PER_GERENTE) {
                    _uiState.update {
                        it.copy(error = "Solo puede registrar un hotel como encargado del hotel")
                    }
                    return@launch
                }
            }
            _uiState.update { it.copy(isSaving = true, error = null) }
            val existingHotel = state.editingHotelId?.let { id ->
                state.hotels.find { it.id == id }
            }
            if (isGerenteMode && !isSuperAdminMode && state.editingHotelId != null) {
                if (existingHotel?.propietarioId != currentUserId) {
                    _uiState.update {
                        it.copy(isSaving = false, error = "No puede modificar hoteles de otras personas")
                    }
                    return@launch
                }
            }
            val adminRating = if (isGerenteMode) {
                Constants.DEFAULT_GERENTE_HOTEL_RATING
            } else {
                state.calificacion.toDoubleOrNull() ?: 4.0
            }
            val displayRating = if (isGerenteMode && existingHotel != null) {
                existingHotel.calificacion
            } else {
                adminRating
            }
            val baseRating = if (isGerenteMode && existingHotel != null) {
                existingHotel.calificacionBase
            } else {
                adminRating
            }
            val hotel = Hotel(
                id = state.editingHotelId ?: "",
                nombre = state.nombre,
                ciudad = state.ciudad,
                direccion = state.direccion,
                descripcion = state.descripcion,
                categoria = state.categoria,
                estrellas = state.estrellas,
                precioMinimo = state.precioMinimo.toDoubleOrNull() ?: 0.0,
                calificacion = displayRating,
                calificacionBase = baseRating,
                servicios = state.selectedServicios.sorted(),
                ubicacion = state.ubicacion,
                destacado = state.destacado,
                oferta = state.oferta,
                imagenes = state.imagenes,
                propietarioId = when {
                    state.editingHotelId != null -> existingHotel?.propietarioId ?: currentUserId
                    isGerenteMode -> currentUserId
                    else -> ""
                }
            )
            val result = if (state.editingHotelId != null) {
                hotelRepository.updateHotel(hotel).map { hotel.id }
            } else {
                hotelRepository.createHotel(hotel)
            }
            result.fold(
                onSuccess = { savedHotelId ->
                    if (isGerenteMode) {
                        val savedHotel = hotel.copy(id = savedHotelId)
                        currentUser?.let { actor ->
                            auditRepository.logEncargadoChange(
                                actor = actor,
                                action = if (state.editingHotelId != null) AuditAction.UPDATE else AuditAction.CREATE,
                                entityType = AuditEntityType.HOTEL,
                                entityId = savedHotelId,
                                entityLabel = savedHotel.nombre,
                                hotelId = savedHotelId,
                                beforeData = existingHotel?.let { AuditLog.hotelSnapshot(it) } ?: emptyMap(),
                                afterData = AuditLog.hotelSnapshot(savedHotel)
                            )
                        }
                    }
                    resenaRepository.refreshHotelRating(savedHotelId)
                    _uiState.update {
                        it.copy(isSaving = false, message = "Hotel guardado", editingHotelId = null)
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun deleteHotel(hotelId: String) {
        if (_uiState.value.isLimitedAdminMode) {
            _uiState.update { it.copy(error = "Los administradores solo pueden consultar hoteles") }
            return
        }
        viewModelScope.launch {
            if (isGerenteMode && !isSuperAdminMode) {
                val hotel = _uiState.value.hotels.find { it.id == hotelId }
                if (hotel?.propietarioId != currentUserId) {
                    _uiState.update { it.copy(error = "No puede eliminar hoteles de otras personas") }
                    return@launch
                }
            }
            val hotelSnapshot = _uiState.value.hotels.find { it.id == hotelId }
            val roomsSnapshot = if (isGerenteMode && hotelSnapshot != null) {
                roomRepository.getRoomsByHotel(hotelId).getOrDefault(emptyList())
            } else {
                emptyList()
            }
            hotelRepository.deleteHotel(hotelId).fold(
                onSuccess = {
                    if (isGerenteMode && hotelSnapshot != null) {
                        currentUser?.let { actor ->
                            auditRepository.logEncargadoChange(
                                actor = actor,
                                action = AuditAction.DELETE,
                                entityType = AuditEntityType.HOTEL,
                                entityId = hotelId,
                                entityLabel = hotelSnapshot.nombre,
                                hotelId = hotelId,
                                beforeData = AuditLog.hotelSnapshot(hotelSnapshot),
                                relatedData = roomsSnapshot.map { AuditLog.roomSnapshot(it) }
                            )
                        }
                    }
                    _uiState.update { it.copy(message = "Hotel eliminado") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }

    private fun formatAdminFilterLabel(admin: User): String = when {
        admin.nombre.isNotBlank() -> admin.nombre
        admin.email.isNotBlank() -> admin.email
        else -> "Administrador sin nombre"
    }

    private fun formatAdminLabel(admin: User?): String {
        if (admin == null || admin.id.isBlank()) return "Sin administrador vinculado"
        return formatAdminFilterLabel(admin)
    }

    private fun formatOwnerLabel(user: com.company.selvabooking.domain.model.User): String {
        return when {
            user.nombre.isNotBlank() && user.email.isNotBlank() -> "${user.nombre} · ${user.email}"
            user.nombre.isNotBlank() -> user.nombre
            user.email.isNotBlank() -> user.email
            else -> "Sin datos de contacto"
        }
    }
}

data class AdminRoomsUiState(
    val isLoading: Boolean = true,
    val hotelId: String = "",
    val hotelName: String = "",
    val rooms: List<Room> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val editingRoomId: String? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val capacidad: String = "2",
    val cantidad: String = "1",
    val disponible: Boolean = true,
    val imagenes: List<String> = emptyList(),
    val accessDenied: Boolean = false,
    val readOnly: Boolean = false
)

class AdminRoomsViewModel(
    application: Application,
    private val hotelId: String,
    initialHotelName: String = ""
) : AndroidViewModel(application) {

    private val authRepository: AuthRepository =
        (application as SelvaBookingApplication).authRepository
    private val hotelRepository: HotelRepository =
        (application as SelvaBookingApplication).hotelRepository
    private val roomRepository: RoomRepository =
        (application as SelvaBookingApplication).roomRepository
    private val auditRepository: AuditRepository =
        (application as SelvaBookingApplication).auditRepository

    private var canManageRooms: Boolean = true
    private var isGerenteMode: Boolean = false
    private var currentUser: com.company.selvabooking.domain.model.User? = null

    private val _uiState = MutableStateFlow(
        AdminRoomsUiState(
            hotelId = hotelId,
            hotelName = initialHotelName
        )
    )
    val uiState: StateFlow<AdminRoomsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUserData().getOrNull()
            val isSuperAdmin = user?.rol == UserRole.SUPER_ADMIN
            val isLimitedAdmin = user?.rol == UserRole.ADMINISTRADOR
            val isGerente = user?.rol == UserRole.GERENTE_HOTEL
            isGerenteMode = isGerente
            currentUser = user
            val hotel = hotelRepository.getHotel(hotelId).getOrNull()
            if (hotel == null) {
                _uiState.update {
                    it.copy(
                        accessDenied = true,
                        isLoading = false,
                        error = "Hotel no encontrado"
                    )
                }
                return@launch
            }
            when {
                isLimitedAdmin -> {
                    val adminId = user?.id.orEmpty()
                    if (adminId.isBlank()) {
                        canManageRooms = false
                        _uiState.update {
                            it.copy(
                                accessDenied = true,
                                isLoading = false,
                                error = "No tiene permiso para ver este hotel"
                            )
                        }
                        return@launch
                    }
                    val gerentes = authRepository.getGerentesHotelFlow().first()
                    val managedIds = AdminDataScope.managedGerenteIds(adminId, gerentes)
                    if (hotel.propietarioId !in managedIds) {
                        canManageRooms = false
                        _uiState.update {
                            it.copy(
                                accessDenied = true,
                                isLoading = false,
                                error = "No tiene permiso para ver este hotel"
                            )
                        }
                        return@launch
                    }
                    canManageRooms = false
                    _uiState.update {
                        it.copy(hotelName = hotel.nombre, readOnly = true)
                    }
                }
                isSuperAdmin -> {
                    canManageRooms = true
                    _uiState.update { it.copy(hotelName = hotel.nombre, readOnly = false) }
                }
                isGerente && hotel.propietarioId != user.id -> {
                    canManageRooms = false
                    _uiState.update {
                        it.copy(
                            accessDenied = true,
                            isLoading = false,
                            error = "No tiene permiso para gestionar este hotel"
                        )
                    }
                    return@launch
                }
                isGerente -> {
                    canManageRooms = true
                    _uiState.update { it.copy(hotelName = hotel.nombre, readOnly = false) }
                }
                else -> {
                    canManageRooms = false
                    _uiState.update {
                        it.copy(
                            accessDenied = true,
                            isLoading = false,
                            error = "No tiene permiso para ver este hotel"
                        )
                    }
                    return@launch
                }
            }
            roomRepository.getRoomsByHotelFlow(hotelId).collect { rooms ->
                _uiState.update { it.copy(isLoading = false, rooms = rooms) }
            }
        }
    }

    fun loadRoomForEdit(room: Room) {
        _uiState.update {
            it.copy(
                editingRoomId = room.id,
                nombre = room.nombre,
                descripcion = room.descripcion,
                precio = room.precio.toString(),
                capacidad = room.capacidad.toString(),
                cantidad = room.cantidad.toString(),
                disponible = room.disponible,
                imagenes = room.imagenes
            )
        }
    }

    fun clearForm() {
        _uiState.update {
            AdminRoomsUiState(
                hotelId = hotelId,
                hotelName = it.hotelName,
                rooms = it.rooms,
                isLoading = false,
                readOnly = it.readOnly
            )
        }
    }

    fun updateNombre(v: String) = _uiState.update { it.copy(nombre = v) }
    fun updateDescripcion(v: String) = _uiState.update { it.copy(descripcion = v) }
    fun updatePrecio(v: String) = _uiState.update { it.copy(precio = v) }
    fun updateCapacidad(v: String) = _uiState.update { it.copy(capacidad = v) }
    fun updateCantidad(v: String) = _uiState.update { it.copy(cantidad = v) }
    fun updateDisponible(v: Boolean) = _uiState.update { it.copy(disponible = v) }

    fun removeImage(index: Int) {
        _uiState.update { state ->
            state.copy(imagenes = state.imagenes.filterIndexed { i, _ -> i != index })
        }
    }

    fun uploadImage(uri: Uri) {
        if (_uiState.value.readOnly) return
        viewModelScope.launch {
            roomRepository.uploadRoomImage(uri).fold(
                onSuccess = { url ->
                    _uiState.update { it.copy(imagenes = it.imagenes + url) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            )
        }
    }

    fun saveRoom() {
        if (!canManageRooms || _uiState.value.readOnly) {
            _uiState.update {
                it.copy(error = "Los administradores solo pueden consultar habitaciones")
            }
            return
        }
        val state = _uiState.value
        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val cantidad = state.cantidad.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val existingRoom = state.editingRoomId?.let { id ->
                state.rooms.find { it.id == id }
            }
            val stock = if (existingRoom != null) {
                val occupied = existingRoom.cantidad - existingRoom.stock
                (cantidad - occupied).coerceIn(0, cantidad)
            } else {
                cantidad
            }
            val room = Room(
                id = state.editingRoomId ?: "",
                hotelId = hotelId,
                nombre = state.nombre,
                descripcion = state.descripcion,
                precio = state.precio.toDoubleOrNull() ?: 0.0,
                capacidad = state.capacidad.toIntOrNull() ?: 2,
                cantidad = cantidad,
                stock = stock,
                disponible = state.disponible,
                imagenes = state.imagenes
            )
            val result: Result<String> = if (state.editingRoomId != null) {
                roomRepository.updateRoom(room).map { room.id }
            } else {
                roomRepository.createRoom(room)
            }
            result.fold(
                onSuccess = { savedRoomId ->
                    val savedRoom = room.copy(id = savedRoomId)
                    if (isGerenteMode) {
                        currentUser?.let { actor ->
                            auditRepository.logEncargadoChange(
                                actor = actor,
                                action = if (state.editingRoomId != null) AuditAction.UPDATE else AuditAction.CREATE,
                                entityType = AuditEntityType.ROOM,
                                entityId = savedRoomId,
                                entityLabel = savedRoom.nombre,
                                hotelId = hotelId,
                                beforeData = existingRoom?.let { AuditLog.roomSnapshot(it) } ?: emptyMap(),
                                afterData = AuditLog.roomSnapshot(savedRoom)
                            )
                        }
                    }
                    hotelRepository.syncPrecioMinimoFromRooms(hotelId)
                    _uiState.update { it.copy(isSaving = false, message = "Habitación guardada") }
                    clearForm()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, error = e.message) }
                }
            )
        }
    }

    fun deleteRoom(roomId: String) {
        if (!canManageRooms || _uiState.value.readOnly) {
            _uiState.update {
                it.copy(error = "Los administradores solo pueden consultar habitaciones")
            }
            return
        }
        viewModelScope.launch {
            val roomSnapshot = if (isGerenteMode) {
                roomRepository.getRoom(roomId).getOrNull()
            } else {
                null
            }
            roomRepository.deleteRoom(roomId).fold(
                onSuccess = {
                    if (isGerenteMode && roomSnapshot != null) {
                        currentUser?.let { actor ->
                            auditRepository.logEncargadoChange(
                                actor = actor,
                                action = AuditAction.DELETE,
                                entityType = AuditEntityType.ROOM,
                                entityId = roomId,
                                entityLabel = roomSnapshot.nombre,
                                hotelId = hotelId,
                                beforeData = AuditLog.roomSnapshot(roomSnapshot)
                            )
                        }
                    }
                    hotelRepository.syncPrecioMinimoFromRooms(hotelId)
                    _uiState.update { it.copy(message = "Habitación eliminada") }
                },
                onFailure = { e -> _uiState.update { it.copy(error = e.message) } }
            )
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }
}
