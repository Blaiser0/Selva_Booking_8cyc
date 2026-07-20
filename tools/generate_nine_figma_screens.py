"""Generate 9 polished mobile SVG screens for Selva Booking."""
from __future__ import annotations

import base64
import io
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    Image = None

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "figma-export" / "svg-nine"
LOGO_SRC = ROOT / "app" / "src" / "main" / "res" / "drawable" / "logo_selva_booking.png"

W, H = 390, 844
PAD = 16
AUTH_PAD = 24
TOP_BAR_H = 64
CONTENT_W = W - PAD * 2
AUTH_W = W - AUTH_PAD * 2

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
IMG_FILL = "#E8EDE9"
IMG_STROKE = "#2E5E3E"

FONT = "Inter, Roboto, Arial, sans-serif"
FONT_SERIF = "Georgia, 'Times New Roman', serif"
LOGO_B64: str | None = None

DEFS = f"""  <defs>
    <linearGradient id="gradDrawer" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="{GREEN}"/>
      <stop offset="100%" stop-color="{GREEN_DARK}"/>
    </linearGradient>
    <linearGradient id="gradHero" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#000000" stop-opacity="0.35"/>
      <stop offset="50%" stop-color="#000000" stop-opacity="0"/>
      <stop offset="100%" stop-color="#000000" stop-opacity="0.55"/>
    </linearGradient>
    <clipPath id="clipFrame"><rect width="{W}" height="{H}"/></clipPath>
    <filter id="shadow-sm" x="-8%" y="-8%" width="116%" height="120%">
      <feDropShadow dx="0" dy="2" stdDeviation="3" flood-color="#000000" flood-opacity="0.08"/>
    </filter>
    <filter id="shadow-md" x="-8%" y="-8%" width="116%" height="120%">
      <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="#000000" flood-opacity="0.12"/>
    </filter>
  </defs>"""


def esc(s: str) -> str:
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")


def ty(y: float, size: int) -> float:
    """Baseline Y for vertically centered single-line text in a box."""
    return y + size * 0.38


def text_el(
    x: float,
    y: float,
    content: str,
    *,
    size: int = 14,
    color: str = TEAL,
    weight: int = 400,
    anchor: str = "start",
    serif: bool = False,
    opacity: float | None = None,
) -> str:
    fam = FONT_SERIF if serif else FONT
    op = f' opacity="{opacity}"' if opacity is not None else ""
    anc = f' text-anchor="{anchor}"' if anchor != "start" else ""
    return (
        f'<text x="{x:.1f}" y="{ty(y, size):.1f}" font-family="{fam}" font-size="{size}" '
        f'font-weight="{weight}" fill="{color}"{anc}{op}>{esc(content)}</text>'
    )


def box(x: int, y: int, w: int, h: int, *, r: int = 0, fill: str = SURFACE, stroke: str | None = None, sw: float = 1.2) -> str:
    s = f' stroke="{stroke}" stroke-width="{sw}"' if stroke else ""
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}"{s}/>'


def img_placeholder(x: int, y: int, w: int, h: int, *, r: int = 12, label: str = "Imagen") -> str:
    cx, cy = w / 2, h / 2
    return f"""<g id="img-ph">
  {box(x, y, w, h, r=r, fill=IMG_FILL, stroke=IMG_STROKE, sw=1)}
  <line x1="{x + 8}" y1="{y + 8}" x2="{x + w - 8}" y2="{y + h - 8}" stroke="{IMG_STROKE}" stroke-width="1" stroke-opacity="0.25"/>
  <line x1="{x + w - 8}" y1="{y + 8}" x2="{x + 8}" y2="{y + h - 8}" stroke="{IMG_STROKE}" stroke-width="1" stroke-opacity="0.25"/>
  {text_el(x + cx, y + cy - 8, label, size=11, color=TEAL, anchor="middle", opacity=0.55) if label else ""}
</g>"""


def svg_file(title: str, parts: list[str]) -> str:
    lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"',
        f'     width="{W}" height="{H}" viewBox="0 0 {W} {H}" preserveAspectRatio="xMidYMid meet">',
        f"  <title>{esc(title)}</title>",
        DEFS,
        f'  <g id="screen">',
        box(0, 0, W, H, fill=BG),
        *parts,
        "  </g>",
        "</svg>",
    ]
    return "\n".join(lines)


def top_bar(title: str, *, menu: bool = True) -> str:
    menu_svg = ""
    if menu:
        menu_svg = f"""<g transform="translate({PAD},{20})">
  <rect x="0" y="0" width="18" height="2.5" rx="1.2" fill="{GREEN}"/>
  <rect x="0" y="7" width="18" height="2.5" rx="1.2" fill="{GREEN}"/>
  <rect x="0" y="14" width="18" height="2.5" rx="1.2" fill="{GREEN}"/>
</g>"""
    return f"""<g id="top-bar">
  {box(0, 0, W, TOP_BAR_H, fill=BG)}
  {menu_svg}
  {text_el(56, 22, title, size=20, color=GREEN, weight=700, serif=True)}
</g>"""


def btn_primary(x: int, y: int, w: int, label: str) -> str:
    return f"""<g id="btn-{esc(label)}">
  {box(x, y, w, 48, r=12, fill=GREEN)}
  {text_el(x + w / 2, y + 14, label, size=14, color=LIGHT, weight=600, anchor="middle")}
</g>"""


def btn_outline(x: int, y: int, w: int, label: str) -> str:
    return f"""<g id="btn-outline-{esc(label)}">
  {box(x, y, w, 44, r=12, fill=BG, stroke=GREEN)}
  {text_el(x + w / 2, y + 12, label, size=14, color=GREEN, weight=600, anchor="middle")}
</g>"""


def auth_field(y: int, label: str, *, value: str = "", password: bool = False) -> str:
    eye = ""
    if password:
        ex = AUTH_PAD + AUTH_W - 36
        eye = f'<circle cx="{ex}" cy="{y + 42}" r="10" fill="none" stroke="{GREEN}" stroke-width="1.5"/>'
    val = text_el(AUTH_PAD + 16, y + 26, value, size=16, color=TEAL) if value else ""
    return f"""<g id="field-{esc(label)}">
  {text_el(AUTH_PAD + 4, y, label, size=12, color=GREEN, weight=500)}
  {box(AUTH_PAD, y + 18, AUTH_W, 56, r=12, fill=BG, stroke=BROWN)}
  {val}
  {eye}
</g>"""


def logo_splash() -> str:
    w, h = int(W * 0.88), 170
    x, y = (W - w) // 2, (H - h) // 2
    if LOGO_B64:
        return f"""<g id="logo">
  {box(x, y, w, h, r=16, fill=SURFACE, stroke=GREEN, sw=1)}
  <image xlink:href="data:image/png;base64,{LOGO_B64}" x="{x + 8}" y="{y + 8}" width="{w - 16}" height="{h - 16}" preserveAspectRatio="xMidYMid meet"/>
</g>"""
    return img_placeholder(x, y, w, h, r=16, label="Logo")


