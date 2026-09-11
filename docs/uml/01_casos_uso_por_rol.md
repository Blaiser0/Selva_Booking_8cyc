# Casos de uso detallados por rol

Diagramas UML con relaciones **`<<incluye>>`** (subproceso obligatorio) y **`<<extiende>>`** (flujo opcional).

> **PlantUML (recomendado para exportar):** archivos `01a` … `01e` en esta carpeta.  
> **Mermaid:** ver secciones siguientes (vista previa en Markdown).

---

## 1. Invitado

```mermaid
flowchart TB
    INV((Invitado))
    FB[[Firebase]]

    UC01[UC-01 Iniciar sesión]
    UC01A[UC-01a Validar credenciales]
    UC01B[UC-01b Autenticar en Firebase Auth]
    UC01C[UC-01c Obtener perfil de usuario]

    UC02[UC-02 Registrarse]
    UC02A[UC-02a Validar nombre solo letras]
    UC02B[UC-02b Validar teléfono 9 dígitos]
    UC02C[UC-02c Validar correo y contraseña]
    UC02D[UC-02d Aceptar términos y condiciones]
    UC02E[UC-02e Crear cuenta Auth + documento Firestore]

    UC03[UC-03 Recuperar contraseña]
    UC03A[UC-03a Validar correo registrado]
    UC03B[UC-03b Enviar correo de recuperación]

    INV --> UC01 & UC02 & UC03
    UC01 -->|incluye| UC01A & UC01B & UC01C
    UC02 -->|incluye| UC02A & UC02B & UC02C & UC02D & UC02E
    UC03 -->|incluye| UC03A & UC03B
    UC01B & UC02E & UC03B --> FB
```

**PlantUML:** [`01a_casos_uso_invitado.puml`](01a_casos_uso_invitado.puml)

| ID | Caso de uso | Tipo | Descripción |
|----|-------------|------|-------------|
| UC-01 | Iniciar sesión | Principal | Acceso con correo y contraseña |
| UC-01a | Validar credenciales | incluye | Campos no vacíos, formato correo |
| UC-01b | Autenticar Firebase | incluye | `signIn` en Firebase Auth |
| UC-01c | Obtener perfil | incluye | Lee documento `usuarios/{id}` |
| UC-02 | Registrarse | Principal | Alta de cliente |
| UC-02a–e | Validaciones y persistencia | incluye | Nombre, teléfono, términos, Firestore |
| UC-03 | Recuperar contraseña | Principal | Restablecimiento por correo |
| UC-03a–b | Validar y enviar | incluye | Correo válido + email Firebase |

---

## 2. Cliente

```mermaid
flowchart TB
    CLI((Cliente))
    FB[[Firebase]]
    PAY[[Pasarela simulada]]

    UC10[UC-10 Explorar hoteles]
    UC10A[UC-10a Buscar por ciudad y criterios]
    UC10B[UC-10b Filtrar y ordenar resultados]
    UC10C[UC-10c Ver detalle del hotel]
    UC10D[UC-10d Ver habitaciones con inventario]

    UC11[UC-11 Reservar y pagar]
    UC11A[UC-11a Validar fechas y huéspedes]
    UC11B[UC-11b Calcular precio total]
    UC11C[UC-11c Crear reserva pendiente de pago]
    UC11D[UC-11d Procesar pago simulado]
    UC11E[UC-11e Confirmar reserva y descontar inventario]

    UC12[UC-12 Ver mis reservas]
    UC12A[UC-12a Filtrar por estado]

    UC13[UC-13 Comentar hotel]
    UC13A[UC-13a Validar reserva previa en el hotel]
    UC13B[UC-13b Recalcular calificación del hotel]

    UC14[UC-14 Gestionar mi cuenta]
    UC14A[UC-14a Editar perfil y teléfono]
    UC14B[UC-14b Gestionar tarjeta guardada]
    UC14C[UC-14c Alternar a modo encargado o admin]

    UC04[UC-04 Cerrar sesión]
    UC99[UC-99 Soporte y FAQ]

    CLI --> UC10 & UC11 & UC12 & UC13 & UC14 & UC04 & UC99
    UC10 -->|incluye| UC10C
    UC10C -->|incluye| UC10D
    UC10A & UC10B -.->|extiende| UC10
    UC10C -.->|extiende| UC11
    UC11 -->|incluye| UC11A & UC11B & UC11C & UC11D & UC11E
    UC12A -.->|extiende| UC12
    UC13 -->|incluye| UC13A & UC13B
    UC10C -.->|extiende| UC13
    UC14 -->|incluye| UC14A
    UC14B & UC14C -.->|extiende| UC14
    UC10 & UC11C & UC11E & UC12 & UC13 & UC14 --> FB
    UC11D --> PAY
```

