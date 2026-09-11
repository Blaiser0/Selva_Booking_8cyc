# 5. Diagrama de clases — Dominio

Modelo principal en `domain/model/`. Utilidades de alcance en `utils/AdminDataScope.kt`.

```mermaid
classDiagram
    direction TB

    class User {
        +String id
        +String nombre
        +String email
        +String telefono
        +String fotoUrl
        +UserRole rol
        +Boolean puedeAlternarRol
        +Boolean perfilCompleto
        +String rolAlternativo
        +String creadoPorAdminId
        +isAdministratorAccount()
        +administratorOwnerId()
        +canSwitchAccountType()
    }

    class UserRole {
        <<enumeration>>
        CLIENTE
        SUPER_ADMIN
        ADMINISTRADOR
        GERENTE_HOTEL
        +hasAdminPanelAccess()
        +hasFullAdminPowers()
    }

    class Hotel {
        +String id
        +String nombre
        +String ciudad
        +String categoria
        +Double precioMinimo
        +Double calificacion
        +Double calificacionBase
        +List~String~ imagenes
        +List~String~ servicios
        +String propietarioId
        +Boolean destacado
        +Boolean oferta
    }

    class Room {
        +String id
        +String hotelId
        +String nombre
        +Double precio
        +Int capacidad
        +Int cantidad
        +Int stock
        +Boolean disponible
        +isReservable()
    }

    class Reservation {
        +String id
        +String userId
        +String hotelId
        +String roomId
        +String fechaIngreso
        +String fechaSalida
        +Int huespedes
        +Double precioTotal
        +ReservationStatus estado
    }

    class ReservationStatus {
        <<enumeration>>
        AWAITING_PAYMENT
        CONFIRMADA
        TERMINADA
        +isPublic()
        +holdsStock()
    }

    class Resena {
        +String id
        +String hotelId
        +String userId
        +String reservationId
        +String userNombre
        +Int calificacion
        +String comentario
        +Long createdAt
    }

    class AuditLog {
        +String id
        +Long timestamp
        +String actorUserId
        +AuditAction action
        +AuditEntityType entityType
        +String entityId
        +Boolean reverted
        +canRevert()
    }

    class AdminDataScope {
        <<utility>>
        +gerentesCreatedBy()
        +hotelsForAdmin()
        +reservationsForAdmin()
        +hotelsWithoutAdministrator()
    }

    User --> UserRole : rol
    User "1" --> "0..*" User : creadoPorAdminId
    Hotel "1" --> "*" Room : contiene
    Hotel "1" --> "*" Reservation : recibe
    Hotel "1" --> "*" Resena : tiene
    Room "1" --> "*" Reservation : reservada en
    User "1" --> "*" Reservation : realiza
    User "1" --> "0..1" Hotel : propietarioId
    User "1" --> "*" Resena : escribe
    User "1" --> "*" AuditLog : actorUserId
    Reservation --> ReservationStatus : estado
```

---

## Relaciones clave

| Relación | Cardinalidad | Campo / regla |
|----------|--------------|---------------|
| Administrador → Encargado | 1..* | `User.creadoPorAdminId` |
| Encargado → Hotel | 0..1 | `Hotel.propietarioId` (máx. 1 hotel) |
| Hotel → Room | 1..* | `Room.hotelId` |
| Cliente → Reservation | 0..* | `Reservation.userId` |
| Room → stock | — | `stock` baja en CONFIRMADA, sube en TERMINADA |
| Encargado → AuditLog | 0..* | Cambios en hotel/habitación |

---

## Enumeración ReservationStatus

```mermaid
stateDiagram-v2
    [*] --> AWAITING_PAYMENT : crear reserva
    AWAITING_PAYMENT --> CONFIRMADA : pago exitoso
    CONFIRMADA --> TERMINADA : fecha salida vencida
    note right of AWAITING_PAYMENT : isPublic = false
    note right of CONFIRMADA : holdsStock = true
    note right of TERMINADA : libera stock
```