def logo_auth(y: int) -> str:
    w, h = 280, 120
    x = (W - w) // 2
    if LOGO_B64:
        return f"""<g id="logo">
  {box(x, y, w, h, r=12, fill=SURFACE, stroke=GREEN, sw=1)}
  <image xlink:href="data:image/png;base64,{LOGO_B64}" x="{x + 6}" y="{y + 6}" width="{w - 12}" height="{h - 12}" preserveAspectRatio="xMidYMid meet"/>
</g>"""
    return img_placeholder(x, y, w, h, r=12, label="Logo")


def filter_chip(x: int, y: int, label: str, *, selected: bool = False) -> str:
    w = max(72, len(label) * 9 + 28)
    bg = GREEN if selected else BG
    fg = LIGHT if selected else GREEN
    stroke = "" if selected else f' stroke="{GREEN}" stroke-width="1"'
    return f"""<g id="chip-{esc(label)}">
  <rect x="{x}" y="{y}" width="{w}" height="32" rx="16" fill="{bg}"{stroke}/>
  {text_el(x + w / 2, y + 8, label, size=12, color=fg, weight=600, anchor="middle")}
</g>"""


def status_chip(x: int, y: int, label: str) -> str:
    palette = {
        "Pendiente": (ORANGE, "#FDF0E4"),
        "Confirmada": (GREEN, "#E8F0EA"),
        "Cancelada": (RED, "#F9E8E7"),
    }
    fg, bg = palette.get(label, (TEAL, SURFACE))
    w = len(label) * 9 + 22
    return f"""<g id="status-{esc(label)}">
  {box(x, y, w, 26, r=8, fill=bg)}
  {text_el(x + 11, y + 5, label, size=11, color=fg, weight=600)}
</g>"""


def hotel_card_vertical(x: int, y: int, name: str, city: str, price: str, *, offer: bool = False) -> str:
    cw, img_h, total_h = 280, 160, 268
    badge = ""
    if offer:
        badge = f"""{box(x + cw - 68, y + 10, 56, 22, r=8, fill=ORANGE)}
  {text_el(x + cw - 40, y + 14, "OFERTA", size=10, color=LIGHT, weight=700, anchor="middle")}"""
    return f"""<g id="hotel-card" filter="url(#shadow-md)">
  {box(x, y, cw, total_h, r=16, fill=SURFACE)}
  {img_placeholder(x, y, cw, img_h, r=16, label="Hotel")}
  <rect x="{x}" y="{y + img_h - 12}" width="{cw}" height="12" fill="{SURFACE}"/>
  {badge}
  {text_el(x + 14, y + img_h + 10, name, size=15, color=DARK, weight=600)}
  {text_el(x + 14, y + img_h + 32, city, size=12, color=TEAL)}
  {text_el(x + 14, y + img_h + 58, f"S/ {price}", size=14, color=GREEN, weight=700)}
  {text_el(x + 14, y + img_h + 76, "/ noche", size=12, color=TEAL)}
</g>"""


def hotel_offer_card(y: int, name: str, city: str, price: str) -> str:
    thumb = 110
    return f"""<g id="hotel-offer" filter="url(#shadow-sm)">
  {box(PAD, y, CONTENT_W, 144, r=16, fill=BG)}
  {img_placeholder(PAD + 12, y + 12, thumb, 120, r=12, label="Hotel")}
  {box(PAD + 12, y + 18, 48, 20, r=6, fill=ORANGE)}
  {text_el(PAD + 36, y + 20, "Oferta", size=10, color=LIGHT, weight=700, anchor="middle")}
  {text_el(PAD + 134, y + 20, name, size=15, color=DARK, weight=600)}
  {text_el(PAD + 134, y + 44, city, size=12, color=TEAL)}
  {text_el(PAD + 134, y + 108, f"desde S/ {price}", size=14, color=GREEN, weight=700)}
</g>"""


def drawer_nav_item(y: int, label: str, subtitle: str, *, selected: bool = False, section: str = "") -> str:
    parts = []
    if section:
        parts.append(text_el(24, y - 20, section.upper(), size=11, color=GREEN, weight=600, opacity=0.65))
        y += 6
    fill_bg = SURFACE if selected else BG
    parts.append(f"""<g id="nav-{esc(label)}">
  {box(20, y, 264, 68, r=14, fill=fill_bg)}
  {img_placeholder(32, y + 13, 42, 42, r=10, label="")}
  {text_el(84, y + 14, label, size=15, color=DARK, weight=600 if selected else 500)}
  {text_el(84, y + 36, subtitle, size=12, color=TEAL, opacity=0.75)}
</g>""")
    return "\n".join(parts)


def prepare_logo() -> None:
    global LOGO_B64
    if not LOGO_SRC.exists():
        return
    if Image:
        img = Image.open(LOGO_SRC).convert("RGBA")
        img.thumbnail((520, 200), Image.LANCZOS)
        buf = io.BytesIO()
        img.save(buf, "PNG", optimize=True)
        LOGO_B64 = base64.b64encode(buf.getvalue()).decode("ascii")
    else:
        LOGO_B64 = base64.b64encode(LOGO_SRC.read_bytes()).decode("ascii")


def write(name: str, content: str) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / name).write_text(content, encoding="utf-8")
    print(f"  {name}")


def gen_splash() -> None:
    write("01-splash.svg", svg_file("Splash", [logo_splash()]))


def gen_login() -> None:
    y = 40
    parts = [
        logo_auth(y),
        text_el(W / 2, y + 140, "Bienvenido", size=24, color=GREEN, weight=700, serif=True, anchor="middle"),
        text_el(W / 2, y + 172, "Inicia sesion para continuar", size=14, color=TEAL, anchor="middle"),
        auth_field(y + 200, "Correo electronico", value="tu@email.com"),
        auth_field(y + 282, "Contrasena", value="********", password=True),
        text_el(W / 2, y + 372, "Olvidaste tu contrasena?", size=14, color=GREEN, anchor="middle"),
        btn_primary(AUTH_PAD, y + 400, AUTH_W, "Iniciar sesion"),
        text_el(W / 2, y + 468, "No tienes cuenta? Registrate", size=14, color=GREEN, anchor="middle"),
    ]
    write("02-login.svg", svg_file("Login", parts))


def gen_register() -> None:
    y = 16
    parts = [
        logo_auth(y),
        text_el(W / 2, y + 136, "Crear cuenta", size=24, color=GREEN, weight=700, serif=True, anchor="middle"),
        auth_field(y + 168, "Nombre completo", value="Jesus Uceda"),
        auth_field(y + 250, "Correo electronico", value="tu@email.com"),
        auth_field(y + 332, "Contrasena", password=True),
        auth_field(y + 414, "Confirmar contrasena", password=True),
        btn_primary(AUTH_PAD, y + 500, AUTH_W, "Registrarse"),
        text_el(W / 2, y + 564, "Ya tienes cuenta? Inicia sesion", size=14, color=GREEN, anchor="middle"),
    ]
    write("03-register.svg", svg_file("Registro", parts))


