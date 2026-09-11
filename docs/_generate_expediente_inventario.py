# -*- coding: utf-8 -*-
"""Inventario de evidencias para el expediente técnico y económico. No modifica el código de la app."""
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

OUT = Path(r"C:\Users\snake\AndroidStudioProjects\SelvaBooking\docs") / (
    "Expediente_tecnico_economico_inventario_evidencias.docx"
)


def set_run_font(run, size=11, bold=False, color=None):
    run.font.name = "Calibri"
    run._element.rPr.rFonts.set(qn("w:eastAsia"), "Calibri")
    run.font.size = Pt(size)
    run.bold = bold
    if color:
        run.font.color.rgb = color


def add_heading(doc, text, level=1):
    p = doc.add_heading(text, level=level)
    for run in p.runs:
        set_run_font(run, size=16 if level == 1 else 13 if level == 2 else 12, bold=True)
    return p


def add_p(doc, text, bold=False, italic=False):
    p = doc.add_paragraph()
    run = p.add_run(text)
    set_run_font(run, bold=bold)
    run.italic = italic
    p.paragraph_format.space_after = Pt(6)
    return p


def add_bullet(doc, text):
    p = doc.add_paragraph(text, style="List Bullet")
    for run in p.runs:
        set_run_font(run)
    return p


def add_table(doc, headers, rows):
    table = doc.add_table(rows=1, cols=len(headers))
    table.style = "Table Grid"
    hdr = table.rows[0].cells
    for i, h in enumerate(headers):
        hdr[i].text = h
        for p in hdr[i].paragraphs:
            for run in p.runs:
                set_run_font(run, size=10, bold=True)
    for row in rows:
        cells = table.add_row().cells
        for i, val in enumerate(row):
            cells[i].text = str(val)
            for p in cells[i].paragraphs:
                for run in p.runs:
                    set_run_font(run, size=10)
    doc.add_paragraph()
    return table


def pending(doc, text):
    p = doc.add_paragraph()
    r1 = p.add_run("Pendiente: ")
    set_run_font(r1, bold=True, color=RGBColor(0xB0, 0x47, 0x00))
    r2 = p.add_run(text)
    set_run_font(r2)
    return p


