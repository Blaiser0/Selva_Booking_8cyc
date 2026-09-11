# -*- coding: utf-8 -*-
"""Genera Selva Booking - mov actualizado.docx siguiendo la estructura del entregable original."""
from __future__ import annotations

from copy import deepcopy
from pathlib import Path

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
from docx.shared import Cm, Pt

BASE = Path(r"C:\Users\snake\AndroidStudioProjects\SelvaBooking\docs")
WIREFRAMES = BASE / "_docx_images"
UML = BASE / "uml" / "imagenes"
OUT = BASE / "Selva Booking - mov actualizado.docx"
TEMPLATE = BASE / "Selva Booking - mov.docx"


def clear_document_body(doc: Document) -> None:
    body = doc.element.body
    for child in list(body):
        if child.tag == qn("w:sectPr"):
            continue
        body.remove(child)


def set_paragraph(text: str, style: str = "Normal", align=None, bold=False):
    p = doc.add_paragraph(style=style)
    if align is not None:
        p.alignment = align
    run = p.add_run(text)
    if bold:
        run.bold = True
    return p


def add_blank():
    doc.add_paragraph("", style="Normal")


def add_bullet(text: str):
    return doc.add_paragraph(text, style="List Paragraph")


def add_table(headers, rows):
    table = doc.add_table(rows=1, cols=len(headers))
    table.style = "Table Grid"
    hdr = table.rows[0].cells
    for i, h in enumerate(headers):
        hdr[i].text = h
    for row in rows:
        cells = table.add_row().cells
        for i, val in enumerate(row):
            cells[i].text = val
    return table


def add_caption(text: str):
    p = doc.add_paragraph(style="Caption")
    p.add_run(text)
    return p


def add_figure(caption: str, image_path: Path, width_cm: float = 14.0):
    add_caption(caption)
    if image_path.exists():
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run()
        run.add_picture(str(image_path), width=Cm(width_cm))
    else:
        doc.add_paragraph(f"[Imagen no encontrada: {image_path.name}]", style="Normal")
    add_blank()
    set_paragraph("Fuente: Elaboración propia", style="Normal")
    add_blank()


def cover_page():
    for line in [
        "UNIVERSIDAD NACIONAL AMAZÓNICA DE MADRE DE DIOS",
        "FACULTAD DE INGENIERÍA",
        "ESCUELA PROFESIONAL DE INGENIERÍA DE SISTEMAS E INFORMÁTICA",
        "",
        "ENTREGABLE 01",
        "",
        "“SELVA BOOKING: APLICACIÓN MÓVIL PARA CENTRALIZAR Y RESERVAR ALOJAMIENTOS EN LA REGIÓN DE MADRE DE DIOS”",
        "",
        "AUTORES:",
        "CONDORI SAHUARICO, Liz Yeiza",
        "UCEDA JALLURANA, Renzo Jesus",
        "CHICATA ACOSTA, Jasseir James",
        "PEREIRA RAMPAS, Korven Joshua",
        "",
        "DOCENTE:",
        "Mg. TINEO VILCHEZ, Francisco Javier",
        "",
        "Puerto Maldonado, julio 2026",
    ]:
        p = doc.add_paragraph(line, style="Normal")
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER


def section_presentacion():
    set_paragraph("PRESENTACIÓN", "TÍTULO 1 MOV")
    add_blank()
    doc.add_paragraph(
        "El presente proyecto, titulado “Selva Booking: Aplicación móvil para centralizar y reservar "
        "alojamientos en la región de Madre de Dios”, tiene como objetivo aplicar y consolidar los "
        "conocimientos adquiridos en el desarrollo de software. Además, busca proponer una solución "
        "tecnológica práctica al problema de dispersión de la oferta hotelera y ecoturística, "
        "incorporando roles diferenciados (cliente, encargado de hotel, administrador y SuperAdmin), "
        "gestión de inventario en tiempo real, reseñas, auditoría y un panel de supervisión acorde "
        "a cada perfil de usuario.",
        style="Normal",
    )
    add_blank()


