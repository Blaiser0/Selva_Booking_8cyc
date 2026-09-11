# Selva Booking — Diagramas UML del proyecto

**Proyecto:** Selva Booking · Android (Kotlin · Jetpack Compose · MVVM) · Firebase  
**Versión:** Julio 2026 · Idioma: español  
**Carpeta:** `docs/uml/`

> Abre este archivo con **Preview** (`Ctrl + Shift + V`) para ver los diagramas Mermaid.  
> Para exportar PNG/SVG: `java -jar plantuml.jar docs/uml/*.puml`  
> **Imágenes generadas:** [`imagenes/`](imagenes/) (PNG listos para documentación)

---

## 1. Diagrama de casos de uso

Actores: **Invitado**, **Cliente**, **Encargado de hotel**, **Administrador** (limitado), **SuperAdmin**, **Firebase** y **Pasarela simulada**.

```mermaid
flowchart TB
    subgraph actores [Actores]
        INV((Invitado))
        CLI((Cliente))
        ENC((Encargado de hotel))
        ADM((Administrador))
        SA((SuperAdmin))
    end

    subgraph externos [Sistemas externos]
        FB[[Firebase]]
        PAY[[Pasarela simulada]]
    end

    subgraph sistema [Sistema Selva Booking]

        subgraph auth [Autenticación]
            UC01[UC-01 Iniciar sesión]
            UC02[UC-02 Registrarse]
            UC03[UC-03 Recuperar contraseña]
            UC04[UC-04 Cerrar sesión]
            UC05[UC-05 Completar perfil encargado]
            UC06[UC-06 Alternar rol]
        end

        subgraph cliente [Módulo Cliente]
            UC10[UC-10 Explorar y buscar hoteles]
            UC11[UC-11 Reservar y pagar]
            UC12[UC-12 Mis reservas]
            UC13[UC-13 Comentar hotel]
            UC14[UC-14 Mi cuenta]
        end

        subgraph encargado [Módulo Encargado]
            UC20[UC-20 Gestionar mi hotel]
            UC21[UC-21 Gestionar habitaciones e inventario]
            UC22[UC-22 Historial de reservas]
            UC23[UC-23 Ver comentarios]
        end

        subgraph adminLim [Administrador limitado]
            UC30[UC-30 Panel de supervisión]
            UC31L[UC-31 Consultar hoteles de su red]
            UC32L[UC-32 Consultar reservas de su red]
            UC33L[UC-33 Gestionar encargados creados]
        end

        subgraph superAdmin [SuperAdmin]
            UC30S[UC-30s Panel global]
            UC31S[UC-31s Gestionar hoteles y habitaciones]
            UC32S[UC-32s Gestionar reservas]
            UC33S[UC-33s Gestionar encargados]
            UC34[UC-34 Gestionar administradores]
            UC35[UC-35 Auditoría y revertir cambios]
            UC36[UC-36 Comentarios por hotel]
        end

        UC99[UC-99 Soporte y preguntas frecuentes]
    end

    INV --> UC01 & UC02 & UC03
    CLI --> UC04 & UC10 & UC11 & UC12 & UC13 & UC14 & UC99
    ENC --> UC04 & UC05 & UC06 & UC20 & UC21 & UC22 & UC23 & UC14 & UC99
    ADM --> UC04 & UC06 & UC30 & UC31L & UC32L & UC33L & UC14 & UC99
    SA --> UC04 & UC06 & UC30S & UC31S & UC32S & UC33S & UC34 & UC35 & UC36 & UC14 & UC99

    UC01 & UC02 & UC03 & UC04 & UC05 --> FB
    UC10 & UC11 & UC12 & UC13 & UC14 --> FB
    UC20 & UC21 & UC22 & UC23 --> FB
    UC30 & UC31L & UC32L & UC33L --> FB
    UC30S & UC31S & UC32S & UC33S & UC34 & UC35 & UC36 --> FB
    UC11 --> PAY

    UC11 -.->|extiende| UC10
    UC23 -.->|extiende| UC20
    UC36 -.->|extiende| UC31S
```

