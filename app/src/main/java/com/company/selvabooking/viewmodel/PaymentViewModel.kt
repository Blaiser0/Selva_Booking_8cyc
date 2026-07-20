package com.company.selvabooking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.company.selvabooking.SelvaBookingApplication
import com.company.selvabooking.domain.model.Reservation
import com.company.selvabooking.domain.model.SavedPaymentCard
import com.company.selvabooking.repository.ReservationRepository
import com.company.selvabooking.repository.SavedCardRepository
import com.company.selvabooking.utils.PaymentInputFormatters
import com.company.selvabooking.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isLoading: Boolean = true,
    val reservation: Reservation? = null,
    val isProcessing: Boolean = false,
    val isSuccess: Boolean = false,
    val showSaveCardPrompt: Boolean = false,
    val paymentFlowComplete: Boolean = false,
    val error: String? = null,
    val savedCard: SavedPaymentCard? = null,
    val useSavedCard: Boolean = false,
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvc: String = "",
    val cardholderName: String = "",
    val country: String = "Perú",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val district: String = "",
    val postalCode: String = "",
    val region: String = "Madre de Dios",
    val cardNumberError: String? = null,
    val cardExpiryError: String? = null,
    val cardCvcError: String? = null,
    val cardholderNameError: String? = null,
    val addressLine1Error: String? = null,
    val districtError: String? = null
)

