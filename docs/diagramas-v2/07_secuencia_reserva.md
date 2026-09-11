# 7. Diagrama de secuencia — Reserva y pago

## Flujo completo: reservar habitación

```mermaid
sequenceDiagram
    actor C as Cliente
    participant BS as BookingScreen
    participant BVM as BookingViewModel
    participant PS as PaymentScreen
    participant PVM as PaymentViewModel
    participant RES as ReservationRepository
    participant ROOM as RoomRepository
    participant FS as FirestoreService

    C->>BS: Selecciona fechas y huéspedes
    BS->>BVM: confirmBooking()
    BVM->>RES: createReservation()
    RES->>FS: guardar AWAITING_PAYMENT
    FS-->>RES: reservationId
    RES-->>BVM: OK
    BVM-->>BS: navegar a Payment

    C->>PS: Ingresa datos tarjeta
    PS->>PVM: processPayment()
    PVM->>RES: updateReservationStatus(CONFIRMADA)
    RES->>ROOM: decrementStock(roomId)
    ROOM->>FS: actualizar stock
    RES->>FS: actualizar estado reserva
    FS-->>PVM: OK
    PVM-->>PS: éxito → Mis Reservas
```

---

## Expiración automática de reservas

```mermaid
sequenceDiagram
    participant VM as ViewModel / init
    participant RES as ReservationRepository
    participant ROOM as RoomRepository
    participant FS as FirestoreService

    VM->>RES: expireFinishedReservations()
    RES->>FS: getAllReservations()
    FS-->>RES: lista
    loop por cada CONFIRMADA vencida
        RES->>RES: estado = TERMINADA
        RES->>ROOM: incrementStock(roomId)
        ROOM->>FS: actualizar stock
        RES->>FS: actualizar reserva
    end
```

---

## Admin consulta hotel y habitaciones

```mermaid
sequenceDiagram
    actor A as Administrador
    participant HS as AdminHotelsScreen
    participant HVM as AdminHotelsViewModel
    participant ADS as AdminDataScope
    participant AR as AuthRepository
    participant HR as HotelRepository
    participant RS as AdminRoomsScreen
    participant RVM as AdminRoomsViewModel

    A->>HS: Ver lista hoteles
    HVM->>HR: getHotelsFlow()
    HVM->>AR: getGerentesHotelFlow()
    HR-->>HVM: todos los hoteles
    AR-->>HVM: gerentes
    HVM->>ADS: hotelsForAdmin(adminId, gerentes, hoteles)
    ADS-->>HVM: hoteles de su red
    HS-->>A: hotel + encargado · sin encargado

    A->>HS: Tocar hotel
    HS->>RS: navigate ADMIN_ROOMS
    RVM->>HR: getHotel(hotelId)
    RVM->>RVM: readOnly = true (Admin limitado)
    RVM->>RVM: getRoomsByHotelFlow()
    RS-->>A: habitaciones solo lectura
```

---

## SuperAdmin ve comentarios por hotel

```mermaid
sequenceDiagram
    actor SA as SuperAdmin
    participant HS as AdminHotelsScreen
    participant HVM as AdminHotelsViewModel
    participant RS as HotelReviewsScreen
    participant RVM as AdminHotelReviewsViewModel
    participant RSR as ResenaRepository

    SA->>HS: Tocar ⭐ en tarjeta hotel
    HS->>RS: navigate ADMIN_HOTEL_REVIEWS/{hotelId}
    RVM->>RSR: getResenasByHotelFlow(hotelId)
    RSR-->>RVM: comentarios + calificación
    RS-->>SA: listado en tiempo real
```

---

## Encargado ve comentarios de su hotel

```mermaid
sequenceDiagram
    actor E as Encargado
    participant MS as ManagerReviewsScreen
    participant MVM as ManagerReviewsViewModel
    participant AR as AuthRepository
    participant RSR as ResenaRepository

    E->>MS: Abrir Comentarios
    MVM->>AR: getHotelsByOwnerFlow(userId)
    AR-->>MVM: hotel propio
    MVM->>RSR: getResenasByHotelFlow(hotelId)
    RSR-->>MVM: comentarios
    MS-->>E: listado en tiempo real
```
