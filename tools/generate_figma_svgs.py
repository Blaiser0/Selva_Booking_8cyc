"""Generate Figma-friendly mobile UI mockups as SVG files."""
from __future__ import annotations

import base64
import shutil
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    Image = None

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "figma-export" / "svg"
ASSETS = OUT / "assets"
LOGO_SRC = ROOT / "app" / "src" / "main" / "res" / "drawable" / "logo_selva_booking.png"
LOGO_ASSET = ASSETS / "logo_selva_booking.png"
LOGO_B64: str | None = None

W, H = 390, 844
FONT = "Inter, Arial, sans-serif"
FONT_SERIF = "Georgia, Times New Roman, serif"

BG = "#FAF9F6"
GREEN = "#2E5E3E"
GREEN_DARK = "#1E3D2A"
TEAL = "#2C4A4A"
SURFACE = "#F3F1EA"
ORANGE = "#E8954A"
RED = "#B3261E"
LIGHT = "#FAF9F6"
DARK = "#1A2E1A"
BROWN = "#8B5E3C"
TROPICAL = "#4A9B5C"

DEFS = f"""  <defs>
    <linearGradient id="gradDrawer" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="{GREEN}"/>
      <stop offset="100%" stop-color="{GREEN_DARK}"/>
    </linearGradient>
    <linearGradient id="gradHero" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="{GREEN_DARK}" stop-opacity="0"/>
      <stop offset="100%" stop-color="#000000" stop-opacity="0.55"/>
    </linearGradient>
    <clipPath id="clipFrame">
      <rect x="0" y="0" width="{W}" height="{H}"/>
    </clipPath>
    <clipPath id="clipLogo">
      <rect x="45" y="318" width="300" height="128" rx="8"/>
    </clipPath>
  </defs>"""


def esc(text: str) -> str:
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
    )


def svg_open(name: str) -> list[str]:
    return [
        '<?xml version="1.0" encoding="UTF-8"?>',
        f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"',
        f'     width="{W}px" height="{H}px" viewBox="0 0 {W} {H}"',
        f'     preserveAspectRatio="xMidYMid meet" fill="none">',
        f"  <title>{esc(name)}</title>",
        DEFS,
        f'  <g id="screen" clip-path="url(#clipFrame)">',
        f'    <rect id="background" x="0" y="0" width="{W}" height="{H}" fill="{BG}"/>',
        status_bar(),
    ]


def svg_close() -> list[str]:
    return ["  </g>", "</svg>"]


def txt(
    x: float,
    y: float,
    content: str,
    *,
    size: int = 14,
    fill: str = TEAL,
    weight: int = 400,
    anchor: str = "start",
    serif: bool = False,
    baseline: str = "auto",
) -> str:
    family = FONT_SERIF if serif else FONT
    baseline_attr = f' dominant-baseline="{baseline}"' if baseline != "auto" else ""
    anchor_attr = f' text-anchor="{anchor}"' if anchor != "start" else ""
    return (
        f'<text x="{x:.1f}" y="{y:.1f}" font-family="{family}" font-size="{size}" '
        f'font-weight="{weight}" fill="{fill}"{anchor_attr}{baseline_attr}>{esc(content)}</text>'
    )


def status_bar() -> str:
    return f"""    <g id="status-bar">
      {txt(24, 20, "9:41", size=12, weight=600)}
      {txt(330, 20, "100%", size=11, anchor="end")}
    </g>"""


def top_bar(title: str, *, menu: bool = True, back: bool = False) -> str:
    menu_icon = (
        """      <g id="menu-icon" transform="translate(16,48)">
        <rect x="0" y="2" width="18" height="2.5" rx="1.2" fill="#2E5E3E"/>
        <rect x="0" y="8" width="18" height="2.5" rx="1.2" fill="#2E5E3E"/>
        <rect x="0" y="14" width="18" height="2.5" rx="1.2" fill="#2E5E3E"/>
      </g>"""
        if menu
        else ""
    )
    back_icon = (
        f"""      <g id="back-icon">
        <path d="M30 72 L20 72 L26 66 M20 72 L26 78" stroke="{GREEN}" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
      </g>"""
        if back
        else ""
    )
    title_x = 56 if back else 48 if menu else 24
    return f"""    <g id="top-bar">
      <rect x="0" y="36" width="{W}" height="56" fill="{BG}"/>
      <line x1="0" y1="92" x2="{W}" y2="92" stroke="{SURFACE}" stroke-width="1"/>
      {menu_icon}{back_icon}
      {txt(title_x, 68, title, size=18, fill=GREEN, weight=700, serif=True)}
    </g>"""


def rect_card(x: int, y: int, w: int, h: int, *, r: int = 16, fill: str = SURFACE) -> str:
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}"/>'


def card(y: int, h: int, *, radius: int = 16) -> str:
    return rect_card(16, y, W - 32, h, r=radius)


def btn_primary(y: int, label: str, *, x: int = 24, w: int | None = None) -> str:
    w = w or (W - 48)
    cy = y + 24
    return f"""    <g id="btn-primary">
      {rect_card(x, y, w, 48, r=12, fill=GREEN)}
      {txt(x + w / 2, cy, label, size=15, fill=LIGHT, weight=600, anchor="middle", baseline="middle")}
    </g>"""


