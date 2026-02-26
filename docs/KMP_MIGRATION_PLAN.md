# KMP Migration Plan — TeamFlowManager

> **Fecha de análisis**: 2026-02-26
> **Rama de análisis**: `kmp-migration-analysis`
> **Arquitecto**: Senior KMP/Android Architect (Claude Code)

---

## 1. Estado Actual del Proyecto

### 1.1 Resumen arquitectónico

El proyecto sigue una arquitectura Clean Architecture estricta de 8 módulos con límites bien definidos:

```
domain      → Pure Kotlin. Modelos, interfaces de repositorio, interfaces de use case.
usecase     → Pure Kotlin. Implementaciones de use cases.
data/core   → Implementaciones de repositorio. Puente entre usecase e interfaces DataSource.
data/local  → Room DAOs e implementaciones DataSource locales.
data/remote → KtorFit + Firebase (Firestore, Storage, Auth) datasource implementations.
service     → Android service (MatchNotificationControllerImpl).
viewmodel   → Jetpack ViewModel + StateFlow.
di          → Koin DI composition root.
app         → Compose UI + MainActivity.
```

### 1.2 Inventario de archivos por módulo

| Módulo | Archivos Kotlin | Tipo actual | Estado KMP |
|--------|----------------|-------------|------------|
| `:domain` | ~38 | `kotlin.jvm` | ✅ Listo (con excepciones*) |
| `:usecase` | ~76 | `kotlin.jvm` | ✅ Listo |
| `:data:core` | ~30 | `kotlin.jvm` | ✅ Listo |
| `:data:local` | ~25 | `android.library` | ⚠️ Requiere refactor (Room) |
| `:data:remote` | ~40 | `android.library` | ⚠️ Bloqueado (Firebase) |
| `:service` | 2 | `android.library` | ❌ Candidato a eliminación |
| `:viewmodel` | 19 | `android.library` | ⚠️ Requiere refactor (SavedStateHandle) |
| `:di` | ~10 | `android.library` | ⚠️ Requiere restructuración |
| `:app` | ~80 | `android.application` | ❌ Android-only |

**Excepciones en `:domain`*:
- `navigation/Route.kt` — contiene `showTopBar`, `showBottomBar`, `showFab`, etc. Conceptos de navegación UI acoplados al dominio.
- `utils/PdfExporter.kt` y `utils/MatchReportPdfExporter.kt` — interfaces cuyos usos dependen de `android.graphics.pdf`.
- `notification/MatchNotificationController.kt` — concepto Android-specific (notifications).

### 1.3 Nivel de acoplamiento Android por módulo

| Módulo | Dependencias Android directas | Nivel de bloqueo |
|--------|------------------------------|-----------------|
| `:domain` | Ninguna en código (`Route.kt` tiene UI concerns) | Bajo |
| `:usecase` | Ninguna | Ninguno |
| `:data:core` | Ninguna | Ninguno |
| `:data:local` | Room 2.8.3, `kotlinx-coroutines-android` | Medio (Room KMP disponible en 2.7+) |
| `:data:remote` | Firebase BOM 33.6.0, `play-services-auth` | Alto (Firebase Android-only) |
| `:service` | `MatchNotificationControllerImpl` referencia use cases; ServiceModule Koin | Android-only |
| `:viewmodel` | `SavedStateHandle` en 17/19 ViewModels, `lifecycle-viewmodel 2.8.6` | Medio (ViewModel KMP desde 2.8.0; SavedStateHandle solo Android) |
| `:app` | Compose, Coil, Lottie, Google Fonts, Firebase Crashlytics | Android-only |

### 1.4 Dependencias críticas y su estado KMP