**PlantUML:** [`01_casos_de_uso.puml`](01_casos_de_uso.puml)

### 1.1 Casos de uso detallados por rol

Diagramas con relaciones **`<<incluye>>`** y **`<<extiende>>`** para cada actor:

| Rol | Documento | PlantUML |
|-----|-----------|----------|
| Invitado | [01_casos_uso_por_rol.md §1](01_casos_uso_por_rol.md) | [`01a_casos_uso_invitado.puml`](01a_casos_uso_invitado.puml) |
| Cliente | [§2](01_casos_uso_por_rol.md) | [`01b_casos_uso_cliente.puml`](01b_casos_uso_cliente.puml) |
| Encargado de hotel | [§3](01_casos_uso_por_rol.md) | [`01c_casos_uso_encargado.puml`](01c_casos_uso_encargado.puml) |
| Administrador limitado | [§4](01_casos_uso_por_rol.md) | [`01d_casos_uso_administrador.puml`](01d_casos_uso_administrador.puml) |
| SuperAdmin | [§5](01_casos_uso_por_rol.md) | [`01e_casos_uso_superadmin.puml`](01e_casos_uso_superadmin.puml) |

```bash
java -jar plantuml.jar docs/uml/01a_casos_uso_invitado.puml docs/uml/01b_casos_uso_cliente.puml docs/uml/01c_casos_uso_encargado.puml docs/uml/01d_casos_uso_administrador.puml docs/uml/01e_casos_uso_superadmin.puml
```

---

## 2. Diagramas de secuencia (por actor)

### 2.1 Invitado — Registro e inicio de sesión

```mermaid
sequenceDiagram
    autonumber
    actor I as Invitado
    participant UI as Pantalla inicio de sesión / registro
    participant AVM as Modelo de vista autenticación
    participant VAL as Utilidad validación
    participant AR as Repositorio autenticación
    participant FAS as Servicio Firebase Auth
    participant FS as Servicio Firestore
    participant FB as Firebase

    alt UC-02 Registrarse
        I->>UI: Completa nombre, correo, teléfono, contraseña
        UI->>AVM: registrar()
        AVM->>VAL: validarNombre / validarTelefono / validarCorreo
        VAL-->>AVM: válido
        AVM->>AR: registrar(nombre, correo, contraseña, telefono)
        AR->>FAS: crearCuenta(correo, contraseña)
        FAS->>FB: Autenticación Firebase
        FB-->>FAS: identificador
        AR->>FS: crearUsuario(rol=CLIENTE)
        FS->>FB: usuarios/{id}
        FB-->>AVM: usuario creado
        AVM-->>UI: navegar a Inicio del cliente
    else UC-01 Iniciar sesión
        I->>UI: Ingresa correo y contraseña
        UI->>AVM: iniciarSesion()
        AVM->>AR: iniciarSesion(correo, contraseña)
        AR->>FAS: iniciarSesion(correo, contraseña)
        FAS->>FB: Autenticación Firebase
        AR->>FS: obtenerUsuario(id)
        FS->>FB: usuarios/{id}
        FB-->>AVM: usuario con rol
        AVM-->>UI: redirigir según rol
    else UC-03 Recuperar contraseña
        I->>UI: Solicita restablecimiento
        UI->>AVM: enviarRecuperacionContrasena()
        AVM->>FAS: enviarCorreoRecuperacion(correo)
        FAS->>FB: correo de recuperación
        FB-->>I: correo enviado
    end
```

**PlantUML:** [`02a_secuencia_invitado.puml`](02a_secuencia_invitado.puml)

---