def btn_outline(y: int, label: str, *, x: int = 24, w: int | None = None) -> str:
    w = w or (W - 48)
    cy = y + 22
    return f"""    <g id="btn-outline">
      <rect x="{x}" y="{y}" width="{w}" height="44" rx="12" fill="{BG}" stroke="{GREEN}" stroke-width="1.5"/>
      {txt(x + w / 2, cy, label, size=14, fill=GREEN, weight=600, anchor="middle", baseline="middle")}
    </g>"""


def field(y: int, label: str, *, value: str = "", password: bool = False) -> str:
    eye = (
        f'<circle cx="{W - 40}" cy="{y + 34}" r="7" stroke="{GREEN}" stroke-width="1.2" fill="{BG}"/>'
        if password
        else ""
    )
    value_txt = (
        txt(36, y + 38, value, size=14, fill=TEAL, baseline="middle")
        if value
        else ""
    )
    return f"""    <g id="field-{esc(label).replace(' ', '-').lower()}">
      {txt(28, y + 8, label, size=11, fill=GREEN)}
      <rect x="24" y="{y + 12}" width="{W - 48}" height="48" rx="12" fill="{BG}" stroke="{BROWN}" stroke-width="1.2"/>
      {value_txt}{eye}
    </g>"""


def section_title(y: int, title: str) -> str:
    return txt(20, y, title, size=20, fill=GREEN, weight=700, serif=True)


def chip(x: int, y: int, label: str, *, selected: bool = False) -> str:
    fill = GREEN if selected else BG
    text_fill = LIGHT if selected else GREEN
    w = max(64, len(label) * 8 + 28)
    cy = y + 16
    return f"""    <g id="chip-{esc(label)}">
      <rect x="{x}" y="{y}" width="{w}" height="32" rx="16" fill="{fill}" stroke="{GREEN}" stroke-width="1"/>
      {txt(x + w / 2, cy, label, size=12, fill=text_fill, weight=600, anchor="middle", baseline="middle")}
    </g>"""


def status_chip(x: int, y: int, status: str) -> str:
    colors = {
        "Pendiente": (ORANGE, "#FDF0E4"),
        "Confirmada": (GREEN, "#E8F0EA"),
        "Cancelada": (RED, "#F9E8E7"),
        "Completada": (TEAL, "#E8EEF0"),
    }
    fg, bg = colors.get(status, (TEAL, SURFACE))
    w = len(status) * 8 + 24
    return f"""    <g id="status-{esc(status)}">
      <rect x="{x}" y="{y}" width="{w}" height="24" rx="8" fill="{bg}"/>
      {txt(x + 12, y + 12, status, size=11, fill=fg, weight=600, baseline="middle")}
    </g>"""


def icon_search(x: int, y: int, size: int = 16) -> str:
    r = size // 3
    return f"""    <g transform="translate({x},{y})">
      <circle cx="{r}" cy="{r}" r="{r}" stroke="{GREEN}" stroke-width="1.5" fill="none"/>
      <line x1="{r + 2}" y1="{r + 2}" x2="{size - 2}" y2="{size - 2}" stroke="{GREEN}" stroke-width="1.5" stroke-linecap="round"/>
    </g>"""


def icon_bookmark(x: int, y: int) -> str:
    return f"""    <g transform="translate({x},{y})">
      <path d="M4 2 H12 V14 L8 11 L4 14 Z" fill="{GREEN}"/>
    </g>"""


def logo_image(cy: int, *, width: int = 300, height: int = 128, embed: bool = False) -> str:
    x = (W - width) // 2
    y = cy - height // 2
    if embed and LOGO_B64:
        href = f"data:image/png;base64,{LOGO_B64}"
    else:
        href = "assets/logo_selva_booking.png"
    return f"""    <g id="logo">
      <image xlink:href="{href}" x="{x}" y="{y}" width="{width}" height="{height}"
             preserveAspectRatio="xMidYMid meet" clip-path="url(#clipLogo)"/>
    </g>"""


def logo_placeholder(cy: int) -> str:
    x = (W - 220) // 2
    y = cy - 45
    return f"""    <g id="logo-placeholder">
      <rect x="{x}" y="{y}" width="220" height="90" rx="12" fill="{SURFACE}" stroke="{GREEN}" stroke-width="1.5"/>
      <circle cx="{W // 2}" cy="{cy - 18}" r="24" fill="{GREEN}" fill-opacity="0.15"/>
      {txt(W / 2, cy + 6, "SELVA BOOKING", size=16, fill=GREEN, weight=700, anchor="middle", serif=True)}
      {txt(W / 2, cy + 26, "Reservas ecologicas", size=9, fill=TEAL, anchor="middle")}
    </g>"""