**PlantUML:** [`01b_casos_uso_cliente.puml`](01b_casos_uso_cliente.puml)

---

## 3. Encargado de hotel

```mermaid
flowchart TB
    ENC((Encargado de hotel))
    FB[[Firebase]]

    UC05[UC-05 Completar perfil encargado]
    UC05A[UC-05a Validar datos obligatorios]

    UC20[UC-20 Gestionar mi hotel]
    UC20A[UC-20a Registrar hotel]
    UC20B[UC-20b Editar hotel]
    UC20C[UC-20c Eliminar hotel]
    UC20D[UC-20d Subir imágenes del hotel]

    UC21[UC-21 Gestionar habitaciones]
    UC21A[UC-21a Registrar habitación]
    UC21B[UC-21b Editar habitación e inventario]
    UC21C[UC-21c Eliminar habitación]
    UC21D[UC-21d Control de inventario disponible]

    UC22[UC-22 Historial de reservas]
    UC22A[UC-22a Filtrar Confirmada / Terminada]
    UC22B[UC-22b Buscar por cliente o habitación]
    UC22C[UC-22c Ver detalle de reserva]

    UC23[UC-23 Ver comentarios de huéspedes]
    UC23A[UC-23a Listado en tiempo real]

    UC06[UC-06 Alternar a modo cliente]
    UC04[UC-04 Cerrar sesión]
    UC14[UC-14 Mi cuenta]
    UC99[UC-99 Soporte y FAQ]

    ENC --> UC05 & UC20 & UC21 & UC22 & UC23 & UC06 & UC04 & UC14 & UC99
    UC05 -->|incluye| UC05A
    UC20A & UC20B & UC20C -.->|extiende| UC20
    UC20A & UC20B -->|incluye| UC20D
    UC21A & UC21B & UC21C -.->|extiende| UC21
    UC21B -->|incluye| UC21D
    UC20 -.->|extiende| UC21
    UC22A & UC22B & UC22C -.->|extiende| UC22
    UC23A -.->|extiende| UC23
    UC20 -.->|extiende| UC23
    UC05 & UC20 & UC21 & UC22 & UC23 --> FB
```

**PlantUML:** [`01c_casos_uso_encargado.puml`](01c_casos_uso_encargado.puml)

---

## 4. Administrador (limitado)