| Librería | Versión actual | KMP en catálogo | Estado KMP | Estrategia |
|----------|---------------|-----------------|------------|------------|
| `koin-core` | 4.0.0 | ✅ | Full KMP | `koin-core` → commonMain; `koin-android` → androidMain |
| `ktor-client-core` | 3.0.1 | ✅ | Full KMP | Core → commonMain; OkHttp/Darwin por plataforma |
| `ktorfit-lib` | 2.6.0 | ✅ | Full KMP | Migrar a targets KMP con KSP por target |
| `kotlinx-coroutines-core` | 1.9.0 | ✅ | Full KMP | `Dispatchers.IO` → `Dispatchers.Default` en commonMain |
| `kotlinx-serialization-json` | 1.7.3 | ✅ | Full KMP | Ya en catálogo, usarlo en commonMain |
| `lifecycle-viewmodel-ktx` | 2.8.6 | ✅ | Partial KMP | ViewModel → commonMain; `SavedStateHandle` → androidMain |
| `compose-multiplatform` | 1.7.3 | ✅ (en catálogo, no aplicado) | Full KMP | Fase 2 |
| `room-runtime` | 2.8.3 | ❌ | Partial KMP | Room KMP disponible desde 2.7+; alternativa: SQLDelight |
| `firebase-bom` | 33.6.0 | ❌ | Android-only | Boundary `expect/actual`; alternativa: GitLive Firebase SDK |
| `moshi` | 1.15.2 | ❌ | Android/JVM-only | Reemplazar con `kotlinx-serialization` |
| `coil-compose` | 2.5.0 | ❌ | Android-only | Coil 3.x tiene soporte KMP; Fase 2 |
| `navigation-compose` | 2.9.5 | ❌ | Android-only | Mover `Route.kt` a `:app`; Fase 2 usar Compose Navigation CMP |

### 1.5 Porcentaje estimado reutilizable en commonMain

| Categoría | % reutilizable |
|-----------|---------------|
| Modelos de dominio (`:domain/model`) | ~95% |
| Interfaces de use cases | ~100% |
| Implementaciones de use cases | ~100% |
| Repositorios (`:data:core`) | ~100% |
| DataSource local (`:data:local`) | ~80% (Room KMP) |
| DataSource remoto (`:data:remote`) | ~60% (excluye Firebase) |
| ViewModels | ~75% (excluye `SavedStateHandle`) |
| **Total estimado** | **~82% del negocio reutilizable** |

---

## 2. Propuesta de Arquitectura KMP

### 2.1 Estructura de módulos propuesta

```
:domain         → kotlin("multiplatform") — commonMain puro
:usecase        → kotlin("multiplatform") — commonMain puro
:data:core      → kotlin("multiplatform") — commonMain puro
:data:local     → kotlin("multiplatform") — commonMain + androidMain (Room) + iosMain (SQLDelight o Room KMP)
:data:remote    → kotlin("multiplatform") — commonMain (Ktorfit/Ktor) + androidMain/iosMain (Firebase expect/actual)
:viewmodel      → kotlin("multiplatform") — commonMain (ViewModel KMP) + androidMain (SavedStateHandle)
:di             → por plataforma — androidMain + iosMain (Koin multiplatform modules)
:app            → android.application — solo Android (MainActivity, Compose UI)
:iosApp         → (nuevo) Xcode project o KMP iOS entry point
```

### 2.2 Qué va a commonMain

**`:domain` commonMain:**
- Todo `model/` (33 modelos de dominio)
- Todo `usecase/` (interfaces)
- `utils/TimeProvider.kt`, `utils/TransactionRunner.kt`, `utils/FileHandler.kt`
- Interfaces `PdfExporter`, `MatchReportPdfExporter` (las implementaciones son platform-specific)
- `MatchNotificationController.kt` (como interfaz; la implementación es androidMain)

**`:domain` — mover a `:app`:**
- `navigation/Route.kt` — conceptos UI (`showTopBar`, `showBottomBar`, etc.) no pertenecen al dominio

**`:usecase` commonMain:**
- Todas las implementaciones (~76 archivos) sin cambios

**`:data:core` commonMain:**
- Todas las implementaciones de repositorios sin cambios

**`:data:local` commonMain:**
- DAOs e implementaciones Room (con `room-runtime-ktx` KMP)
- Entidades anotadas con `@Entity` (compatibles KMP)

**`:data:remote` commonMain:**
- Interfaces Ktorfit, modelos de red, lógica de negocio de red
- `FirestoreTransactionRunner` → expect/actual

**`:viewmodel` commonMain:**
- 17/19 ViewModels completos (los que no usan `SavedStateHandle` directamente)
- Los que usan `SavedStateHandle`: extraer a función `initFromArgs(matchId: Long?)` que se llama desde androidMain

### 2.3 Patrón `expect/actual` necesario

