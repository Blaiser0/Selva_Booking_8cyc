# Exposición del Código — Selva Booking

**Proyecto:** Selva Booking (Android · Kotlin · Jetpack Compose · Firebase)  
**Equipo:** 4 integrantes  
**Fuente:** Elaboración propia

---

## Distribución de la exposición

| Bloque | Tema | Integrante | Duración sugerida | Archivo |
|:------:|------|------------|:-----------------:|---------|
| **1** | Arquitectura, stack y capas del proyecto | Persona 1 | 8–10 min | [BLOQUE_1_Arquitectura.md](BLOQUE_1_Arquitectura.md) |
| **2** | Autenticación, perfil y roles | Persona 2 | 8–10 min | [BLOQUE_2_Autenticacion.md](BLOQUE_2_Autenticacion.md) |
| **3** | Módulo cliente (reserva y pago) | Persona 3 | 10–12 min | [BLOQUE_3_Cliente.md](BLOQUE_3_Cliente.md) |
| **4** | Módulo administrador y Firebase | Persona 4 | 10–12 min | [BLOQUE_4_Administrador.md](BLOQUE_4_Administrador.md) |

**Duración total estimada:** 36–44 minutos + preguntas

---

## Orden recomendado de presentación

```
Bloque 1 (base técnica)
    ↓
Bloque 2 (cómo entra el usuario)
    ↓
Bloque 3 (qué hace el cliente)
    ↓
Bloque 4 (cómo administra el sistema)
```

---

## Demo en vivo (opcional, al cierre)

1. Registro de cliente con términos y condiciones
2. Búsqueda de hotel → reserva → pago simulado
3. Panel admin → editar hotel → ver reserva confirmada

---

## Archivos del proyecto que deben conocer todos

| Carpeta | Contenido |
|---------|-----------|
| `app/src/main/java/com/company/selvabooking/` | Código fuente Kotlin |
| `docs/diagramas/` | Diagramas de casos de uso UML |
| `docs/exposicion/` | Esta documentación de exposición |
| `app/build/outputs/apk/debug/app-debug.apk` | APK de prueba |

---

## Checklist antes de exponer

- [ ] Android Studio abre el proyecto sin errores
- [ ] Emulador o celular con APK instalado
- [ ] Cuenta cliente y cuenta admin de prueba
- [ ] Firebase Console abierta (Firestore + Auth) para mostrar datos en tiempo real
- [ ] Cada integrante tiene abierto **su bloque** y los archivos listados en él

---

*Fuente: Elaboración propia — Proyecto Selva Booking*