def section_introduccion():
    set_paragraph("INTRODUCCIÓN", "TÍTULO 1 MOV")
    add_blank()
    doc.add_paragraph(
        "El turismo en la Amazonía peruana, específicamente en Puerto Maldonado y la zona de Tambopata, "
        "cuenta con una amplia variedad de opciones de alojamiento, desde hoteles urbanos hasta eco-lodges "
        "en plena naturaleza. Sin embargo, los turistas suelen enfrentar dificultades para comparar precios, "
        "disponibilidad y servicios porque la información está fragmentada en múltiples canales.",
        style="Normal",
    )
    doc.add_paragraph(
        "Ante ello, Selva Booking es una aplicación Android desarrollada con Kotlin y Jetpack Compose "
        "que centraliza la oferta de alojamientos, permite reservar habitaciones con control de inventario, "
        "incorpora una pasarela de pago simulada y ofrece paneles de gestión para encargados de hotel, "
        "administradores limitados y SuperAdmin. El backend utiliza Firebase (Authentication, Firestore y "
        "Storage), siguiendo arquitectura MVVM y metodología ágil XP.",
        style="Normal",
    )
    add_blank()


def section_indice():
    set_paragraph("ÍNDICE", "TÍTULO 1 MOV")
    add_blank()
    for item in [
        "DEFINICIÓN DEL PROYECTO",
        "MARCO TEÓRICO",
        "METODOLOGÍA DE LA INVESTIGACIÓN",
        "WIREFRAME",
        "DIAGRAMACIÓN UML",
        "CONCLUSIONES",
        "RECOMENDACIONES",
        "REFERENCIAS BIBLIOGRÁFICAS",
        "ANEXOS",
    ]:
        doc.add_paragraph(item, style="Normal")
    add_blank()


def section_indice_figuras():
    set_paragraph("ÍNDICE DE FIGURAS", "TÍTULO 1 MOV")
    add_blank()
    figures = [
        (1, "Pantalla de Bienvenida (Splash)"),
        (2, "Inicio de Sesión"),
        (3, "Registro de Usuario"),
        (4, "Recuperar Contraseña"),
        (5, "Mi Perfil"),
        (6, "Soporte y Ayuda"),
        (7, "Menú Lateral (Cliente)"),
        (8, "Menú Lateral (Administrador / SuperAdmin)"),
        (9, "Pantalla de Inicio"),
        (10, "Comparar Alojamientos (Búsqueda)"),
        (11, "Filtros de Búsqueda"),
        (12, "Detalle del Alojamiento"),
        (13, "Completar Reserva"),
        (14, "Resumen de Pago"),
        (15, "Pago Confirmado"),
        (16, "Mis Reservas"),
        (17, "Panel de Administración (Dashboard)"),
        (18, "Gestión de Encargados y Administradores"),
        (19, "Gestión de Alojamientos"),
        (20, "Formulario de Nuevo Alojamiento"),
        (21, "Gestión de Habitaciones"),
        (22, "Nueva Reserva (Manual)"),
        (23, "Panel General de Reservas"),
        (24, "Detalle de Reserva (Administrador)"),
        (25, "Diagrama de casos de uso general"),
        (26, "Diagrama de casos de uso — Invitado"),
        (27, "Diagrama de casos de uso — Cliente"),
        (28, "Diagrama de casos de uso — Encargado de hotel"),
        (29, "Diagrama de casos de uso — Administrador limitado"),
        (30, "Diagrama de casos de uso — SuperAdmin"),
        (31, "Diagrama de secuencia — Cliente (reserva y pago)"),
        (32, "Diagrama de secuencia — Encargado de hotel"),
        (33, "Diagrama de secuencia — SuperAdmin"),
        (34, "Diagrama de estados — Reserva"),
        (35, "Diagrama de clases — Dominio"),
        (36, "Diagrama de componentes — MVVM"),
        (37, "Diagrama de despliegue"),
    ]
    for num, title in figures:
        p = doc.add_paragraph(style="table of figures")
        p.add_run(f"Figura {num}: {title}\t{num + 11}")
    add_blank()


def section_indice_tablas():
    set_paragraph("ÍNDICE DE TABLAS", "TÍTULO 1 MOV")
    add_blank()
    for num, title in [
        (1, "Glosario de términos"),
        (2, "Fases de la metodología XP aplicadas a Selva Booking"),
        (3, "Especificaciones del entorno de desarrollo y tecnologías aplicadas"),
        (4, "Presupuesto estimado del proyecto"),
    ]:
        p = doc.add_paragraph(style="table of figures")
        p.add_run(f"Tabla {num}: {title}\t{num + 4}")
    add_blank()


