# TeamFlow Manager

> ⚽ App de gestión de equipos de fútbol infantil. Controla minutos, cambios, asistencias y estadísticas en tiempo real. La herramienta esencial para el entrenador.

[![CI](https://github.com/jesuslcorominas/teamflowmanager/actions/workflows/pr-checks.yml/badge.svg)](https://github.com/jesuslcorominas/teamflowmanager/actions/workflows/pr-checks.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin)
![Android](https://img.shields.io/badge/Min%20SDK-29-green?logo=android)

---

## Features

- **Control de minutos** — Cronómetro individual por jugador, acumulando tiempo real de juego
- **Gestión de cambios** — Registro de sustituciones con control automático de tiempos
- **Temporizador de partido** — Cronómetro global con soporte de pausa y reanudación
- **Gestión del equipo** — Alta, edición y baja de jugadores y equipos
- **Estadísticas en tiempo real** — Visualización de tiempos y participación durante el partido
- **Soporte multiusuario** — Autenticación con Google Sign-In y datos sincronizados via Firestore
- **Deep linking** — Acceso directo a partido activo (`teamflowmanager://match`) e invitación de equipo (`teamflowmanager://team/accept`)

---

## Tech Stack

| Capa | Tecnología |
|------|-----------|
| **Lenguaje** | Kotlin 2.1.0 |
| **UI** | Jetpack Compose + Material 3 |
| **Arquitectura** | Clean Architecture + MVVM |
| **DI** | Koin 4.0.0 |
| **Base de datos local** | Room 2.8.3 |
| **Red** | Ktor Client 3.0.1 + KtorFit 2.6.0 |
| **Backend** | Firebase (Auth, Firestore, Storage, Crashlytics, Analytics) |
| **Autenticación** | Google Sign-In (Credential Manager) |
| **Async** | Kotlin Coroutines + Flow |
| **Imágenes** | Coil |
| **Animaciones** | Lottie |
| **Code style** | ktlint |
| **Testing** | JUnit 4, Mockk, Turbine, Coroutines Test |

---

## Architecture

El proyecto sigue **Clean Architecture** con separación estricta por capas y el siguiente flujo de dependencias:

```
┌─────────────────────────────────────┐
│            :app  (UI Layer)         │
│       Jetpack Compose + Material 3  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      :viewmodel  (Presentation)     │
│        ViewModel + StateFlow        │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    :usecase  (Business Logic)       │
│     Use Cases + Repository ifaces   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     :data:core  (Data Layer)        │
│     Repository implementations      │
└──────────┬──────────────┬───────────┘
           │              │
┌──────────▼─────┐  ┌─────▼──────────┐
│  :data:local   │  │  :data:remote  │
│  Room Database │  │  Ktor + KtorFit│
└────────────────┘  └────────────────┘

┌─────────────────────────────────────┐
│        :domain  (Domain Models)     │
│  Pure Kotlin — sin dependencias     │
│  Todas las capas dependen de él     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│        :di  (Koin DI wiring)        │
│  Agrega todos los módulos Koin      │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│        :service                     │
│  Servicios en segundo plano         │
└─────────────────────────────────────┘
```

> Las interfaces de repositorio se definen en `:usecase`; sus implementaciones viven en `:data:core`. Las implementaciones son `internal` a su módulo.

---

## Project Structure

```
TeamFlowManager/
├── app/                    # UI layer — Composables, Activities
├── viewmodel/              # ViewModels y estados de UI (StateFlow)
├── usecase/                # Casos de uso + interfaces de repositorio
├── domain/                 # Modelos de dominio (pure Kotlin, sin deps)
├── data/
│   ├── core/               # Implementaciones de repositorios
│   ├── local/              # Room DAOs, entities y database
│   └── remote/             # Ktor client + KtorFit API services
├── di/                     # Configuración Koin (AppModule + submódulos)
├── service/                # Servicios en background
├── gradle/
│   └── libs.versions.toml  # Version catalog centralizado
├── .github/
│   └── workflows/
│       └── pr-checks.yml   # CI — build, tests, ktlint
├── firestore.rules         # Reglas de seguridad Firestore
└── firebase.json           # Configuración Firebase
```

---

## Prerequisites

- **Android Studio** Ladybug (2024.2.1) o superior
- **JDK 17** (recomendado: Eclipse Temurin)
- **Android SDK** con API 29+ instalado
- Cuenta de Firebase con proyecto configurado y `google-services.json` en `app/`

---

## Installation

```bash
# 1. Clonar el repositorio
git clone https://github.com/jesuslcorominas/teamflowmanager.git
cd teamflowmanager

# 2. Añadir el fichero de configuración Firebase
#    Descárgalo desde Firebase Console y colócalo en:
cp google-services.json app/

# 3. Abrir en Android Studio o construir desde línea de comandos
./gradlew assembleDevDebug
```

---

## Build

El proyecto tiene dos **product flavors**:

| Flavor | App ID suffix | Descripción |
|--------|--------------|-------------|
| `dev`  | `.dev`       | Entorno de desarrollo |
| `prod` | —            | Entorno de producción |

```bash
# Debug (flavor dev)
./gradlew assembleDevDebug

# Debug (flavor prod)
./gradlew assembleProdDebug

# Release (requiere keystore configurado)
./gradlew assembleProdRelease

# Build completo (todos los variants)
./gradlew build
```

---

## Testing

```bash
# Todos los tests unitarios
./gradlew test

# Tests de un módulo concreto
./gradlew :usecase:test
./gradlew :viewmodel:test
./gradlew :data:core:test

# Una clase de test específica
./gradlew :usecase:test --tests "*.DeletePlayerUseCaseTest"

# Tests con informe detallado
./gradlew test --continue
```

Los tests usan **JUnit 4**, **Mockk** para mocking, **Turbine** para testing de Flows y **Coroutines Test** para código asíncrono.

---

## Code Style

```bash
# Comprobar estilo
./gradlew ktlintCheck

# Formatear automáticamente
./gradlew ktlintFormat
```

ktlint se aplica automáticamente a todos los subproyectos vía el plugin raíz.

---

## Continuous Integration

GitHub Actions ejecuta las siguientes comprobaciones en cada Pull Request a `main`:

| Check | Comando |
|-------|---------|
| Build | `./gradlew build --no-daemon` |
| Unit Tests | `./gradlew test --no-daemon` |
| Code Style | `./gradlew ktlintCheck --no-daemon` |

Los resultados de tests y reportes de build se publican como artefactos en cada ejecución. El workflow se ejecuta en un runner self-hosted con JDK 17.

---

## Kotlin Multiplatform (KMM) Readiness

El proyecto está preparado para una futura migración a KMP:

### ✅ Componentes ya multiplatform-ready
- **DI**: Koin 4.0.0 (totalmente multiplataforma)
- **Red**: Ktor Client 3.0.1 + KtorFit 2.6.0 (compatible con KMP)
- **Lógica de negocio**: módulos pure Kotlin (`:usecase`, `:domain`, `:data:core`, `:data:remote`)
- **UI Framework**: Compose Multiplatform 1.7.3 configurado

### 🔄 Componentes platform-specific (Android)
- **UI**: Jetpack Compose (Android-only actualmente)
- **Almacenamiento local**: Room → necesitaría SQLDelight para KMP
- **ViewModel**: implementación Android

### 🎯 Ruta de migración futura
1. Crear estructura de módulos KMP (`commonMain`, `androidMain`, `iosMain`)
2. Migrar componentes de UI compartidos a Compose Multiplatform
3. Reemplazar Room con SQLDelight para base de datos multiplataforma
4. Compartir lógica de negocio, capa de datos y UI entre plataformas

---

## License

This project is licensed under the Apache License 2.0 — see the [LICENSE](LICENSE) file for details.
