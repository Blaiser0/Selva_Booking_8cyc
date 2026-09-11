# Imágenes UML — Selva Booking

Diagramas exportados en PNG desde los archivos `.puml` de `docs/uml/`.

Generados con PlantUML (Julio 2026). Los diagramas usan **nombres descriptivos** (sin códigos UC-XX).

## Casos de uso

| Archivo | Diagrama |
|---------|----------|
| `casos_de_uso.png` | Casos de uso general (todos los actores) |
| `casos_uso_invitado.png` | Casos de uso detallados — Invitado |
| `casos_uso_cliente.png` | Casos de uso detallados — Cliente |
| `casos_uso_encargado.png` | Casos de uso detallados — Encargado |
| `casos_uso_administrador.png` | Casos de uso detallados — Administrador |
| `casos_uso_superadmin.png` | Casos de uso detallados — SuperAdmin |

## Secuencia

| Archivo | Diagrama |
|---------|----------|
| `secuencia_invitado.png` | Secuencia — Invitado |
| `secuencia_cliente.png` | Secuencia — Cliente |
| `secuencia_encargado.png` | Secuencia — Encargado |
| `secuencia_administrador.png` | Secuencia — Administrador |
| `secuencia_superadmin.png` | Secuencia — SuperAdmin |
| `secuencia_todos.png` … `secuencia_todos_004.png` | Secuencia combinada (multipágina) |

## Otros diagramas

| Archivo | Diagrama |
|---------|----------|
| `estados.png` | Estados de reserva |
| `clases.png` | Clases de dominio |
| `componentes.png` | Componentes MVVM |
| `despliegue.png` | Despliegue Android + Firebase |

## Regenerar imágenes

```powershell
$java = "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
$jar  = "..\..\tools\plantuml.jar"
Set-Location docs\uml
& $java -jar $jar -tpng -o imagenes *.puml
```