def section_definicion():
    set_paragraph("DEFINICIÓN DEL PROYECTO", "TÍTULO 1 MOV")
    set_paragraph("Descripción del Proyecto", "TÍTULO 2 MOV")
    doc.add_paragraph(
        "Selva Booking es una aplicación móvil Android que agrupa y gestiona reservas de hoteles y "
        "eco-lodges en Madre de Dios. Los clientes pueden buscar alojamientos, filtrar por ciudad, "
        "precio y categoría, reservar habitaciones con fechas y huéspedes, pagar mediante una pasarela "
        "simulada y publicar reseñas. Los encargados de hotel gestionan un hotel (máximo uno), sus "
        "habitaciones, inventario y comentarios de huéspedes. Los administradores limitados supervisan "
        "la red de encargados que ellos crearon; el SuperAdmin tiene control global, auditoría y gestión "
        "de administradores. La app usa Firebase, arquitectura MVVM y navegación por roles.",
        style="Normal",
    )

    set_paragraph("Formulación del problema", "TÍTULO 2 MOV")
    set_paragraph("Problema general", "TÍTULO 3")
    doc.add_paragraph(
        "¿De qué manera una aplicación móvil centralizada puede optimizar el proceso de búsqueda, "
        "reserva y supervisión de alojamientos para turistas y operadores en Madre de Dios?",
        style="Normal",
    )
    set_paragraph("Problemas específicos", "TÍTULO 3")
    add_bullet(
        "¿De qué forma un sistema centralizado facilita al turista organizar y dar seguimiento a sus "
        "reservas sin depender de múltiples canales?"
    )
    add_bullet(
        "¿Cómo ayuda un panel por roles (encargado, administrador, SuperAdmin) a gestionar inventario, "
        "reservas y cuentas de forma segura y trazable?"
    )
    add_bullet(
        "¿Cómo contribuye el control de inventario en tiempo real a evitar sobreventa de habitaciones?"
    )

    set_paragraph("Objetivos", "TÍTULO 2 MOV")
    set_paragraph("Objetivo general", "TÍTULO 3")
    doc.add_paragraph(
        "Desarrollar una aplicación móvil nativa en Android que unifique la oferta de alojamientos en "
        "Madre de Dios, permitiendo buscar, comparar, reservar y administrar hoteles, habitaciones y "
        "reservas con roles diferenciados y backend en Firebase.",
        style="Normal",
    )
    set_paragraph("Objetivos específicos", "TÍTULO 3")
    for obj in [
        "Implementar módulo cliente con registro (teléfono de 9 dígitos), búsqueda, reserva, pago simulado y reseñas.",
        "Integrar Firebase Auth, Cloud Firestore y Firebase Storage con arquitectura MVVM.",
        "Desarrollar módulo encargado: un hotel por usuario, CRUD de habitaciones, stock e historial de reservas.",
        "Implementar panel administrador limitado (supervisión de encargados creados) y panel SuperAdmin (CRUD global y auditoría).",
        "Evaluar usabilidad y funcionamiento en emulador y dispositivo Android real.",
    ]:
        add_bullet(obj)

    set_paragraph("Justificación", "TÍTULO 2 MOV")
    doc.add_paragraph(
        "La dispersión de la información turística obliga a los viajeros a invertir tiempo comparando "
        "opciones en distintos medios. Selva Booking centraliza hoteles urbanos y eco-lodges, reduce "
        "fricción en la reserva y brinda herramientas de gestión a operadores locales. Además, la "
        "auditoría y los roles jerárquicos fortalecen la trazabilidad de cambios en el catálogo.",
        style="Normal",
    )

    set_paragraph("Consideraciones éticas", "TÍTULO 2 MOV")
    doc.add_paragraph(
        "La aplicación informa en términos y condiciones el tratamiento de datos personales (nombre, "
        "correo, teléfono y reservas). Las reglas de Firebase limitan el acceso según rol. La pasarela "
        "de pago es simulada con fines académicos; no procesa dinero real. Las reseñas exigen haber "
        "realizado al menos una reserva en el hotel.",
        style="Normal",
    )
    add_blank()


