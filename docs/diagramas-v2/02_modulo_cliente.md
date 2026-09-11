# 2. Módulo Cliente

## Diagrama de casos de uso — Cliente

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]
    PAY[[Pasarela simulada]]
    LOCAL[[SharedPreferences]]

    subgraph exploracion [Exploración]
        UC10[UC-10 Ver inicio]
        UC10A[UC-10a Buscar hoteles]
        UC10B[UC-10b Filtrar y ordenar]
        UC10C[UC-10c Ver detalle hotel]
        UC10D[UC-10d Ver habitaciones disponibles]
    end

    subgraph reserva [Reserva y pago]
        UC11[UC-11 Completar reserva]
        UC11A[UC-11a Validar fechas y huéspedes]
        UC11B[UC-11b Calcular precio]
        UC11C[UC-11c Crear reserva Pendiente de pago]
        UC11D[UC-11d Realizar pago]
        UC11E[UC-11e Confirmar reserva y bajar stock]
    end

    subgraph misReservas [Mis reservas]
        UC12[UC-12 Ver mis reservas]
        UC12A[UC-12a Filtrar por estado]
    end

    subgraph resenas [Reseñas]
        UC13[UC-13 Comentar hotel]
        UC13A[UC-13a Validar reserva previa]
        UC13B[UC-13b Recalcular calificación]
    end

    subgraph cuenta [Mi cuenta]
        UC14[UC-14 Ver y editar perfil]
        UC14T[UC-14t Editar teléfono 9 dígitos]
        UC14A[UC-14a Alternar a modo encargado o admin]
        UC14B[UC-14b Gestionar tarjeta guardada]
    end

    CLI --> UC10 & UC10A & UC10C & UC11 & UC11D & UC12 & UC13 & UC14
    UC10B -.->|extend| UC10A
    UC10D -->|include| UC10C
    UC11 -->|include| UC11A & UC11B & UC11C
    UC11D -->|include| UC11E
    UC12A -.->|extend| UC12
    UC13 -->|include| UC13A & UC13B
    UC14 -->|include| UC14T
    UC14A -.->|extend| UC14

    UC10 & UC10C & UC11C & UC11E & UC12 & UC13 & UC14 --> FB
    UC11D --> PAY
    UC14B --> LOCAL
```

---

## Flujo principal del cliente

```mermaid
flowchart LR
    A[Inicio / Buscar] --> B[Detalle hotel]
    B --> C[Seleccionar habitación]
    C --> D[Completar fechas]
    D --> E[Pantalla de pago]
    E --> F{Pago exitoso?}
    F -->|Sí| G[Reserva Confirmada]
    G --> H[Stock -1]
    F -->|No| E
    G --> I[Mis reservas]
    B --> J[Comentar hotel]
```

---

## Tabla de casos de uso

| ID | Caso de uso | Descripción | Pantalla / ruta |
|----|-------------|-------------|-----------------|
| UC-10 | Ver inicio | Hoteles destacados y ofertas | `CLIENT_HOME` |
| UC-10a | Buscar hoteles | Búsqueda por ciudad, precio, categoría | `CLIENT_SEARCH` |
| UC-10b | Filtrar y ordenar | Filtros en pantalla de búsqueda | `CLIENT_SEARCH` |
| UC-10c | Ver detalle hotel | Galería, servicios, calificación | `HOTEL_DETAIL/{id}` |
| UC-10d | Ver habitaciones | Solo tipos con stock > 0 | `HOTEL_DETAIL/{id}` |
| UC-11 | Completar reserva | Fechas, huéspedes, habitación | `BOOKING/{hotelId}/{roomId}` |
| UC-11c | Crear reserva | Estado interno `AWAITING_PAYMENT` | `BOOKING` → `PAYMENT` |
| UC-11d | Realizar pago | Formulario tarjeta simulado | `PAYMENT/{reservationId}` |
| UC-11e | Confirmar reserva | Pasa a `CONFIRMADA`, decrementa stock | `PaymentViewModel` |
| UC-12 | Ver mis reservas | Listado en tiempo real | `CLIENT_RESERVATIONS` |
| UC-13 | Comentar hotel | Requiere al menos 1 reserva en el hotel | `HOTEL_DETAIL` |
| UC-14 | Mi cuenta | Perfil, foto, teléfono | `CLIENT_PROFILE` |
| UC-14a | Alternar rol | Encargado / Admin / SuperAdmin ↔ Cliente | `ProfileScreen` |

---

## Registro previo (Invitado → Cliente)

El registro (UC-02) ocurre antes del módulo cliente pero define el perfil inicial:

| Campo | Validación |
|-------|------------|
| Nombre | Solo letras y espacios, mín. 3 |
| Teléfono | Solo dígitos, exactamente 9 |
| Email / contraseña | Estándar Firebase Auth |