def build():
    doc = Document()
    for section in doc.sections:
        section.top_margin = Cm(2)
        section.bottom_margin = Cm(2)
        section.left_margin = Cm(2.2)
        section.right_margin = Cm(2.2)

    t = doc.add_paragraph()
    t.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = t.add_run("SELVA BOOKING")
    set_run_font(r, size=22, bold=True)
    st = doc.add_paragraph()
    st.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = st.add_run(
        "Inventario de evidencias para el expediente técnico y económico"
    )
    set_run_font(r, size=14, bold=True)
    sub = doc.add_paragraph()
    sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = sub.add_run(
        "Documento de análisis a partir del código y archivos existentes. "
        "No sustituye el expediente formal completo. No inventa costos, mercado ni métricas ausentes."
    )
    set_run_font(r, size=11)
    add_p(
        doc,
        "Alcance: repositorio Android StudioProjects/SelvaBooking. "
        "Fecha de análisis según el estado del código en el workspace. "
        "No se modificó el código de la aplicación para elaborar este documento.",
        italic=True,
    )
    add_p(
        doc,
        "Criterio: cada afirmación cita ruta de archivo. Lo que no aparece en el proyecto se marca Pendiente.",
        italic=True,
    )

    # --- 1 ---
    add_heading(doc, "1. Diagnóstico tecnológico", 1)
    add_heading(doc, "1.1 Problema que aparentemente resuelve el sistema", 2)
    add_p(
        doc,
        "El README y la documentación de exposición describen una aplicación Android de reservas "
        "ecoturísticas en Madre de Dios, Perú, para buscar lodges y hoteles, comparar precios, "
        "reservar habitaciones y gestionar el catálogo y las reservas según rol.",
    )
    add_bullet(doc, "Evidencia: README.md (párrafo inicial y características).")
    add_bullet(doc, "Evidencia: docs/exposicion/BLOQUE_1_Arquitectura.md (guion de inicio).")
    add_bullet(doc, "Evidencia de datos demo: app/src/main/java/com/company/selvabooking/data/SampleData.kt (hoteles en Puerto Maldonado, Tambopata y otras localidades de Madre de Dios; precios en números usados con formato es-PE).")
    add_p(
        doc,
        "El código no contiene un diagnóstico formal de stakeholders, encuestas, estudio de mercado ni "
        "descripción del problema fuera de esos textos de producto.",
    )
    pending(
        doc,
        "descripción formal del problema (causas, magnitud, población afectada, alternativas actuales fuera de la app).",
    )

    add_heading(doc, "1.2 Situación actual identificable desde el proyecto", 2)
    add_bullet(doc, "Existe un cliente Android nativo (package com.company.selvabooking, versionName 1.0, versionCode 1) en app/build.gradle.kts.")
    add_bullet(doc, "El backend no es un servidor propio: se usan SDKs de Firebase (Auth, Firestore, Storage) según app/build.gradle.kts y gradle/libs.versions.toml.")
    add_bullet(doc, "El pago no es un procesador real: PaymentViewModel.confirmPayment() espera 1500 ms y llama confirmReservation (app/src/main/java/com/company/selvabooking/viewmodel/PaymentViewModel.kt). README.md indica explícitamente que el pago es simulado.")
    add_bullet(doc, "Hay seed de catálogo si Firestore está vacío: AuthRepository.seedSampleDataIfNeeded() (referenciado desde AdminDashboardViewModel.kt).")
    add_bullet(doc, "Hay APK debug documentado: README.md (assembleDebug → app/build/outputs/apk/debug/app-debug.apk).")
    add_p(
        doc,
        "El README.md describe solo roles Cliente y Administrador y solicitudes de acceso admin; "
        "el código actual define SuperAdmin, Administrador, EncargadoHotel y Cliente (UserRole.kt). "
        "Esa discrepancia es un hallazgo documental, no un diagnóstico de negocio.",
    )

    add_heading(doc, "1.3 Necesidad tecnológica que cubre", 2)
    add_p(doc, "A partir de funciones implementadas (no de un estudio de necesidad):")
    add_bullet(doc, "Catálogo de alojamientos con búsqueda/filtros (ui/client/SearchScreen.kt, HomeScreen.kt).")
    add_bullet(doc, "Reserva con fechas, huéspedes, inventario de habitaciones (BookingScreen.kt, Room.kt stock/cantidad, ReservationRepository.kt).")
    add_bullet(doc, "Cuentas y roles con Firebase Auth + documento en colección usuarios (FirebaseAuthService.kt, AuthRepository.kt, User.kt).")
    add_bullet(doc, "Panel de gestión diferenciado por SuperAdmin, Administrador limitado y Encargado (NavigationDrawer.kt, AdminDataScope.kt).")
    pending(doc, "justificación de necesidad frente a competidores, volumen de turismo o brecha digital; no hay esos datos en el repo.")

    add_heading(doc, "1.4 Tecnologías actualmente utilizadas", 2)
    add_table(
        doc,
        ["Tecnología", "Uso en el proyecto", "Evidencia"],
        [
            ["Kotlin 2.2.10", "Lenguaje de la app", "gradle/libs.versions.toml"],
            ["Android Gradle Plugin 9.2.1", "Build Android", "gradle/libs.versions.toml"],
            ["compileSdk 36 / minSdk 24 / targetSdk 36", "Plataforma Android", "app/build.gradle.kts"],
            ["Jetpack Compose + Material 3", "UI", "app/build.gradle.kts; ui/theme/"],
            ["Navigation Compose 2.9.0", "Rutas", "libs.versions.toml; navigation/NavGraph.kt"],
            ["Lifecycle ViewModel + Coroutines + Flow", "Estado asíncrono", "libs.versions.toml; viewmodel/"],
            ["Firebase BOM 33.12.0", "Auth, Firestore, Storage", "libs.versions.toml; data/firebase/"],
            ["Plugin google-services 4.4.2", "Vinculación Firebase Android", "app/build.gradle.kts"],
            ["Coil 2.7.0", "Imágenes por URL", "libs.versions.toml"],
            ["JUnit / Espresso / Compose test", "Dependencias de prueba (plantillas)", "app/build.gradle.kts; ExampleUnitTest.kt"],
        ],
    )

    add_heading(doc, "1.5 Información faltante", 2)
    pending(doc, "diagnóstico organizacional (proceso actual de reservas sin la app).")
    pending(doc, "inventario de sistemas heredados o competencia.")
    pending(doc, "restricciones legales/regulatorias formales (solo hay texto de términos en ui/auth/TermsAndConditions.kt).")
    pending(doc, "criterios de éxito de negocio (ocupación, conversión) fuera de las métricas de UI del dashboard.")

    # --- 2 ---
    add_heading(doc, "2. Especificación de requerimientos", 1)
    add_p(
        doc,
        "Solo se listan funciones presentes en código o pantallas enlazadas en NavGraph.kt. "
        "No se añaden requisitos “deseables” no implementados.",
    )

    add_heading(doc, "2.1 Requerimientos funcionales (implementados)", 2)
    add_table(
        doc,
        ["ID inf.", "Función observada", "Evidencia"],
        [
            ["RF-INV-01", "Iniciar sesión correo/contraseña", "ui/auth/AuthScreens.kt; data/firebase/FirebaseAuthService.kt"],
            ["RF-INV-02", "Registrarse como cliente (nombre, teléfono, correo, contraseña, términos)", "AuthScreens.kt; ValidationUtils.kt; AuthRepository.kt"],
            ["RF-INV-03", "Recuperar contraseña por correo Firebase", "Routes.FORGOT_PASSWORD; AuthScreens.kt"],
            ["RF-INV-04", "Explorar inicio y búsqueda sin cuenta", "guestDrawerItems en NavigationDrawer.kt; GuestAuthRequiredDialog.kt"],
            ["RF-CLI-01", "Filtrar/ordenar hoteles (ciudad, precio, estrellas, etc.)", "ui/client/SearchScreen.kt y ViewModel asociado"],
            ["RF-CLI-02", "Ver detalle, galería, habitaciones con stock", "HotelDetailScreen.kt; Room.isReservable"],
            ["RF-CLI-03", "Crear reserva y pagar de forma simulada", "BookingScreen.kt; PaymentScreen.kt; PaymentViewModel.confirmPayment"],
            ["RF-CLI-04", "Ver mis reservas (estados públicos Confirmada/Terminada)", "MyReservationsScreen.kt; ReservationStatus.publicStatuses"],
            ["RF-CLI-05", "Comentar hotel si hay reserva previa; recalcular calificación", "HotelDetailViewModel; HotelRatingCalculator.kt; colección resenas"],
            ["RF-CLI-06", "Editar perfil, foto, tarjeta guardada local, alternar rol si aplica", "ProfileScreen.kt; ProfileViewModel.kt; SavedCardRepository.kt"],
            ["RF-ENC-01", "Completar perfil de encargado si perfilCompleto=false", "GerenteProfileCompletionScreen.kt; User.needsProfileCompletion"],
            ["RF-ENC-02", "CRUD de su hotel (máximo asociado por propietarioId)", "MANAGER_HOTELS; AdminHotelsViewModel / pantallas manager"],
            ["RF-ENC-03", "CRUD habitaciones e inventario (cantidad/stock)", "AdminRoomsScreen.kt; Room.kt"],
            ["RF-ENC-04", "Historial de reservas de su hotel", "MANAGER_RESERVATIONS"],
            ["RF-ENC-05", "Ver comentarios de su hotel", "ManagerReviewsScreen.kt"],
            ["RF-ADM-01", "Dashboard acotado a su red (encargados con creadoPorAdminId)", "AdminDashboardViewModel.kt; AdminDataScope.kt"],
            ["RF-ADM-02", "Consultar hoteles/habitaciones/reservas de su red (lectura según pantallas)", "limitedAdminDrawerItems; AdminHotelsScreen / AdminReservationsScreen"],
            ["RF-ADM-03", "Crear y eliminar encargados propios", "AdminGerentesScreen.kt; AuthRepository"],
            ["RF-ADM-04", "Ver comentarios de hoteles de su red", "AdminLimitedReviewsScreen.kt; ruta ADMIN_REVIEWS"],
            ["RF-SA-01", "Dashboard global (hoteles, habitaciones, reservas, ingresos sumados, usuarios, encargados, administradores)", "AdminDashboardViewModel.kt"],
            ["RF-SA-02", "CRUD hoteles y habitaciones global; filtros sin encargado / sin admin", "Routes.AdminHotelsFilter; AdminHotelsScreen"],
            ["RF-SA-03", "Gestionar reservas globales (crear/editar según AdminReservationsViewModel)", "AdminReservationsScreen.kt"],
            ["RF-SA-04", "Gestionar administradores y todos los encargados", "AdminAdministradoresScreen.kt; AdminGerentesScreen.kt"],
            ["RF-SA-05", "Listar usuarios y filtrar por rol", "AdminUsersScreen.kt"],
            ["RF-SA-06", "Auditoría de cambios de hotel/habitación y revertir", "AuditLog.kt; AdminAuditScreen.kt; AuditRepository.kt"],
            ["RF-SA-07", "Ver reseñas por hotel", "HotelReviewsScreen.kt; ADMIN_HOTEL_REVIEWS"],
            ["RF-COM-01", "Soporte/FAQ", "ui/support/SupportScreen.kt"],
            ["RF-COM-02", "Cerrar sesión", "NavigationDrawer (isLogout); AuthViewModel"],
            ["RF-SYS-01", "Pasar reservas Confirmada a Terminada si fechaSalida ya pasó", "ReservationRepository.expireFinishedReservations"],
            ["RF-SYS-02", "Carga de datos demo y migración de staff para hoteles huérfanos", "AuthRepository (seed y migrate); Constants.MIGRATION_*"],
        ],
    )

    add_heading(doc, "2.2 Requerimientos no funcionales (observables, no inventados)", 2)
    add_table(
        doc,
        ["Observación", "Evidencia", "Límite"],
        [
            ["App requiere Internet", "AndroidManifest.xml: INTERNET, ACCESS_NETWORK_STATE", "No hay SLA de red en el repo"],
            ["Sesión con Firebase Auth", "FirebaseAuthService.kt", "No hay política de timeout documentada"],
            ["Imágenes Storage: tipo image/* y tamaño < 5 MB en reglas", "storage.rules (isImageUpload)", "Solo reglas de Storage en el repo"],
            ["Moneda formateada es-PE", "DateUtils.formatCurrency", "No hay tipo de cambio ni política de precios"],
            ["UI Compose Material 3, tema verde", "ui/theme/Color.kt, Theme.kt", "No hay guía de accesibilidad formal"],
            ["minSdk 24", "app/build.gradle.kts", "No hay matriz de dispositivos de prueba"],
            ["ProGuard definido pero minify desactivado en release", "app/build.gradle.kts isMinifyEnabled = false", "No hay hardening de release activado"],
        ],
    )
    pending(doc, "RNF cuantitativos (tiempo de respuesta, disponibilidad %, concurrencia, RPO/RTO). No están en el proyecto.")
    pending(doc, "requisitos de cumplimiento (Ley de protección de datos, PCI). El pago es local/simulado; no hay certificación.")

    add_heading(doc, "2.3 Roles de usuario", 2)
    add_p(doc, "Enum en domain/model/UserRole.kt:")
    add_table(
        doc,
        ["Valor persistido (rol)", "Etiqueta UI", "Notas en código"],
        [
            ["Cliente", "Cliente", "Registro por defecto"],
            ["SuperAdmin", "SuperAdmin", "hasFullAdminPowers()"],
            ["Administrador", "Administrador", "isLimitedAdmin(); hasAdminPanelAccess()"],
            ["EncargadoHotel", "Encargado del Hotel", "también acepta legado GerenteHotel (fromString)"],
        ],
    )
    add_p(
        doc,
        "El invitado no es un valor de UserRole: es el estado sin sesión (guestDrawerItems en NavigationDrawer.kt).",
    )

    add_heading(doc, "2.4 Funcionalidades por rol", 2)
    add_p(
        doc,
        "Matriz alineada al menú real (NavigationDrawer.kt) y a docs/diagramas-v2/09_matriz_actores.md, "
        "verificada contra NavGraph.kt. La matriz markdown es más antigua en algunos puntos; prevalece el código del drawer.",
    )
    add_table(
        doc,
        ["Rol", "Entradas de menú / rutas principales"],
        [
            ["Invitado", "Inicio, Buscar, Soporte, Iniciar sesión, Crear cuenta"],
            ["Cliente", "Inicio, Buscar, Mis Reservas, Mi Cuenta, Soporte, Cerrar sesión"],
            ["Encargado", "Mi Hotel, Historial de reservas, Comentarios, Mi Cuenta, Soporte"],
            ["Administrador", "Dashboard, Encargados, Hoteles (consulta), Comentarios, Reservas, Mi Cuenta, Soporte"],
            ["SuperAdmin", "Dashboard, Administradores, Encargados, Hoteles, Reservas, Registro y respaldos, Mi Cuenta, Soporte; además rutas ADMIN_USERS y reseñas por hotel en NavGraph"],
        ],
    )

    add_heading(doc, "2.5 Posibles casos de uso (derivados de pantallas existentes)", 2)
    add_p(
        doc,
        "Hay diagramas PlantUML en docs/uml/*.puml y PNG en docs/uml/imagenes/. "
        "Los casos de uso inferibles coinciden con los RF de la sección 2.1 (login, registro, explorar, reservar, pagar simulado, reseñas, CRUD hotel/habitación, paneles, auditoría).",
    )

    add_heading(doc, "2.6 Historias de usuario inferidas de lo implementado", 2)
    add_bullet(doc, "Como invitado, quiero ver hoteles y buscar, para decidir si me registro (guestDrawerItems + Home/Search).")
    add_bullet(doc, "Como invitado, quiero registrarme o iniciar sesión, para reservar (AuthScreens; GuestAuthRequiredDialog al reservar/pagar).")
    add_bullet(doc, "Como cliente, quiero reservar una habitación disponible y confirmar un pago simulado, para obtener estado Confirmada (Booking + PaymentViewModel + ReservationStatus).")
    add_bullet(doc, "Como cliente, quiero dejar una reseña de un hotel donde ya reservé, para actualizar la calificación mostrada (Resena + HotelRatingCalculator).")
    add_bullet(doc, "Como encargado, quiero registrar o editar un hotel y sus habitaciones, para publicar inventario (MANAGER_HOTELS / rooms).")
    add_bullet(doc, "Como administrador, quiero crear encargados ligados a mi id, para ver solo la red de hoteles de esos encargados (creadoPorAdminId + AdminDataScope).")
    add_bullet(doc, "Como SuperAdmin, quiero revertir un cambio auditado de hotel o habitación, para restaurar datos (AdminAuditScreen + AuditLog.canRevert).")
    add_p(doc, "No se redactan historias de usuario para funciones no cableadas en navegación.")

    add_heading(doc, "2.7 Información faltante / no inventar", 2)
    add_bullet(doc, "AdminRequestsScreen.kt y AdminRequestsViewModel.kt existen, pero NavGraph.kt no declara esa ruta: la aprobación de solicitudes admin no está expuesta en el menú actual.")
    add_bullet(doc, "README.md habla de cancelación de reservas; ReservationStatus actual mapea “Cancelada” a TERMINADA (ReservationStatus.fromString). No hay estado Cancelada separado en el enum vigente.")
    pending(doc, "documento SRS formal, criterios de aceptación firmados, priorización MoSCoW de negocio.")
    pending(doc, "requisitos de pasarela de pago real, facturación electrónica, o app iOS/web: no existen en el repo.")

    # --- 3 ---
    add_heading(doc, "3. Arquitectura tecnológica e infraestructura", 1)
    add_heading(doc, "3.1 Frontend", 2)
    add_p(
        doc,
        "Única capa de presentación: Android Jetpack Compose (sin layouts XML de pantallas). "
        "Punto de entrada MainActivity.kt → SelvaBookingTheme + SelvaNavGraph. "
        "Pantallas bajo ui/admin, ui/auth, ui/client, ui/profile, ui/splash, ui/support, ui/navigation, ui/components, ui/theme.",
    )

    add_heading(doc, "3.2 Backend / APIs / servicios", 2)
    add_p(
        doc,
        "No hay API REST propia ni módulo servidor en este repositorio. "
        "La app habla con Firebase mediante SDK (FirebaseAuthService, FirestoreService, StorageService en data/firebase/). "
        "No hay Cloud Functions, ni backend Kotlin/Node en el árbol del proyecto.",
    )
    add_p(
        doc,
        "Proyecto Firebase vinculado: project_id asassas-95da4, storage_bucket asassas-95da4.firebasestorage.app, "
        "package_name com.company.selvabooking (app/google-services.json). "
        "No se reproducen claves API en este documento.",
    )

    add_heading(doc, "3.3 Frameworks, lenguajes y dependencias principales", 2)
    add_p(doc, "Ver tabla de la sección 1.4. Catálogo: gradle/libs.versions.toml y app/build.gradle.kts.")

    add_heading(doc, "3.4 Comunicación entre componentes", 2)
    add_p(doc, "Patrón MVVM documentado en README.md y docs/exposicion/BLOQUE_1_Arquitectura.md:")
    add_p(
        doc,
        "UI Compose observa StateFlow/UiState → ViewModel llama Repository → Repository usa servicios Firebase o SharedPreferences. "
        "SelvaBookingApplication.kt instancia repositorios (lazy) para los ViewModels.",
    )

    add_heading(doc, "3.5 Autenticación y autorización", 2)
    add_bullet(doc, "Autenticación: correo y contraseña Firebase Auth (FirebaseAuthService.kt).")
    add_bullet(doc, "Perfil y rol: documento usuarios/{uid} (User.kt, FirestoreService).")
    add_bullet(doc, "Autorización de alcance de datos en cliente: AdminDataScope.kt (filtra por creadoPorAdminId y propietarioId). SuperAdmin no usa ese filtro en el dashboard.")
    add_bullet(doc, "storage.rules: lectura de imágenes si hay sesión; escritura de hoteles/habitaciones si el documento usuario tiene rol exactamente igual a 'Administrador'. No hay en el archivo comprobación de SuperAdmin ni EncargadoHotel.")
    add_p(doc, "No existe firestore.rules ni firestore.indexes.json en el repositorio (búsqueda en el proyecto: 0 coincidencias).")
    pending(doc, "reglas de Firestore versionadas; modelo de autorización servidor para Encargado/SuperAdmin en Storage.")

    add_heading(doc, "3.6 Almacenamiento", 2)
    add_bullet(doc, "Cloud Firestore: colecciones en Constants.kt.")
    add_bullet(doc, "Firebase Storage: prefijos hoteles/, habitaciones/, perfiles/ (Constants.kt; storage.rules).")
    add_bullet(doc, "SharedPreferences selva_saved_payment_card: últimos 4 dígitos y datos de facturación, no el PAN completo (SavedCardRepository.kt).")
    add_p(doc, "No hay Room, SQLite ni SQLCipher en las dependencias Gradle.")

    add_heading(doc, "3.7 Configuración de despliegue", 2)
    add_bullet(doc, "Ejecución local: README.md (installDebug / Run Android Studio).")
    add_bullet(doc, "APK debug: assembleDebug.")
    add_bullet(doc, "Tarea uninstallLegacyApp desinstala com.example.selvabooking antes de installDebug (app/build.gradle.kts).")
    add_p(doc, "No hay Fastlane, Play Console, CI/CD, ni keystore de release en la documentación del README.")
    pending(doc, "pipeline de publicación, firma release, entornos (dev/prod) separados.")

    add_heading(doc, "3.8 Servicios cloud", 2)
    add_p(doc, "Google Firebase (Auth, Firestore, Storage) según google-services.json y dependencias. No hay AWS, Azure, Stripe ni otros proveedores en Gradle.")

    add_heading(doc, "3.9 Descripción textual para diagrama de arquitectura", 2)
    add_p(
        doc,
        "Dibujar tres bandas. Banda 1 — Dispositivo Android (minSdk 24): APK Selva Booking. Dentro: pantallas Compose, Navigation Compose, ViewModels con Coroutines/StateFlow, repositorios, pasarela de pago simulada (validación de formulario + delay), SharedPreferences de tarjeta. "
        "Banda 2 — Firebase/Google Cloud: Authentication (email/password); Cloud Firestore con colecciones usuarios, hoteles, habitaciones, reservas, resenas, audit_logs; Storage con objetos de imagen. "
        "Banda 3 — Actores: Invitado y Cliente usan flujo de catálogo/reserva; Encargado usa Mi Hotel; Administrador usa panel acotado; SuperAdmin usa panel global y auditoría. "
        "Flechas: UI→VM→Repository; Repository→Auth/Firestore/Storage por SDK HTTPS; Repository↔SharedPreferences solo para tarjeta. "
        "No dibujar API Gateway ni microservicios propios. Referencia visual existente: docs/uml/06_despliegue.puml y docs/uml/imagenes/despliegue.png (el PUML nombra audit_logs de forma distinta en un nodo; el nombre real de colección es audit_logs según Constants.kt).",
    )

    # --- 4 ---
    add_heading(doc, "4. Ingeniería de datos y base de datos", 1)
    add_heading(doc, "4.1 Motor", 2)
    add_p(
        doc,
        "Cloud Firestore (documentos/colecciones, NoSQL). No hay motor SQL, migraciones Flyway/Room ni scripts .sql en el repositorio.",
    )

    add_heading(doc, "4.2 Colecciones y entidades", 2)
    add_table(
        doc,
        ["Colección (Constants.kt)", "Modelo Kotlin", "Identificador"],
        [
            ["usuarios", "User.kt", "id = uid Auth"],
            ["hoteles", "Hotel.kt", "id documento"],
            ["habitaciones", "Room.kt", "id documento; hotelId"],
            ["reservas", "Reservation.kt", "id documento; userId, hotelId, roomId"],
            ["resenas", "Resena.kt", "id documento; hotelId, userId, reservationId"],
            ["audit_logs", "AuditLog.kt", "id documento; entityId, hotelId, actorUserId"],
        ],
    )

    add_heading(doc, "4.3 Campos principales (mapeo toMap/fromMap)", 2)
    add_p(doc, "Usuario: nombre, email, telefono, fotoUrl, rol, solicitudAdmin, puedeAlternarRol, perfilCompleto, rolAlternativo, creadoPorAdminId.")
    add_p(doc, "Hotel: nombre, ciudad, direccion, descripcion, categoria, estrellas, precioMinimo, calificacion, calificacionBase, imagenes, servicios, ubicacion (texto lat,lng en SampleData), destacado, oferta, propietarioId.")
    add_p(doc, "Habitación: hotelId, nombre, descripcion, precio, capacidad, cantidad, stock, disponible, imagenes.")
    add_p(doc, "Reserva: userId, hotelId, roomId, nombres denormalizados, fechas ingreso/salida (yyyy-MM-dd), huespedes, precioTotal, estado, createdAt.")
    add_p(doc, "Reseña: hotelId, userId, reservationId, userNombre, calificacion (1–5), comentario, createdAt.")
    add_p(doc, "Auditoría: timestamp, actor*, action (CREATE/UPDATE/DELETE), entityType (HOTEL/ROOM), entityId, entityLabel, hotelId, beforeData/afterData/relatedData, reverted, revertedAt, revertedBy.")

    add_heading(doc, "4.4 Relaciones (lógicas; Firestore no impone FK)", 2)
    add_bullet(doc, "User 1 — 0..1 Hotel como propietario (Hotel.propietarioId → User.id de Encargado).")
    add_bullet(doc, "User (Administrador) 1 — N User Encargado (User.creadoPorAdminId).")
    add_bullet(doc, "Hotel 1 — N Room (Room.hotelId).")
    add_bullet(doc, "User (Cliente) 1 — N Reservation (Reservation.userId).")
    add_bullet(doc, "Hotel 1 — N Reservation; Room 1 — N Reservation.")
    add_bullet(doc, "Hotel 1 — N Resena; Resena puede citar reservationId.")
    add_bullet(doc, "AuditLog N — 1 Hotel/Room vía entityId / hotelId.")
    add_p(doc, "No hay claves foráneas ni índices compuestos versionados en el repo. Consultas típicas: whereEqualTo propietarioId, hotelId, userId, rol, solicitudAdmin; orderBy createdAt / timestamp (FirestoreService.kt).")

    add_heading(doc, "4.5 ORM / migraciones / SQL", 2)
    add_p(doc, "No aplica ORM relacional. Serialización manual toMap/fromMap en cada data class. “Migración” de datos de negocio: AuthRepository.migrateOrphanHotelsWithStaff (emails encargadoN@gmail.com / administradorN@gmail.com y contraseña en Constants.MIGRATION_STAFF_PASSWORD), no es migración de esquema SQL.")

    add_heading(doc, "4.6 Datos principales almacenados / seed", 2)
    add_p(doc, "SampleData.kt define hoteles demo (p. ej. Eco Lodge Selva Verde, Amazonia Rainforest Resort, Cabañas Madre de Dios, Tambopata Jungle Lodge) con categorías Ecológico/Lujo/Económico/Aventura, estrellas, precios mínimos y URLs Unsplash.")

    add_heading(doc, "4.7 Esquema textual para DER (entidades lógicas)", 2)
    add_p(
        doc,
        "USUARIO (id PK, nombre, email, telefono, fotoUrl, rol, solicitudAdmin, puedeAlternarRol, perfilCompleto, rolAlternativo, creadoPorAdminId FK lógica a USUARIO). "
        "HOTEL (id PK, atributos de catálogo, propietarioId FK lógica a USUARIO). "
        "HABITACION (id PK, hotelId FK, precio, capacidad, cantidad, stock, disponible, imagenes). "
        "RESERVA (id PK, userId, hotelId, roomId, fechas, huespedes, precioTotal, estado, createdAt, campos denormalizados de nombres). "
        "RESENA (id PK, hotelId, userId, reservationId, calificacion, comentario, createdAt). "
        "AUDIT_LOG (id PK, timestamp, actorUserId, action, entityType, entityId, snapshots, reverted*). "
        "TARJETA_LOCAL (no es entidad Firestore: lastFour, expiry, cardholder, dirección; SharedPreferences). "
        "Relaciones: USUARIO crea RESERVA; USUARIO (encargado) posee HOTEL; HOTEL contiene HABITACION; RESERVA referencia HABITACION; RESENA sobre HOTEL; AUDIT_LOG sobre HOTEL o HABITACION.",
    )

    add_heading(doc, "4.8 Información faltante", 2)
    pending(doc, "reglas e índices Firestore exportados.")
    pending(doc, "diccionario de datos oficial, tamaños, retención, backup policy de Google Cloud.")
    pending(doc, "modelo dimensional (hechos/dimensiones) para BI aparte del dashboard operativo.")

    # --- 5 ---
    add_heading(doc, "5. MVP o prototipo funcional", 1)
    add_p(
        doc,
        "El proyecto se comporta como aplicación Android funcional conectada a Firebase (prototipo/MVP académico), no como mock de Figma únicamente. Hay exportación Figma en figma-export/ (no analizada como runtime).",
    )

    add_heading(doc, "5.1 Funcional — flujos que un usuario puede ejecutar", 2)
    add_bullet(doc, "Splash (2 s) → login o home según sesión (SplashScreen.kt, Constants.SPLASH_DELAY_MS).")
    add_bullet(doc, "Invitado: ver inicio/búsqueda; al reservar o pagar se exige auth (GuestAuthRequiredDialog.kt).")
    add_bullet(doc, "Cliente: registro/login, explorar, detalle, booking, pago simulado, mis reservas, perfil, reseña, soporte.")
    add_bullet(doc, "Encargado: completar perfil, gestionar hotel/habitaciones, ver reservas y comentarios.")
    add_bullet(doc, "Administrador: dashboard de red, encargados, consulta hoteles/reservas, comentarios de su red.")
    add_bullet(doc, "SuperAdmin: panel global, CRUD catálogo, reservas, admins, encargados, usuarios, auditoría, reseñas por hotel.")

    add_heading(doc, "5.2 Pantallas existentes (archivos *Screen.kt / Auth)", 2)
    add_p(
        doc,
        "SplashScreen, AuthScreens (login/registro/recuperación), GerenteProfileCompletionScreen, HomeScreen, SearchScreen, HotelDetailScreen, BookingScreen, PaymentScreen, MyReservationsScreen, ProfileScreen, SupportScreen, GuestAuthRequiredDialog, AdminDashboardScreen, AdminHotelsScreen, AdminRoomsScreen, AdminReservationsScreen, AdminAdministradoresScreen, AdminGerentesScreen, AdminAuditScreen, AdminUsersScreen, AdminLimitedReviewsScreen, HotelReviewsScreen, ManagerReviewsScreen, AdminRequestsScreen (código presente, no en NavGraph).",
    )

    add_heading(doc, "5.3 Qué puede considerarse MVP", 2)
    add_p(
        doc,
        "Núcleo MVP observable: catálogo Madre de Dios + reserva + pago simulado + roles de operación (encargado/admin/superadmin) + reseñas + dashboard de conteos. Coincide con características de README.md más los roles ampliados en código.",
    )

    add_heading(doc, "5.4 Clasificación Funcional / Parcialmente funcional / Pendiente", 2)
    add_table(
        doc,
        ["Estado", "Ítem", "Evidencia / motivo"],
        [
            ["Funcional", "Auth email/password, registro, recuperación", "AuthScreens + FirebaseAuthService"],
            ["Funcional", "Catálogo, búsqueda, detalle, reserva, inventario stock", "client/* + ReservationRepository"],
            ["Funcional", "Pago simulado y tarjeta en dispositivo", "PaymentViewModel delay 1500 ms; SavedCardRepository"],
            ["Funcional", "Paneles por rol cableados en NavGraph", "NavGraph.kt; NavigationDrawer.kt"],
            ["Funcional", "Auditoría y revertir (SuperAdmin)", "AdminAuditScreen; AuditLog"],
            ["Funcional", "Reseñas y recálculo de calificación", "ResenaRepository; HotelRatingCalculator"],
            ["Parcialmente funcional", "Pago", "No hay pasarela real (README y delay local)"],
            ["Parcialmente funcional", "Solicitudes de acceso admin", "Pantalla y ViewModel existen; no hay ruta en NavGraph.kt"],
            ["Parcialmente funcional", "README vs roles reales", "README desactualizado respecto a UserRole.kt"],
            ["Parcialmente funcional", "Reglas Storage vs roles actuales", "storage.rules solo rol Administrador"],
            ["Parcialmente funcional", "Pruebas automatizadas", "ExampleUnitTest solo 2+2=4; no cubren dominio"],
            ["Pendiente", "API/backend propio, web, iOS", "No hay módulos"],
            ["Pendiente", "Publicación Play Store / CI", "No hay evidencia de pipeline"],
            ["Pendiente", "firestore.rules en repo", "Archivo ausente"],
            ["Pendiente", "Dashboard BI analítico (Power BI, Looker, etc.)", "Solo métricas Compose en AdminDashboard"],
        ],
    )

    # --- 6 ---
    add_heading(doc, "6. Evaluación económica y financiera", 1)
    add_p(
        doc,
        "Búsqueda de costos, tarifas, CAPEX, OPEX, VAN, TIR, payback, B/C, WACC, precios de venta al cliente (salvo precioMinimo/precio de habitación como datos de catálogo) y licencias de pago: no hay hojas financieras, ni .xlsx de costos, ni variables de inversión en el código.",
    )
    add_heading(doc, "6.1 Lo que sí aparece (sin montos de proveedor)", 2)
    add_bullet(doc, "Servicios externos usados: Firebase Auth, Firestore, Storage (dependencias + google-services.json). El plan de facturación de Google no está en el repo.")
    add_bullet(doc, "Imágenes de seed: URLs de images.unsplash.com en SampleData.kt (no hay contrato ni tarifa).")
    add_bullet(doc, "Coil, Compose, Kotlin, Android SDK: dependencias open source en Gradle; no hay montos de licencia comercial.")
    add_bullet(doc, "README.md: “Proyecto académico — elaboración propia” / licencia no comercial detallada.")
    add_bullet(doc, "Precios de habitación/hotel en SampleData y campos precio / precioMinimo / precioTotal son datos de negocio de la app, no un modelo financiero de la empresa.")

    add_heading(doc, "6.2 CAPEX, OPEX, VAN, TIR, Payback, B/C", 2)
    pending(doc, "este apartado NO puede completarse con el repositorio. Faltan: inversión inicial, costos de desarrollo/hora, costos Firebase reales, dispositivos, dominio, Play Console, salarios, demanda, precio de comisión o suscripción, impuestos, horizonte, WACC o tasa de descuento, tipo de cambio, inflaciones.")
    add_p(doc, "No se inventan montos.")

    # --- 7 ---
    add_heading(doc, "7. Simulación de riesgos", 1)
    add_heading(doc, "A. Riesgos técnicos deducibles del proyecto (sin probabilidades)", 2)
    add_table(
        doc,
        ["Riesgo", "Por qué se deduce del código", "Evidencia"],
        [
            ["Dependencia total de Firebase", "Sin Firebase la app no autentica ni persiste", "data/firebase/*; google-services.json"],
            ["Sin reglas Firestore en el repo", "Autorización puede estar solo en cliente (AdminDataScope)", "Ausencia de firestore.rules; AdminDataScope.kt"],
            ["Desalineación Storage vs roles", "Escritura Storage exige rol Administrador literal", "storage.rules isAdmin()"],
            ["Pago no real / fraude académico", "Confirmación local con delay, sin PSP", "PaymentViewModel.kt; README.md"],
            ["Datos de tarjeta en dispositivo", "SharedPreferences; allowBackup true", "SavedCardRepository.kt; AndroidManifest allowBackup"],
            ["Contraseña de migración en código", "MIGRATION_STAFF_PASSWORD = 123456", "utils/Constants.kt"],
            ["google-services.json en el árbol", "Configuración de cliente Firebase versionada", "app/google-services.json"],
            ["Consultas get() de colecciones enteras", "Dashboard y expire recorren listados completos en cliente", "AdminDashboardViewModel; FirestoreService getAll*"],
            ["Disponibilidad = la de Google + red del dispositivo", "Permiso INTERNET obligatorio", "AndroidManifest.xml"],
            ["minify desactivado en release", "Código no ofuscado si se genera release así", "app/build.gradle.kts"],
            ["Pruebas de dominio casi nulas", "Regresiones no automatizadas", "ExampleUnitTest.kt"],
            ["Índices Firestore no versionados", "Consultas orderBy pueden fallar en un proyecto nuevo", "FirestoreService orderBy createdAt/timestamp"],
        ],
    )
    add_p(doc, "No se asignan P, impacto monetario ni correlaciones: el proyecto no contiene esos datos.")

    add_heading(doc, "B. Variables aún no definidas para Montecarlo / simulación económica", 2)
    add_p(doc, "Estas variables NO están en el código. Deben definirse fuera del repositorio:")
    add_bullet(doc, "demanda (reservas/mes, tasa de conversión invitado→pago)")
    add_bullet(doc, "precio (comisión, ticket promedio real de mercado, no solo precioTotal de reservas demo)")
    add_bullet(doc, "costos cloud Firebase (lecturas/escrituras/almacenamiento/Auth)")
    add_bullet(doc, "costos operativos (soporte, dispositivos, Play Console)")
    add_bullet(doc, "crecimiento y estacionalidad turismo Madre de Dios")
    add_bullet(doc, "tipo de cambio e inflación")
    add_bullet(doc, "probabilidad e impacto cuantificado de caídas de Firebase, churn, fraude")

    # --- 8 ---
    add_heading(doc, "8. Dashboard de Business Intelligence", 1)
    add_p(
        doc,
        "No hay herramienta BI (Power BI, Tableau, Looker Studio, BigQuery export) en el repositorio. "
        "Hay un dashboard operativo Compose que ya calcula KPIs a partir de Firestore.",
    )
    add_p(doc, "Fuente de implementación: viewmodel/AdminDashboardViewModel.kt y ui/admin/AdminDashboardScreen.kt.")

    add_table(
        doc,
        ["KPI propuesto", "Fuente", "Colección/origen", "Cálculo en código", "¿Datos existen?"],
        [
            ["Total hoteles", "AdminDashboardViewModel", "hoteles (flujo getHotelsFlow)", "size de lista (filtrada si admin limitado)", "Sí"],
            ["Total habitaciones", "mismo + RoomRepository.getAllRooms", "habitaciones", "filtro hotelId in hoteles del alcance", "Sí"],
            ["Total reservas públicas", "getAllReservationsFlow", "reservas", "filtro estado.isPublic (Confirmada/Terminada)", "Sí"],
            ["Reservas confirmadas", "mismo", "reservas", "count estado == CONFIRMADA", "Sí"],
            ["Reservas terminadas", "mismo", "reservas", "count estado == TERMINADA", "Sí"],
            ["Ingresos (suma precioTotal)", "mismo", "reservas.precioTotal", "sum de Confirmada o Terminada en el alcance", "Sí (es suma de reservas, no caja real ni pago PSP)"],
            ["Total usuarios", "getAllUsersFlow", "usuarios", "size; solo se asigna si isSuperAdmin", "Sí para SuperAdmin"],
            ["Total encargados", "getGerentesHotelFlow", "usuarios rol EncargadoHotel", "size (filtrado creadoPorAdminId si admin limitado)", "Sí"],
            ["Total administradores", "getAdministratorAccountsFlow", "usuarios rol Administrador", "size (dashboard SuperAdmin muestra el recuento global del snapshot)", "Sí"],
            ["Calificación hotel mostrada", "HotelRatingCalculator + calificacionBase + resenas", "hoteles + resenas", "base + deltas por estrella 1–5", "Sí"],
            ["Stock / inventario habitación", "Room.stock, Room.cantidad", "habitaciones", "campos directos; isReservable = disponible && stock>0", "Sí"],
            ["Reservas por cliente", "MyReservationsViewModel", "reservas where userId", "listado, no gráfico BI", "Sí"],
            ["Eventos de auditoría", "AdminAuditViewModel", "audit_logs", "listado timestamp/action; no KPI agregado en dashboard", "Sí datos; KPI agregado Pendiente en UI"],
            ["Conversión invitado→registro", "—", "—", "no hay analítica de eventos", "Pendiente"],
            ["CAC, LTV, churn", "—", "—", "—", "Pendiente"],
            ["Costos Firebase", "—", "consola Google (fuera del repo)", "—", "Pendiente"],
        ],
    )

    # --- Resumen ---
    add_heading(doc, "Tabla resumen por etapa", 1)
    add_table(
        doc,
        ["Etapa", "Se puede completar", "Nivel de avance", "Evidencia encontrada", "Información faltante"],
        [
            [
                "1. Diagnóstico tecnológico",
                "Parcialmente",
                "Stack y propósito de producto documentados; no hay diagnóstico de negocio formal",
                "README.md; BLOQUE_1; SampleData.kt; build.gradle.kts",
                "Problema formal, mercado, stakeholders, AS-IS organizacional",
            ],
            [
                "2. Especificación de requerimientos",
                "Parcialmente",
                "RF inferibles del código y roles; no hay SRS ni RNF cuantitativos",
                "UserRole.kt; NavGraph.kt; NavigationDrawer.kt; pantallas y repositorios",
                "SRS, aceptación, RNF medibles; RF de solicitudes admin no navegables",
            ],
            [
                "3. Arquitectura e infraestructura",
                "Sí",
                "MVVM + Firebase + Compose descritos y verificables",
                "app/build.gradle.kts; libs.versions.toml; data/firebase; 06_despliegue.puml; google-services.json",
                "CI/CD, entornos, firestore.rules, diagrama de red corporativa",
            ],
            [
                "4. Ingeniería de datos",
                "Sí",
                "Modelo documental completo vía data classes; sin SQL",
                "domain/model/*; Constants.kt; FirestoreService.kt",
                "Reglas/índices Firestore, DER oficial, retención/backups",
            ],
            [
                "5. MVP / prototipo",
                "Sí",
                "App ejecutable con flujos por rol; pago simulado; huecos menores de navegación",
                "ui/**; NavGraph.kt; PaymentViewModel.kt",
                "Pasarela real, tests de aceptación, publicación store",
            ],
            [
                "6. Evaluación económica",
                "No",
                "Cero cifras de inversión/costo/beneficio",
                "Solo indicios de servicios (Firebase) sin tarifas",
                "CAPEX, OPEX, precios de venta del negocio, demanda, WACC, horizonte, impuestos",
            ],
            [
                "7. Simulación de riesgos",
                "Parcialmente",
                "Riesgos técnicos identificables; sin Montecarlo",
                "storage.rules; Constants.kt; PaymentViewModel; ausencia firestore.rules",
                "Probabilidades, impactos monetarios, variables de demanda y costos cloud",
            ],
            [
                "8. Dashboard BI",
                "Parcialmente",
                "KPIs operativos en app; no hay capa analítica",
                "AdminDashboardViewModel.kt; colecciones Firestore",
                "Herramienta BI, ETL, KPIs de negocio (CAC/LTV), costos",
            ],
        ],
    )

    add_heading(doc, "Información que necesito proporcionar manualmente", 1)
    add_p(
        doc,
        "Solo datos que no se pueden obtener del código o archivos del proyecto:",
    )
    add_bullet(doc, "Descripción formal del problema (contexto institucional, proceso actual sin app, dolor cuantificado).")
    add_bullet(doc, "Mercado objetivo, tamaño de mercado, competencia y propuesta de valor comercial (más allá de “reservas ecoturísticas en Madre de Dios” del README).")
    add_bullet(doc, "Modelo de ingresos (comisión, suscripción hotel, publicidad) y precios de venta del servicio Selva Booking como negocio.")
    add_bullet(doc, "Proyecciones de clientes/reservas, tasa de crecimiento, estacionalidad.")
    add_bullet(doc, "Inversión inicial (CAPEX): equipos, desarrollo, diseño, constitución, Play Console, etc.")
    add_bullet(doc, "Costos operativos (OPEX): Firebase real, soporte, internet, salarios, mantenimiento.")
    add_bullet(doc, "WACC o tasa de descuento, horizonte financiero, inflación, tipo de cambio.")
    add_bullet(doc, "Supuestos de riesgo para Montecarlo (distribuciones de demanda, costos, caídas de servicio) y valores P × impacto.")
    add_bullet(doc, "Política de backups, RTO/RPO, y reglas Firestore de producción (si existen solo en consola).")
    add_bullet(doc, "Requisitos no funcionales numéricos (tiempos, usuarios concurrentes, disponibilidad).")
    add_bullet(doc, "Criterios legales (protección de datos, términos definitivos, PCI si hubiera pago real).")
    add_bullet(doc, "Organigrama y responsables del expediente; presupuesto institucional.")

    add_heading(doc, "Notas de consistencia documental", 1)
    add_bullet(doc, "README.md y BLOQUE_1 aún mencionan 2 roles y solicitudes admin; el código vigente tiene 4 roles persistidos + invitado y no enruta AdminRequestsScreen.")
    add_bullet(doc, "docs/uml/06_despliegue.puml etiqueta un nodo como registros_auditoria; Constants.COLLECTION_AUDIT_LOGS = audit_logs.")
    add_bullet(doc, "Este Word es un inventario de evidencias para completar el expediente; no es el expediente económico calculado.")

    doc.save(OUT)
    print(f"Wrote {OUT}")


if __name__ == "__main__":
    build()