def section_marco():
    set_paragraph("MARCO TEÓRICO", "TÍTULO 1 MOV")
    set_paragraph("Antecedentes de estudio", "TÍTULO 2 MOV")
    doc.add_paragraph(
        "En Madre de Dios, la oferta de alojamientos permanece fragmentada en múltiples plataformas, "
        "lo que dificulta comparar precios y disponibilidad. Proyectos previos de reservas móviles y "
        "turismo digital evidencian la necesidad de centralización, roles administrativos y datos en "
        "tiempo real para el sector hotelero y ecoturístico amazónico.",
        style="Normal",
    )

    set_paragraph("Marco teórico", "TÍTULO 2 MOV")
    set_paragraph("Desarrollo de aplicaciones móviles Android", "TÍTULO 3")
    doc.add_paragraph(
        "Selva Booking utiliza Kotlin y Jetpack Compose con Material 3, Navigation Compose, ViewModels "
        "y StateFlow. El patrón MVVM separa la UI de la lógica de negocio y el acceso a datos mediante "
        "repositorios.",
        style="Normal",
    )
    set_paragraph("Firebase y servicios en la nube", "TÍTULO 3")
    doc.add_paragraph(
        "Firebase Authentication gestiona registro e inicio de sesión. Cloud Firestore almacena usuarios, "
        "hoteles, habitaciones, reservas, reseñas y registros de auditoría. Firebase Storage guarda "
        "imágenes de hoteles y perfiles.",
        style="Normal",
    )
    set_paragraph("Sistemas de reservas hoteleras y ecoturismo", "TÍTULO 3")
    doc.add_paragraph(
        "El sistema modela estados de reserva (pendiente de pago, confirmada, terminada) y descuenta o "
        "libera inventario de habitaciones automáticamente. Los eco-lodges comparten el mismo flujo que "
        "hoteles urbanos, con capacidad limitada por habitación.",
        style="Normal",
    )

    set_paragraph("Definición de términos", "TÍTULO 2 MOV")
    doc.add_paragraph(
        "Se definen los acrónimos y conceptos técnicos más relevantes del proyecto Selva Booking.",
        style="Normal",
    )
    add_caption("Tabla 1: Glosario de términos")
    add_table(
        ["Término", "Definición"],
        [
            ["Android", "Sistema operativo móvil donde se ejecuta Selva Booking (minSdk 24)."],
            ["Administrador", "Usuario con panel de supervisión limitado a encargados que él creó."],
            ["SuperAdmin", "Usuario con control total: CRUD global, administradores y auditoría."],
            ["Encargado de hotel", "Usuario que gestiona un hotel, habitaciones, reservas y comentarios propios."],
            ["Cliente", "Turista que busca, reserva, paga (simulado) y comentar hoteles."],
            ["Cloud Firestore", "Base de datos NoSQL en tiempo real para usuarios, hoteles, reservas y reseñas."],
            ["creadoPorAdminId", "Campo que vincula un encargado al administrador que lo creó."],
            ["Inventario (stock)", "Cantidad de habitaciones disponibles para reservar en un tipo de habitación."],
            ["Jetpack Compose", "Framework declarativo de UI en Kotlin."],
            ["Kotlin", "Lenguaje principal de la aplicación Android."],
            ["MVVM", "Patrón Model-View-ViewModel: UI, lógica y datos separados."],
            ["Pasarela simulada", "Validación local de tarjeta sin procesador de pagos real."],
            ["Reseña", "Comentario y calificación de un hotel por un cliente con reserva previa."],
            ["Registro de auditoría", "Log de cambios en hoteles/habitaciones revertible por SuperAdmin."],
            ["UML", "Lenguaje unificado de modelado del sistema."],
            ["XP", "Metodología ágil de Programación Extrema aplicada al proyecto."],
        ],
    )
    add_blank()
    set_paragraph("Fuente: Elaboración propia", style="Normal")
    add_blank()