### 2.2 Cliente — Reservar, pagar y comentar

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant HS as Pantalla inicio / búsqueda
    participant HD as Pantalla detalle hotel
    participant BS as Pantalla reserva
    participant PS as Pantalla pago
    participant BVM as Modelo vista reserva
    participant PVM as Modelo vista pago
    participant RES as Repositorio reservas
    participant RSR as Repositorio reseñas
    participant ROOM as Repositorio habitaciones
    participant FS as Servicio Firestore

    C->>HS: Explora y busca hoteles
    HS->>FS: obtenerFlujoHoteles()
    C->>HD: Abre detalle del hotel
    HD->>FS: obtenerHabitacionesDisponibles(inventario > 0)
    C->>BS: Selecciona fechas, huéspedes y habitación
    BS->>BVM: confirmarReserva()
    BVM->>RES: crearReserva(PENDIENTE_PAGO)
    RES->>FS: reservas/{id}
    BVM-->>PS: navegar a pago

    C->>PS: Ingresa tarjeta (simulada)
    PS->>PVM: procesarPago()
    PVM->>RES: actualizarEstado(CONFIRMADA)
    RES->>ROOM: decrementarInventario(idHabitacion)
    RES->>FS: actualizar reserva e inventario
    PS-->>C: Mis reservas

    C->>HD: Escribe reseña
    HD->>RSR: crearResena(idHotel, calificacion)
    RSR->>FS: resenas/{id} + recalcular calificación
```

**PlantUML:** [`02b_secuencia_cliente.puml`](02b_secuencia_cliente.puml)

---

### 2.3 Encargado de hotel — Gestionar hotel, habitaciones y comentarios

```mermaid
sequenceDiagram
    autonumber
    actor E as Encargado
    participant GP as Pantalla completar perfil
    participant MH as Pantalla mi hotel
    participant RS as Pantalla habitaciones
    participant MR as Pantalla comentarios
    participant AVM as Modelo vista autenticación
    participant HVM as Modelo vista hoteles
    participant MVM as Modelo vista comentarios
    participant HR as Repositorio hoteles
    participant RR as Repositorio habitaciones
    participant AUD as Repositorio auditoría
    participant RSR as Repositorio reseñas
    participant FS as Servicio Firestore

    alt UC-05 Completar perfil (primer acceso)
        E->>GP: Completa datos obligatorios
        GP->>AVM: completarPerfilEncargado()
        AVM->>FS: actualizarUsuario(perfilCompleto=true)
        AVM-->>MH: navegar a Mi Hotel
    end

    E->>MH: Crea o edita su hotel (máx. 1)
    MH->>HVM: guardarHotel()
    HVM->>HR: crearHotel / actualizarHotel
    HR->>FS: hoteles/{id}
    HVM->>AUD: registrarAuditoria(CREAR/ACTUALIZAR)

    E->>RS: Gestiona habitaciones e inventario
    RS->>RR: crearHabitacion / actualizarHabitacion
    RR->>FS: habitaciones/{id}
    RS->>AUD: registrarAuditoria()

    E->>MR: Ver comentarios de huéspedes
    MR->>MVM: inicializar
    MVM->>HR: obtenerHotelesDelPropietario(idUsuario)
    MVM->>RSR: obtenerResenasPorHotel(idHotel)
    RSR->>FS: resenas (tiempo real)
    MR-->>E: listado de reseñas
```

**PlantUML:** [`02c_secuencia_encargado.puml`](02c_secuencia_encargado.puml)

---

### 2.4 Administrador — Supervisión de su red

```mermaid
sequenceDiagram
    autonumber
    actor A as Administrador
    participant DS as Pantalla panel supervisión
    participant HS as Pantalla hoteles
    participant RS as Pantalla reservas
    participant GS as Pantalla encargados
    participant DVM as Modelo vista panel
    participant HVM as Modelo vista hoteles
    participant GVM as Modelo vista encargados
    participant ADS as Alcance administrador
    participant AR as Repositorio autenticación
    participant FS as Servicio Firestore

    A->>DS: Abre panel de supervisión
    DVM->>AR: obtenerUsuarioActual()
    DVM->>FS: hoteles, reservas, reseñas
    DVM->>ADS: hotelesParaAdmin(idAdmin, encargados, hoteles)
    DVM->>ADS: reservasParaAdmin(...)
    DS-->>A: métricas solo de su red

    A->>HS: Consulta hoteles (solo lectura)
    HVM->>ADS: hotelesParaAdmin(...)
    HVM-->>A: hoteles + encargado vinculado

    A->>RS: Consulta reservas de su red
    RS->>ADS: reservasParaAdmin(...)
    RS-->>A: Confirmada / Terminada

    A->>GS: Crea encargado
    GS->>GVM: crearEncargado()
    GVM->>AR: crearEncargadoHotel(correo, contraseña)
    AR->>FS: crearUsuario(creadoPorAdminId=idAdmin)
    GS-->>A: encargado en su red