def hotel_card(y: int, name: str, city: str, price: str, *, offer: bool = False) -> str:
    offer_badge = (
        f"""      <rect x="28" y="{y + 8}" width="52" height="20" rx="6" fill="{ORANGE}"/>
      {txt(54, y + 22, "Oferta", size=10, fill=LIGHT, weight=700, anchor="middle", baseline="middle")}"""
        if offer
        else ""
    )
    return f"""    <g id="hotel-card">
      {card(y, 120)}
      <rect x="28" y="{y + 12}" width="96" height="96" rx="12" fill="{GREEN}" fill-opacity="0.15"/>
      <rect x="44" y="{y + 40}" width="64" height="40" rx="8" fill="{GREEN}" fill-opacity="0.08"/>
      {txt(76, y + 64, "Hotel", size=11, fill=GREEN, weight=600, anchor="middle", baseline="middle")}
      {offer_badge}
      {txt(136, y + 36, name, size=15, fill=DARK, weight=700)}
      {txt(136, y + 56, city, size=12, fill=TEAL)}
      {txt(136, y + 84, f"desde {price}", size=13, fill=GREEN, weight=700)}
      {txt(136, y + 104, "5 estrellas  4.8", size=11, fill=TEAL)}
    </g>"""


def scrim() -> str:
    return f'<rect x="0" y="0" width="{W}" height="{H}" fill="#000000" fill-opacity="0.48"/>'


def drawer_panel(*, admin: bool = False) -> str:
    items = (
        [
            ("ADMINISTRACION", "Dashboard", True),
            ("", "Solicitudes", False),
            ("", "Hoteles", False),
            ("", "Reservas", False),
            ("TU CUENTA", "Mi Cuenta", False),
            ("", "Soporte y Ayuda", False),
            ("SESION", "Cerrar Sesion", False),
        ]
        if admin
        else [
            ("MENU PRINCIPAL", "Inicio", True),
            ("", "Buscar", False),
            ("", "Mis Reservas", False),
            ("TU CUENTA", "Mi Cuenta", False),
            ("", "Soporte y Ayuda", False),
            ("SESION", "Cerrar Sesion", False),
        ]
    )
    panel_w = int(W * 0.78)
    panel_h = int(H * 0.94)
    panel_y = H - panel_h
    role = "Administrador" if admin else "Cliente"
    rows = []
    y = panel_y + 118
    for section, label, selected in items:
        if section:
            rows.append(txt(28, y, section, size=10, fill=GREEN, weight=700))
            y += 20
        if selected:
            rows.append(rect_card(20, y - 12, panel_w - 40, 52, r=14, fill=SURFACE))
        rows.append(f'<rect x="36" y="{y}" width="36" height="36" rx="10" fill="{GREEN}" fill-opacity="0.1"/>')
        rows.append(txt(84, y + 14, label, size=14, fill=DARK if selected else GREEN, weight=600 if selected else 500))
        rows.append(txt(84, y + 30, "Acceso rapido", size=11, fill=TEAL))
        y += 56
    rows_svg = "\n      ".join(rows)
    return f"""    <g id="drawer-overlay">
      {scrim()}
      <rect x="0" y="{panel_y}" width="{panel_w}" height="{panel_h}" rx="28" fill="{BG}"/>
      <rect x="0" y="{panel_y}" width="{panel_w}" height="88" rx="28" fill="url(#gradDrawer)"/>
      {txt(24, panel_y + 40, "Selva Booking", size=20, fill=LIGHT, weight=700, serif=True)}
      {txt(24, panel_y + 60, "Tu selva, tu destino", size=11, fill=LIGHT)}
      {txt(panel_w - 32, panel_y + 48, "X", size=18, fill=LIGHT, anchor="end", baseline="middle")}
      {rect_card(20, panel_y + 96, panel_w - 40, 96, r=18)}
      <circle cx="52" cy="{panel_y + 144}" r="28" fill="{GREEN}" fill-opacity="0.12"/>
      <circle cx="52" cy="{panel_y + 144}" r="12" fill="{GREEN}" fill-opacity="0.25"/>
      {txt(92, panel_y + 130, "Hola!", size=11, fill=GREEN)}
      {txt(92, panel_y + 150, "Usuario Demo", size=15, fill=DARK, weight=700)}
      {txt(92, panel_y + 168, "demo@selvabooking.com", size=11, fill=TEAL)}
      <rect x="92" y="{panel_y + 176}" width="88" height="20" rx="10" fill="{TROPICAL}" fill-opacity="0.18"/>
      {txt(136, panel_y + 190, role, size=10, fill=GREEN, weight=600, anchor="middle", baseline="middle")}
      {rows_svg}
      <rect x="{(panel_w - 48) // 2}" y="{H - 28}" width="48" height="4" rx="2" fill="{GREEN}" fill-opacity="0.3"/>
    </g>"""


def write(name: str, parts: list[str]) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / name).write_text("\n".join(parts), encoding="utf-8")
    print(f"  {name}")


def prepare_assets() -> None:
    global LOGO_B64
    ASSETS.mkdir(parents=True, exist_ok=True)
    if Image and LOGO_SRC.exists():
        img = Image.open(LOGO_SRC).convert("RGBA")
        img.thumbnail((600, 240), Image.LANCZOS)
        img.save(LOGO_ASSET, "PNG", optimize=True)
    elif LOGO_SRC.exists():
        shutil.copy2(LOGO_SRC, LOGO_ASSET)
    if LOGO_ASSET.exists():
        LOGO_B64 = base64.b64encode(LOGO_ASSET.read_bytes()).decode("ascii")