def section_metodologia():
    set_paragraph("METODOLOGÍA DE LA INVESTIGACIÓN", "TÍTULO 1 MOV")
    set_paragraph("Tipo de estudio", "TÍTULO 2 MOV")
    doc.add_paragraph(
        "Investigación aplicada, descriptiva-proyectiva y de diseño no experimental, orientada a "
        "construir y evaluar un producto software móvil para reservas ecoturísticas.",
        style="Normal",
    )

    set_paragraph("Población", "TÍTULO 2 MOV")
    set_paragraph("Población del modelo", "TÍTULO 3")
    doc.add_paragraph(
        "Entidades: Usuario (cliente, encargado, administrador, SuperAdmin), Hotel, Habitación, Reserva, "
        "Reseña y RegistroAuditoria, persistidas en colecciones Firestore.",
        style="Normal",
    )
    set_paragraph("Población de usuarios", "TÍTULO 3")
    doc.add_paragraph(
        "Turistas nacionales e internacionales que visitan Madre de Dios, encargados de establecimientos "
        "y personal administrativo del sistema.",
        style="Normal",
    )
    set_paragraph("Muestra", "TÍTULO 3")
    doc.add_paragraph(
        "Muestra de conveniencia de aproximadamente 10 usuarios en Puerto Maldonado para pruebas de "
        "usabilidad del flujo cliente y validación del panel administrativo.",
        style="Normal",
    )

    set_paragraph("Metodología de desarrollo", "TÍTULO 2 MOV")
    doc.add_paragraph(
        "Se aplicó Programación Extrema (XP) con ciclos cortos, entregas incrementales y pruebas "
        "continuas en emulador y dispositivo físico.",
        style="Normal",
    )
    set_paragraph("Fases de la metodología XP", "TÍTULO 3")
    add_caption("Tabla 2. Fases de la metodología XP aplicadas a Selva Booking")
    add_table(
        ["Fase", "Descripción general", "Aplicación en Selva Booking"],
        [
            [
                "Planificación",
                "Historias de usuario, criterios de aceptación y priorización.",
                "Registro con teléfono, búsqueda, reserva, pago simulado, roles admin/encargado, reseñas y auditoría.",
            ],
            [
                "Diseño",
                "Prototipos y diagramas UML.",
                "Wireframes, paleta Selva, diagramas de casos de uso por rol, secuencia, estados, clases, componentes y despliegue.",
            ],
            [
                "Codificación",
                "Implementación iterativa en Kotlin.",
                "Módulos Compose, ViewModels, repositorios, AdminDataScope, Firebase Auth/Firestore/Storage.",
            ],
            [
                "Pruebas",
                "Validación funcional y refactorización.",
                "Login, filtros, reserva, stock, paneles por rol, alternar rol, comentarios y revertir auditoría.",
            ],
            [
                "Lanzamiento",
                "Entrega de incremento usable.",
                "APK debug funcional (app-debug.apk) probado con usuarios de conveniencia.",
            ],
        ],
    )
    add_blank()
    set_paragraph("Fuente: Elaboración propia", style="Normal")
    add_blank()

    set_paragraph("Recursos", "TÍTULO 2 MOV")
    set_paragraph("Recursos humanos", "TÍTULO 3")
    doc.add_paragraph(
        "Proyecto desarrollado por estudiantes de Ingeniería de Sistemas e Informática de la UNAMAD, "
        "con asesoría del Mg. Tineo Vilchez:",
        style="Normal",
    )
    for item in [
        "Condori Sahuarico, Liz Yeiza — UI/UX, wireframes y usabilidad.",
        "Uceda Jallurana, Renzo Jesus — Desarrollo Android, Firebase, reservas y paneles admin.",
        "Pereira Rampas, Korven Joshua — Backend Firebase y reglas de seguridad.",
        "Chicata Acosta, Jasseir James — Pruebas y documentación.",
    ]:
        add_bullet(item)

    set_paragraph("Recursos tecnológicos", "TÍTULO 3")
    add_caption("Tabla 3: Especificaciones del entorno de desarrollo y tecnologías aplicadas")
    add_table(
        ["Software", "Versión / Detalle", "Rol"],
        [
            ["Android Studio", "Ladybug / estable", "IDE principal"],
            ["Kotlin", "2.x", "Lenguaje de la app"],
            ["Jetpack Compose", "BOM actual", "Interfaz declarativa"],
            ["Firebase BOM", "33.x+", "Auth, Firestore, Storage"],
            ["Coil", "2.x", "Imágenes remotas"],
            ["Navigation Compose", "2.x", "Navegación y rutas por rol"],
            ["PlantUML", "1.2024+", "Diagramas UML exportados"],
            ["Figma", "Plan gratuito", "Prototipado visual"],
        ],
    )
    add_blank()
    set_paragraph("Fuente: Elaboración propia", style="Normal")
    add_blank()

    set_paragraph("Presupuesto", "TÍTULO 2 MOV")
    doc.add_paragraph(
        "La Tabla 4 presenta la estructura de costos estimada para el desarrollo.",
        style="Normal",
    )
    add_caption("Tabla 4: Presupuesto estimado del proyecto")
    add_table(
        ["Categoría", "Recurso", "Costo"],
        [
            ["Software", "Android Studio", "S/. 0"],
            ["Software", "Kotlin + Jetpack Compose", "S/. 0"],
            ["Software", "Figma + PlantUML", "S/. 0"],
            ["Servicios en la nube", "Firebase — Plan Spark", "S/. 0"],
            ["Hardware", "Computadoras personales", "S/. 0"],
            ["Hardware", "Smartphone Android para pruebas", "S/. 0"],
            ["Contingencia", "Reserva", "S/. 50"],
            ["Otros", "Internet (1 mes)", "S/. 100"],
            ["TOTAL ESTIMADO", "TOTAL ESTIMADO", "S/. 150"],
        ],
    )
    add_blank()
    set_paragraph("Fuente: Elaboración propia", style="Normal")
    add_blank()