```

**PlantUML:** [`02d_secuencia_administrador.puml`](02d_secuencia_administrador.puml)

---

### 2.5 SuperAdmin — Control global y auditoría

```mermaid
sequenceDiagram
    autonumber
    actor SA as SuperAdmin
    participant DS as Pantalla panel global
    participant HS as Pantalla hoteles
    participant HR as Pantalla comentarios por hotel
    participant AS as Pantalla auditoría
    participant ADS as Pantalla administradores
    participant DVM as Modelo vista panel
    participant HVM as Modelo vista hoteles
    participant RVM as Modelo vista comentarios
    participant AVM as Modelo vista auditoría
    participant ADV as Modelo vista administradores
    participant HRr as Repositorio hoteles
    participant RSR as Repositorio reseñas
    participant AUD as Repositorio auditoría
    participant AR as Repositorio autenticación
    participant FS as Servicio Firestore

    SA->>DS: Panel global
    DVM->>FS: todos los hoteles, reservas y reseñas
    DS-->>SA: métricas globales + sin encargado/admin

    SA->>HS: Crear / editar / eliminar hotel y habitaciones
    HVM->>HRr: crearHotel / actualizarHotel / eliminarHotel
    HRr->>FS: hoteles / habitaciones

    SA->>HR: Ver comentarios por hotel (⭐)
    HR->>RVM: inicializar(idHotel)
    RVM->>RSR: obtenerResenasPorHotel(idHotel)
    HR-->>SA: reseñas del hotel

    SA->>ADS: Crear administrador
    ADV->>AR: crearAdministrador(correo, contraseña)
    AR->>FS: crearUsuario(rol=ADMINISTRADOR)

    SA->>AS: Revertir cambio de encargado
    AS->>AVM: revertirCambio(idRegistro)
    AVM->>AUD: revertirCambio(idRegistro, idSuperAdmin)
    AUD->>FS: restaurar entidad + marcar revertido
    AS-->>SA: cambio revertido
```

**PlantUML:** [`02e_secuencia_superadmin.puml`](02e_secuencia_superadmin.puml)

**PlantUML combinado:** [`02_secuencia.puml`](02_secuencia.puml)

---

## 3. Diagrama de estados

Estados del ciclo de vida de una **reserva** y su impacto en el **inventario** de la habitación.

```mermaid
stateDiagram-v2
    direction LR

    [*] --> PENDIENTE_PAGO : crear reserva\n(ModeloVistaReserva)

    PENDIENTE_PAGO --> CONFIRMADA : pago exitoso\ninventario - 1

    CONFIRMADA --> TERMINADA : fecha de salida vencida\nexpirarReservasFinalizadas()\ninventario + 1

    state PENDIENTE_PAGO {
        state "Interno" as INT
        note right of INT
            esPublico = falso
            No visible en listados
            admin / encargado / cliente
        end note
    }

    state CONFIRMADA {
        state "Activa" as ACT
        note right of ACT
            reservaInventario = verdadero
            Visible en filtros
        end note
    }

    state TERMINADA {
        state "Finalizada" as FIN
        note right of FIN
            esPublico = verdadero
            Inventario liberado
        end note
    }