def gen_splash() -> None:
    p = svg_open("Splash")
    p.append(logo_image(400, width=300, height=128, embed=True))
    p += svg_close()
    write("01-splash.svg", p)


def gen_login() -> None:
    p = svg_open("Inicio de sesion")
    p.append(logo_placeholder(150))
    p += [
        txt(W / 2, 250, "Bienvenido", size=24, fill=GREEN, weight=700, anchor="middle", serif=True),
        txt(W / 2, 276, "Inicia sesion para continuar", size=14, fill=TEAL, anchor="middle"),
        field(300, "Correo electronico", value="tu@email.com"),
        field(370, "Contrasena", value="********", password=True),
        txt(W - 28, 450, "Olvidaste tu contrasena?", size=13, fill=GREEN, anchor="end"),
        btn_primary(480, "Iniciar sesion"),
        txt(W / 2, 560, "No tienes cuenta? Registrate", size=13, fill=GREEN, anchor="middle"),
    ]
    p += svg_close()
    write("02-login.svg", p)


def gen_register() -> None:
    p = svg_open("Registro")
    p.append(logo_placeholder(120))
    p += [
        txt(W / 2, 210, "Crear cuenta", size=24, fill=GREEN, weight=700, anchor="middle", serif=True),
        field(250, "Nombre completo"),
        field(320, "Correo electronico"),
        field(390, "Contrasena", password=True),
        field(460, "Confirmar contrasena", password=True),
        btn_primary(540, "Registrarse"),
        txt(W / 2, 620, "Ya tienes cuenta? Inicia sesion", size=13, fill=GREEN, anchor="middle"),
    ]
    p += svg_close()
    write("03-register.svg", p)


def gen_forgot() -> None:
    p = svg_open("Recuperar contrasena")
    p.append(top_bar("Recuperar contrasena", menu=False, back=True))
    p += [
        txt(W / 2, 150, "Restablecer contrasena", size=22, fill=GREEN, weight=700, anchor="middle", serif=True),
        txt(W / 2, 190, "Te enviaremos un enlace a tu correo", size=13, fill=TEAL, anchor="middle"),
        field(240, "Correo electronico", value="tu@email.com"),
        btn_primary(330, "Enviar enlace"),
        txt(W / 2, 410, "Volver al inicio de sesion", size=13, fill=GREEN, anchor="middle"),
    ]
    p += svg_close()
    write("04-forgot-password.svg", p)


def gen_home() -> None:
    p = svg_open("Inicio")
    p.append(top_bar("Selva Booking"))
    p += [
        f"""    <g id="search-bar">
      <rect x="16" y="108" width="{W - 32}" height="48" rx="16" fill="{BG}" stroke="{GREEN}" stroke-width="1" stroke-opacity="0.3"/>
      {icon_search(28, 124)}
      {txt(52, 138, "Buscar hoteles por ciudad o nombre...", size=14, fill=TEAL)}
    </g>""",
        f"""    <g id="quick-access">
      {card(172, 72)}
      {icon_bookmark(28, 188)}
      {txt(56, 206, "Mis Reservas", size=16, fill=DARK, weight=700)}
      {txt(56, 226, "Consulta y gestiona tus reservas", size=12, fill=TEAL)}
    </g>""",
        section_title(270, "Hoteles destacados"),
        hotel_card(290, "Eco Lodge Selva Verde", "Puerto Maldonado", "S/ 280", offer=True),
        section_title(430, "Ofertas especiales"),
        hotel_card(450, "Cabanas Madre de Dios", "Puerto Maldonado", "S/ 120", offer=True),
        section_title(590, "Recomendaciones"),
        hotel_card(610, "Amazonia Rainforest Resort", "Tambopata", "S/ 450"),
    ]
    p += svg_close()
    write("05-home.svg", p)


def gen_search(*, filters: bool = False) -> None:
    fname = "06-search-filters.svg" if filters else "06-search.svg"
    p = svg_open("Comparar hoteles" + (" - Filtros" if filters else ""))
    p.append(top_bar("Comparar hoteles"))
    p += [
        f"""    <g id="search-input">
      <rect x="16" y="108" width="{W - 32}" height="48" rx="24" fill="{BG}" stroke="{GREEN}" stroke-width="1" stroke-opacity="0.25"/>
      {icon_search(28, 124)}
      {txt(52, 138, "Tambopata", size=14, fill=TEAL)}
    </g>""",
        txt(20, 180, "4 ofertas encontradas", size=13, fill=TEAL),
        txt(W - 20, 180, "Ocultar filtros" if filters else "Filtros", size=13, fill=GREEN, weight=600, anchor="end"),
        chip(16, 192, "Recomendados", selected=True),
        chip(140, 192, "Menor precio"),
        chip(260, 192, "Mejor valoracion"),
    ]
    base = 380 if filters else 240
    if filters:
        p += [
            card(240, 120, radius=14),
            field(252, "Ciudad", value="Tambopata"),
            txt(28, 340, "Precio maximo", size=11, fill=GREEN),
            f'<rect x="24" y="348" width="{W - 48}" height="40" rx="10" fill="{BG}" stroke="{BROWN}" stroke-width="1.2"/>',
            txt(36, 370, "500", size=13, fill=TEAL, baseline="middle"),
            chip(24, 400, "0 estrellas"),
            chip(110, 400, "3 estrellas"),
            chip(196, 400, "4 estrellas", selected=True),
            chip(282, 400, "5 estrellas"),
        ]
    p += [
        hotel_card(base, "Eco Lodge Selva Verde", "Puerto Maldonado", "S/ 280", offer=True),
        hotel_card(base + 140, "Tambopata Jungle Lodge", "Tambopata", "S/ 200"),
    ]
    p += svg_close()
    write(fname, p)