def gen_home() -> None:
    y0 = TOP_BAR_H
    parts = [
        top_bar("Selva Booking"),
        f"""<g id="search">
  {box(PAD, y0 + PAD, CONTENT_W, 56, r=16, fill=BG, stroke=GREEN, sw=1)}
  <g transform="translate({PAD + 16},{y0 + PAD + 18})">
    <circle cx="8" cy="8" r="6" stroke="{GREEN}" stroke-width="1.8" fill="none"/>
    <line x1="12" y1="12" x2="17" y2="17" stroke="{GREEN}" stroke-width="1.8" stroke-linecap="round"/>
  </g>
  {text_el(PAD + 48, y0 + PAD + 16, "Buscar hoteles...", size=14, color=TEAL)}
</g>""",
        f"""<g id="quick-access" filter="url(#shadow-sm)">
  {box(PAD, y0 + 88, CONTENT_W, 72, r=16, fill=SURFACE)}
  <g transform="translate({PAD + 16},{y0 + 108})">
    <path d="M3 2 H15 V16 L9 12 L3 16 Z" fill="{GREEN}"/>
  </g>
  {text_el(PAD + 52, y0 + 106, "Mis Reservas", size=16, color=DARK, weight=600)}
  {text_el(PAD + 52, y0 + 128, "Consulta y gestiona tus reservas", size=12, color=TEAL)}
</g>""",
        text_el(PAD, y0 + 176, "Hoteles destacados", size=20, color=GREEN, weight=700, serif=True),
        hotel_card_vertical(PAD, y0 + 198, "Eco Lodge Selva Verde", "Puerto Maldonado", "280", offer=True),
        hotel_card_vertical(PAD + 296, y0 + 198, "Tambopata Jungle", "Tambopata", "200"),
        text_el(PAD, y0 + 482, "Ofertas especiales", size=20, color=GREEN, weight=700, serif=True),
        hotel_offer_card(y0 + 504, "Cabanas Madre de Dios", "Puerto Maldonado", "120"),
        text_el(PAD, y0 + 664, "Recomendaciones", size=20, color=GREEN, weight=700, serif=True),
        hotel_offer_card(y0 + 686, "Amazonia Rainforest", "Tambopata", "450"),
    ]
    write("05-home.svg", svg_file("Inicio", parts))


def gen_reservations() -> None:
    y0 = TOP_BAR_H
    card1_y = y0 + 52
    card2_y = card1_y + 212
    parts = [
        top_bar("Mis Reservas"),
        filter_chip(PAD, y0 + 8, "Todas", selected=True),
        filter_chip(96, y0 + 8, "Pendiente"),
        filter_chip(196, y0 + 8, "Confirmada"),
        f"""<g id="reserva-1" filter="url(#shadow-sm)">
  {box(PAD, card1_y, CONTENT_W, 196, r=16, fill=SURFACE)}
  {text_el(PAD + 16, card1_y + 16, "Eco Lodge Selva Verde", size=16, color=DARK, weight=600)}
  {status_chip(W - PAD - 96, card1_y + 14, "Pendiente")}
  {text_el(PAD + 16, card1_y + 46, "Habitacion: Cabana Deluxe", size=14, color=TEAL)}
  {text_el(PAD + 16, card1_y + 70, "20/07/2026 - 23/07/2026", size=12, color=TEAL)}
  {text_el(PAD + 16, card1_y + 94, "S/ 840", size=15, color=GREEN, weight=700)}
  {btn_outline(PAD + 16, card1_y + 124, CONTENT_W - 32, "Cancelar reserva")}
</g>""",
        f"""<g id="reserva-2" filter="url(#shadow-sm)">
  {box(PAD, card2_y, CONTENT_W, 160, r=16, fill=SURFACE)}
  {text_el(PAD + 16, card2_y + 16, "Tambopata Jungle Lodge", size=16, color=DARK, weight=600)}
  {status_chip(W - PAD - 104, card2_y + 14, "Confirmada")}
  {text_el(PAD + 16, card2_y + 46, "Habitacion: Suite Selva", size=14, color=TEAL)}
  {text_el(PAD + 16, card2_y + 70, "01/08/2026 - 04/08/2026", size=12, color=TEAL)}
  {text_el(PAD + 16, card2_y + 94, "S/ 600", size=15, color=GREEN, weight=700)}
</g>""",
    ]
    write("11-my-reservations.svg", svg_file("Mis Reservas", parts))


def gen_drawer() -> None:
    pw, ph = int(W * 0.78), int(H * 0.94)
    py = H - ph
    parts = [
        f'<rect width="{W}" height="{H}" fill="#000000" fill-opacity="0.48"/>',
        f"""<g id="drawer" filter="url(#shadow-md)">
  {box(0, py, pw, ph, r=28, fill=BG)}
  <rect x="0" y="{py}" width="{pw}" height="100" rx="28" fill="url(#gradDrawer)"/>
  {text_el(20, py + 22, "Selva Booking", size=20, color=LIGHT, weight=700, serif=True)}
  {text_el(20, py + 48, "Tu selva, tu destino", size=12, color=LIGHT, opacity=0.85)}
  {text_el(pw - 20, py + 34, "X", size=20, color=LIGHT, weight=600, anchor="end")}
  {box(20, py + 112, pw - 40, 96, r=18, fill=SURFACE)}
  {img_placeholder(32, py + 124, 72, 72, r=36, label="Foto")}
  {text_el(116, py + 130, "Hola!", size=12, color=GREEN, opacity=0.7)}
  {text_el(116, py + 150, "Usuario Demo", size=16, color=DARK, weight=700)}
  {text_el(116, py + 172, "demo@selvabooking.com", size=12, color=TEAL)}
  {box(116, py + 188, 80, 24, r=12, fill=TROPICAL)}
  <rect x="116" y="{py + 188}" width="80" height="24" rx="12" fill="{TROPICAL}" fill-opacity="0.2"/>
  {text_el(156, py + 192, "Cliente", size=11, color=GREEN, weight=600, anchor="middle")}
</g>""",
    ]
    nav_y = py + 224
    items = [
        ("MENU PRINCIPAL", "Inicio", "Explora hoteles destacados", True, False),
        ("", "Buscar", "Compara precios y ofertas", False, False),
        ("", "Mis Reservas", "Consulta tus viajes activos", False, False),
        ("TU CUENTA", "Mi Cuenta", "Perfil, foto y preferencias", False, False),
        ("", "Soporte y Ayuda", "Preguntas frecuentes", False, False),
        ("SESION", "Cerrar Sesion", "Salir de tu cuenta", False, True),
    ]
    for section, label, sub, sel, _ in items:
        parts.append(drawer_nav_item(nav_y, label, sub, selected=sel, section=section))
        nav_y += 84 if section else 76
    parts.append(f"""<g id="drawer-footer">
  {box((pw - 48) // 2, H - 24, 48, 4, r=2, fill=GREEN)}
  <rect x="{(pw - 48) // 2}" y="{H - 24}" width="48" height="4" rx="2" fill="{GREEN}" fill-opacity="0.35"/>
  {text_el(20, H - 56, "Selva Booking v1.0", size=11, color=GREEN, opacity=0.7)}
</g>""")
    write("14-drawer-client.svg", svg_file("Drawer Cliente", parts))