```

| Estado | Cliente | Admin / SuperAdmin | Encargado | Inventario |
|--------|:-------:|:------------------:|:---------:|:----------:|
| Pendiente de pago | Durante pago | No | No | Sin cambio |
| Confirmada | Sí | Sí | Sí (su hotel) | −1 |
| Terminada | Sí | Sí | Sí (su hotel) | +1 |

**PlantUML:** [`03_estados.puml`](03_estados.puml)

---

## 4. Diagrama de clases

Modelo de dominio principal (`domain/model/`).

```mermaid
classDiagram
    direction TB

    class Usuario {
        +String id
        +String nombre
        +String correo
        +String telefono
        +String urlFoto
        +RolUsuario rol
        +Boolean puedeAlternarRol
        +Boolean perfilCompleto
        +String rolAlternativo
        +String creadoPorAdminId
        +esCuentaAdministrador()
        +idAdministradorPropietario()
    }

    class RolUsuario {
        <<enumeración>>
        CLIENTE
        SUPER_ADMIN
        ADMINISTRADOR
        GERENTE_HOTEL
        +tieneAccesoPanelAdmin()
        +tienePoderesCompletos()
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
        +String idPropietario
        +Boolean destacado
        +Boolean oferta
    }

    class Habitacion {
        +String id
        +String idHotel
        +String nombre
        +Double precio
        +Int capacidad
        +Int cantidad
        +Int inventario
        +Boolean disponible
        +esReservable()
    }

    class Reserva {
        +String id
        +String idUsuario
        +String idHotel
        +String idHabitacion
        +String fechaIngreso
        +String fechaSalida
        +Int huespedes
        +Double precioTotal
        +EstadoReserva estado
    }

    class EstadoReserva {
        <<enumeración>>
        PENDIENTE_PAGO
        CONFIRMADA
        TERMINADA
        +esPublico()
        +reservaInventario()
    }

    class Resena {
        +String id
        +String idHotel
        +String idUsuario
        +String idReserva
        +String nombreUsuario
        +Int calificacion
        +String comentario
        +Long fechaCreacion
    }

    class RegistroAuditoria {
        +String id
        +Long marcaTiempo
        +String idActor
        +AccionAuditoria accion
        +TipoEntidadAuditoria tipoEntidad
        +String idEntidad
        +Boolean revertido
        +puedeRevertir()
    }

    Usuario --> RolUsuario : rol
    Usuario "1" --> "0..*" Usuario : creadoPorAdminId
    Usuario "1" --> "0..1" Hotel : propietario
    Usuario "1" --> "*" Reserva : realiza
    Usuario "1" --> "*" Resena : escribe
    Usuario "1" --> "*" RegistroAuditoria : actor
    Hotel "1" *-- "*" Habitacion : contiene
    Hotel "1" --> "*" Reserva : recibe
    Hotel "1" --> "*" Resena : tiene
    Habitacion "1" --> "*" Reserva : reservada en
    Reserva --> EstadoReserva : estado
```

**PlantUML:** [`04_clases.puml`](04_clases.puml)

---

## 5. Diagrama de componentes

Arquitectura **MVVM** del proyecto Android.

```mermaid
flowchart TB
    subgraph device [Dispositivo Android]
        subgraph presentation [Capa Presentación — ui/]
            MA[Actividad principal]
            NG[Grafo de navegación + menú lateral]
            SCR[Pantallas Compose]
            CMP[Componentes de interfaz]
        end

        subgraph viewmodel [Capa Modelo de vista — viewmodel/]
            AVM[Modelo vista autenticación]
            CVM[Inicio · Búsqueda · Detalle · Reserva · Pago · Mis reservas · Perfil]
            MVM[Modelo vista comentarios encargado]
            ADM[Panel · Hoteles · Reservas · Encargados · Administradores · Auditoría · Comentarios]
        end

        subgraph repository [Capa Repositorio — repository/]
            AR[Repositorio autenticación]
            HR[Repositorio hoteles]
            RR[Repositorio habitaciones]
            RESR[Repositorio reservas]
            RSR[Repositorio reseñas]
            SCR2[Repositorio tarjeta guardada]
        end

        subgraph data [Capa Datos — data/]
            FAS[Servicio Firebase Auth]
            FS[Servicio Firestore]
            SS[Servicio almacenamiento]
        end

        subgraph local [Almacenamiento local]
            SP[(Preferencias compartidas)]
        end

        subgraph domain [Dominio — domain/model/]
            MODELS[Usuario · Hotel · Habitación · Reserva · Reseña · Auditoría]
        end

        subgraph utils [Utilidades — utils/]
            ADS[Alcance administrador]
            VAL[Utilidad validación]
        end
    end

    MA --> NG --> SCR --> CMP
    SCR --> AVM & CVM & MVM & ADM
    AVM & CVM --> VAL
    ADM --> ADS
    AVM & CVM & MVM & ADM --> AR & HR & RR & RESR & RSR & SCR2
    AR --> FAS & FS
    HR & RR & RESR & RSR --> FS
    HR --> SS
    SCR2 --> SP
    AR & HR & RR & RESR & RSR --> MODELS