def section_wireframes():
    set_paragraph("WIREFRAME", "TÍTULO 1 MOV")
    set_paragraph("Autenticación y Gestión de Cuenta", "TÍTULO 2 MOV")
    wireframe_caps = [
        "Figura 1: Pantalla de Bienvenida (Splash).",
        "Figura 2: Inicio de Sesión.",
        "Figura 3: Registro de Usuario (nombre, correo, teléfono 9 dígitos).",
        "Figura 4: Recuperar Contraseña.",
        "Figura 5: Mi Perfil (edición y alternar rol).",
        "Figura 6: Soporte y Ayuda.",
    ]
    for i, cap in enumerate(wireframe_caps, start=1):
        add_figure(cap, WIREFRAMES / f"fig_{i:02d}.png")

    set_paragraph("Módulo del Cliente (Búsqueda y Reservas)", "TÍTULO 2 MOV")
    for i, cap in enumerate(
        [
            "Figura 7: Menú Lateral (Cliente).",
            "Figura 8: Menú Lateral (Administrador / SuperAdmin / Encargado).",
        ],
        start=7,
    ):
        add_figure(cap, WIREFRAMES / f"fig_{i:02d}.png")

    set_paragraph("Módulo de Perfil y Navegación", "TÍTULO 2 MOV")
    client_caps = [
        "Figura 9: Pantalla de Inicio.",
        "Figura 10: Comparar Alojamientos (Búsqueda).",
        "Figura 11: Filtros de Búsqueda.",
        "Figura 12: Detalle del Alojamiento.",
        "Figura 13: Completar Reserva.",
        "Figura 14: Resumen de Pago.",
        "Figura 15: Pago Confirmado.",
        "Figura 16: Mis Reservas.",
    ]
    for i, cap in enumerate(client_caps, start=9):
        add_figure(cap, WIREFRAMES / f"fig_{i:02d}.png")

    set_paragraph("Módulo de Administración (Gestión y Control)", "TÍTULO 2 MOV")
    admin_caps = [
        "Figura 17: Panel de Administración (Dashboard por rol).",
        "Figura 18: Gestión de Encargados y Administradores.",
        "Figura 19: Gestión de Alojamientos.",
        "Figura 20: Formulario de Nuevo Alojamiento.",
        "Figura 21: Gestión de Habitaciones e inventario.",
        "Figura 22: Nueva Reserva (Manual — SuperAdmin).",
        "Figura 23: Panel General de Reservas.",
        "Figura 24: Detalle de Reserva (Administrador).",
    ]
    for i, cap in enumerate(admin_caps, start=17):
        add_figure(cap, WIREFRAMES / f"fig_{i:02d}.png")


