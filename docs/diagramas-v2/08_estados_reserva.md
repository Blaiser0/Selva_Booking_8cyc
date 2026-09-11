# 8. Estados de reserva

## Máquina de estados

```mermaid
stateDiagram-v2
    direction LR

    [*] --> AWAITING_PAYMENT : UC-11c Crear reserva

    AWAITING_PAYMENT --> CONFIRMADA : UC-11e Pago exitoso\nstock - 1

    CONFIRMADA --> TERMINADA : fechaSalida pasada\nexpireFinishedReservations()\nstock + 1

    state AWAITING_PAYMENT {
        [*] --> Interno
        note right of Interno : No visible en listas\nadmin/gerente/cliente público
    }

    state CONFIRMADA {
        [*] --> Activa
        note right of Activa : holdsStock = true\nVisible en filtros
    }

    state TERMINADA {
        [*] --> Finalizada
        note right of Finalizada : isPublic = true\nStock liberado
    }
```

---

## Visibilidad por rol

| Estado | Cliente (Mis reservas) | Admin / SuperAdmin | Encargado |
|--------|:----------------------:|:------------------:|:---------:|
| AWAITING_PAYMENT | Durante flujo de pago | No | No |
| CONFIRMADA | Sí | Sí (Admin: su red) | Sí (su hotel) |
| TERMINADA | Sí | Sí (Admin: su red) | Sí (su hotel) |

---

## Impacto en stock de habitación

```mermaid
flowchart TD
    subgraph room [Room]
        CANT[cantidad = total físico]
        STK[stock = disponibles ahora]
    end

    CONF[Reserva CONFIRMADA] -->|decrementStock| STK
    TERM[Reserva TERMINADA] -->|incrementStock| STK
    STK --> CHECK{stock > 0 y disponible?}
    CHECK -->|Sí| RESERV[isReservable = true]
    CHECK -->|No| BLOCK[isReservable = false]
```

---

## Transiciones en código

| Transición | Clase / método |
|------------|----------------|
| → AWAITING_PAYMENT | `ReservationRepository.createReservation()` |
| → CONFIRMADA | `ReservationRepository.updateReservationStatus()` + `RoomRepository.decrementStock()` |
| → TERMINADA | `ReservationRepository.expireFinishedReservations()` + `RoomRepository.incrementStock()` |