```

**PlantUML:** [`05_componentes.puml`](05_componentes.puml)

---

## 6. Diagrama de despliegue

Distribución física: **aplicación Android** en el dispositivo del usuario y **servicios Firebase** en la nube.

```mermaid
flowchart TB
    subgraph users [Usuarios]
        U1[Invitado / Cliente]
        U2[Encargado de hotel]
        U3[Administrador / SuperAdmin]
    end

    subgraph android [Dispositivo Android — SDK mínimo 24]
        APK["APK Selva Booking\ncom.company.selvabooking"]
        subgraph appRuntime [Entorno de ejecución]
            COMPOSE[Interfaz Jetpack Compose]
            VM[Modelos de vista + corrutinas]
            REPO[Repositorios]
            SP[(Preferencias compartidas\ntarjeta guardada)]
            PAYLOCAL[Pasarela simulada\nvalidación local]
        end
        APK --> COMPOSE --> VM --> REPO
        VM --> PAYLOCAL
        REPO --> SP
    end

    subgraph google [Google Cloud — Firebase]
        AUTH[Autenticación Firebase\nCorreo / Contraseña]
        FS[(Cloud Firestore)]
        STG[Almacenamiento Firebase\nimágenes de hoteles]
    end

    subgraph collections [Colecciones Firestore]
        C1[usuarios]
        C2[hoteles]
        C3[habitaciones]
        C4[reservas]
        C5[resenas]
        C6[registros_auditoria]
    end

    U1 & U2 & U3 --> APK
    REPO -->|HTTPS / SDK| AUTH
    REPO -->|HTTPS / SDK| FS
    REPO -->|HTTPS / SDK| STG
    FS --> C1 & C2 & C3 & C4 & C5 & C6
    PAYLOCAL -.->|sin red externa| VM
```

| Nodo | Tecnología | Responsabilidad |
|------|------------|-----------------|
| APK Android | Kotlin · Compose · MVVM | Interfaz, lógica de negocio, navegación |
| Preferencias compartidas | Almacenamiento local Android | Tarjeta de pago simulada guardada |
| Autenticación Firebase | Google Cloud | Registro, inicio de sesión, recuperación de contraseña |
| Cloud Firestore | Google Cloud | Persistencia de usuarios, hoteles, reservas, reseñas y auditoría |
| Almacenamiento Firebase | Google Cloud | Imágenes de hoteles subidas por encargados |
| Pasarela simulada | En la aplicación | Validación de tarjeta sin procesador real |

**PlantUML:** [`06_despliegue.puml`](06_despliegue.puml)

---

## Exportar diagramas

```bash
# Todos los diagramas
java -jar plantuml.jar docs/uml/*.puml

# Solo casos de uso por rol
java -jar plantuml.jar docs/uml/01a_casos_uso_invitado.puml docs/uml/01b_casos_uso_cliente.puml docs/uml/01c_casos_uso_encargado.puml docs/uml/01d_casos_uso_administrador.puml docs/uml/01e_casos_uso_superadmin.puml

# Solo secuencias por actor
java -jar plantuml.jar docs/uml/02a_secuencia_invitado.puml docs/uml/02b_secuencia_cliente.puml docs/uml/02c_secuencia_encargado.puml docs/uml/02d_secuencia_administrador.puml docs/uml/02e_secuencia_superadmin.puml
```

Genera PNG/SVG en la misma carpeta `docs/uml/`.

---

*Fuente: Elaboración propia — Proyecto Selva Booking*