| Concepto | commonMain (expect) | androidMain (actual) | iosMain (actual) |
|----------|--------------------|-----------------------|------------------|
| Firebase Auth | `AuthDataSource` interface | Firebase Auth Android | GitLive SDK o stub |
| Firebase Firestore | `RemoteDataSource` interface | Firestore Android | GitLive SDK o stub |
| MatchNotificationController | `interface MatchNotificationController` | `MatchNotificationControllerImpl` | No-op o local notifications |
| PDF Export | `interface PdfExporter` | `android.graphics.pdf` impl | PDFKit (iOS) |
| `Dispatchers.IO` | Usar `Dispatchers.Default` | OK | OK |
| `SavedStateHandle` args | `fun initFromArgs(id: Long?)` en base | `SavedStateHandle.get<>()` → llama initFromArgs | NavigationArgs propio |
| `TransactionRunner` | `interface TransactionRunner` | Room `withTransaction` | SQLite transaction |

### 2.4 Componentes a eliminar

Ver Sección 4.

### 2.5 Cambios de build system

1. Agregar `kotlin("multiplatform")` plugin a `:domain`, `:usecase`, `:data:core`
2. Reemplazar `kotlin("multiplatform")` por `kotlin("android")` donde aplique
3. Agregar `ios()` o `iosArm64()` + `iosSimulatorArm64()` targets
4. Configurar KSP para múltiples targets (Room, Ktorfit)
5. Reemplazar `moshi` por `kotlinx-serialization` en `:data:local`

---

## 3. Plan de Migración — Fase 1: Infraestructura

> Las tareas están formateadas como GitHub Issues. Cada una es independiente o tiene dependencias explícitas.
> **Orden recomendado**: seguir numeración. Las tareas de infraestructura (KMP-1 a KMP-5) son prerequisitos para el resto.

---

### KMP-1: Migrar `:domain` a Kotlin Multiplatform

**Tipo**: `enhancement` `kmp` `no-breaking`
**Módulo**: `:domain`
**Estimación**: 4h

**Descripción**
El módulo `:domain` es Pure Kotlin sin dependencias Android y puede convertirse en un módulo KMP inmediatamente. Este es el paso fundacional de toda la migración.

**Objetivo**
Hacer que `:domain` compile para targets Android y iOS sin cambios en el código de negocio.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:domain`:
   - Reemplazar `id("org.jetbrains.kotlin.jvm")` por `kotlin("multiplatform")`
   - Configurar targets: `androidTarget()`, `iosArm64()`, `iosSimulatorArm64()`
   - Mover todas las dependencias a `commonMain`
2. Mover `navigation/Route.kt` a `:app` (ver KMP-2)
3. Actualizar `settings.gradle.kts` si aplica
4. Verificar que todos los módulos dependientes compilan

**Criterios de aceptación**
- [ ] `./gradlew :domain:compileKotlinAndroid` pasa sin errores
- [ ] `./gradlew :domain:compileKotlinIosArm64` pasa sin errores
- [ ] Todos los tests de módulos dependientes siguen pasando: `./gradlew :usecase:test`

---

### KMP-2: Mover `Route.kt` de `:domain` a `:app`

**Tipo**: `refactor` `kmp`
**Módulo**: `:domain` → `:app`
**Dependencias**: ninguna (puede hacerse antes que KMP-1)
**Estimación**: 2h

**Descripción**
`domain/navigation/Route.kt` contiene propiedades de UI (`showTopBar`, `showBottomBar`, `showFab`, `canGoBack`, `hasSearchBar`, `showSettingsButton`). Estas son responsabilidades de la capa de presentación y no del dominio. Su presencia en `:domain` es un acoplamiento UI-domain que bloquea la portabilidad KMP.

**Objetivo**
Limpiar `:domain` de conceptos de navegación UI. El dominio no debe conocer nada sobre cómo se presenta la navegación.

**Pasos técnicos**
1. Crear `app/src/main/kotlin/.../navigation/Route.kt` con el contenido actual
2. Actualizar todos los imports en `:app` (MainViewModel, NavGraph, etc.)
3. Verificar que `:viewmodel` no importa directamente `Route` (si lo hace, evaluar si ese import también debe moverse)
4. Eliminar `domain/src/main/kotlin/.../navigation/Route.kt`
5. Eliminar el package `navigation` de `:domain`

**Criterios de aceptación**
- [ ] `:domain` no tiene ningún archivo en el package `navigation`
- [ ] `:domain` no tiene dependencias implícitas a frameworks de navegación
- [ ] `./gradlew :app:assembleDevDebug` compila sin errores
- [ ] La navegación funciona correctamente en runtime

---

### KMP-3: Migrar `:usecase` a Kotlin Multiplatform

**Tipo**: `enhancement` `kmp`
**Módulo**: `:usecase`
**Dependencias**: KMP-1
**Estimación**: 2h

**Descripción**
El módulo `:usecase` es Pure Kotlin con dependencia única en `:domain`. Una vez que `:domain` es KMP, `:usecase` puede convertirse en KMP sin cambios en la lógica.

**Objetivo**
Hacer que las ~76 implementaciones de use case compilen para Android e iOS.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:usecase`:
   - `id("org.jetbrains.kotlin.jvm")` → `kotlin("multiplatform")`
   - Configurar mismos targets que `:domain`
   - Dependencia `implementation(project(":domain"))` en `commonMain`