def gen_hotel_detail() -> None:
    p = svg_open("Detalle del hotel")
    p += [
        f'<rect x="0" y="36" width="{W}" height="240" fill="{GREEN_DARK}"/>',
        f'<rect x="0" y="180" width="{W}" height="96" fill="url(#gradHero)"/>',
        f'<circle cx="36" cy="72" r="18" fill="{LIGHT}"/>',
        f'<path d="M32 72 H40" stroke="{GREEN}" stroke-width="2" stroke-linecap="round"/>',
        f'<rect x="16" y="196" width="52" height="32" rx="8" fill="{GREEN}"/>',
        txt(42, 214, "4.8", size=12, fill=LIGHT, weight=700, anchor="middle", baseline="middle"),
        txt(20, 290, "Eco Lodge Selva Verde", size=22, fill=LIGHT, weight=700, serif=True),
        txt(20, 314, "Puerto Maldonado", size=13, fill=LIGHT),
        txt(20, 360, "5 estrellas  Ecologico", size=13, fill=ORANGE),
        f'<rect x="200" y="342" width="96" height="22" rx="6" fill="{ORANGE}"/>',
        txt(248, 355, "Oferta especial", size=11, fill=LIGHT, weight=700, anchor="middle", baseline="middle"),
        section_title(420, "Servicios del hotel"),
        chip(16, 432, "WiFi"),
        chip(80, 432, "Piscina"),
        chip(155, 432, "Spa"),
        chip(210, 432, "Tours"),
        section_title(490, "Habitaciones disponibles"),
        card(510, 140, radius=14),
        f'<rect x="28" y="522" width="80" height="80" rx="10" fill="{GREEN}" fill-opacity="0.12"/>',
        txt(120, 548, "Cabana Deluxe", size=15, fill=DARK, weight=700),
        txt(120, 568, "Capacidad: 2", size=12, fill=TEAL),
        txt(120, 592, "S/ 280 / noche", size=14, fill=GREEN, weight=700),
        btn_primary(620, "Reservar", w=120),
        f'<rect x="0" y="{H - 72}" width="{W}" height="72" fill="{BG}"/>',
        txt(20, H - 36, "Desde", size=12, fill=TEAL),
        txt(20, H - 16, "S/ 280", size=20, fill=GREEN, weight=700),
        btn_primary(H - 60, "Ver ofertas", w=150),
    ]
    p += svg_close()
    write("07-hotel-detail.svg", p)


def gen_booking() -> None:
    p = svg_open("Completar reserva")
    p.append(top_bar("Completa tu reserva", menu=False, back=True))
    p += [
        card(108, 100, radius=14),
        f'<rect x="28" y="120" width="72" height="72" rx="10" fill="{GREEN}" fill-opacity="0.12"/>',
        txt(112, 146, "Eco Lodge Selva Verde", size=15, fill=DARK, weight=700),
        txt(112, 166, "Cabana Deluxe", size=12, fill=TEAL),
        txt(112, 188, "S/ 280 / noche", size=13, fill=GREEN, weight=700),
        section_title(230, "Fechas de estadia"),
        card(250, 72, radius=12),
        txt(48, 276, "Entrada", size=11, fill=TEAL),
        txt(48, 300, "20/07/2026", size=14, fill=DARK, weight=600),
        txt(220, 276, "Salida", size=11, fill=TEAL),
        txt(220, 300, "23/07/2026", size=14, fill=DARK, weight=600),
        card(340, 88, radius=14),
        txt(32, 368, "Huespedes", size=15, fill=DARK, weight=600),
        txt(32, 388, "Max. 2 por habitacion", size=12, fill=TEAL),
        txt(W - 48, 382, "2", size=22, fill=GREEN, weight=700, anchor="end"),
        card(444, 120, radius=14),
        section_title(464, "Resumen de precio"),
        txt(32, 500, "Precio por noche", size=13, fill=TEAL),
        txt(W - 32, 500, "S/ 280", size=13, fill=DARK, anchor="end"),
        txt(32, 524, "Noches", size=13, fill=TEAL),
        txt(W - 32, 524, "3", size=13, fill=DARK, anchor="end"),
        f'<rect x="24" y="536" width="{W - 48}" height="40" rx="10" fill="{GREEN}" fill-opacity="0.08"/>',
        txt(36, 562, "Total a pagar", size=15, fill=DARK, weight=700),
        txt(W - 36, 562, "S/ 840", size=18, fill=GREEN, weight=700, anchor="end"),
        f'<rect x="0" y="{H - 72}" width="{W}" height="72" fill="{BG}"/>',
        txt(20, H - 30, "3 noches", size=13, fill=TEAL),
        txt(20, H - 12, "S/ 840", size=18, fill=GREEN, weight=700),
        btn_primary(H - 58, "Reservar ahora", w=170),
    ]
    p += svg_close()
    write("08-booking.svg", p)