def section_uml():
    set_paragraph("DIAGRAMACIÓN UML", "TÍTULO 1 MOV")
    set_paragraph("Diagramas de Comportamiento", "TÍTULO 2 MOV")

    uml_figures = [
        ("Figura 25: Diagrama de casos de uso general", UML / "casos_de_uso.png"),
        ("Figura 26: Diagrama de casos de uso — Invitado", UML / "casos_uso_invitado.png"),
        ("Figura 27: Diagrama de casos de uso — Cliente", UML / "casos_uso_cliente.png"),
        ("Figura 28: Diagrama de casos de uso — Encargado de hotel", UML / "casos_uso_encargado.png"),
        ("Figura 29: Diagrama de casos de uso — Administrador limitado", UML / "casos_uso_administrador.png"),
        ("Figura 30: Diagrama de casos de uso — SuperAdmin", UML / "casos_uso_superadmin.png"),
        ("Figura 31: Diagrama de secuencia — Cliente (reserva y pago)", UML / "secuencia_cliente.png"),
        ("Figura 32: Diagrama de secuencia — Encargado de hotel", UML / "secuencia_encargado.png"),
        ("Figura 33: Diagrama de secuencia — SuperAdmin", UML / "secuencia_superadmin.png"),
        ("Figura 34: Diagrama de estados — Reserva", UML / "estados.png"),
    ]
    for cap, path in uml_figures:
        add_figure(cap, path)

    set_paragraph("Diagramas Estructurales", "TÍTULO 2 MOV")
    for cap, path in [
        ("Figura 35: Diagrama de clases — Dominio", UML / "clases.png"),
        ("Figura 36: Diagrama de componentes — MVVM", UML / "componentes.png"),
        ("Figura 37: Diagrama de despliegue", UML / "despliegue.png"),
    ]:
        add_figure(cap, path)


def section_cierre():
    set_paragraph("CONCLUSIONES", "TÍTULO 1 MOV")
    doc.add_paragraph(
        "Selva Booking centraliza la oferta de alojamientos de Madre de Dios en una aplicación Android "
        "con roles diferenciados, control de inventario, reseñas y auditoría. Kotlin, Jetpack Compose, "
        "MVVM y Firebase permitieron un desarrollo modular y escalable. Los diagramas UML documentan "
        "casos de uso por actor, secuencias, estados, clases, componentes y despliegue alineados al "
        "código actual del proyecto.",
        style="Normal",
    )
    add_blank()

    set_paragraph("RECOMENDACIONES", "TÍTULO 1 MOV")
    doc.add_paragraph(
        "Integrar una pasarela de pago real, ampliar la oferta de hoteles, publicar la app en Play Store, "
        "fortalecer reglas de seguridad Firestore, añadir notificaciones push para reservas y continuar "
        "pruebas de usabilidad con turistas y encargados reales en Puerto Maldonado.",
        style="Normal",
    )
    add_blank()

    set_paragraph("REFERENCIAS BIBLIOGRÁFICAS", "TÍTULO 1 MOV")
    refs = [
        "Google. (2024). Documentación oficial de Android Developers — Jetpack Compose. https://developer.android.com/jetpack/compose",
        "Google. (2024). Documentación Firebase — Authentication, Firestore y Storage. https://firebase.google.com/docs",
        "Beck, K. (2000). Extreme Programming Explained: Embrace Change. Addison-Wesley.",
        "Reyes Quispe, (2026). Antecedentes de turismo digital en Madre de Dios. [Referencia académica del equipo]",
        "Sommerville, I. (2011). Ingeniería de Software (9.ª ed.). Pearson.",
    ]
    for r in refs:
        doc.add_paragraph(r, style="Normal")
    add_blank()

    set_paragraph("ANEXOS", "TÍTULO 1 MOV")
    doc.add_paragraph(
        "Anexo A: Código fuente en repositorio del proyecto (com.company.selvabooking).",
        style="Normal",
    )
    doc.add_paragraph(
        "Anexo B: Diagramas UML en docs/uml/ y exportación PNG en docs/uml/imagenes/.",
        style="Normal",
    )
    doc.add_paragraph(
        "Anexo C: APK debug en app/build/outputs/apk/debug/app-debug.apk.",
        style="Normal",
    )


# --- main ---
doc = Document(TEMPLATE)
clear_document_body(doc)

cover_page()
doc.add_page_break()
section_presentacion()
section_introduccion()
section_indice()
section_indice_figuras()
section_indice_tablas()
doc.add_page_break()
section_definicion()
doc.add_page_break()
section_marco()
doc.add_page_break()
section_metodologia()
doc.add_page_break()
section_wireframes()
doc.add_page_break()
section_uml()
doc.add_page_break()
section_cierre()

doc.save(OUT)
print(f"Documento generado: {OUT}")