2. Verificar que `kotlinx-coroutines-core` en `commonMain` (ya lo está)
3. Asegurarse de que no se usa `Dispatchers.IO` directamente (usar `Dispatchers.Default` o inyectar dispatcher)

**Criterios de aceptación**
- [ ] `./gradlew :usecase:compileKotlinAndroid` pasa
- [ ] `./gradlew :usecase:compileKotlinIosArm64` pasa
- [ ] `./gradlew :usecase:test` pasa (todos los ~76 tests existentes)

---

### KMP-4: Migrar `:data:core` a Kotlin Multiplatform

**Tipo**: `enhancement` `kmp`
**Módulo**: `:data:core`
**Dependencias**: KMP-3
**Estimación**: 3h

**Descripción**
`:data:core` implementa las interfaces de repositorio definidas en `:usecase` y delega a DataSources abstractos. No tiene dependencias Android directas y puede migrar a KMP sin cambios de lógica.

**Objetivo**
Hacer que las implementaciones de repositorio compilen para todas las plataformas.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:data:core`:
   - `id("org.jetbrains.kotlin.jvm")` → `kotlin("multiplatform")`
   - Mover dependencias a `commonMain`
2. Verificar que las interfaces `DataSource` que define no usan tipos Android
3. Revisar `kotlinx-coroutines-core` ya en `commonMain`

**Criterios de aceptación**
- [ ] `./gradlew :data:core:compileKotlinAndroid` pasa
- [ ] `./gradlew :data:core:compileKotlinIosArm64` pasa
- [ ] `./gradlew :data:core:test` pasa

---

### KMP-5: Reemplazar Moshi por kotlinx.serialization en `:data:local`

**Tipo**: `refactor` `kmp`
**Módulo**: `:data:local`
**Dependencias**: ninguna (blocker para KMP-6)
**Estimación**: 4h

**Descripción**
`moshi` y `moshi-kotlin-codegen` son bibliotecas JVM/Android-only. Se usan en `:data:local` para serializar preferencias locales. `kotlinx-serialization-json` (ya en el catálogo a versión 1.7.3) es su reemplazo KMP-compatible directo.

**Objetivo**
Eliminar todas las dependencias de Moshi del proyecto y migrar a kotlinx.serialization.

**Pasos técnicos**
1. Identificar todos los usos de `@Json`, `@JsonClass`, `Moshi`, `JsonAdapter` en `:data:local`
2. Reemplazar `@JsonClass(generateAdapter = true)` por `@Serializable`
3. Reemplazar `@Json(name = "...")` por `@SerialName("...")`
4. Reemplazar instancias de `Moshi` por `Json { ... }` de kotlinx.serialization
5. Eliminar de `build.gradle.kts`: `moshi`, `moshi-kotlin`, `moshi-kotlin-codegen`, `ksp(libs.moshi.kotlin.codegen)`
6. Agregar `implementation(libs.kotlinx.serialization.json)` y `id("org.jetbrains.kotlin.plugin.serialization")` plugin
7. Eliminar `moshi*` del catálogo `libs.versions.toml` si ya no se usa en ningún módulo

**Criterios de aceptación**
- [ ] Ningún archivo en el proyecto importa `com.squareup.moshi`
- [ ] `./gradlew :data:local:testDebugUnitTest` pasa
- [ ] `./gradlew :app:assembleDevDebug` compila sin errores

---

### KMP-6: Migrar `:data:local` a Kotlin Multiplatform con Room KMP

**Tipo**: `enhancement` `kmp`
**Módulo**: `:data:local`
**Dependencias**: KMP-4, KMP-5
**Estimación**: 8h

**Descripción**
Room tiene soporte KMP desde la versión 2.7. La versión actual es 2.8.3, suficientemente moderna. La migración requiere configurar el driver de base de datos por plataforma (`BundledSQLiteDriver` para Android/iOS).

**Objetivo**
Hacer que el módulo de persistencia local compile para Android e iOS.

**Pasos técnicos**
1. Actualizar `build.gradle.kts` de `:data:local`:
   - Cambiar de `android.library` a `kotlin("multiplatform")` con `androidTarget()` + iOS targets
   - Agregar `room-runtime` con KMP support (verificar que `2.8.3` incluye KMP o actualizar)
   - Configurar KSP para múltiples targets: `add("kspAndroid", ...)`, `add("kspIosArm64", ...)`
2. Agregar en `commonMain`: `implementation(libs.androidx.room.runtime)`
3. En `androidMain`: usar `Room.databaseBuilder()` con `AndroidSQLiteDriver`
4. En `iosMain`: crear `actual fun createDatabase()` con `BundledSQLiteDriver`
5. Marcar `@Database` con `@ConstructedBy` para factory multiplataforma
6. Verificar que todos los DAOs usan `suspend` / `Flow` (no callbacks Android)

**Criterios de aceptación**
- [ ] `./gradlew :data:local:compileKotlinAndroid` pasa
- [ ] `./gradlew :data:local:compileKotlinIosArm64` pasa
- [ ] `./gradlew :data:local:testDebugUnitTest` pasa
- [ ] Base de datos funciona correctamente en Android (smoke test manual)

---

### KMP-7: Crear boundary `expect/actual` para Firebase en `:data:remote`

**Tipo**: `enhancement` `kmp` `architecture`
**Módulo**: `:data:remote`
**Dependencias**: KMP-4
**Estimación**: 12h

**Descripción**
Firebase Android SDK no tiene soporte KMP oficial. La estrategia es extraer toda la lógica Firebase detrás de interfaces (ya existe parcialmente) y crear implementaciones `actual` por plataforma. Para iOS se puede usar el GitLive Firebase KMP SDK (`dev.gitlive:firebase-*`) como proveedor, o un stub que lanza `UnsupportedOperationException` en una primera fase.

**Objetivo**
Desacoplar `:data:remote` del SDK Firebase Android para que `commonMain` compile en iOS.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:data:remote` a `kotlin("multiplatform")`
2. En `commonMain`: interfaces Ktorfit, modelos de red, lógica de negocio remota sin Firebase
3. Crear `expect class FirestoreDataSource` en `commonMain`
4. En `androidMain`: `actual class FirestoreDataSource` con Firebase Android SDK
5. En `iosMain`: `actual class FirestoreDataSource` con GitLive SDK (`dev.gitlive:firebase-firestore:2.1.0`) o stub
6. Mismo patrón para `FirebaseAuthDataSource` y `FirebaseStorageDataSource`
7. `FirestoreTransactionRunner` → `expect/actual` (ya en exclusiones JaCoCo)
8. Migrar cliente Ktor de `ktor-client-okhttp` (Android) a engine por plataforma:
   - `androidMain`: `ktor-client-okhttp`
   - `iosMain`: `ktor-client-darwin`

