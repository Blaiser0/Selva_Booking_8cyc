# Selva Booking — Diagramas UML (v2)

**Proyecto:** Selva Booking · Android · Kotlin · Jetpack Compose · Firebase  
**Versión:** v2.1 · Julio 2026 (actualizado 22/07/2026) — alineado al código actual  
**Carpeta:** `docs/diagramas-v2/`

> Abre cualquier archivo `.md` con **Preview** (`Ctrl + Shift + V`) para ver los diagramas Mermaid.

---

## Cambios respecto a `docs/diagramas/` (v1)

| Aspecto | v1 (antiguo) | v2 (actual) |
|---------|--------------|-------------|
| Actores | 3 humanos + Firebase | **5 humanos** (Invitado, Cliente, Encargado, Administrador, SuperAdmin) |
| Roles admin | Un solo Administrador | **SuperAdmin** (control total) + **Administrador** (supervisión limitada) |
| Alcance admin limitado | No documentado | Solo ve encargados/hoteles/reservas que **él creó** (`creadoPorAdminId`) |
| Admin hoteles | Solo consulta | SuperAdmin: **CRUD** · Administrador: **solo lectura** |
| Admin habitaciones | Solo lectura | SuperAdmin: **CRUD** · Administrador: **solo lectura** |
| Admin reservas | Solo consulta | SuperAdmin: **CRUD** · Administrador: **solo lectura** |
| Comentarios encargado | No documentado | Pantalla `MANAGER_REVIEWS` |
| Comentarios SuperAdmin | No documentado | Por hotel desde `ADMIN_HOTELS` → `ADMIN_HOTEL_REVIEWS` |
| Auditoría | No documentado | `ADMIN_AUDIT` — revertir cambios de encargados |
| Alternar rol | Cliente ↔ Gerente | + Admin/SuperAdmin ↔ Cliente (vínculo encargados persiste) |
| Registro | Email y contraseña | + **teléfono** (9 dígitos) · nombre solo letras |
| Dashboard | Métricas básicas | Métricas por rol + comentarios + hoteles sin encargado/admin |

---

## Índice de archivos

| # | Archivo | Contenido |
|---|---------|-----------|
| 1 | [01_actores_y_general.md](01_actores_y_general.md) | Actores, diagrama general del sistema |
| 2 | [02_modulo_cliente.md](02_modulo_cliente.md) | Casos de uso del cliente |
| 3 | [03_modulo_gerente.md](03_modulo_gerente.md) | Casos de uso del encargado de hotel |
| 4 | [04_modulo_administrador.md](04_modulo_administrador.md) | SuperAdmin y Administrador limitado |
| 5 | [05_diagrama_clases.md](05_diagrama_clases.md) | Modelo de dominio (clases) |
| 6 | [06_arquitectura_mvvm.md](06_arquitectura_mvvm.md) | Capas, componentes y paquetes |
| 7 | [07_secuencia_reserva.md](07_secuencia_reserva.md) | Flujo reserva → pago → stock |
| 8 | [08_estados_reserva.md](08_estados_reserva.md) | Máquina de estados de reserva |
| 9 | [09_matriz_actores.md](09_matriz_actores.md) | Matriz actor × módulo |

## PlantUML (exportar PNG/SVG)

| Archivo | Diagrama |
|---------|----------|
| [casos_de_uso_general.puml](casos_de_uso_general.puml) | Vista general casos de uso |
| [diagrama_clases.puml](diagrama_clases.puml) | Clases de dominio |
| [arquitectura_mvvm.puml](arquitectura_mvvm.puml) | Capas MVVM |
| [secuencia_reserva.puml](secuencia_reserva.puml) | Secuencia reserva y pago |

```bash
# Requiere PlantUML instalado
java -jar plantuml.jar docs/diagramas-v2/*.puml
```

---

*Fuente: Elaboración propia — Proyecto Selva Booking*