class PaymentViewModel(
    application: Application,
    private val reservationId: String
) : AndroidViewModel(application) {

    private val reservationRepository: ReservationRepository =
        (application as SelvaBookingApplication).reservationRepository

    private val savedCardRepository: SavedCardRepository =
        (application as SelvaBookingApplication).savedCardRepository

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadSavedCard()
        loadReservation()
    }

    private fun loadSavedCard() {
        val saved = savedCardRepository.getSavedCard()
        _uiState.update { state ->
            state.copy(
                savedCard = saved,
                useSavedCard = saved != null
            )
        }
        saved?.let { applySavedCard(it) }
    }

    private fun loadReservation() {
        viewModelScope.launch {
            reservationRepository.getAllReservationsFlow().collect { reservations ->
                val reservation = reservations.find { it.id == reservationId }
                _uiState.update { state ->
                    val shouldPrefillName = state.cardholderName.isBlank() && !state.useSavedCard
                    state.copy(
                        reservation = reservation,
                        isLoading = false,
                        cardholderName = if (shouldPrefillName) {
                            reservation?.userNombre.orEmpty()
                        } else {
                            state.cardholderName
                        }
                    )
                }
            }
        }
    }

    private fun applySavedCard(card: SavedPaymentCard) {
        _uiState.update {
            it.copy(
                cardNumber = card.maskedNumber,
                cardExpiry = card.expiry,
                cardholderName = card.cardholderName,
                country = card.country,
                addressLine1 = card.addressLine1,
                addressLine2 = card.addressLine2,
                district = card.district,
                postalCode = card.postalCode,
                region = card.region,
                cardCvc = "",
                useSavedCard = true,
                cardNumberError = null,
                cardExpiryError = null,
                cardCvcError = null,
                cardholderNameError = null,
                addressLine1Error = null,
                districtError = null
            )
        }
    }

    fun useSavedCard() {
        _uiState.value.savedCard?.let { applySavedCard(it) }
    }

    fun switchToNewCard() {
        val reservationName = _uiState.value.reservation?.userNombre.orEmpty()
        _uiState.update {
            it.copy(
                useSavedCard = false,
                cardNumber = "",
                cardExpiry = "",
                cardCvc = "",
                cardholderName = reservationName,
                addressLine1 = "",
                addressLine2 = "",
                district = "",
                postalCode = "",
                cardNumberError = null,
                cardExpiryError = null,
                cardCvcError = null,
                cardholderNameError = null,
                addressLine1Error = null,
                districtError = null
            )
        }
    }

    fun updateCardNumber(value: String) {
        if (_uiState.value.useSavedCard) return
        _uiState.update { it.copy(cardNumber = value, cardNumberError = null) }
    }

    fun updateCardExpiry(value: String) {
        if (_uiState.value.useSavedCard) return
        _uiState.update { it.copy(cardExpiry = value, cardExpiryError = null) }
    }

    fun updateCardCvc(value: String) {
        _uiState.update {
            it.copy(cardCvc = PaymentInputFormatters.formatCvc(value), cardCvcError = null)
        }
    }

    fun updateCardholderName(value: String) {
        if (_uiState.value.useSavedCard) return
        _uiState.update { it.copy(cardholderName = value, cardholderNameError = null) }
    }

    fun updateAddressLine1(value: String) {
        if (_uiState.value.useSavedCard) return
        _uiState.update { it.copy(addressLine1 = value, addressLine1Error = null) }
    }

    fun updateAddressLine2(value: String) {
        if (_uiState.value.useSavedCard) return
        _uiState.update { it.copy(addressLine2 = value) }
    }

    fun updateDistrict(value: String) {
        if (_uiState.value.useSavedCard) return
        _uiState.update { it.copy(district = value, districtError = null) }
    }

    fun updatePostalCode(value: String) {
        if (_uiState.value.useSavedCard) return
        _uiState.update { it.copy(postalCode = value) }
    }

    fun confirmPayment() {
        val state = _uiState.value
        val validationErrors = validatePaymentForm(state)
        if (validationErrors != null) {
            _uiState.value = validationErrors
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            kotlinx.coroutines.delay(1500)
            reservationRepository.confirmReservation(reservationId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            isSuccess = true,
                            showSaveCardPrompt = !state.useSavedCard,
                            paymentFlowComplete = state.useSavedCard
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isProcessing = false, error = e.message)
                    }
                }
            )
        }
    }

    fun saveCardForFuture() {
        val state = _uiState.value
        val digits = state.cardNumber.filter { it.isDigit() }
        if (digits.length >= 4) {
            savedCardRepository.saveCard(
                SavedPaymentCard(
                    lastFour = digits.takeLast(4),
                    expiry = state.cardExpiry,
                    cardholderName = state.cardholderName,
                    country = state.country,
                    addressLine1 = state.addressLine1,
                    addressLine2 = state.addressLine2,
                    district = state.district,
                    postalCode = state.postalCode,
                    region = state.region
                )
            )
            _uiState.update { it.copy(savedCard = savedCardRepository.getSavedCard()) }
        }
        finishPaymentFlow()
    }

    fun declineSaveCard() {
        finishPaymentFlow()
    }

    private fun finishPaymentFlow() {
        _uiState.update {
            it.copy(
                showSaveCardPrompt = false,
                paymentFlowComplete = true
            )
        }
    }

    private fun validatePaymentForm(state: PaymentUiState): PaymentUiState? {
        val cardCvcError = if (!ValidationUtils.isValidCvc(state.cardCvc)) {
            "CVC inválido"
        } else null

        if (state.useSavedCard) {
            if (cardCvcError != null) {
                return state.copy(cardCvcError = cardCvcError)
            }
            return null
        }

        val cardNumberError = if (!ValidationUtils.isValidCardNumber(state.cardNumber)) {
            "Número de tarjeta inválido"
        } else null
        val cardExpiryError = if (!ValidationUtils.isValidCardExpiry(state.cardExpiry)) {
            "Fecha MM/AA inválida"
        } else null
        val cardholderNameError = if (!ValidationUtils.isValidName(state.cardholderName)) {
            "Nombre del titular requerido"
        } else null
        val addressLine1Error = if (state.addressLine1.isBlank()) {
            "Ingrese la dirección de facturación"
        } else null
        val districtError = if (state.district.isBlank()) {
            "Ingrese el distrito"
        } else null

        if (listOf(
                cardNumberError,
                cardExpiryError,
                cardCvcError,
                cardholderNameError,
                addressLine1Error,
                districtError
            ).any { it != null }
        ) {
            return state.copy(
                cardNumberError = cardNumberError,
                cardExpiryError = cardExpiryError,
                cardCvcError = cardCvcError,
                cardholderNameError = cardholderNameError,
                addressLine1Error = addressLine1Error,
                districtError = districtError
            )
        }
        return null
    }
}
