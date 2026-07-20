# Selva Booking — Exportación SVG para Figma

Mockups de todas las interfaces de la app, listos para importar en Figma.

## Carpeta

```
figma-export/svg/
├── assets/
│   └── logo_selva_booking.png   ← requerido para el splash
├── 01-splash.svg
├── 02-login.svg
└── … (24 pantallas en total)
```

**Importante:** arrastra la carpeta `svg` completa (incluyendo `assets/`) a Figma. Si solo importas un `.svg` suelto, el logo del splash no cargará y algunos elementos pueden verse mal.

## Pantallas incluidas (24 archivos)

| Archivo | Pantalla |
|---------|----------|
| `01-splash.svg` | Splash / inicio de app |
| `02-login.svg` | Iniciar sesión |
| `03-register.svg` | Registro |
| `04-forgot-password.svg` | Recuperar contraseña |
| `05-home.svg` | Inicio (cliente) |
| `06-search.svg` | Comparar hoteles |
| `06-search-filters.svg` | Búsqueda con filtros |
| `07-hotel-detail.svg` | Detalle del hotel |
| `08-booking.svg` | Completar reserva |
| `09-payment.svg` | Pago |
| `10-payment-success.svg` | Pago confirmado |
| `11-my-reservations.svg` | Mis reservas |
| `12-profile.svg` | Mi perfil |
| `13-support.svg` | Soporte y ayuda |
| `14-drawer-client.svg` | Menú lateral (cliente) |
| `15-drawer-admin.svg` | Menú lateral (admin) |
| `16-admin-dashboard.svg` | Dashboard admin |
| `17-admin-requests.svg` | Solicitudes admin |
| `18-admin-hotels.svg` | Gestión de hoteles |
| `19-admin-hotel-form.svg` | Formulario hotel |
| `20-admin-rooms.svg` | Habitaciones |
| `21-admin-reservation-form.svg` | Nueva reserva (admin) |
| `22-admin-reservations.svg` | Gestión de reservas |
| `23-admin-reservation-detail.svg` | Detalle reserva (admin) |

## Cómo importar en Figma (sin deformaciones)

1. Abre Figma → **File → New design file**
2. Arrastra la carpeta **`figma-export/svg`** entera al canvas (no archivos sueltos)
3. Cada SVG es **390 × 844 px** con `viewBox` fijo — no redimensiones al importar
4. Si Figma pregunta por fuentes, usa **Inter** o **Arial** como sustituto
5. Opcional: agrupa cada pantalla en un **Frame** de 390×844 llamado igual que el archivo

### Correcciones aplicadas para Figma

- `preserveAspectRatio="xMidYMid meet"` en cada SVG
- Gradientes y `clipPath` declarados en `<defs>` al inicio
- Colores en hex + `fill-opacity` (sin `rgba()`)
- Iconos vectoriales en lugar de emojis
- Logo como PNG externo optimizado (no base64 embebido de 700 KB)
- Texto con `dominant-baseline="middle"` para alineación correcta

## Regenerar los SVG

Si cambias la UI en Android, vuelve a generar:

```bash
python tools/generate_figma_svgs.py
```

## Paleta de colores

| Token | Hex |
|-------|-----|
| Fondo | `#FAF9F6` |
| Verde principal | `#2E5E3E` |
| Verde oscuro | `#1E3D2A` |
| Superficie | `#F3F1EA` |
| Naranja (ofertas) | `#E8954A` |
| Texto | `#1A2E1A` / `#2C4A4A` |
| Error | `#B3261E` |