def gen_admin() -> None:
    y0 = TOP_BAR_H
    parts = [
        top_bar("Dashboard"),
        f"""<g id="alert" filter="url(#shadow-sm)">
  {box(PAD, y0 + PAD, CONTENT_W, 76, r=16, fill=SURFACE)}
  {text_el(PAD + 16, y0 + PAD + 14, "Solicitudes pendientes", size=16, color=GREEN, weight=600)}
  {text_el(PAD + 16, y0 + PAD + 40, "2 solicitudes de admin", size=13, color=TEAL)}
  <path d="M{W - PAD - 28} {y0 + PAD + 38} L{W - PAD - 16} {y0 + PAD + 38} L{W - PAD - 22} {y0 + PAD + 32} L{W - PAD - 22} {y0 + PAD + 44} Z" fill="{GREEN}"/>
</g>""",
        text_el(PAD, y0 + 108, "Estadisticas generales", size=20, color=GREEN, weight=700, serif=True),
    ]
    stats = [("12", "Hoteles"), ("34", "Habitaciones"), ("28", "Reservas"), ("15", "Activas"), ("56", "Usuarios"), ("2", "Solicitudes")]
    positions = [(PAD, y0 + 132), (205, y0 + 132), (PAD, y0 + 232), (205, y0 + 232), (PAD, y0 + 332), (205, y0 + 332)]
    for (val, lbl), (x, y) in zip(stats, positions):
        parts.append(f"""<g id="stat-{esc(lbl)}" filter="url(#shadow-sm)">
  {box(x, y, 169, 88, r=16, fill=SURFACE)}
  {text_el(x + 84, y + 22, val, size=24, color=GREEN, weight=700, anchor="middle")}
  {text_el(x + 84, y + 56, lbl, size=12, color=TEAL, anchor="middle")}
</g>""")
    write("16-admin-dashboard.svg", svg_file("Admin Dashboard", parts))


def gen_hotel_detail() -> None:
    parts = [
        f"""<g id="hero">
  {img_placeholder(0, 0, W, 280, r=0, label="Hotel")}
  <rect x="0" y="0" width="{W}" height="280" fill="url(#gradHero)"/>
  {box(12, 16, 40, 40, r=20, fill=LIGHT)}
  <path d="M34 36 H22 M26 30 L22 36 L26 42" stroke="{GREEN}" stroke-width="2" fill="none" stroke-linecap="round"/>
  {box(W - 68, 20, 52, 32, r=8, fill=GREEN)}
  {text_el(W - 42, 24, "4.8", size=14, color=LIGHT, weight=700, anchor="middle")}
  {text_el(PAD, 228, "Eco Lodge Selva Verde", size=22, color=LIGHT, weight=700, serif=True)}
  {text_el(PAD, 256, "Puerto Maldonado", size=14, color=LIGHT)}
</g>""",
        f"""<g id="meta">
  {text_el(PAD, 296, "★★★★★", size=16, color=ORANGE)}
  {text_el(PAD + 90, 296, "Ecologico", size=14, color=TEAL)}
  {box(PAD + 200, 288, 110, 28, r=8, fill=ORANGE)}
  <rect x="{PAD + 200}" y="288" width="110" height="28" rx="8" fill="{ORANGE}" fill-opacity="0.15"/>
  {text_el(PAD + 255, 292, "Oferta especial", size=12, color=ORANGE, weight=600, anchor="middle")}
  {text_el(PAD, 328, "Lodge ecologico en plena selva con cabanas de madera.", size=14, color=TEAL)}
</g>""",
        text_el(PAD, 364, "Servicios del hotel", size=20, color=GREEN, weight=700, serif=True),
    ]
    chips = ["WiFi", "Piscina", "Spa", "Tours"]
    cx = PAD
    for ch in chips:
        parts.append(f"""<g id="chip-{esc(ch)}">
  {box(cx, 388, 76, 36, r=20, fill=GREEN)}
  <rect x="{cx}" y="388" width="76" height="36" rx="20" fill="{GREEN}" fill-opacity="0.1"/>
  {text_el(cx + 38, 392, ch, size=13, color=GREEN, weight=500, anchor="middle")}
</g>""")
        cx += 84
    parts.append(f"""<g id="room" filter="url(#shadow-sm)">
  {box(PAD, 440, CONTENT_W, 220, r=16, fill=BG)}
  {img_placeholder(PAD + 12, 452, CONTENT_W - 24, 120, r=12, label="Habitacion")}
  {text_el(PAD + 16, 584, "Cabana Deluxe", size=16, color=DARK, weight=600)}
  {text_el(PAD + 16, 606, "Capacidad: 2 personas", size=12, color=TEAL)}
  {text_el(PAD + 16, 626, "S/ 280 / noche", size=16, color=GREEN, weight=700)}
  {btn_primary(W - PAD - 130, 616, 120, "Reservar")}
</g>""")
    parts.append(f"""<g id="sticky-bar">
  {box(0, H - 72, W, 72, fill=BG)}
  <line x1="0" y1="{H - 72}" x2="{W}" y2="{H - 72}" stroke="{SURFACE}" stroke-width="1"/>
  {text_el(PAD, H - 52, "Desde", size=12, color=TEAL)}
  {text_el(PAD, H - 28, "S/ 280", size=20, color=GREEN, weight=700)}
  {btn_primary(W - PAD - 160, H - 58, 160, "Ver ofertas")}
</g>""")
    write("07-hotel-detail.svg", svg_file("Detalle Hotel", parts))


def top_bar_back(title: str, *, subtitle: str = "") -> str:
    sub = text_el(56, 40, subtitle, size=12, color=TEAL) if subtitle else ""
    return f"""<g id="top-bar">
  {box(0, 0, W, TOP_BAR_H, fill=BG)}
  <g transform="translate({PAD},{24})">
    <path d="M18 4 H6 M10 0 L6 4 L10 8" stroke="{GREEN}" stroke-width="2" fill="none" stroke-linecap="round"/>
  </g>
  {text_el(56, 18, title, size=18, color=GREEN, weight=700, serif=True)}
  {sub}
</g>"""


def section_title(y: int, text: str) -> str:
    return text_el(PAD, y, text, size=20, color=GREEN, weight=700, serif=True)


def fab_button() -> str:
    x, y = W - PAD - 52, H - PAD - 52
    return f"""<g id="fab">
  {box(x, y, 52, 52, r=26, fill=ORANGE)}
  {text_el(x + 26, y + 16, "+", size=28, color=LIGHT, weight=600, anchor="middle")}
</g>"""


