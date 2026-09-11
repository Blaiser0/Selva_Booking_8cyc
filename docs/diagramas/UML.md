# Selva Booking — Diagramas UML (v1 — histórico)

> **Documentación actualizada:** usa [`docs/diagramas-v2/`](../diagramas-v2/00_INDICE.md) (Jul 2026).  
> Incluye SuperAdmin, Administrador limitado, `creadoPorAdminId`, comentarios, auditoría y validaciones de registro.

**Proyecto:** Selva Booking · Android · Kotlin · Jetpack Compose · Firebase  
**Fuente:** Elaboración propia  
**Total:** 70 casos de uso · 5 actores · 4 módulos  
**Versión documento:** 2026 (v1 — conservado como referencia)

> Abre este archivo con **Preview** (`Ctrl + Shift + V`) para ver todos los diagramas Mermaid.

---

## Índice

1. [Leyenda UML](#1-leyenda-uml)
2. [Actores](#2-actores)
3. [Diagrama general](#3-diagrama-general)
4. [Módulo Autenticación (UC-01 – UC-10)](#4-módulo-autenticación-uc-01--uc-10)
5. [Módulo Cliente (UC-11 – UC-37)](#5-módulo-cliente-uc-11--uc-37)
6. [Módulo Cliente — Reseñas (UC-67 – UC-69)](#6-módulo-cliente--reseñas-uc-67--uc-69)
7. [Módulo Administrador (UC-38 – UC-65)](#7-módulo-administrador-uc-38--uc-65)
8. [Diagrama UML clásico — Cliente](#8-diagrama-uml-clásico--cliente)
9. [Diagrama UML clásico — Administrador](#9-diagrama-uml-clásico--administrador)
10. [Relaciones include / extend](#10-relaciones-include--extend)
11. [Matriz actor × módulo](#11-matriz-actor--módulo)
12. [Archivos PlantUML](#12-archivos-plantuml)

---

## 1. Leyenda UML

| Símbolo | Significado |
|---------|-------------|
| `(Actor)` | Actor humano o sistema externo |
| `[Casos de uso]` | Óvalo / nodo de caso de uso |
| `<<include>>` | Subproceso **obligatorio** |
| `<<extend>>` | Flujo **opcional** |
| Frontera del sistema | Rectángulo que delimita Selva Booking |
| Paquete interno | Agrupación temática de casos |
| Hub central | Caso de uso principal que especializa el módulo |

---

## 2. Actores

| Actor | Tipo | Descripción |
|-------|------|-------------|
| **Invitado** | Primario | Sin sesión. Splash, login, registro, recuperar contraseña. |
| **Cliente** | Primario | Rol `CLIENTE`. Busca, reserva, paga, comenta y gestiona perfil. |
| **Administrador** | Primario | Rol `ADMINISTRADOR`. Gestiona catálogo, reservas y solicitudes. |
| **Firebase** | Secundario | Auth, Firestore y Storage. |
| **Pasarela simulada** | Secundario | Validación local de tarjeta (sin procesador real). |

---

## 3. Diagrama general

```mermaid
flowchart TB
    subgraph actores [Actores]
        INV((Invitado))
        CLI((Cliente))
        ADM((Administrador))
        FB[[Firebase]]
        PAY[[Pasarela simulada]]
    end

    subgraph sistema [Sistema Selva Booking]
        subgraph acceso [Acceso al sistema]
            UC02[UC-02 Iniciar sesión]
            UC03[UC-03 Registrarse]
            UC04[UC-04 Recuperar contraseña]
            UC05[UC-05 Cerrar sesión]
        end

        subgraph modCliente [Módulo Cliente]
            UC12[UC-12 Buscar hoteles]
            UC15[UC-15 Ver detalle hotel]
            UC18[UC-18 Completar reserva]
            UC22[UC-22 Realizar pago]
            UC30[UC-30 Mis reservas]
            UC33[UC-33 Mi cuenta]
            UC67[UC-67 Comentar hotel]
        end

        subgraph modAdmin [Módulo Administrador]
            UC38[UC-38 Dashboard]
            UC41[UC-41 Solicitudes admin]
            UC44[UC-44 Gestionar hoteles]
            UC50[UC-50 Gestionar habitaciones]
            UC56[UC-56 Gestionar reservas]
        end

        subgraph compartido [Compartido]
            UC66[UC-66 Soporte y FAQ]
        end
    end

    INV --> UC02 & UC03 & UC04
    CLI --> UC05 & UC12 & UC15 & UC18 & UC22 & UC30 & UC33 & UC67 & UC66
    ADM --> UC05 & UC38 & UC41 & UC44 & UC50 & UC56 & UC33 & UC66

    UC15 -.->|extend| UC12
    UC18 -.->|extend| UC15
    UC22 -.->|extend| UC18
    UC67 -.->|extend| UC15
    UC30 -.->|extend| UC22
    UC50 -.->|extend| UC44
    UC56 -.->|extend| UC38

    UC02 & UC03 & UC18 & UC30 & UC44 & UC56 --> FB
    UC22 --> PAY
```

---

## 4. Módulo Autenticación (UC-01 – UC-10)

```mermaid
flowchart TB
    subgraph actoresAuth [Actores]
        INV((Invitado))
        USR((Cliente / Admin))
        FB[[Firebase Auth]]
    end

    subgraph auth [Autenticación y sesión]
        UC01[UC-01 Visualizar splash]
        UC02[UC-02 Iniciar sesión]
        UC03[UC-03 Registrarse]
        UC04[UC-04 Recuperar contraseña]
        UC05[UC-05 Cerrar sesión]
        UC06[UC-06 Restaurar sesión]
    end

    subgraph subAuth [Subcasos incluidos]
        UC07[UC-07 Validar credenciales]
        UC08[UC-08 Aceptar términos]
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

## 5. Módulo Cliente (UC-11 – UC-37)

### 5.1 Exploración (UC-11 – UC-17)

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]

    subgraph exploracion [Exploración]
        UC11[UC-11 Ver inicio]
        UC12[UC-12 Buscar hoteles]
        UC13[UC-13 Filtrar]
        UC14[UC-14 Ordenar]
        UC15[UC-15 Ver detalle hotel]
        UC16[UC-16 Ver galería]
        UC17[UC-17 Ver habitaciones]
    end

    CLI --> UC11 & UC12 & UC15
    UC13 & UC14 -.->|extend| UC12
    UC16 -.->|extend| UC15
    UC15 -->|include| UC17
    UC11 & UC12 & UC15 --> FB
```

### 5.2 Reserva y pago (UC-18 – UC-32)

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]
    PAY[[Pasarela simulada]]

    subgraph reserva [Reserva y pago]
        UC18[UC-18 Completar reserva]
        UC19[UC-19 Validar fechas]
        UC20[UC-20 Calcular precio]
        UC21[UC-21 Crear reserva Pendiente]
        UC22[UC-22 Realizar pago]
        UC23[UC-23 Validar tarjeta]
        UC24[UC-24 Confirmar reserva]
        UC25[UC-25 Resumen de pago]
        UC26[UC-26 Dir. facturación]
        UC27[UC-27 Guardar tarjeta]
        UC28[UC-28 Usar tarjeta guardada]
        UC29[UC-29 Usar otra tarjeta]
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

**Flujo principal cliente**

```mermaid
flowchart LR
    A[Inicio / Buscar] --> B[Detalle hotel]
    B --> C[Completar reserva]
    C --> D[Realizar pago]
    D --> E{Guardar tarjeta?}
    E -->|Sí| F[Tarjeta guardada]
    E -->|No| G[Mis reservas]
    F --> G
    B --> H[Comentar hotel]
```

| ID | Caso de uso | Descripción |
|----|-------------|-------------|
| UC-18 | Completar reserva | Fechas y huéspedes. |
| UC-19 | Validar fechas | Fechas y capacidad. |
| UC-20 | Calcular precio | Noches × precio. |
| UC-21 | Crear reserva | Estado **Pendiente** en Firestore. |
| UC-22 | Realizar pago | Pasarela simulada. |
| UC-23 | Validar tarjeta | 16 dígitos, MM/AA, CVC, facturación. |
| UC-24 | Confirmar reserva | Estado **Confirmada**. |
| UC-25 | Resumen de pago | Hotel, fechas, total. |
| UC-26 | Dir. facturación | Distrito Madre de Dios y código postal. |
| UC-27 | Guardar tarjeta | Post-pago, almacenamiento local. |
| UC-28 | Usar tarjeta guardada | Pago solo con CVC. |
| UC-29 | Usar otra tarjeta | Formulario completo alternativo. |
| UC-30 | Ver mis reservas | Listado personal en tiempo real. |
| UC-31 | Filtrar reservas | Por estado. |
| UC-32 | Cancelar reserva | Cancelación del cliente. |

### 5.3 Mi cuenta, método de pago y soporte (UC-33 – UC-37, UC-66, UC-70)

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]
    LOCAL[[SharedPreferences]]

    subgraph cuenta [Mi cuenta]
        UC33[UC-33 Ver perfil]
        UC34[UC-34 Editar nombre]
        UC35[UC-35 Subir foto]
        UC36[UC-36 Solicitar admin]
        UC37[UC-37 Modo administrador]
        UC70[UC-70 Gestionar método de pago]
    end

    subgraph soporte [Soporte]
        UC66[UC-66 Soporte y FAQ]
    end

    CLI --> UC33 & UC36 & UC70 & UC66
    UC34 & UC35 & UC37 -.->|extend| UC33
    UC33 & UC35 & UC36 --> FB
    UC70 --> LOCAL
```

| ID | Caso de uso | Descripción |
|----|-------------|-------------|
| UC-33 | Ver perfil | Nombre, email, foto, rol. |
| UC-34 | Editar nombre | Actualizar en Firestore. |
| UC-35 | Subir foto | Firebase Storage. |
| UC-36 | Solicitar admin | Solicitud de acceso admin. |
| UC-37 | Modo administrador | Si `puedeAlternarRol`. |
| UC-66 | Soporte y FAQ | Preguntas frecuentes. |
| UC-70 | Gestionar método de pago | Agregar, modificar o eliminar tarjeta en perfil. |

---

## 6. Módulo Cliente — Reseñas (UC-67 – UC-69)

> Solo usuarios con **al menos una reserva** en el hotel pueden comentar.

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]

    subgraph resenas [Comentarios y calificación]
        UC67[UC-67 Comentar hotel]
        UC68[UC-68 Modificar reseña]
        UC69[UC-69 Eliminar reseña]
        UC67A[UC-67a Validar reserva previa]
        UC67B[UC-67b Calificar 1-5 estrellas]
        UC67C[UC-67c Actualizar calificación hotel]
    end

    CLI --> UC67 & UC68 & UC69
    UC67 -->|include| UC67A & UC67B & UC67C
    UC68 -.->|extend| UC67
    UC69 -.->|extend| UC67
    UC67 & UC68 & UC69 & UC67C --> FB
```

| ID | Caso de uso | Descripción |
|----|-------------|-------------|
| UC-67 | Comentar hotel | Escribir reseña con estrellas (1–5) y texto. |
| UC-67a | Validar reserva previa | Verifica al menos 1 reserva en el hotel. |
| UC-67b | Calificar 1–5 estrellas | Selector visual de estrellas. |
| UC-67c | Actualizar calificación hotel | Ajusta `calificacion` según reseñas (sin cambio drástico). |
| UC-68 | Modificar reseña | Editar estrellas y comentario propios. |
| UC-69 | Eliminar reseña | Borrar comentario y recalcular calificación. |

**Regla de calificación del hotel**

| Estrellas del usuario | Efecto sobre la calificación base |
|----------------------|-----------------------------------|
| 1 – 2 | Baja (−0,6 / −0,3) |
| 3 | Se mantiene (0) |
| 4 – 5 | Sube (+0,3 / +0,6) |

---

## 7. Módulo Administrador (UC-38 – UC-65)

### 7.1 Panel y solicitudes (UC-38 – UC-43)

```mermaid
flowchart TB
    ADM((Administrador))
    FB[[Firebase]]

    subgraph panel [Panel]
        UC38[UC-38 Ver dashboard]
        UC39[UC-39 Alerta solicitudes]
        UC40[UC-40 Sembrar datos ejemplo]
    end

    subgraph solicitudes [Solicitudes admin]
        UC41[UC-41 Listar solicitudes]
        UC42[UC-42 Aprobar solicitud]
        UC43[UC-43 Rechazar solicitud]
    end

    ADM --> UC38 & UC41
    UC39 -.->|extend| UC38
    UC42 & UC43 -.->|extend| UC41
    UC38 & UC40 & UC41 & UC42 & UC43 --> FB
```

### 7.2 Hoteles y habitaciones (UC-44 – UC-55)

```mermaid
flowchart TB
    ADM((Administrador))
    FB[[Firebase]]

    subgraph hoteles [Hoteles]
        UC44[UC-44 Listar hoteles]
        UC45[UC-45 Registrar hotel]
        UC46[UC-46 Editar hotel]
        UC47[UC-47 Eliminar hotel]
        UC48[UC-48 Imágenes hotel]
        UC49[UC-49 Ir a habitaciones]
    end

    subgraph habitaciones [Habitaciones]
        UC50[UC-50 Listar habitaciones]
        UC51[UC-51 Registrar habitación]
        UC52[UC-52 Editar habitación]
        UC53[UC-53 Eliminar habitación]
        UC54[UC-54 Imágenes habitación]
        UC55[UC-55 Sync precio mínimo]
    end

    ADM --> UC44 & UC49 & UC50
    UC45 & UC46 -->|include| UC48
    UC51 & UC52 -->|include| UC54
    UC52 & UC53 -->|include| UC55
    UC49 -.->|extend| UC44
    UC50 -.->|extend| UC49
    UC44 & UC50 --> FB
```

### 7.3 Reservas, cuenta y soporte (UC-56 – UC-65, UC-66)

```mermaid
flowchart TB
    ADM((Administrador))
    FB[[Firebase]]

    subgraph reservas [Reservas admin]
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

    subgraph cuentaAdmin [Cuenta y soporte]
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

---

## 8. Diagrama UML clásico — Cliente

**Caso central:** `Gestionar reserva ecoturística`

```mermaid
flowchart TB
    subgraph frontera [Sistema Selva Booking]
        UCAcc[Acceder al sistema]
        UCHub[Gestionar reserva ecoturistica]

        subgraph pkgExp [Explorar alojamiento]
            UC11b[Ver inicio]
            UC12b[Buscar hoteles]
            UC15b[Ver detalle hotel]
        end

        subgraph pkgRes [Completar reserva]
            UC18b[Seleccionar fechas]
            UC21b[Crear reserva pendiente]
        end

        subgraph pkgPag [Realizar pago]
            UC22b[Confirmar pago]
            UC26b[Dir. facturación]
        end

        subgraph pkgCon [Consultar reservas]
            UC30b[Listar mis reservas]
        end

        subgraph pkgResenas [Comentarios]
            UC67b[Comentar hotel]
        end

        subgraph pkgCta [Gestionar cuenta]
            UC33b[Ver y editar perfil]
            UC70b[Gestionar método de pago]
        end

        UCExt1[[Cancelar reserva]]
        UCExt2[[Guardar tarjeta]]
    end

    CLI((Cliente)) --> UCAcc
    CLI --> UCHub
    FB[[Firebase]] --> UCHub
    PAY[[Pasarela]] --> UC22b

    UCAcc -->|include| UCHub
    UC11b & UC12b & UC15b --> UCHub
    UC18b & UC21b --> UCHub
    UC22b --> UCHub
    UC30b --> UCHub
    UC67b --> UCHub
    UC33b & UC70b --> UCHub

    UCExt1 -.->|extend| UCHub
    UCExt2 -.->|extend| UC22b
```

---

## 9. Diagrama UML clásico — Administrador

**Caso central:** `Gestionar operaciones admin`

```mermaid
flowchart TB
    subgraph frontera [Sistema Selva Booking]
        UCAcc[Acceder al panel admin]
        UCHub[Gestionar operaciones admin]

        subgraph pkgDash [Dashboard]
            UC38b[Ver estadísticas]
        end

        subgraph pkgHot [Gestionar hoteles]
            UC44b[Listar hoteles]
            UC45b[Registrar hotel]
        end

        subgraph pkgRoom [Gestionar habitaciones]
            UC50b[Listar habitaciones]
        end

        subgraph pkgRes [Gestionar reservas]
            UC56b[Listar reservas]
            UC62b[Confirmar reserva]
        end

        subgraph pkgSol [Gestionar solicitudes]
            UC41b[Listar solicitudes]
            UC42b[Aprobar solicitud]
        end

        UCExt1[[Rechazar solicitud]]
        UCExt2[[Alternar a modo cliente]]
    end

    ADM((Administrador)) --> UCAcc
    ADM --> UCHub
    FB[[Firebase]] --> UCHub

    UCAcc -->|include| UCHub
    UC38b & UC44b & UC45b --> UCHub
    UC50b --> UCHub
    UC56b & UC62b --> UCHub
    UC41b & UC42b --> UCHub

    UCExt1 -.->|extend| UCHub
    UCExt2 -.->|extend| UC33b[Ver perfil admin]
```

---

## 10. Relaciones include / extend

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
| UC-67 | UC-67a, UC-67b, UC-67c |

### Extend (opcional)

| Caso base | Extiende |
|-----------|----------|
| UC-12 | UC-13, UC-14 |
| UC-15 | UC-16, UC-67 |
| UC-22 | UC-27, UC-28, UC-29 |
| UC-30 | UC-32 |
| UC-33 | UC-34, UC-35, UC-37, UC-65 |
| UC-38 | UC-39 |
| UC-41 | UC-42, UC-43 |
| UC-44 | UC-49 |
| UC-49 | UC-50 |
| UC-56 | UC-57, UC-58 |
| UC-58 | UC-62, UC-63, UC-64 |
| UC-67 | UC-68, UC-69 |

---

## 11. Matriz actor × módulo

| Módulo | Invitado | Cliente | Administrador |
|--------|:--------:|:-------:|:-------------:|
| Autenticación | ●●●● | ● | ● |
| Exploración | — | ●●● | — |
| Reserva y pago | — | ●●●● | — |
| Reseñas | — | ●●● | — |
| Mi cuenta / método de pago | — | ●●●● | — |
| Panel admin | — | — | ●● |
| Solicitudes | — | ○ | ●●● |
| Hoteles / Habitaciones | — | — | ●●●● |
| Reservas admin | — | — | ●●●● |
| Soporte | — | ● | ● |

● acceso directo · ○ indirecto · — no aplica

---

## 12. Archivos PlantUML

Para exportar diagramas UML clásicos como PNG de alta resolución:

| Archivo | Contenido |
|---------|-----------|
| `casos_de_uso_detallado.puml` | Vista general + módulos por separado |
| `casos_de_uso_estilo_uml.puml` | Diagramas detallados Cliente y Admin (hub central) |

```bash
java -jar plantuml.jar docs/diagramas/casos_de_uso_estilo_uml.puml
java -jar plantuml.jar docs/diagramas/casos_de_uso_detallado.puml
```

---

*Fuente: Elaboración propia — Proyecto Selva Booking*