**Criterios de aceptación**
- [ ] `./gradlew :data:remote:compileKotlinAndroid` pasa
- [ ] `./gradlew :data:remote:compileKotlinIosArm64` pasa sin Firebase real (stub OK)
- [ ] `./gradlew :data:remote:testDebugUnitTest` pasa (tests existentes no rotos)
- [ ] Login con Google funciona en Android tras la migración

---

### KMP-8: Migrar `:viewmodel` a Kotlin Multiplatform

**Tipo**: `enhancement` `kmp`
**Módulo**: `:viewmodel`
**Dependencias**: KMP-3, KMP-4
**Estimación**: 8h

**Descripción**
`lifecycle-viewmodel` tiene soporte KMP desde la versión 2.8.0. La versión actual (`2.8.6`) ya lo soporta. El único bloqueador son los 17 ViewModels que usan `SavedStateHandle` para recibir argumentos de navegación. La estrategia es extraer la inicialización a métodos `open`/`protected` que en Android se llaman desde el constructor con `SavedStateHandle`, y en iOS se llaman desde la capa de navegación.

**Objetivo**
Hacer que los 19 ViewModels compilen para Android e iOS con mínimos cambios de lógica.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:viewmodel` a `kotlin("multiplatform")`
2. En `commonMain`: `implementation(libs.androidx.lifecycle.viewmodel.ktx)` (ya KMP)
3. Para ViewModels con `SavedStateHandle`:
   - Ejemplo: `MatchViewModel(savedStateHandle: SavedStateHandle)` →
     - `MatchViewModel()` en commonMain con `protected open fun initArgs(matchId: Long?) {}`
     - `MatchViewModelAndroid(savedStateHandle: SavedStateHandle) : MatchViewModel()` en androidMain que llama `initArgs(savedStateHandle.get("matchId"))`
   - O alternativamente: extraer factory en androidMain
4. Asegurarse de que `viewModelScope` funciona en KMP (usa `Dispatchers.Main`)
5. Verificar que `koin-androidx-compose` permanece en androidMain

**Criterios de aceptación**
- [ ] `./gradlew :viewmodel:compileKotlinAndroid` pasa
- [ ] `./gradlew :viewmodel:compileKotlinIosArm64` pasa
- [ ] `./gradlew :viewmodel:testDebugUnitTest` — todos los tests existentes pasan
- [ ] Los ViewModels se instancian correctamente en Android con argumentos de navegación

---

### KMP-9: Restructurar `:di` para soporte multiplataforma

**Tipo**: `enhancement` `kmp`
**Módulo**: `:di`
**Dependencias**: KMP-6, KMP-7, KMP-8
**Estimación**: 6h

**Descripción**
El módulo `:di` es el composition root de Koin. Koin 4.0 tiene soporte KMP completo: `koin-core` es KMP, `koin-android` es androidMain. La restructuración implica separar los módulos Koin comunes (use cases, repositorios, ViewModels sin Android-specific) de los módulos Android-specific (Firebase, Room Android driver, `koin-android` initializer).

**Objetivo**
Tener un conjunto de módulos Koin en `commonMain` y extensiones en `androidMain`/`iosMain`.

**Pasos técnicos**
1. Cambiar `:di` a `kotlin("multiplatform")`
2. `commonMain`: módulos Koin para `:usecase`, `:data:core`, `:viewmodel` (lo que no es platform-specific)
3. `androidMain`: módulos para Firebase implementations, Room Android, `koin-android` setup
4. `iosMain`: módulos para Firebase iOS (GitLive) o stubs, Room iOS driver
5. Exponer función `fun initKoin(additionalModules: List<Module> = emptyList())` en commonMain
6. En `:app` (Android): llamar `startKoin { androidContext(this); modules(androidModules()) }`
7. En iOS entry point: llamar `initKoin()`

**Criterios de aceptación**
- [ ] `./gradlew :di:compileKotlinAndroid` pasa
- [ ] `./gradlew :di:compileKotlinIosArm64` pasa
- [ ] `./gradlew :app:assembleDevDebug` compila y la app arranca correctamente
- [ ] Inyección de dependencias funciona en Android para todos los flujos existentes

---

## 4. Componentes Candidatos a Eliminación

### 4.1 `:service` — Candidato principal

**Motivo**: El módulo `:service` contiene exactamente 2 archivos:
- `MatchNotificationControllerImpl.kt` — implementación Android del interface `MatchNotificationController` del dominio
- `ServiceModule.kt` — módulo Koin que registra la implementación

La interfaz `MatchNotificationController` ya vive en `:domain`. La implementación usa servicios de notificación Android que no tienen equivalente en iOS. En una arquitectura KMP:

- La implementación Android de `MatchNotificationController` puede vivir directamente en `:app` (androidMain) o en `:di` (androidMain)
- No justifica tener un módulo separado de 2 archivos
- El `ServiceModule.kt` se integraría en el módulo Koin de `:di`

**Acción propuesta**: Mover `MatchNotificationControllerImpl` a `:app/androidMain/` o `:di/androidMain/` y eliminar el módulo `:service`.

**Issue asociada**: Ver KMP-10.

### 4.2 `domain/notification/MatchNotificationController.kt` — Evaluar reubicación

**Motivo**: La interfaz `MatchNotificationController` define contratos de notificación. En KMP, las notificaciones son platform-specific. La interfaz puede permanecer en commonMain de `:domain` si se considera parte del dominio de negocio (la app necesita notificar eventos de partido), o puede moverse a androidMain si se decide que las notificaciones no son cross-platform.

**Recomendación**: Mantener en commonMain como interfaz; crear `expect/actual` si se implementa en iOS.

### 4.3 Librería `moshi` — Eliminación total

**Motivo**: Moshi (v1.15.2) es JVM/Android-only. No tiene soporte KMP. Se usa exclusivamente en `:data:local` para serializar preferencias.
**Reemplazar por**: `kotlinx-serialization-json` (ya en catálogo a v1.7.3).
**Acción**: KMP-5 cubre esta eliminación.
**Eliminar del catálogo**: `moshi`, `moshi-kotlin`, `moshi-kotlin-codegen` y sus versiones.

### 4.4 `ktor-client-okhttp` — Confinado a androidMain

**Motivo**: OkHttp es Android/JVM-only. En KMP, cada plataforma usa su propio engine de Ktor.
**Acción**: Mover a `androidMain`, agregar `ktor-client-darwin` para iOS en `iosMain`.

---

### KMP-10: Eliminar módulo `:service` y reubicar su contenido

**Tipo**: `refactor` `kmp` `cleanup`
**Módulo**: `:service`
**Dependencias**: KMP-9
**Estimación**: 2h

**Descripción**
El módulo `:service` tiene exactamente 2 archivos (19 líneas de lógica real). No justifica existir como módulo independiente una vez que se complete la restructuración KMP.

**Objetivo**
Eliminar el módulo `:service` para reducir la complejidad del grafo de módulos.

**Pasos técnicos**
1. Mover `MatchNotificationControllerImpl.kt` a `:app/src/main/kotlin/.../service/` (o a `:di/androidMain/`)
2. Integrar `ServiceModule.kt` en el módulo Koin de Android en `:di`
3. Eliminar `:service` de `settings.gradle.kts`
4. Eliminar la carpeta `service/`
5. Actualizar todas las dependencias a `:service` en otros módulos

**Criterios de aceptación**
- [ ] El directorio `service/` no existe en el proyecto
- [ ] `settings.gradle.kts` no menciona `:service`
- [ ] `./gradlew :app:assembleDevDebug` compila sin errores
- [ ] Las notificaciones de partido siguen funcionando en Android

---

## 5. Fase 2: Estrategia UI con Compose Multiplatform

### 5.1 Estado actual de Compose Multiplatform en el proyecto

El catálogo ya incluye:
```toml
composeMultiplatform = "1.7.3"
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
```

Este plugin **no está aplicado a ningún módulo** actualmente. La UI es 100% Jetpack Compose Android en el módulo `:app`.

### 5.2 Viabilidad de Compose Multiplatform 1.7.3

**Estado en febrero 2026**: Compose Multiplatform 1.7.x es estable para iOS. El runtime, layouts, Material3, animaciones, y la mayoría de los componentes funcionan en iOS.

| Componente | Estado CMP 1.7.3 | Impacto en TeamFlowManager |
|-----------|-----------------|---------------------------|
| Material3 | ✅ Estable | Uso extensivo — directamente portable |
| `LazyColumn`, `LazyRow` | ✅ Estable | Listas de equipos, partidos — portable |
| `Canvas` / gráficas | ✅ Estable | `compose-charts` requiere fork KMP o alternativa |
| Animaciones | ✅ Estable | Lottie → alternativa KMP necesaria |
| Navegación | ✅ Stable (Compose Navigation CMP 2.8+) | Route.kt migrado en KMP-2 |
| `coil-compose` | ⚠️ Coil 3.x KMP | Actualizar a Coil 3.x con KMP support |
| Google Fonts | ❌ Android-only | Bundlear fuentes o usar sistema |
| Lottie | ❌ Android-only | Reemplazar con animaciones Compose nativas |
| Firebase Crashlytics UI | ❌ Android-only | Mantener solo en androidMain |

### 5.3 Composables candidatos a compartir en commonMain

**Alta prioridad (portable sin cambios):**
- Componentes de lista: `PlayerListItem`, `TeamListItem`, `MatchListItem`
- Formularios: `CreateClubScreen`, `CreateTeamScreen`, componentes de wizard de jugador
- Pantallas de análisis/estadísticas: `AnalysisScreen` (si no usa charts Android-specific)
- Componentes de display: `PlayerCard`, `MatchCard`, `ScoreDisplay`
- Componentes de estado: `LoadingScreen`, `ErrorScreen`, `EmptyState`

**Media prioridad (requieren refactor menor):**
- `MatchScreen` — usa lógica de tiempo compleja; la lógica es portable, la UI también
- `TeamScreen` — posible uso de cámara/imagen para foto de equipo (platform-specific)
- `LoginScreen` — Google Sign-In button es Android-specific; `expect/actual` para el botón

**Baja prioridad / Android-only:**
- `MainActivity.kt` — siempre Android-only
- Composables que usan `LocalContext` directamente
- Cualquier `Intent` o `startActivity`

### 5.4 Qué bloquea la migración UI

1. **`compose-charts` (v0.2.0)**: No es KMP. Para la pantalla de análisis, evaluar:
   - `koalaplot` (KMP) como alternativa
   - Implementar gráficas básicas con Canvas de CMP

2. **`lottie-compose` (v6.6.10)**: Android-only. Alternativas KMP:
   - Animaciones Compose nativas con `rememberInfiniteTransition`
   - `Rive` (tiene runtime KMP experimental)

3. **`coil-compose` (v2.5.0)**: Actualizar a Coil 3.x que tiene soporte KMP.

4. **`navigation-compose` (v2.9.5)**: Compose Navigation tiene soporte CMP desde 2.8.x. La migración de `Route.kt` (KMP-2) es prerequisito.

5. **Google Fonts**: No disponible en iOS. Opciones: fuentes del sistema, o bundlear en ambas plataformas.

### 5.5 Estrategia recomendada para Fase 2

**Enfoque incremental por pantallas:**

1. Crear módulo `:shared-ui` con `kotlin("multiplatform")` + Compose Multiplatform
2. Migrar componentes atómicos primero (botones, cards, typography)
3. Migrar pantallas de menor complejidad (Settings, ClubSelection, CreateClub)
4. Migrar pantallas de mayor valor (MatchScreen, TeamScreen)
5. `:app` (Android) importa `:shared-ui` + sus especificidades Android
6. `iosApp` importa `:shared-ui` + sus especificidades iOS

**No crear `iosApp` hasta que Fase 1 esté completa.** La inversión en UI multiplataforma solo tiene sentido cuando la infraestructura (datos, lógica de negocio, ViewModels) ya funciona en ambas plataformas.

### 5.6 Análisis coste/beneficio Fase 2

| Factor | Android actual | Con CMP |
|--------|---------------|---------|
| Tiempo estimado migración UI | — | 3-4 sprints |
| % código UI reutilizable | 100% Android | ~70-80% en commonMain |
| Riesgo de regresión Android | Bajo | Medio (requiere testing exhaustivo) |
| Valor: soporte iOS sin reescribir | N/A | Alto |
| Dependencias a reemplazar | — | compose-charts, lottie, coil upgrade |

**Recomendación**: Fase 2 es viable y rentable si el objetivo es lanzar en iOS. El riesgo de regresión en Android es gestionable con la cobertura de tests actual (~85% en lógica de negocio).

---

## Resumen de dependencias entre issues

```
KMP-2 (Route.kt)
    ↓
KMP-1 (domain KMP)
    ↓
KMP-3 (usecase KMP)
    ↓
KMP-4 (data:core KMP)
    ↓ (paralelo)
KMP-5 (Moshi → kotlinx.serialization)   KMP-7 (Firebase boundary)   KMP-8 (viewmodel KMP)
    ↓                                        ↓                            ↓
KMP-6 (data:local KMP)                       └──────────────────────────────┘
    ↓                                                    ↓
    └──────────────────────── KMP-9 (di restructuring) ──┘
                                    ↓
                              KMP-10 (:service elimination)
```

**Issues independientes** (pueden empezar en paralelo desde el día 1):
- **KMP-2**: No requiere ningún otro issue
- **KMP-5**: No requiere KMP completado, solo edición en `:data:local`

---

*Documento generado por análisis arquitectónico del proyecto en rama `kmp-migration-analysis`.*
*Versiones analizadas: Kotlin 2.1.0 · AGP 8.6.1 · Compose Multiplatform 1.7.3 · Koin 4.0.0 · Ktor 3.0.1 · Room 2.8.3 · Firebase BOM 33.6.0*