def gen_payment(*, success: bool = False) -> None:
    if success:
        p = svg_open("Pago confirmado")
        p.append(top_bar("Pago"))
        p += [
            f'<circle cx="{W // 2}" cy="340" r="48" fill="{GREEN}" fill-opacity="0.12"/>',
            f'<path d="M{W // 2 - 14} 340 L{W // 2 - 4} 350 L{W // 2 + 16} 328" stroke="{GREEN}" stroke-width="3" fill="none" stroke-linecap="round"/>',
            txt(W / 2, 420, "Pago confirmado!", size=24, fill=GREEN, weight=700, anchor="middle", serif=True),
            txt(W / 2, 452, "Tu reserva ha sido registrada", size=14, fill=TEAL, anchor="middle"),
        ]
        p += svg_close()
        write("10-payment-success.svg", p)
        return
    p = svg_open("Pago")
    p.append(top_bar("Pago"))
    p += [
        section_title(120, "Resumen de pago"),
        card(140, 200, radius=16),
        txt(32, 172, "Eco Lodge Selva Verde", size=15, fill=DARK, weight=700),
        txt(32, 194, "Cabana Deluxe", size=12, fill=TEAL),
        txt(32, 218, "20/07/2026 - 23/07/2026", size=12, fill=TEAL),
        txt(32, 242, "2 huespedes", size=12, fill=TEAL),
        txt(32, 280, "Total: S/ 840", size=14, fill=GREEN, weight=700),
        txt(32, 320, "Pago simulado para demostracion", size=12, fill=TEAL),
        btn_primary(400, "Confirmar pago"),
    ]
    p += svg_close()
    write("09-payment.svg", p)


def gen_reservations() -> None:
    p = svg_open("Mis Reservas")
    p.append(top_bar("Mis Reservas"))
    p += [
        chip(16, 108, "Todas", selected=True),
        chip(88, 108, "Pendiente"),
        chip(188, 108, "Confirmada"),
        chip(288, 108, "Cancelada"),
        card(160, 150, radius=16),
        txt(28, 188, "Eco Lodge Selva Verde", size=16, fill=DARK, weight=700),
        status_chip(250, 170, "Pendiente"),
        txt(28, 214, "Habitacion: Cabana Deluxe", size=13, fill=TEAL),
        txt(28, 236, "20/07/2026 - 23/07/2026", size=12, fill=TEAL),
        txt(28, 260, "S/ 840", size=14, fill=GREEN, weight=700),
        btn_outline(270, "Cancelar reserva"),
        card(340, 150, radius=16),
        txt(28, 368, "Tambopata Jungle Lodge", size=16, fill=DARK, weight=700),
        status_chip(250, 350, "Confirmada"),
        txt(28, 394, "Habitacion: Suite Selva", size=13, fill=TEAL),
        txt(28, 416, "01/08/2026 - 04/08/2026", size=12, fill=TEAL),
        txt(28, 440, "S/ 600", size=14, fill=GREEN, weight=700),
    ]
    p += svg_close()
    write("11-my-reservations.svg", p)


def gen_profile() -> None:
    p = svg_open("Mi perfil")
    p.append(top_bar("Mi perfil"))
    p += [
        card(108, 130, radius=20),
        f'<circle cx="60" cy="158" r="36" fill="{GREEN}" fill-opacity="0.12"/>',
        f'<circle cx="88" cy="186" r="12" fill="{GREEN}"/>',
        txt(112, 156, "Jesus Uceda", size=18, fill=DARK, weight=700),
        txt(112, 178, "jesus@selvabooking.com", size=12, fill=TEAL),
        f'<rect x="112" y="186" width="80" height="22" rx="11" fill="{TROPICAL}" fill-opacity="0.18"/>',
        txt(152, 200, "Cliente", size=11, fill=GREEN, weight=600, anchor="middle", baseline="middle"),
        card(256, 180, radius=16),
        txt(32, 284, "Informacion de la cuenta", size=15, fill=GREEN, weight=700),
        txt(32, 314, "Jesus Uceda", size=13, fill=TEAL),
        txt(32, 342, "jesus@selvabooking.com", size=13, fill=TEAL),
        txt(32, 370, "Tipo: Cliente", size=13, fill=TEAL),
        btn_outline(390, "Editar nombre"),
        card(456, 90, radius=16),
        txt(32, 486, "Sesion", size=15, fill=GREEN, weight=700),
        btn_outline(510, "Cerrar sesion"),
    ]
    p += svg_close()
    write("12-profile.svg", p)