def scrim() -> str:
    return f'<rect width="{W}" height="{H}" fill="#000000" fill-opacity="0.45"/>'


def admin_hotel_list_card(y: int, name: str, city: str, price: str, *, offer: bool = False) -> str:
    badge = ""
    if offer:
        badge = f"""{box(PAD + 8, y + 8, 52, 20, r=6, fill=ORANGE)}
  {text_el(PAD + 34, y + 10, "Oferta", size=10, color=LIGHT, weight=700, anchor="middle")}"""
    return f"""<g id="admin-hotel" filter="url(#shadow-md)">
  {box(PAD, y, CONTENT_W, 200, r=16, fill=SURFACE)}
  {img_placeholder(PAD + 8, y + 8, CONTENT_W - 16, 96, r=12, label="Hotel")}
  {badge}
  {text_el(PAD + 16, y + 118, name, size=16, color=DARK, weight=600)}
  {text_el(PAD + 16, y + 142, city, size=12, color=TEAL)}
  {text_el(PAD + 16, y + 166, f"Desde S/ {price} / noche", size=13, color=GREEN, weight=700)}
</g>"""


def admin_room_card(y: int, name: str, price: str, capacity: str) -> str:
    return f"""<g id="admin-room" filter="url(#shadow-sm)">
  {box(PAD, y, CONTENT_W, 96, r=14, fill=SURFACE)}
  {img_placeholder(PAD + 12, y + 12, 72, 72, r=10, label="")}
  {text_el(PAD + 96, y + 18, name, size=15, color=DARK, weight=600)}
  {text_el(PAD + 96, y + 40, f"S/ {price}/noche", size=13, color=GREEN, weight=700)}
  {text_el(PAD + 96, y + 62, capacity, size=12, color=TEAL)}
</g>"""


def admin_request_card(y: int, name: str, email: str, note: str) -> str:
    return f"""<g id="admin-request" filter="url(#shadow-sm)">
  {box(PAD, y, CONTENT_W, 148, r=16, fill=SURFACE)}
  {text_el(PAD + 16, y + 16, name, size=16, color=DARK, weight=600)}
  {text_el(PAD + 16, y + 40, email, size=12, color=TEAL)}
  {text_el(PAD + 16, y + 64, note, size=12, color=ORANGE)}
  {btn_outline(PAD + 16, y + 92, 140, "Rechazar")}
  {btn_primary(PAD + 168, y + 92, 140, "Aceptar")}
</g>"""


def admin_reservation_card(y: int, hotel: str, client: str, total: str, status: str) -> str:
    return f"""<g id="admin-reserva" filter="url(#shadow-sm)">
  {box(PAD, y, CONTENT_W, 150, r=14, fill=SURFACE)}
  {text_el(PAD + 16, y + 16, hotel, size=16, color=DARK, weight=600)}
  {status_chip(W - PAD - 96, y + 14, status)}
  {text_el(PAD + 16, y + 46, f"Cliente: {client}", size=12, color=TEAL)}
  {text_el(PAD + 16, y + 70, f"Total: {total}", size=13, color=GREEN, weight=700)}
  {text_el(PAD + 16, y + 94, "20/07/2026 - 23/07/2026", size=12, color=TEAL)}
</g>"""


def gen_forgot() -> None:
    y0 = TOP_BAR_H
    parts = [
        top_bar_back("Recuperar contrasena"),
        text_el(W / 2, y0 + 40, "Restablecer contrasena", size=22, color=GREEN, weight=700, serif=True, anchor="middle"),
        text_el(W / 2, y0 + 72, "Te enviaremos un enlace a tu correo", size=14, color=TEAL, anchor="middle"),
        auth_field(y0 + 110, "Correo electronico", value="tu@email.com"),
        btn_primary(AUTH_PAD, y0 + 200, AUTH_W, "Enviar enlace"),
        text_el(W / 2, y0 + 268, "Volver al inicio de sesion", size=14, color=GREEN, anchor="middle"),
    ]
    write("04-forgot-password.svg", svg_file("Recuperar contrasena", parts))


def gen_search(*, filters: bool = False) -> None:
    y0 = TOP_BAR_H
    base = y0 + 340 if filters else y0 + 200
    parts = [
        top_bar("Comparar hoteles"),
        f"""<g id="search-input">
  {box(PAD, y0 + PAD, CONTENT_W, 56, r=24, fill=BG, stroke=GREEN, sw=1)}
  <g transform="translate({PAD + 16},{y0 + PAD + 18})">
    <circle cx="8" cy="8" r="6" stroke="{GREEN}" stroke-width="1.8" fill="none"/>
    <line x1="12" y1="12" x2="17" y2="17" stroke="{GREEN}" stroke-width="1.8" stroke-linecap="round"/>
  </g>
  {text_el(PAD + 48, y0 + PAD + 16, "Tambopata", size=14, color=TEAL)}
</g>""",
        text_el(PAD, y0 + 88, "4 ofertas encontradas", size=13, color=TEAL),
        text_el(W - PAD, y0 + 88, "Ocultar filtros" if filters else "Filtros", size=13, color=GREEN, weight=600, anchor="end"),
        filter_chip(PAD, y0 + 104, "Recomendados", selected=True),
        filter_chip(140, y0 + 104, "Menor precio"),
        filter_chip(260, y0 + 104, "Mejor valoracion"),
    ]
    if filters:
        parts += [
            f"""<g id="filters" filter="url(#shadow-sm)">
  {box(PAD, y0 + 148, CONTENT_W, 176, r=14, fill=SURFACE)}
  {text_el(PAD + 16, y0 + 164, "Ciudad", size=12, color=GREEN, weight=500)}
  {box(PAD + 16, y0 + 182, CONTENT_W - 32, 44, r=10, fill=BG, stroke=BROWN)}
  {text_el(PAD + 28, y0 + 194, "Tambopata", size=14, color=TEAL)}
  {text_el(PAD + 16, y0 + 238, "Precio maximo", size=12, color=GREEN, weight=500)}
  {box(PAD + 16, y0 + 256, CONTENT_W - 32, 44, r=10, fill=BG, stroke=BROWN)}
  {text_el(PAD + 28, y0 + 268, "S/ 500", size=14, color=TEAL)}
</g>""",
            filter_chip(PAD, y0 + 296, "0 estrellas"),
            filter_chip(110, y0 + 296, "3 estrellas"),
            filter_chip(196, y0 + 296, "4 estrellas", selected=True),
            filter_chip(282, y0 + 296, "5 estrellas"),
        ]
    parts += [
        hotel_offer_card(base, "Eco Lodge Selva Verde", "Puerto Maldonado", "280"),
        hotel_offer_card(base + 160, "Tambopata Jungle Lodge", "Tambopata", "200"),
    ]
    fname = "06-search-filters.svg" if filters else "06-search.svg"
    write(fname, svg_file("Comparar hoteles" + (" - Filtros" if filters else ""), parts))


