# Selva Booking — 24 pantallas (diseño móvil preciso)

Mockups alineados con el código Android real.

## Carpeta

```
figma-export/svg-nine/
```

## Pantallas (24)

| Archivo | Pantalla |
|---------|----------|
| `01-splash.svg` | Splash |
| `02-login.svg` | Login |
| `03-register.svg` | Registro |
| `04-forgot-password.svg` | Recuperar contraseña |
| `05-home.svg` | Inicio |
| `06-search.svg` | Comparar hoteles |
| `06-search-filters.svg` | Comparar hoteles (filtros abiertos) |
| `07-hotel-detail.svg` | Detalle hotel |
| `08-booking.svg` | Completar reserva |
| `09-payment.svg` | Pago |
| `10-payment-success.svg` | Pago confirmado |
| `11-my-reservations.svg` | Mis reservas |
| `12-profile.svg` | Perfil |
| `13-support.svg` | Soporte y ayuda |
| `14-drawer-client.svg` | Drawer cliente |
| `15-drawer-admin.svg` | Drawer administrador |
| `16-admin-dashboard.svg` | Dashboard admin |
| `17-admin-requests.svg` | Solicitudes de admin |
| `18-admin-hotels.svg` | Gestión de hoteles |
| `19-admin-hotel-form.svg` | Formulario nuevo hotel |
| `20-admin-rooms.svg` | Habitaciones de un hotel |
| `21-admin-reservation-form.svg` | Nueva reserva (admin) |
| `22-admin-reservations.svg` | Gestión de reservas |
| `23-admin-reservation-detail.svg` | Detalle reserva (admin) |

## Importar en Figma

1. Abre [Selva Booking — UI](https://www.figma.com/design/xprpSSiPN0P853V9PinYYC)
2. Arrastra los `.svg` de `svg-nine/` al canvas
3. **No redimensiones** al soltar — cada frame es **390 × 844 px**

## Regenerar

```bash
python tools/generate_nine_figma_screens.py
```

## Placeholders de imagen

Donde la app muestra fotos verás un **cuadrado con cruz diagonal** y etiqueta ("Imagen", "Hotel", "Logo" o "Foto").

## Calidad v2

- Top bar **64dp**, padding **16dp** / **24dp** auth
- Texto con baseline calculada — sin letras cortadas
- Botones y FABs completos en todas las pantallas
- Modales admin con scrim semitransparente
- `viewBox` 390×844 fijo para importación sin deformar