def gen_support() -> None:
    p = svg_open("Soporte y Ayuda")
    p.append(top_bar("Soporte y Ayuda"))
    p += [
        txt(20, 120, "Necesitas ayuda?", size=22, fill=GREEN, weight=700, serif=True),
        txt(20, 148, "Estamos aqui para ayudarte con reservas y tu cuenta.", size=13, fill=TEAL),
        card(168, 110, radius=16),
        txt(32, 196, "Preguntas frecuentes", size=15, fill=GREEN, weight=700),
        txt(32, 222, "- Como reservo un hotel?", size=12, fill=TEAL),
        txt(32, 244, "- Puedo cancelar?", size=12, fill=TEAL),
        txt(32, 266, "- Olvide mi contrasena?", size=12, fill=TEAL),
        card(296, 100, radius=16),
        txt(32, 324, "Contacto", size=15, fill=GREEN, weight=700),
        txt(32, 350, "soporte@selvabooking.com", size=12, fill=TEAL),
        card(414, 100, radius=16),
        txt(32, 442, "Reportar un problema", size=15, fill=GREEN, weight=700),
        txt(32, 468, "Describe el error y envialo a soporte", size=12, fill=TEAL),
    ]
    p += svg_close()
    write("13-support.svg", p)


def gen_drawer(*, admin: bool = False) -> None:
    fname = "15-drawer-admin.svg" if admin else "14-drawer-client.svg"
    p = svg_open("Menu lateral")
    p.append(top_bar("Selva Booking"))
    p.append(f"""    <g id="background-content" opacity="0.35">
      {hotel_card(120, "Eco Lodge", "Puerto Maldonado", "S/ 280")}
    </g>""")
    p.append(drawer_panel(admin=admin))
    p += svg_close()
    write(fname, p)


def gen_admin_dashboard() -> None:
    p = svg_open("Dashboard Admin")
    p.append(top_bar("Dashboard"))
    p += [
        card(108, 72, radius=14),
        txt(32, 140, "Solicitudes de admin pendientes", size=14, fill=GREEN, weight=700),
        txt(32, 162, "2", size=22, fill=ORANGE, weight=700),
        section_title(200, "Estadisticas generales"),
    ]
    stats = [("12", "Hoteles"), ("34", "Habitaciones"), ("28", "Reservas"), ("15", "Activas"), ("56", "Usuarios"), ("2", "Solicitudes")]
    positions = [(16, 220), (205, 220), (16, 320), (205, 320), (16, 420), (205, 420)]
    for (val, label), (x, y) in zip(stats, positions):
        p += [
            rect_card(x, y, 169, 84, r=12),
            txt(x + 16, y + 40, val, size=24, fill=GREEN, weight=700),
            txt(x + 16, y + 64, label, size=12, fill=TEAL),
        ]
    p += svg_close()
    write("16-admin-dashboard.svg", p)


def gen_admin_requests() -> None:
    p = svg_open("Solicitudes Admin")
    p.append(top_bar("Solicitudes de admin"))
    p += [
        txt(20, 116, "2 solicitudes pendientes", size=13, fill=TEAL),
        card(132, 130, radius=14),
        txt(32, 168, "Maria Lopez", size=16, fill=DARK, weight=700),
        txt(32, 190, "maria@email.com", size=12, fill=TEAL),
        txt(32, 212, "Solicita acceso como administrador", size=12, fill=ORANGE),
        btn_outline(236, "Rechazar", x=32, w=140),
        btn_primary(236, "Aceptar", w=140),
        card(290, 130, radius=14),
        txt(32, 328, "Carlos Ruiz", size=12, fill=TEAL),
    ]
    p += svg_close()
    write("17-admin-requests.svg", p)


def gen_admin_hotels(*, form: bool = False) -> None:
    if form:
        p = svg_open("Formulario Hotel")
        p.append(top_bar("Gestion de Hoteles"))
        p += [scrim(), rect_card(20, 120, W - 40, 620, r=20, fill=BG), txt(40, 160, "Nuevo hotel", size=18, fill=GREEN, weight=700)]
        y = 180
        for label in ["Nombre", "Ciudad", "Direccion", "Descripcion", "Categoria", "Precio minimo", "Calificacion"]:
            p.append(field(y, label))
            y += 70
        p += [btn_primary(700, "Guardar"), btn_outline(760, "Cancelar")]
        p += svg_close()
        write("19-admin-hotel-form.svg", p)
        return
    p = svg_open("Gestion de Hoteles")
    p.append(top_bar("Gestion de Hoteles"))
    p += [
        f'<rect x="{W - 68}" y="760" width="52" height="52" rx="26" fill="{ORANGE}"/>',
        txt(W - 42, 786, "+", size=28, fill=LIGHT, anchor="middle", baseline="middle"),
        card(108, 200, radius=16),
        f'<rect x="28" y="120" width="{W - 56}" height="96" rx="12" fill="{GREEN}" fill-opacity="0.15"/>',
        f'<rect x="36" y="128" width="52" height="20" rx="6" fill="{ORANGE}"/>',
        txt(62, 142, "Oferta", size=10, fill=LIGHT, weight=700, anchor="middle", baseline="middle"),
        txt(28, 250, "Eco Lodge Selva Verde", size=16, fill=DARK, weight=700),
        txt(28, 272, "Puerto Maldonado - Ecologico", size=12, fill=TEAL),
        txt(28, 294, "Desde S/ 280 / noche", size=13, fill=GREEN, weight=700),
    ]
    p += svg_close()
    write("18-admin-hotels.svg", p)