def gen_booking() -> None:
    y0 = TOP_BAR_H
    parts = [
        top_bar_back("Completa tu reserva", subtitle="Confirma fechas y huespedes"),
        f"""<g id="hotel-summary" filter="url(#shadow-sm)">
  {box(PAD, y0 + PAD, CONTENT_W, 100, r=14, fill=SURFACE)}
  {img_placeholder(PAD + 12, y0 + PAD + 12, 72, 72, r=10, label="Hotel")}
  {text_el(PAD + 96, y0 + PAD + 18, "Eco Lodge Selva Verde", size=15, color=DARK, weight=600)}
  {text_el(PAD + 96, y0 + PAD + 40, "Cabana Deluxe", size=12, color=TEAL)}
  {text_el(PAD + 96, y0 + PAD + 62, "S/ 280 / noche", size=13, color=GREEN, weight=700)}
</g>""",
        section_title(y0 + 132, "Fechas de estadia"),
        f"""<g id="dates" filter="url(#shadow-sm)">
  {box(PAD, y0 + 160, CONTENT_W, 72, r=12, fill=BG)}
  {text_el(PAD + 32, y0 + 176, "Entrada", size=11, color=TEAL)}
  {text_el(PAD + 32, y0 + 198, "20/07/2026", size=14, color=DARK, weight=600)}
  {text_el(PAD + 204, y0 + 176, "Salida", size=11, color=TEAL)}
  {text_el(PAD + 204, y0 + 198, "23/07/2026", size=14, color=DARK, weight=600)}
</g>""",
        f"""<g id="guests" filter="url(#shadow-sm)">
  {box(PAD, y0 + 248, CONTENT_W, 88, r=14, fill=BG)}
  {text_el(PAD + 16, y0 + 266, "Huespedes", size=15, color=DARK, weight=600)}
  {text_el(PAD + 16, y0 + 288, "Max. 2 por habitacion", size=12, color=TEAL)}
  {text_el(W - PAD - 16, y0 + 280, "2", size=22, color=GREEN, weight=700, anchor="end")}
</g>""",
        f"""<g id="price-summary" filter="url(#shadow-sm)">
  {box(PAD, y0 + 352, CONTENT_W, 120, r=14, fill=BG)}
  {text_el(PAD + 16, y0 + 370, "Resumen de precio", size=16, color=GREEN, weight=600)}
  {text_el(PAD + 16, y0 + 398, "Precio por noche", size=13, color=TEAL)}
  {text_el(W - PAD - 16, y0 + 398, "S/ 280", size=13, color=DARK, anchor="end")}
  {text_el(PAD + 16, y0 + 420, "Noches", size=13, color=TEAL)}
  {text_el(W - PAD - 16, y0 + 420, "3", size=13, color=DARK, anchor="end")}
  {box(PAD + 12, y0 + 436, CONTENT_W - 24, 40, r=10, fill=GREEN)}
  <rect x="{PAD + 12}" y="{y0 + 436}" width="{CONTENT_W - 24}" height="40" rx="10" fill="{GREEN}" fill-opacity="0.08"/>
  {text_el(PAD + 24, y0 + 446, "Total a pagar", size=15, color=DARK, weight=700)}
  {text_el(W - PAD - 24, y0 + 446, "S/ 840", size=18, color=GREEN, weight=700, anchor="end")}
</g>""",
        f"""<g id="sticky-bar">
  {box(0, H - 72, W, 72, fill=BG)}
  <line x1="0" y1="{H - 72}" x2="{W}" y2="{H - 72}" stroke="{SURFACE}" stroke-width="1"/>
  {text_el(PAD, H - 52, "Total · 3 noches", size=12, color=TEAL)}
  {text_el(PAD, H - 28, "S/ 840", size=20, color=GREEN, weight=700)}
  {btn_primary(W - PAD - 170, H - 58, 170, "Reservar ahora")}
</g>""",
    ]
    write("08-booking.svg", svg_file("Completar reserva", parts))


def gen_payment(*, success: bool = False) -> None:
    if success:
        parts = [
            top_bar("Pago"),
            f"""<g id="success">
  <circle cx="{W // 2}" cy="340" r="48" fill="{GREEN}" fill-opacity="0.12"/>
  <path d="M{W // 2 - 14} 340 L{W // 2 - 4} 350 L{W // 2 + 16} 328" stroke="{GREEN}" stroke-width="3" fill="none" stroke-linecap="round"/>
  {text_el(W / 2, 420, "Pago confirmado!", size=24, color=GREEN, weight=700, serif=True, anchor="middle")}
  {text_el(W / 2, 452, "Tu reserva ha sido registrada", size=14, color=TEAL, anchor="middle")}
  {btn_primary(AUTH_PAD, 500, AUTH_W, "Ver mis reservas")}
</g>""",
        ]
        write("10-payment-success.svg", svg_file("Pago confirmado", parts))
        return
    y0 = TOP_BAR_H
    parts = [
        top_bar("Pago"),
        section_title(y0 + PAD, "Resumen de pago"),
        f"""<g id="payment-summary" filter="url(#shadow-md)">
  {box(PAD, y0 + 52, CONTENT_W, 200, r=16, fill=SURFACE)}
  {text_el(PAD + 16, y0 + 72, "Eco Lodge Selva Verde", size=15, color=DARK, weight=600)}
  {text_el(PAD + 16, y0 + 96, "Cabana Deluxe", size=12, color=TEAL)}
  {text_el(PAD + 16, y0 + 118, "20/07/2026 - 23/07/2026", size=12, color=TEAL)}
  {text_el(PAD + 16, y0 + 140, "2 huespedes", size=12, color=TEAL)}
  {text_el(PAD + 16, y0 + 168, "Total: S/ 840", size=16, color=GREEN, weight=700)}
  {text_el(PAD + 16, y0 + 196, "Pago simulado para demostracion", size=12, color=TEAL)}
</g>""",
        btn_primary(AUTH_PAD, y0 + 280, AUTH_W, "Confirmar pago"),
    ]
    write("09-payment.svg", svg_file("Pago", parts))


