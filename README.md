# Selva Booking

Aplicación Android de reservas ecoturísticas en **Madre de Dios, Perú**. Permite a los clientes buscar lodges y hoteles, comparar precios, reservar habitaciones y pagar con una pasarela simulada. Los administradores gestionan el catálogo, las reservas y las solicitudes de acceso desde un panel dedicado.

**Package:** `com.company.selvabooking`  
**Versión:** 1.0

---

## Características

### Cliente
- Registro e inicio de sesión con Firebase Auth
- Términos y condiciones obligatorios en el registro
- Exploración de hoteles con filtros (ciudad, precio, estrellas)
- Detalle de hotel con galería de imágenes y habitaciones
- Reserva con fechas, huéspedes y cálculo automático de precio
- Pago simulado con opción de guardar tarjeta en el dispositivo
- Consulta y cancelación de reservas en tiempo real

### Administrador
- Dashboard con estadísticas (hoteles, habitaciones, reservas, usuarios)
- Aprobación o rechazo de solicitudes de acceso admin
- CRUD de hoteles y habitaciones con imágenes en Firebase Storage
- Gestión completa de reservas (confirmar, cancelar, completar)
- Carga automática de datos demo si Firestore está vacío

---

## Stack tecnológico

| Tecnología | Uso |
|------------|-----|
| Kotlin | Lenguaje principal |
| Jetpack Compose + Material 3 | Interfaz declarativa |
| MVVM | Separación UI / lógica / datos |
| Navigation Compose | Navegación entre pantallas |
| Firebase Auth | Autenticación |
| Cloud Firestore | Base de datos en tiempo real |
| Firebase Storage | Imágenes (hoteles, habitaciones, perfiles) |
| Coil | Carga de imágenes desde URL |
| Coroutines + Flow | Operaciones asíncronas y streams reactivos |

---

## Arquitectura

```
UI (Compose)  →  ViewModel  →  Repository  →  Firebase / SharedPreferences
                      ↓
               domain/model
```

- La UI no accede a Firebase directamente.
- Los ViewModels exponen `StateFlow` / `UiState`.
- Los Repositories abstraen Firestore, Auth, Storage y almacenamiento local.

---

## Estructura del proyecto

```
app/src/main/java/com/company/selvabooking/
├── MainActivity.kt
├── SelvaBookingApplication.kt
├── data/
│   ├── SampleData.kt              # Datos demo de Madre de Dios
│   └── firebase/                  # Auth, Firestore, Storage
├── domain/model/                  # Hotel, Room, User, Reservation...
├── navigation/                    # Routes.kt, NavGraph.kt
├── repository/
├── ui/
│   ├── admin/                     # Panel administrador
│   ├── auth/                      # Login, registro, términos
│   ├── client/                    # Home, búsqueda, reserva, pago
│   ├── components/                # Componentes reutilizables
│   ├── profile/
│   ├── splash/
│   └── theme/
├── utils/
└── viewmodel/
```

---

## Requisitos

- Android Studio (Ladybug o superior recomendado)
- JDK 11+
- Android SDK (minSdk 24, targetSdk 36)
- Proyecto Firebase configurado con `google-services.json` en `app/`
- Emulador Android o dispositivo físico

---

## Cómo ejecutar

1. Clona o abre el proyecto en Android Studio.
2. Verifica que `app/google-services.json` esté presente (Firebase).
3. Sincroniza Gradle.
4. Ejecuta la app en un emulador o dispositivo:

```bash
./gradlew installDebug
```

O usa **Run** en Android Studio.

### Generar APK debug

```bash
./gradlew assembleDebug
```

El APK se genera en:

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Firebase

El proyecto usa las siguientes colecciones en Firestore:

| Colección | Contenido |
|-----------|-----------|
| `usuarios` | Perfiles y roles |
| `hoteles` | Catálogo de alojamientos |
| `habitaciones` | Habitaciones por hotel |
| `reservas` | Reservas de clientes |

Storage: carpetas `hoteles/`, `habitaciones/` y `perfiles/`.

> **Nota:** El pago es **simulado**. No se integra con Stripe ni procesadores reales. Los datos de tarjeta guardados permanecen solo en el dispositivo (SharedPreferences, enmascarados).

---

## Documentación

| Recurso | Descripción |
|---------|-------------|
| [docs/exposicion/00_INDICE.md](docs/exposicion/00_INDICE.md) | Guía de exposición del código (4 bloques) |
| [docs/exposicion/BLOQUE_1_Arquitectura.md](docs/exposicion/BLOQUE_1_Arquitectura.md) | Arquitectura y stack |
| [docs/exposicion/BLOQUE_2_Autenticacion.md](docs/exposicion/BLOQUE_2_Autenticacion.md) | Auth, perfil y roles |
| [docs/exposicion/BLOQUE_3_Cliente.md](docs/exposicion/BLOQUE_3_Cliente.md) | Flujo cliente y pago |
| [docs/exposicion/BLOQUE_4_Administrador.md](docs/exposicion/BLOQUE_4_Administrador.md) | Panel admin y Firebase |
| [docs/diagramas/](docs/diagramas/) | Diagramas de casos de uso (PlantUML / Mermaid) |

---

## Roles de usuario

| Rol | Acceso |
|-----|--------|
| **Cliente** | Búsqueda, reserva, pago, perfil |
| **Administrador** | Dashboard, CRUD, solicitudes, reservas |

Un cliente puede solicitar acceso admin desde su perfil (triple toque en "Tipo de cuenta"). Un administrador existente aprueba o rechaza la solicitud.

---

## Licencia

Proyecto académico — elaboración propia.