def gen_admin_rooms() -> None:
    p = svg_open("Habitaciones")
    p.append(top_bar("Habitaciones - Eco Lodge", back=True, menu=False))
    p += [
        f'<rect x="{W - 68}" y="760" width="52" height="52" rx="26" fill="{ORANGE}"/>',
        txt(W - 42, 786, "+", size=28, fill=LIGHT, anchor="middle", baseline="middle"),
        card(108, 96, radius=14),
        f'<rect x="28" y="120" width="72" height="72" rx="10" fill="{GREEN}" fill-opacity="0.12"/>',
        txt(112, 146, "Cabana Deluxe", size=15, fill=DARK, weight=700),
        txt(112, 168, "S/ 280/noche", size=13, fill=GREEN, weight=700),
        txt(112, 188, "Capacidad: 2 - Disponible", size=12, fill=TEAL),
        card(220, 96, radius=14),
        txt(112, 258, "Suite Selva", size=15, fill=DARK, weight=700),
        txt(112, 280, "S/ 350/noche", size=13, fill=GREEN, weight=700),
    ]
    p += svg_close()
    write("20-admin-rooms.svg", p)


def gen_admin_reservations(*, form: bool = False, detail: bool = False) -> None:
    if detail:
        p = svg_open("Detalle Reserva Admin")
        p.append(top_bar("Gestion de Reservas"))
        p += [
            scrim(),
            rect_card(24, 180, W - 48, 420, r=20, fill=BG),
            txt(44, 220, "Detalle de reserva", size=18, fill=GREEN, weight=700),
            txt(44, 252, "Hotel: Eco Lodge Selva Verde", size=13, fill=TEAL),
            txt(44, 276, "Habitacion: Cabana Deluxe", size=13, fill=TEAL),
            txt(44, 300, "Cliente: Jesus Uceda", size=13, fill=TEAL),
            txt(44, 324, "Total: S/ 840", size=13, fill=TEAL),
            status_chip(44, 340, "Pendiente"),
            btn_outline(420, "Editar", x=44, w=90),
            btn_outline(420, "Eliminar", x=150, w=90),
            btn_primary(420, "Confirmar", w=130),
            btn_outline(476, "Cerrar", x=44, w=120),
        ]
        p += svg_close()
        write("23-admin-reservation-detail.svg", p)
        return
    if form:
        p = svg_open("Nueva Reserva Admin")
        p.append(top_bar("Gestion de Reservas"))
        p += [
            scrim(),
            rect_card(16, 80, W - 32, 700, r=20, fill=BG),
            txt(32, 120, "Nueva reserva", size=18, fill=GREEN, weight=700),
            section_title(150, "Hotel"),
            rect_card(32, 162, W - 64, 48, r=10, fill=SURFACE),
            txt(32, 198, "Eco Lodge Selva Verde", size=14, fill=DARK, weight=600),
            field(230, "Nombre del cliente", value="Jesus Uceda"),
            field(310, "Email", value="jesus@email.com"),
            btn_primary(680, "Guardar"),
        ]
        p += svg_close()
        write("21-admin-reservation-form.svg", p)
        return
    p = svg_open("Gestion de Reservas")
    p.append(top_bar("Gestion de Reservas"))
    p += [
        field(100, "Buscar por hotel, cliente o email"),
        chip(16, 180, "Todas", selected=True),
        chip(88, 180, "Pendiente"),
        chip(188, 180, "Confirmada"),
        f'<rect x="{W - 68}" y="760" width="52" height="52" rx="26" fill="{ORANGE}"/>',
        txt(W - 42, 786, "+", size=28, fill=LIGHT, anchor="middle", baseline="middle"),
        card(230, 150, radius=14),
        txt(32, 268, "Eco Lodge Selva Verde", size=16, fill=DARK, weight=700),
        status_chip(260, 248, "Pendiente"),
        txt(32, 292, "Cliente: Jesus Uceda", size=12, fill=TEAL),
        txt(32, 314, "S/ 840", size=13, fill=GREEN, weight=700),
    ]
    p += svg_close()
    write("22-admin-reservations.svg", p)


def main() -> None:
    print(f"Generating Figma-friendly SVGs in {OUT}")
    prepare_assets()
    gen_splash()
    gen_login()
    gen_register()
    gen_forgot()
    gen_home()
    gen_search(filters=False)
    gen_search(filters=True)
    gen_hotel_detail()
    gen_booking()
    gen_payment(success=False)
    gen_payment(success=True)
    gen_reservations()
    gen_profile()
    gen_support()
    gen_drawer(admin=False)
    gen_drawer(admin=True)
    gen_admin_dashboard()
    gen_admin_requests()
    gen_admin_hotels(form=False)
    gen_admin_hotels(form=True)
    gen_admin_rooms()
    gen_admin_reservations(form=False)
    gen_admin_reservations(form=True)
    gen_admin_reservations(detail=True)
    count = len(list(OUT.glob("*.svg")))
    print(f"Done: {count} SVG files + assets/logo_selva_booking.png")


if __name__ == "__main__":
    main()