def gen_support() -> None:
    y0 = TOP_BAR_H
    parts = [
        top_bar("Soporte y Ayuda"),
        text_el(PAD, y0 + PAD, "Necesitas ayuda?", size=22, color=GREEN, weight=700, serif=True),
        text_el(PAD, y0 + 52, "Estamos aqui para ayudarte con reservas y tu cuenta.", size=13, color=TEAL),
        f"""<g id="faq" filter="url(#shadow-sm)">
  {box(PAD, y0 + 88, CONTENT_W, 110, r=16, fill=SURFACE)}
  {text_el(PAD + 16, y0 + 108, "Preguntas frecuentes", size=15, color=GREEN, weight=600)}
  {text_el(PAD + 16, y0 + 134, "- Como reservo un hotel?", size=12, color=TEAL)}
  {text_el(PAD + 16, y0 + 154, "- Puedo cancelar?", size=12, color=TEAL)}
  {text_el(PAD + 16, y0 + 174, "- Olvide mi contrasena?", size=12, color=TEAL)}
</g>""",
        f"""<g id="contact" filter="url(#shadow-sm)">
  {box(PAD, y0 + 214, CONTENT_W, 100, r=16, fill=SURFACE)}
  {text_el(PAD + 16, y0 + 234, "Contacto", size=15, color=GREEN, weight=600)}
  {text_el(PAD + 16, y0 + 260, "soporte@selvabooking.com", size=12, color=TEAL)}
  {text_el(PAD + 16, y0 + 282, "+51 999 888 777", size=12, color=TEAL)}
</g>""",
        f"""<g id="report" filter="url(#shadow-sm)">
  {box(PAD, y0 + 330, CONTENT_W, 100, r=16, fill=SURFACE)}
  {text_el(PAD + 16, y0 + 350, "Reportar un problema", size=15, color=GREEN, weight=600)}
  {text_el(PAD + 16, y0 + 376, "Describe el error y envialo a soporte", size=12, color=TEAL)}
  {btn_outline(PAD + 16, y0 + 396, CONTENT_W - 32, "Enviar reporte")}
</g>""",
    ]
    write("13-support.svg", svg_file("Soporte y Ayuda", parts))


def gen_drawer_admin() -> None:
    pw, ph = int(W * 0.78), int(H * 0.94)
    py = H - ph
    parts = [
        f'<rect width="{W}" height="{H}" fill="#000000" fill-opacity="0.48"/>',
        f"""<g id="drawer" filter="url(#shadow-md)">
  {box(0, py, pw, ph, r=28, fill=BG)}
  <rect x="0" y="{py}" width="{pw}" height="100" rx="28" fill="url(#gradDrawer)"/>
  {text_el(20, py + 22, "Selva Booking", size=20, color=LIGHT, weight=700, serif=True)}
  {text_el(20, py + 48, "Panel de administracion", size=12, color=LIGHT, opacity=0.85)}
  {text_el(pw - 20, py + 34, "X", size=20, color=LIGHT, weight=600, anchor="end")}
  {box(20, py + 112, pw - 40, 96, r=18, fill=SURFACE)}
  {img_placeholder(32, py + 124, 72, 72, r=36, label="Foto")}
  {text_el(116, py + 130, "Hola!", size=12, color=GREEN, opacity=0.7)}
  {text_el(116, py + 150, "Admin Demo", size=16, color=DARK, weight=700)}
  {text_el(116, py + 172, "admin@selvabooking.com", size=12, color=TEAL)}
  {box(116, py + 188, 110, 24, r=12, fill=ORANGE)}
  <rect x="116" y="{py + 188}" width="110" height="24" rx="12" fill="{ORANGE}" fill-opacity="0.2"/>
  {text_el(171, py + 192, "Administrador", size=11, color=ORANGE, weight=600, anchor="middle")}
</g>""",
    ]
    nav_y = py + 224
    items = [
        ("PANEL", "Dashboard", "Estadisticas y resumen", True),
        ("", "Solicitudes", "Peticiones de admin", False),
        ("GESTION", "Hoteles", "CRUD de hoteles", False),
        ("", "Reservas", "Gestion de reservas", False),
        ("CUENTA", "Mi Cuenta", "Perfil de administrador", False),
        ("", "Soporte y Ayuda", "Preguntas frecuentes", False),
        ("SESION", "Cerrar Sesion", "Salir de tu cuenta", False),
    ]
    for section, label, sub, sel in items:
        parts.append(drawer_nav_item(nav_y, label, sub, selected=sel, section=section))
        nav_y += 84 if section else 76
    parts.append(f"""<g id="drawer-footer">
  {box((pw - 48) // 2, H - 24, 48, 4, r=2, fill=GREEN)}
  <rect x="{(pw - 48) // 2}" y="{H - 24}" width="48" height="4" rx="2" fill="{GREEN}" fill-opacity="0.35"/>
  {text_el(20, H - 56, "Selva Booking v1.0", size=11, color=GREEN, opacity=0.7)}
</g>""")
    write("15-drawer-admin.svg", svg_file("Drawer Admin", parts))


def gen_admin_requests() -> None:
    y0 = TOP_BAR_H
    parts = [
        top_bar("Solicitudes de admin"),
        text_el(PAD, y0 + 8, "2 solicitudes pendientes", size=13, color=TEAL),
        admin_request_card(y0 + 36, "Maria Lopez", "maria@email.com", "Solicita acceso como administrador"),
        admin_request_card(y0 + 200, "Carlos Ruiz", "carlos@email.com", "Solicita acceso como administrador"),
    ]
    write("17-admin-requests.svg", svg_file("Solicitudes Admin", parts))


def gen_admin_hotels(*, form: bool = False) -> None:
    if form:
        y0 = TOP_BAR_H
        parts = [
            top_bar("Gestion de Hoteles"),
            scrim(),
            f"""<g id="hotel-form" filter="url(#shadow-md)">
  {box(20, 120, W - 40, 620, r=20, fill=BG)}
  {text_el(40, 140, "Nuevo hotel", size=18, color=GREEN, weight=700)}
</g>""",
        ]
        y = 168
        for label in ["Nombre", "Ciudad", "Direccion", "Descripcion", "Categoria", "Precio minimo", "Calificacion"]:
            parts.append(f"""<g id="field-{esc(label)}">
  {text_el(40, y, label, size=12, color=GREEN, weight=500)}
  {box(36, y + 18, W - 72, 44, r=10, fill=BG, stroke=BROWN)}
</g>""")
            y += 70
        parts += [
            btn_primary(36, 640, W - 72, "Guardar"),
            btn_outline(36, 700, W - 72, "Cancelar"),
        ]
        write("19-admin-hotel-form.svg", svg_file("Formulario Hotel", parts))
        return
    y0 = TOP_BAR_H
    parts = [
        top_bar("Gestion de Hoteles"),
        admin_hotel_list_card(y0 + PAD, "Eco Lodge Selva Verde", "Puerto Maldonado - Ecologico", "280", offer=True),
        admin_hotel_list_card(y0 + 232, "Tambopata Jungle Lodge", "Tambopata", "200"),
        fab_button(),
    ]
    write("18-admin-hotels.svg", svg_file("Gestion de Hoteles", parts))


def gen_admin_rooms() -> None:
    y0 = TOP_BAR_H
    parts = [
        top_bar_back("Habitaciones", subtitle="Eco Lodge Selva Verde"),
        admin_room_card(y0 + PAD, "Cabana Deluxe", "280", "Capacidad: 2 - Disponible"),
        admin_room_card(y0 + 120, "Suite Selva", "350", "Capacidad: 4 - Disponible"),
        admin_room_card(y0 + 228, "Bungalow Familiar", "420", "Capacidad: 6 - Ocupada"),
        fab_button(),
    ]
    write("20-admin-rooms.svg", svg_file("Habitaciones", parts))


