# 6. Arquitectura MVVM

## Diagrama de componentes

```mermaid
flowchart TB
    subgraph presentation [Capa Presentación — ui/]
        MA[MainActivity]
        NG[NavGraph]
        DR[NavigationDrawer]
        SCR[Pantallas Screen.kt]
        CMP[Componentes reutilizables]
    end

    subgraph viewmodel [Capa ViewModel — viewmodel/]
        AVM[AuthViewModel]
        CVM[HomeViewModel · SearchViewModel · HotelDetailViewModel · BookingViewModel · PaymentViewModel · MyReservationsViewModel · ProfileViewModel]
        MVM[ManagerReviewsViewModel]
        ADMVM[AdminDashboardViewModel · AdminHotelsViewModel · AdminReservationsViewModel · AdminGerentesViewModel · AdminAdministradoresViewModel · AdminAuditViewModel · AdminHotelReviewsViewModel]
    end

    subgraph repository [Capa Repository — repository/]
        AR[AuthRepository]
        HR[HotelRepository]
        RR[RoomRepository]
        RESR[ReservationRepository]
        RSR[ResenaRepository]
        SCR2[SavedCardRepository]
    end

    subgraph data [Capa Datos — data/]
        FAS[FirebaseAuthService]
        FS[FirestoreService]
        SS[StorageService]
        SP[(SharedPreferences)]
    end

    subgraph domain [Dominio — domain/model/]
        MODELS[User · Hotel · Room · Reservation · Resena · AuditLog]
    end

    subgraph utils [Utilidades — utils/]
        ADS[AdminDataScope]
        VAL[ValidationUtils]
    end

    subgraph external [Externos]
        FA[(Firebase Auth)]
        FF[(Firestore)]
        FST[(Firebase Storage)]
    end

    MA --> NG
    NG --> SCR
    SCR --> CMP
    SCR --> AVM & CVM & MVM & ADMVM
    AVM & CVM & MVM & ADMVM --> AR & HR & RR & RESR & RSR & SCR2
    ADMVM --> ADS
    AVM & CVM --> VAL
    AR --> FAS & FS
    HR & RR & RESR & RSR --> FS & SS
    SCR2 --> SP
    FAS --> FA
    FS --> FF
    SS --> FST
    AR & HR & RR & RESR & RSR --> MODELS
```

---

## ViewModels por módulo

| Módulo | ViewModel | Pantalla principal |
|--------|-----------|-------------------|
| Auth | `AuthViewModel` | Login, Registro, Recuperar contraseña |
| Cliente | `HomeViewModel`, `SearchViewModel`, `HotelDetailViewModel` | Inicio, búsqueda, detalle |
| Reserva | `BookingViewModel`, `PaymentViewModel` | Reserva, pago |
| Cliente | `MyReservationsViewModel`, `ProfileViewModel` | Mis reservas, perfil |
| Encargado | `AdminHotelsViewModel`*, `AdminReservationsViewModel`*, `ManagerReviewsViewModel` | Mi hotel, reservas, comentarios |
| SuperAdmin / Admin | `AdminDashboardViewModel`, `AdminHotelsViewModel`, `AdminReservationsViewModel`, `AdminGerentesViewModel`, `AdminAdministradoresViewModel`, `AdminAuditViewModel`, `AdminHotelReviewsViewModel` | Panel admin |

\* Reutilizados con alcance distinto según rol (`AdminDataScope` para administrador limitado).

---

## AdminDataScope — filtrado por red

```mermaid
flowchart LR
    VM[Admin*ViewModel] --> ADS[AdminDataScope]
    ADS -->|gerentesCreatedBy| ENC[Encargados creadoPorAdminId]
    ADS -->|hotelsForAdmin| HOT[Hoteles de esos encargados]
    ADS -->|reservationsForAdmin| RES[Reservas de esos hoteles]
    ADS -->|hotelsWithoutAdministrator| SIN[Hoteles sin admin vinculado]
    VM --> AR[AuthRepository.getAdministratorAccountsFlow]
    VM --> HR[HotelRepository / FirestoreService]
```

---

## Diagrama de paquetes

```mermaid
flowchart LR
    subgraph app [com.company.selvabooking]
        UI[ui.*]
        VM[viewmodel.*]
        REPO[repository.*]
        DATA[data.*]
        DOM[domain.model.*]
        NAV[navigation.*]
        UTIL[utils.*]
    end

    UI --> VM
    VM --> REPO
    VM --> UTIL
    REPO --> DATA
    REPO --> DOM
    DATA --> DOM
    UI --> NAV
```

---

## Flujo de datos (unidireccional)

```mermaid
sequenceDiagram
    participant S as Screen
    participant VM as ViewModel
    participant R as Repository
    participant F as FirestoreService

    S->>VM: acción usuario
    VM->>R: suspend / Flow
    R->>F: CRUD
    F-->>R: datos
    R-->>VM: Result / List
    VM-->>S: UiState via StateFlow
```

---

## SelvaBookingApplication

```mermaid
flowchart LR
    APP[SelvaBookingApplication] --> AR[authRepository]
    APP --> HR[hotelRepository]
    APP --> RR[roomRepository]
    APP --> RESR[reservationRepository]
    APP --> RSR[resenaRepository]
    APP --> SCR[savedCardRepository]

    VM[ViewModels] --> APP
```
