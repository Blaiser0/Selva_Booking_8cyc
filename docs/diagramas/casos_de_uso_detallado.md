# Diagrama de Casos de Uso — Selva Booking

**Proyecto:** Selva Booking · Android · Kotlin · Firebase  
**Fuente:** Elaboración propia  
**Total:** 66 casos de uso · 5 actores · 4 módulos

> Abre este archivo en **Preview** (`Ctrl+Shift+V`) para ver los diagramas Mermaid.

---

## Índice

1. [Actores](#1-actores)
2. [Diagrama general](#2-diagrama-general)
3. [Módulo 1 — Autenticación](#3-módulo-1--autenticación-uc-01-a-uc-10)
4. [Módulo 2 — Cliente](#4-módulo-2--cliente)
5. [Módulo 3 — Administrador](#5-módulo-3--administrador)
6. [Relaciones include / extend](#6-relaciones-include--extend)
7. [Matriz actor × módulo](#7-matriz-actor--módulo)

---

## 1. Actores

| Actor | Tipo | Descripción |
|-------|------|-------------|
| **Invitado** | Primario | Sin sesión. Splash, login, registro, recuperar contraseña. |
| **Cliente** | Primario | Rol `CLIENTE`. Busca, reserva, paga y gestiona perfil. |
| **Administrador** | Primario | Rol `ADMINISTRADOR`. Gestiona catálogo, reservas y solicitudes. |
| **Firebase** | Secundario | Auth, Firestore y Storage. |
| **Pasarela simulada** | Secundario | Validación local de tarjeta (sin procesador real). |

---

## 2. Diagrama general

Vista resumida de los casos de uso principales por actor.

```mermaid
flowchart TB
    subgraph actores [Actores]
        INV((Invitado))
        CLI((Cliente))
        ADM((Administrador))
    end

    subgraph acceso [Acceso al sistema]
        UC02[UC-02 Iniciar sesion]
        UC03[UC-03 Registrarse]
        UC04[UC-04 Recuperar contraseña]
        UC05[UC-05 Cerrar sesion]
    end

    subgraph modCliente [Modulo Cliente]
        UC12[UC-12 Buscar hoteles]
        UC15[UC-15 Ver detalle hotel]
        UC18[UC-18 Completar reserva]
        UC22[UC-22 Realizar pago]
        UC30[UC-30 Mis reservas]
        UC33[UC-33 Mi cuenta]
    end

    subgraph modAdmin [Modulo Administrador]
        UC38[UC-38 Dashboard]
        UC41[UC-41 Solicitudes admin]
        UC44[UC-44 Gestionar hoteles]
        UC50[UC-50 Gestionar habitaciones]
        UC56[UC-56 Gestionar reservas]
    end

    subgraph compartido [Compartido]
        UC66[UC-66 Soporte y FAQ]
    end

    INV --> UC02 & UC03 & UC04
    CLI --> UC05 & UC12 & UC15 & UC18 & UC22 & UC30 & UC33 & UC66
    ADM --> UC05 & UC38 & UC41 & UC44 & UC50 & UC56 & UC33 & UC66

    UC15 -.->|extend| UC12
    UC18 -.->|extend| UC15
    UC22 -.->|extend| UC18
    UC30 -.->|extend| UC22
    UC50 -.->|extend| UC44
    UC56 -.->|extend| UC38
```

| ID | Caso de uso | Actor |
|----|-------------|-------|
| UC-02 | Iniciar sesión | Invitado |
| UC-03 | Registrarse | Invitado |
| UC-04 | Recuperar contraseña | Invitado |
| UC-05 | Cerrar sesión | Cliente, Administrador |
| UC-12 | Buscar hoteles | Cliente |
| UC-15 | Ver detalle de hotel | Cliente |
| UC-18 | Completar reserva | Cliente |
| UC-22 | Realizar pago | Cliente |
| UC-30 | Mis reservas | Cliente |
| UC-33 | Mi cuenta | Cliente, Administrador |
| UC-38 | Dashboard | Administrador |
| UC-41 | Solicitudes admin | Administrador |
| UC-44 | Gestionar hoteles | Administrador |
| UC-50 | Gestionar habitaciones | Administrador |
| UC-56 | Gestionar reservas | Administrador |
| UC-66 | Soporte y FAQ | Cliente, Administrador |

---

## 3. Módulo 1 — Autenticación (UC-01 a UC-10)

```mermaid
flowchart TB
    subgraph actores1 [Actores]
        INV((Invitado))
        USR((Cliente / Admin))
        FB[[Firebase Auth]]
    end

    subgraph auth [Autenticacion y sesion]
        UC01[UC-01 Visualizar splash]
        UC02[UC-02 Iniciar sesion]
        UC03[UC-03 Registrarse]
        UC04[UC-04 Recuperar contraseña]
        UC05[UC-05 Cerrar sesion]
        UC06[UC-06 Restaurar sesion]
    end

    subgraph subAuth [Subcasos incluidos]
        UC07[UC-07 Validar credenciales]
        UC08[UC-08 Aceptar terminos]
        UC09[UC-09 Validar registro]
        UC10[UC-10 Enviar correo reset]
    end

    INV --> UC01 & UC02 & UC03 & UC04
    USR --> UC05

    UC01 -->|include| UC06
    UC02 -->|include| UC07
    UC03 -->|include| UC07 & UC08 & UC09
    UC04 -->|include| UC10

    UC02 & UC03 & UC04 & UC05 & UC06 --> FB
```

| ID | Caso de uso | Actor | Descripción |
|----|-------------|-------|-------------|
| UC-01 | Visualizar splash | Invitado | Pantalla de bienvenida al abrir la app. |
| UC-02 | Iniciar sesión | Invitado | Acceso con email y contraseña. |
| UC-03 | Registrarse | Invitado | Creación de cuenta nueva. |
| UC-04 | Recuperar contraseña | Invitado | Enlace de restablecimiento por email. |
| UC-05 | Cerrar sesión | Cliente, Admin | Cierre de sesión Firebase. |
| UC-06 | Restaurar sesión | Sistema | Redirección si hay sesión activa. |
| UC-07 | Validar credenciales | Sistema | Validación de email y contraseña. |
| UC-08 | Aceptar términos | Invitado | Lectura obligatoria antes de registrarse. |
| UC-09 | Validar registro | Sistema | Validación de campos del formulario. |
| UC-10 | Enviar correo reset | Sistema | Email vía Firebase Auth. |

---

## 4. Módulo 2 — Cliente

### 4.1 Exploración (UC-11 a UC-17)

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]

    subgraph exploracion [A. Exploracion]
        UC11[UC-11 Ver inicio]
        UC12[UC-12 Buscar hoteles]
        UC13[UC-13 Filtrar]
        UC14[UC-14 Ordenar]
        UC15[UC-15 Ver detalle hotel]
        UC16[UC-16 Ver galeria]
        UC17[UC-17 Ver habitaciones]
    end

    CLI --> UC11 & UC12 & UC15
    UC13 & UC14 -.->|extend| UC12
    UC16 -.->|extend| UC15
    UC15 -->|include| UC17
    UC11 & UC12 & UC15 --> FB
```

| ID | Caso de uso | Descripción |
|----|-------------|-------------|
| UC-11 | Ver inicio | Destacados, ofertas y recomendados. |
| UC-12 | Buscar hoteles | Búsqueda con filtros y ordenamiento. |
| UC-13 | Filtrar | Ciudad, precio máximo, estrellas. |
| UC-14 | Ordenar | Recomendados, precio o valoración. |
| UC-15 | Ver detalle hotel | Info, galería y habitaciones. |
| UC-16 | Ver galería | Imágenes del hotel. |
| UC-17 | Ver habitaciones | Habitaciones disponibles. |

### 4.2 Reserva y pago (UC-18 a UC-32)

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]
    PAY[[Pasarela simulada]]

    subgraph reserva [B. Reserva y pago]
        UC18[UC-18 Completar reserva]
        UC19[UC-19 Validar fechas]
        UC20[UC-20 Calcular precio]
        UC21[UC-21 Crear reserva Pendiente]
        UC22[UC-22 Realizar pago]
        UC23[UC-23 Validar tarjeta]
        UC24[UC-24 Confirmar reserva]
        UC25[UC-25 Resumen de pago]
        UC26[UC-26 Dir. facturacion]
        UC27[UC-27 Guardar tarjeta]
        UC28[UC-28 Tarjeta guardada]
        UC29[UC-29 Tarjeta alternativa]
        UC30[UC-30 Ver mis reservas]
        UC31[UC-31 Filtrar reservas]
        UC32[UC-32 Cancelar reserva]
    end

    CLI --> UC18 & UC22 & UC30

    UC18 -->|include| UC19 & UC20 & UC21
    UC22 -->|include| UC23 & UC24 & UC25 & UC26
    UC27 & UC28 & UC29 -.->|extend| UC22
    UC30 -->|include| UC31
    UC32 -.->|extend| UC30

    UC21 & UC24 & UC30 & UC32 --> FB
    UC22 & UC27 --> PAY
```

**Flujo principal**

```mermaid
flowchart LR
    A[Inicio / Buscar] --> B[Detalle hotel]
    B --> C[Completar reserva]
    C --> D[Realizar pago]
    D --> E{Guardar tarjeta?}
    E -->|Si| F[Tarjeta guardada]
    E -->|No| G[Mis reservas]
    F --> G
```

| ID | Caso de uso | Descripción |
|----|-------------|-------------|
| UC-18 | Completar reserva | Fechas y huéspedes. |
| UC-19 | Validar fechas | Fechas y capacidad. |
| UC-20 | Calcular precio | Noches × precio. |
| UC-21 | Crear reserva | Estado **Pendiente**. |
| UC-22 | Realizar pago | Pasarela simulada. |
| UC-23 | Validar tarjeta | Número, vencimiento, CVC. |
| UC-24 | Confirmar reserva | Estado **Confirmada**. |
| UC-25 | Resumen de pago | Hotel, fechas, total. |
| UC-26 | Dir. facturación | Datos de facturación. |
| UC-27 | Guardar tarjeta | Post-pago, futuras reservas. |
| UC-28 | Tarjeta guardada | Pago solo con CVC. |
| UC-29 | Tarjeta alternativa | Nueva tarjeta. |
| UC-30 | Ver mis reservas | Listado personal. |
| UC-31 | Filtrar reservas | Por estado. |
| UC-32 | Cancelar reserva | Cancelación cliente. |

### 4.3 Mi cuenta y soporte (UC-33 a UC-37, UC-66)

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]

    subgraph cuenta [C. Mi cuenta]
        UC33[UC-33 Ver perfil]
        UC34[UC-34 Editar nombre]
        UC35[UC-35 Subir foto]
        UC36[UC-36 Solicitar admin]
        UC37[UC-37 Modo administrador]
    end

    subgraph soporte [D. Soporte]
        UC66[UC-66 Soporte y FAQ]
    end

    CLI --> UC33 & UC36 & UC66
    UC34 & UC35 & UC37 -.->|extend| UC33
    UC33 & UC35 & UC36 --> FB
```

| ID | Caso de uso | Descripción |
|----|-------------|-------------|
| UC-33 | Ver perfil | Nombre, email, foto, rol. |
| UC-34 | Editar nombre | Actualizar en Firestore. |
| UC-35 | Subir foto | Firebase Storage. |
| UC-36 | Solicitar admin | Solicitud de acceso admin. |
| UC-37 | Modo administrador | Si `puedeAlternarRol`. |
| UC-66 | Soporte y FAQ | Preguntas frecuentes. |

---

## 5. Módulo 3 — Administrador

### 5.1 Panel y solicitudes (UC-38 a UC-43)

```mermaid
flowchart TB
    ADM((Administrador))
    FB[[Firebase]]

    subgraph panel [A. Panel]
        UC38[UC-38 Ver dashboard]
        UC39[UC-39 Alerta solicitudes]
        UC40[UC-40 Sembrar datos ejemplo]
    end

    subgraph solicitudes [B. Solicitudes admin]
        UC41[UC-41 Listar solicitudes]
        UC42[UC-42 Aprobar solicitud]
        UC43[UC-43 Rechazar solicitud]
    end

    ADM --> UC38 & UC41
    UC39 -.->|extend| UC38
    UC42 & UC43 -.->|extend| UC41
    UC38 & UC40 & UC41 & UC42 & UC43 --> FB
```

### 5.2 Hoteles y habitaciones (UC-44 a UC-55)

```mermaid
flowchart TB
    ADM((Administrador))
    FB[[Firebase]]

    subgraph hoteles [C. Hoteles]
        UC44[UC-44 Listar hoteles]
        UC45[UC-45 Registrar hotel]
        UC46[UC-46 Editar hotel]
        UC47[UC-47 Eliminar hotel]
        UC48[UC-48 Imagenes hotel]
        UC49[UC-49 Ir a habitaciones]
    end

    subgraph habitaciones [D. Habitaciones]
        UC50[UC-50 Listar habitaciones]
        UC51[UC-51 Registrar habitacion]
        UC52[UC-52 Editar habitacion]
        UC53[UC-53 Eliminar habitacion]
        UC54[UC-54 Imagenes habitacion]
        UC55[UC-55 Sync precio minimo]
    end

    ADM --> UC44 & UC49 & UC50
    UC45 & UC46 -->|include| UC48
    UC51 & UC52 -->|include| UC54
    UC52 & UC53 -->|include| UC55
    UC49 -.->|extend| UC44
    UC50 -.->|extend| UC49
    UC44 & UC50 --> FB
```

### 5.3 Reservas, cuenta y soporte (UC-56 a UC-65, UC-66)

```mermaid
flowchart TB
    ADM((Administrador))
    FB[[Firebase]]

    subgraph reservas [E. Reservas admin]
        UC56[UC-56 Listar reservas]
        UC57[UC-57 Buscar reservas]
        UC58[UC-58 Ver detalle]
        UC59[UC-59 Crear reserva manual]
        UC60[UC-60 Editar reserva]
        UC61[UC-61 Eliminar reserva]
        UC62[UC-62 Confirmar]
        UC63[UC-63 Cancelar]
        UC64[UC-64 Completar]
    end

    subgraph cuentaAdmin [F. Cuenta y soporte]
        UC33[UC-33 Ver perfil]
        UC65[UC-65 Modo cliente]
        UC66[UC-66 Soporte y FAQ]
    end

    ADM --> UC56 & UC59 & UC60 & UC61 & UC33 & UC66
    UC57 & UC58 -.->|extend| UC56
    UC62 & UC63 & UC64 -.->|extend| UC58
    UC65 -.->|extend| UC33
    UC56 & UC59 & UC60 & UC61 & UC62 & UC63 & UC64 --> FB
```

| Sección | IDs | Casos principales |
|---------|-----|-------------------|
| Panel | UC-38 a UC-40 | Dashboard, alertas, datos ejemplo |
| Solicitudes | UC-41 a UC-43 | Aprobar / rechazar acceso admin |
| Hoteles | UC-44 a UC-49 | CRUD + imágenes + navegar a habitaciones |
| Habitaciones | UC-50 a UC-55 | CRUD + imágenes + sync precio mínimo |
| Reservas | UC-56 a UC-64 | CRUD + confirmar / cancelar / completar |
| Cuenta | UC-33, UC-65, UC-66 | Perfil, alternar rol, soporte |

---

## 6. Relaciones include / extend

### Include (obligatorio)

| Caso base | Incluye |
|-----------|---------|
| UC-01 | UC-06 |
| UC-02, UC-03 | UC-07 |
| UC-03 | UC-08, UC-09 |
| UC-04 | UC-10 |
| UC-15 | UC-17 |
| UC-18 | UC-19, UC-20, UC-21 |
| UC-22 | UC-23, UC-24, UC-25, UC-26 |
| UC-30 | UC-31 |
| UC-45, UC-46 | UC-48 |
| UC-51, UC-52 | UC-54 |
| UC-52, UC-53 | UC-55 |

### Extend (opcional)

| Caso base | Extiende |
|-----------|----------|
| UC-12 | UC-13, UC-14 |
| UC-15 | UC-16 |
| UC-22 | UC-27, UC-28, UC-29 |
| UC-30 | UC-32 |
| UC-33 | UC-34, UC-35, UC-37, UC-65 |
| UC-38 | UC-39 |
| UC-41 | UC-42, UC-43 |
| UC-44 | UC-49 |
| UC-49 | UC-50 |
| UC-56 | UC-57, UC-58 |
| UC-58 | UC-62, UC-63, UC-64 |

---

## 7. Matriz actor × módulo

| Módulo | Invitado | Cliente | Administrador |
|--------|:--------:|:-------:|:-------------:|
| Autenticación | ●●●● | ● | ● |
| Exploración | — | ●●● | — |
| Reserva y pago | — | ●●●● | — |
| Mi cuenta | — | ●●● | — |
| Panel admin | — | — | ●● |
| Solicitudes | — | ○ | ●●● |
| Hoteles / Habitaciones | — | — | ●●●● |
| Reservas admin | — | — | ●●●● |
| Soporte | — | ● | ● |

● acceso directo · ○ indirecto · — no aplica

---

## Archivos relacionados

| Archivo | Uso |
|---------|-----|
| `casos_de_uso_detallado.md` | Este documento (preview Mermaid) |
| `casos_de_uso_detallado.puml` | PlantUML para exportar PNG/PDF |
| `casos_de_uso_00_general.png` | Imagen del diagrama general |
| `casos_de_uso_01_autenticacion.png` | Imagen módulo autenticación |
| `casos_de_uso_02_cliente.png` | Imagen módulo cliente |
| `casos_de_uso_03_administrador.png` | Imagen módulo administrador |

---

*Fuente: Elaboración propia — Proyecto Selva Booking*