```mermaid
flowchart TB
    ADM((Administrador))
    FB[[Firebase]]

    UC30[UC-30 Panel de supervisión]
    UC30A[UC-30a Calcular métricas de su red]
    UC30B[UC-30b Mostrar alcance de encargados creados]

    UC31[UC-31 Consultar hoteles de su red]
    UC31A[UC-31a Filtrar por creadoPorAdminId]
    UC31B[UC-31b Ver encargado vinculado]
    UC31C[UC-31c Consultar habitaciones solo lectura]

    UC32[UC-32 Consultar reservas de su red]
    UC32A[UC-32a Filtrar por estado]
    UC32B[UC-32b Buscar por hotel o cliente]
    UC32C[UC-32c Ver detalle de reserva]

    UC33[UC-33 Gestionar encargados creados]
    UC33A[UC-33a Crear encargado]
    UC33B[UC-33b Eliminar encargado y cascada]
    UC33C[UC-33c Asignar creadoPorAdminId]

    UC06[UC-06 Alternar a modo cliente]
    UC04[UC-04 Cerrar sesión]
    UC14[UC-14 Mi cuenta]
    UC99[UC-99 Soporte y FAQ]

    ADM --> UC30 & UC31 & UC32 & UC33 & UC06 & UC04 & UC14 & UC99
    UC30 -->|incluye| UC30A & UC30B
    UC31 -->|incluye| UC31A & UC31B
    UC31C -.->|extiende| UC31
    UC32A & UC32B & UC32C -.->|extiende| UC32
    UC33A & UC33B -.->|extiende| UC33
    UC33A -->|incluye| UC33C
    UC30 & UC31 & UC32 & UC33 --> FB
```

**PlantUML:** [`01d_casos_uso_administrador.puml`](01d_casos_uso_administrador.puml)

---

## 5. SuperAdmin

```mermaid
flowchart TB
    SA((SuperAdmin))
    FB[[Firebase]]

    UC30S[UC-30s Panel global]
    UC30SA[UC-30sa Métricas ingresos y reservas]
    UC30SB[UC-30sb Hoteles sin encargado / sin admin]
    UC30SC[UC-30sc Total comentarios globales]

    UC31S[UC-31s Gestionar hoteles y habitaciones]
    UC31A[UC-31a Crear / editar / eliminar hotel]
    UC31B[UC-31b Filtrar hoteles por administrador]
    UC31C[UC-31c CRUD habitaciones e inventario]
    UC31D[UC-31d Ver encargado y administrador vinculado]
    UC36[UC-36 Ver comentarios por hotel]

    UC32S[UC-32s Gestionar reservas globales]
    UC32A[UC-32a Crear reserva manual]
    UC32B[UC-32b Editar o eliminar reserva]
    UC32C[UC-32c Filtrar y buscar reservas]
    UC32D[UC-32d Terminar reserva anticipada]

    UC33S[UC-33s Gestionar todos los encargados]
    UC34[UC-34 Gestionar administradores]
    UC34A[UC-34a Crear cuenta administrador]
    UC34B[UC-34b Eliminar administrador]

    UC35[UC-35 Auditoría del sistema]
    UC35A[UC-35a Consultar registro de cambios]
    UC35B[UC-35b Revertir cambio de encargado]

    UC06[UC-06 Alternar a modo cliente]
    UC04[UC-04 Cerrar sesión]
    UC14[UC-14 Mi cuenta]
    UC99[UC-99 Soporte y FAQ]

    SA --> UC30S & UC31S & UC32S & UC33S & UC34 & UC35 & UC06 & UC04 & UC14 & UC99
    UC30S -->|incluye| UC30SA & UC30SB & UC30SC
    UC31A & UC31B & UC31C & UC31D & UC36 -.->|extiende| UC31S
    UC32A & UC32B & UC32C & UC32D -.->|extiende| UC32S
    UC34A & UC34B -.->|extiende| UC34
    UC35 -->|incluye| UC35A
    UC35B -.->|extiende| UC35
    UC31S -.->|extiende| UC36
    UC30S & UC31S & UC32S & UC33S & UC34 & UC35 & UC36 --> FB
```

**PlantUML:** [`01e_casos_uso_superadmin.puml`](01e_casos_uso_superadmin.puml)

---

## Leyenda

| Relación | Significado | Notación PlantUML |
|----------|-------------|-------------------|
| **incluye** | Subproceso obligatorio del caso base | `..> : <<incluye>>` |
| **extiende** | Variante u opción del caso base | `..> : <<extiende>>` |
| Actor → caso | El actor inicia o participa en el caso | `-->` |

---

*Fuente: Elaboración propia — Proyecto Selva Booking*