def gen_admin_reservations(*, form: bool = False, detail: bool = False) -> None:
    if detail:
        parts = [
            top_bar("Gestion de Reservas"),
            scrim(),
            f"""<g id="reservation-detail" filter="url(#shadow-md)">
  {box(24, 180, W - 48, 420, r=20, fill=BG)}
  {text_el(44, 200, "Detalle de reserva", size=18, color=GREEN, weight=700)}
  {text_el(44, 232, "Hotel: Eco Lodge Selva Verde", size=13, color=TEAL)}
  {text_el(44, 256, "Habitacion: Cabana Deluxe", size=13, color=TEAL)}
  {text_el(44, 280, "Cliente: Jesus Uceda", size=13, color=TEAL)}
  {text_el(44, 304, "20/07/2026 - 23/07/2026", size=13, color=TEAL)}
  {text_el(44, 328, "Total: S/ 840", size=13, color=TEAL)}
  {status_chip(44, 348, "Pendiente")}
  {btn_outline(44, 400, 90, "Editar")}
  {btn_outline(150, 400, 90, "Eliminar")}
  {btn_primary(250, 400, 130, "Confirmar")}
  {btn_outline(44, 456, 120, "Cerrar")}
</g>""",
        ]
        write("23-admin-reservation-detail.svg", svg_file("Detalle Reserva Admin", parts))
        return
    if form:
        parts = [
            top_bar("Gestion de Reservas"),
            scrim(),
            f"""<g id="reservation-form" filter="url(#shadow-md)">
  {box(16, 80, W - 32, 700, r=20, fill=BG)}
  {text_el(32, 100, "Nueva reserva", size=18, color=GREEN, weight=700)}
  {section_title(140, "Hotel")}
  {box(32, 162, W - 64, 48, r=10, fill=SURFACE)}
  {text_el(44, 174, "Eco Lodge Selva Verde", size=14, color=DARK, weight=600)}
  {text_el(32, 230, "Nombre del cliente", size=12, color=GREEN, weight=500)}
  {box(32, 248, W - 64, 44, r=10, fill=BG, stroke=BROWN)}
  {text_el(44, 260, "Jesus Uceda", size=14, color=TEAL)}
  {text_el(32, 310, "Email", size=12, color=GREEN, weight=500)}
  {box(32, 328, W - 64, 44, r=10, fill=BG, stroke=BROWN)}
  {text_el(44, 340, "jesus@email.com", size=14, color=TEAL)}
  {btn_primary(32, 680, W - 64, "Guardar")}
</g>""",
        ]
        write("21-admin-reservation-form.svg", svg_file("Nueva Reserva Admin", parts))
        return
    y0 = TOP_BAR_H
    parts = [
        top_bar("Gestion de Reservas"),
        f"""<g id="search">
  {box(PAD, y0 + 8, CONTENT_W, 44, r=12, fill=BG, stroke=BROWN)}
  {text_el(PAD + 16, y0 + 20, "Buscar por hotel, cliente o email", size=14, color=TEAL)}
</g>""",
        filter_chip(PAD, y0 + 64, "Todas", selected=True),
        filter_chip(96, y0 + 64, "Pendiente"),
        filter_chip(196, y0 + 64, "Confirmada"),
        admin_reservation_card(y0 + 108, "Eco Lodge Selva Verde", "Jesus Uceda", "S/ 840", "Pendiente"),
        admin_reservation_card(y0 + 274, "Tambopata Jungle Lodge", "Maria Lopez", "S/ 600", "Confirmada"),
        fab_button(),
    ]
    write("22-admin-reservations.svg", svg_file("Gestion de Reservas", parts))


def gen_profile() -> None:
    y0 = TOP_BAR_H
    cx = W // 2
    parts = [
        top_bar("Mi perfil"),
        f"""<g id="profile-header" filter="url(#shadow-sm)">
  {box(PAD, y0 + PAD, CONTENT_W, 230, r=20, fill=SURFACE)}
  {img_placeholder(cx - 44, y0 + PAD + 20, 88, 88, r=44, label="Foto")}
  <circle cx="{cx + 30}" cy="{y0 + PAD + 88}" r="14" fill="{GREEN}"/>
  <rect x="{cx + 22}" y="{y0 + PAD + 80}" width="16" height="16" rx="8" fill="{LIGHT}"/>
  {text_el(cx, y0 + PAD + 128, "Jesus Uceda", size=22, color=GREEN, weight=700, serif=True, anchor="middle")}
  {text_el(cx, y0 + PAD + 156, "jesus@selvabooking.com", size=14, color=TEAL, anchor="middle")}
  {box(cx - 44, y0 + PAD + 176, 88, 28, r=14, fill=GREEN)}
  <rect x="{cx - 44}" y="{y0 + PAD + 176}" width="88" height="28" rx="14" fill="{GREEN}" fill-opacity="0.12"/>
  {text_el(cx, y0 + PAD + 180, "Cliente", size=13, color=GREEN, weight=600, anchor="middle")}
</g>""",
        f"""<g id="account" filter="url(#shadow-sm)">
  {box(PAD, y0 + 278, CONTENT_W, 200, r=16, fill=BG)}
  {text_el(PAD + 16, y0 + 298, "Informacion de la cuenta", size=16, color=GREEN, weight=600)}
  {text_el(PAD + 16, y0 + 330, "Nombre: Jesus Uceda", size=14, color=TEAL)}
  {text_el(PAD + 16, y0 + 354, "Email: jesus@selvabooking.com", size=14, color=TEAL)}
  {text_el(PAD + 16, y0 + 378, "Tipo: Cliente", size=14, color=TEAL)}
  {btn_outline(PAD + 16, y0 + 408, 155, "Editar nombre")}
  {btn_primary(PAD + 183, y0 + 408, 155, "Guardar")}
</g>""",
        f"""<g id="session" filter="url(#shadow-sm)">
  {box(PAD, y0 + 494, CONTENT_W, 108, r=16, fill=BG)}
  {text_el(PAD + 16, y0 + 514, "Sesion", size=16, color=GREEN, weight=600)}
  {btn_outline(PAD + 16, y0 + 544, CONTENT_W - 32, "Cerrar sesion")}
</g>""",
    ]
    write("12-profile.svg", svg_file("Perfil", parts))


def main() -> None:
    print(f"Generating polished screens -> {OUT}")
    prepare_logo()
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
    gen_drawer()
    gen_drawer_admin()
    gen_admin()
    gen_admin_requests()
    gen_admin_hotels(form=False)
    gen_admin_hotels(form=True)
    gen_admin_rooms()
    gen_admin_reservations(form=False)
    gen_admin_reservations(form=True)
    gen_admin_reservations(detail=True)
    print(f"Done: {len(list(OUT.glob('*.svg')))} files")


if __name__ == "__main__":
    main()
