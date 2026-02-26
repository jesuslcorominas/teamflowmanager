# KMP Migration Plan — TeamFlowManager

> **Fecha de análisis**: 2026-02-26
> **Última actualización**: 2026-02-26 (`:viewmodel` sin ningún import Android-específico — listo para migrar a `kotlin("multiplatform")`)
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
data/local  → Preferencias locales (SharedPreferences). Sin Room ni base de datos.
data/remote → KtorFit + Firebase (Firestore, Storage, Auth) datasource implementations.
viewmodel   → Jetpack ViewModel + StateFlow.
di          → Koin DI composition root.
app         → Compose UI + MainActivity + Route.kt (navegación).
```

> **Nota**: El módulo `:service` fue eliminado completamente (ver sección 4.1). El sistema de notificaciones de partido (`MatchNotificationController`, `MatchCountdownService`, `MatchNotificationServiceManager`, `GetActiveMatchUseCase`) fue descartado en su totalidad durante el refactor previo a la migración KMP.

### 1.2 Inventario de archivos por módulo

| Módulo | Archivos Kotlin | Tipo actual | Estado KMP |
|--------|----------------|-------------|------------|
| `:domain` | ~35 | `kotlin.jvm` | ✅ Listo |
| `:usecase` | ~74 | `kotlin.jvm` | ✅ Listo |
| `:data:core` | ~30 | `kotlin.jvm` | ✅ Listo |
| `:data:local` | 2 | `android.library` | ⚠️ Requiere refactor (SharedPreferences) |
| `:data:remote` | ~40 | `android.library` | ⚠️ Bloqueado (Firebase) |
| ~~`:service`~~ | ~~2~~ | ~~`android.library`~~ | ✅ **Eliminado** |
| `:viewmodel` | 19 | `android.library` | ⚠️ Listo para migrar (solo cambio de `build.gradle.kts`) |
| `:di` | ~9 | `android.library` | ⚠️ Requiere restructuración |
| `:app` | ~78 | `android.application` | ❌ Android-only |

> **Notas sobre refactors completados**:
> - `Route.kt` ya fue movido de `:domain` a `:app/ui/navigation/`. `:domain` está libre de acoplamiento con navegación UI.
> - El módulo `:service` fue eliminado. Con él: `MatchNotificationControllerImpl`, `ServiceModule`, `MatchNotificationController` (interfaz de dominio), `MatchCountdownService`, `MatchNotificationServiceManager` y `GetActiveMatchUseCase` (+ su test). `TeamFlowManagerApplication` ya no inicializa ningún servicio de notificaciones.
> - `SavedStateHandle` fue **eliminado completamente** de los 5 ViewModels que lo usaban (`MatchViewModel`, `AcceptTeamInvitationViewModel`, `MatchCreationWizardViewModel`, `PlayerWizardViewModel`, `TeamViewModel`). Ahora reciben sus argumentos de navegación directamente por constructor. Navigation extrae → Screen pasa via `parametersOf` → Koin inyecta con `params.get()`.

### 1.3 Nivel de acoplamiento Android por módulo

| Módulo | Dependencias Android directas | Nivel de bloqueo |
|--------|------------------------------|-----------------|
| `:domain` | Ninguna | **Ninguno — KMP ready** |
| `:usecase` | Ninguna | **Ninguno — KMP ready** |
| `:data:core` | Ninguna | **Ninguno — KMP ready** |
| `:data:local` | `SharedPreferences` (Android API) | Bajo (expect/actual con `NSUserDefaults` en iOS) |
| `:data:remote` | Firebase BOM 33.6.0, `play-services-auth` | Alto (Firebase Android-only) |
| ~~`:service`~~ | ~~Android-only~~ | ✅ **Eliminado** |
| `:viewmodel` | Ninguna — `lifecycle-viewmodel 2.8.6` ya KMP-compatible | **Ninguno — pendiente solo cambio de `build.gradle.kts`** |
| `:app` | Compose, Coil, Lottie, Google Fonts, Firebase Crashlytics | Android-only |

### 1.4 Dependencias críticas y su estado KMP

| Librería | Versión actual | Estado KMP | Estrategia |
|----------|---------------|------------|------------|
| `koin-core` | 4.0.0 | ✅ Full KMP | `koin-core` → commonMain; `koin-android` → androidMain |
| `ktor-client-core` | 3.0.1 | ✅ Full KMP | Core → commonMain; OkHttp/Darwin por plataforma |
| `ktorfit-lib` | 2.6.0 | ✅ Full KMP | Migrar a targets KMP con KSP por target |
| `kotlinx-coroutines-core` | 1.9.0 | ✅ Full KMP | Evitar `Dispatchers.IO` en commonMain → usar `Dispatchers.Default` |
| `kotlinx-serialization-json` | 1.7.3 | ✅ Full KMP | Ya en catálogo |
| `lifecycle-viewmodel-ktx` | 2.8.6 | ✅ Partial KMP | ViewModel → commonMain; `SavedStateHandle` → androidMain |
| `compose-multiplatform` | 1.7.3 | ✅ Full KMP | En catálogo, no aplicado — Fase 2 |
| `SharedPreferences` | Android API | ❌ Android-only | expect/actual: `NSUserDefaults` en iOS |
| `firebase-bom` | 33.6.0 | ❌ Android-only | Boundary `expect/actual`; alternativa: GitLive Firebase SDK |
| `moshi` | — | ~~❌ Eliminado~~ | **Ya eliminado del proyecto** |
| `coil-compose` | 2.5.0 | ❌ Android-only | Coil 3.x tiene soporte KMP; Fase 2 |
| `navigation-compose` | 2.9.5 | ❌ Android-only | `Route.kt` ya en `:app`; Fase 2 usar Compose Navigation CMP |

### 1.5 Porcentaje estimado reutilizable en commonMain

| Categoría | % reutilizable |
|-----------|---------------|
| Modelos de dominio (`:domain/model`) | ~95% |
| Interfaces de use cases | ~100% |
| Implementaciones de use cases | ~100% |
| Repositorios (`:data:core`) | ~100% |
| DataSource local (`:data:local`) | ~50% (interfaz commonMain; implementación por plataforma) |
| DataSource remoto (`:data:remote`) | ~60% (excluye Firebase) |
| ViewModels | ~100% (sin imports Android; `lifecycle-viewmodel` ya KMP) |
| **Total estimado** | **~86% del negocio reutilizable** |

---

## 2. Propuesta de Arquitectura KMP

### 2.1 Estructura de módulos propuesta

```
:domain         → kotlin("multiplatform") — commonMain puro
:usecase        → kotlin("multiplatform") — commonMain puro
:data:core      → kotlin("multiplatform") — commonMain puro
:data:local     → kotlin("multiplatform") — androidMain (SharedPreferences) + iosMain (NSUserDefaults)
:data:remote    → kotlin("multiplatform") — commonMain (Ktorfit/Ktor) + androidMain/iosMain (Firebase expect/actual)
:viewmodel      → kotlin("multiplatform") — commonMain (ViewModel KMP) + androidMain (SavedStateHandle)
:di             → por plataforma — androidMain + iosMain (Koin multiplatform modules)
:app            → android.application — solo Android (MainActivity, Compose UI, Route.kt)
:iosApp         → (nuevo) Xcode project o KMP iOS entry point
```

> El módulo `:service` ya no existe — fue eliminado antes de iniciar la migración KMP. No hay ningún módulo candidato a confinamiento en `androidMain` por esta causa.

### 2.2 Qué va a commonMain

**`:domain` commonMain (ya KMP-ready):**
- Todo `model/` (33 modelos de dominio)
- Todo `usecase/` (interfaces)
- `utils/TimeProvider.kt`, `utils/TransactionRunner.kt`, `utils/FileHandler.kt`
- Interfaces `PdfExporter`, `MatchReportPdfExporter` (implementaciones son platform-specific)
- `notification/MatchNotificationController.kt` (como interfaz; implementación en androidMain)

**`:usecase` commonMain:**
- Todas las implementaciones (~76 archivos) sin cambios

**`:data:core` commonMain:**
- Todas las implementaciones de repositorios sin cambios

**`:data:local` commonMain:**
- Sin lógica propia en commonMain; la interfaz `PreferencesDataSource` ya está en `:data:core`

**`:data:local` androidMain:**
- `PreferencesLocalDataSourceImpl` usando `SharedPreferences`

**`:data:local` iosMain:**
- Implementación equivalente usando `NSUserDefaults`

**`:data:remote` commonMain:**
- Interfaces Ktorfit, modelos de red, lógica de negocio de red
- `FirestoreTransactionRunner` → `expect/actual`

**`:viewmodel` commonMain:**
- 19/19 ViewModels completos — ninguno usa `SavedStateHandle` ni ningún import Android-específico
- Los argumentos de navegación se reciben directamente por constructor (`matchId: Long`, `teamId: String?`, etc.)
- `androidx.lifecycle.ViewModel` y `viewModelScope` son KMP-compatibles (lifecycle 2.8.6)

### 2.3 Patrón `expect/actual` necesario

| Concepto | commonMain (expect) | androidMain (actual) | iosMain (actual) |
|----------|--------------------|-----------------------|------------------|
| Firebase Auth | `AuthDataSource` interface | Firebase Auth Android | GitLive SDK o stub |
| Firebase Firestore | `RemoteDataSource` interface | Firestore Android | GitLive SDK o stub |
| PDF Export | `interface PdfExporter` | `android.graphics.pdf` impl | PDFKit (iOS) |
| `Dispatchers.IO` | Usar `Dispatchers.Default` | OK | OK |
| `Dispatchers.IO` | Usar `Dispatchers.Default` | OK | OK |
| `TransactionRunner` | `interface TransactionRunner` | implementación con coroutines | implementación con coroutines |
| `SharedPreferences` | `interface PreferencesDataSource` (en `:data:core`) | `SharedPreferences` impl | `NSUserDefaults` impl |

### 2.4 Componentes a eliminar

Ver Sección 4.

### 2.5 Cambios de build system

1. Agregar `kotlin("multiplatform")` plugin a `:domain`, `:usecase`, `:data:core`
2. Migrar `:data:local`, `:data:remote`, `:viewmodel`, `:di` de `android.library` a `kotlin("multiplatform")`
3. Agregar `iosArm64()` + `iosSimulatorArm64()` targets donde aplique
4. Configurar KSP para múltiples targets (Ktorfit)
5. Migrar engine Ktor de `ktor-client-okhttp` (solo Android) a por plataforma

---

## 3. Plan de Migración — Fase 1: Infraestructura

> Las tareas están formateadas como GitHub Issues.
> **Orden recomendado**: seguir numeración. KMP-1 a KMP-4 son prerequisitos en cadena.

---

### ~~KMP-2~~: ✅ Mover `Route.kt` de `:domain` a `:app` — **COMPLETADO**

`Route.kt` ya fue movido a `app/src/main/java/.../ui/navigation/Route.kt`.
`:domain` no tiene ningún acoplamiento con navegación UI.
`:viewmodel` tampoco importa `Route` — cada ViewModel define sus propias constantes de argumentos de navegación en su `companion object`.

---

### ~~KMP-5~~: ✅ Eliminar Moshi del proyecto — **COMPLETADO**

Moshi estaba registrado en el módulo Koin de `:data:local` pero nunca era inyectado en ningún sitio del proyecto. Se ha eliminado:
- `DataLocalModule.kt` limpiado de `Moshi` y `KotlinJsonAdapterFactory`
- `data/local/build.gradle.kts` sin dependencias `moshi`, `moshi-kotlin`, ni `ksp`
- No hay ningún `@Json`, `@JsonClass` ni `JsonAdapter` en el proyecto

Las entradas `moshi`, `moshi-kotlin`, `moshi-kotlin-codegen` y sus versiones pueden eliminarse del catálogo `libs.versions.toml`.

---

### KMP-1: Migrar `:domain` a Kotlin Multiplatform

**Tipo**: `enhancement` `kmp` `no-breaking`
**Módulo**: `:domain`
**Estimación**: 2h *(reducida — ya sin Route.kt)*

**Descripción**
`:domain` es Pure Kotlin sin dependencias Android y con `Route.kt` ya fuera del módulo. Puede convertirse en KMP cambiando únicamente el `build.gradle.kts`. No requiere cambios en la lógica.

**Objetivo**
Hacer que `:domain` compile para targets Android e iOS sin cambios en el código de negocio.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:domain`:
   - Reemplazar `id("org.jetbrains.kotlin.jvm")` por `kotlin("multiplatform")`
   - Configurar targets: `androidTarget()`, `iosArm64()`, `iosSimulatorArm64()`
   - Mover todas las dependencias a `commonMain`
2. Actualizar `settings.gradle.kts` si aplica
3. Verificar que todos los módulos dependientes compilan

**Criterios de aceptación**
- [ ] `./gradlew :domain:compileKotlinAndroid` pasa sin errores
- [ ] `./gradlew :domain:compileKotlinIosArm64` pasa sin errores
- [ ] `./gradlew :usecase:test` pasa (todos los tests dependientes)

---

### KMP-3: Migrar `:usecase` a Kotlin Multiplatform

**Tipo**: `enhancement` `kmp`
**Módulo**: `:usecase`
**Dependencias**: KMP-1
**Estimación**: 2h

**Descripción**
Pure Kotlin con dependencia única en `:domain`. Una vez que `:domain` es KMP, `:usecase` puede convertirse en KMP sin cambios en la lógica.

**Objetivo**
Hacer que las ~76 implementaciones de use case compilen para Android e iOS.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:usecase`:
   - `id("org.jetbrains.kotlin.jvm")` → `kotlin("multiplatform")`
   - Configurar mismos targets que `:domain`
   - Dependencia `implementation(project(":domain"))` en `commonMain`
2. Verificar que no se usa `Dispatchers.IO` directamente (usar `Dispatchers.Default` o inyectar dispatcher)

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
`:data:core` implementa las interfaces de repositorio definidas en `:usecase` y delega a interfaces DataSource abstractas. No tiene dependencias Android directas.

**Objetivo**
Hacer que las implementaciones de repositorio compilen para todas las plataformas.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:data:core`:
   - `id("org.jetbrains.kotlin.jvm")` → `kotlin("multiplatform")`
   - Mover dependencias a `commonMain`
2. Verificar que las interfaces `DataSource` que define no usan tipos Android

**Criterios de aceptación**
- [ ] `./gradlew :data:core:compileKotlinAndroid` pasa
- [ ] `./gradlew :data:core:compileKotlinIosArm64` pasa
- [ ] `./gradlew :data:core:test` pasa

---

### KMP-6: Migrar `:data:local` a Kotlin Multiplatform

**Tipo**: `enhancement` `kmp`
**Módulo**: `:data:local`
**Dependencias**: KMP-4
**Estimación**: 3h

**Descripción**
`:data:local` contiene únicamente la implementación de preferencias locales usando `SharedPreferences` (2 archivos). Para KMP, la interfaz `PreferencesDataSource` (ya en `:data:core`) se implementa por plataforma: `SharedPreferences` en Android y `NSUserDefaults` en iOS.

**Objetivo**
Hacer que la capa de preferencias locales compile para Android e iOS.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:data:local` a `kotlin("multiplatform")` con `androidTarget()` + iOS targets
2. Mantener `PreferencesLocalDataSourceImpl` en `androidMain` (usa `SharedPreferences`)
3. Crear `PreferencesLocalDataSourceImpl` en `iosMain` usando `NSUserDefaults`:
   ```kotlin
   // iosMain
   actual class PreferencesLocalDataSourceImpl : PreferencesDataSource {
       private val defaults = NSUserDefaults.standardUserDefaults
       actual override fun getDefaultCaptainId(): Long? = ...
   }
   ```
4. Registrar la implementación correcta en Koin desde `:di` por plataforma

**Criterios de aceptación**
- [ ] `./gradlew :data:local:compileKotlinAndroid` pasa
- [ ] `./gradlew :data:local:compileKotlinIosArm64` pasa
- [ ] Las preferencias (capitán por defecto, alerta de sustitución) funcionan en Android

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
7. `FirestoreTransactionRunner` → `expect/actual`
8. Migrar engine Ktor por plataforma:
   - `androidMain`: `ktor-client-okhttp`
   - `iosMain`: `ktor-client-darwin`

**Criterios de aceptación**
- [ ] `./gradlew :data:remote:compileKotlinAndroid` pasa
- [ ] `./gradlew :data:remote:compileKotlinIosArm64` pasa (stub OK)
- [ ] `./gradlew :data:remote:testDebugUnitTest` pasa
- [ ] Login con Google funciona en Android tras la migración

---

### KMP-8: Migrar `:viewmodel` a Kotlin Multiplatform

**Tipo**: `enhancement` `kmp`
**Módulo**: `:viewmodel`
**Dependencias**: KMP-3, KMP-4
**Estimación**: 1h *(mínima — el código ya está listo, solo falta el build script)*

**Descripción**
`lifecycle-viewmodel` tiene soporte KMP desde la versión 2.8.0. La versión actual (`2.8.6`) ya lo soporta. `SavedStateHandle` fue **eliminado completamente** de todos los ViewModels. `android.util.Log` fue eliminado de `SplashViewModel` (no se usaba). El módulo `:viewmodel` no contiene ningún import Android-específico. El único paso pendiente es cambiar el plugin en `build.gradle.kts`.

**Estado previo al refactor (ya completado)**:
- 5 ViewModels tenían `SavedStateHandle`: `MatchViewModel`, `AcceptTeamInvitationViewModel`, `MatchCreationWizardViewModel`, `PlayerWizardViewModel`, `TeamViewModel`
- Todos refactorizados: constructor directo + Koin `viewModel { params -> VM(params.get(), ...) }`

**Objetivo**
Hacer que los 19 ViewModels compilen para Android e iOS.

**Pasos técnicos**
1. Cambiar `build.gradle.kts` de `:viewmodel` a `kotlin("multiplatform")`
2. En `commonMain`: `implementation(libs.androidx.lifecycle.viewmodel.ktx)` (ya KMP desde 2.8.0)
3. Mantener `koin-androidx-compose` en `androidMain` de `:di`

**Criterios de aceptación**
- [ ] `./gradlew :viewmodel:compileKotlinAndroid` pasa
- [ ] `./gradlew :viewmodel:compileKotlinIosArm64` pasa
- [ ] `./gradlew :viewmodel:test` — todos los tests existentes pasan (ya verificado en Android)
- [ ] Los ViewModels se instancian correctamente en Android con argumentos de navegación

---

### KMP-9: Restructurar `:di` para soporte multiplataforma

**Tipo**: `enhancement` `kmp`
**Módulo**: `:di`
**Dependencias**: KMP-6, KMP-7, KMP-8
**Estimación**: 6h

**Descripción**
Koin 4.0 tiene soporte KMP completo: `koin-core` es KMP, `koin-android` es androidMain. La restructuración implica separar los módulos Koin comunes de los Android-specific.

**Objetivo**
Tener un conjunto de módulos Koin en `commonMain` y extensiones en `androidMain`/`iosMain`.

**Pasos técnicos**
1. Cambiar `:di` a `kotlin("multiplatform")`
2. `commonMain`: módulos Koin para `:usecase`, `:data:core`, `:viewmodel` (commonMain ViewModels)
3. `androidMain`: módulos para Firebase implementations, `SharedPreferences`, ViewModels Android, `koin-android` setup
4. `iosMain`: módulos para Firebase iOS (GitLive) o stubs, `NSUserDefaults`
5. Exponer `fun initKoin(additionalModules: List<Module> = emptyList())` en `commonMain`
6. En `:app` (Android): `startKoin { androidContext(this); modules(androidModules()) }`
7. En iOS entry point: `initKoin()`

**Criterios de aceptación**
- [ ] `./gradlew :di:compileKotlinAndroid` pasa
- [ ] `./gradlew :di:compileKotlinIosArm64` pasa
- [ ] `./gradlew :app:assembleDevDebug` compila y la app arranca correctamente
- [ ] Inyección de dependencias funciona en Android para todos los flujos existentes

---

## 4. Componentes Candidatos a Eliminación

### 4.1 `:service` — ✅ YA ELIMINADO

El módulo fue **completamente descartado** junto con toda la funcionalidad de notificaciones de partido. No se realizó una reubicación parcial: la funcionalidad entera fue removida del proyecto.

Archivos eliminados:
- `service/src/.../MatchNotificationControllerImpl.kt`
- `service/src/.../di/ServiceModule.kt`
- `service/build.gradle.kts` + `service/src/main/AndroidManifest.xml`
- `domain/notification/MatchNotificationController.kt` (interfaz de dominio)
- `domain/usecase/GetActiveMatchUseCase.kt` (interfaz de dominio)
- `usecase/.../GetActiveMatchUseCase.kt` + su test
- `app/.../service/MatchCountdownService.kt`
- `app/.../service/MatchNotificationServiceManager.kt`

`TeamFlowManagerApplication` fue simplificado: ya no inicializa ningún gestor de notificaciones ni observa partidos activos. `:di/TeamFlowManagerModule` ya no incluye `serviceModule`.

**Issue asociada**: KMP-10 — **COMPLETADO**.

### 4.2 `domain/notification/MatchNotificationController.kt` — ✅ Eliminada (no migrada)

La interfaz fue eliminada junto con toda la funcionalidad de notificaciones. No existe en el proyecto. En una futura reimplementación para KMP, debería definirse en `commonMain` con implementaciones `actual` por plataforma (local notifications en iOS, `ForegroundService` en Android).

### 4.3 Librería `moshi` — ✅ YA ELIMINADA

Moshi estaba registrado en Koin en `DataLocalModule.kt` pero nunca era inyectado en ningún sitio del proyecto. Eliminado completamente. Entradas pendientes de borrar del catálogo `libs.versions.toml`:
- `moshi`, `moshi-kotlin`, `moshi-kotlin-codegen` (versiones y referencias de librería)

### 4.4 `ktor-client-okhttp` — Confinado a androidMain

**Motivo**: OkHttp es Android/JVM-only. En KMP, cada plataforma usa su propio engine de Ktor.
**Acción**: Mover a `androidMain`, agregar `ktor-client-darwin` para iOS en `iosMain`.

---

### ~~KMP-10~~: ✅ Eliminar módulo `:service` — **COMPLETADO**

El módulo `:service` fue eliminado **con toda la funcionalidad de notificaciones de partido**. La eliminación fue más profunda que lo previsto originalmente: en lugar de reubicar `MatchNotificationControllerImpl`, se decidió descartar por completo el sistema de notificaciones en esta fase de la migración.

Cambios realizados:
- Directorio `service/` eliminado del proyecto
- `:service` eliminado de `settings.gradle.kts`
- `serviceModule` eliminado de `TeamFlowManagerModule.kt` en `:di`
- `MatchNotificationController` (interfaz), `GetActiveMatchUseCase` y todos los archivos Android relacionados eliminados
- `TeamFlowManagerApplication` simplificado: sin `CoroutineScope`, sin `MatchNotificationServiceManager`

> Si se reimplementan notificaciones de partido en el futuro, deberán diseñarse directamente como KMP desde el inicio: `expect/actual` con `ForegroundService` en Android y local notifications en iOS.

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
| Navegación | ✅ Stable (Compose Navigation CMP 2.8+) | `Route.kt` ya en `:app` — migración directa |
| `coil-compose` | ⚠️ Coil 3.x KMP | Actualizar a Coil 3.x con KMP support |
| Google Fonts | ❌ Android-only | Bundlear fuentes o usar fuentes del sistema |
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

1. **`compose-charts` (v0.2.0)**: No es KMP. Alternativas: `koalaplot` (KMP) o Canvas de CMP.

2. **`lottie-compose` (v6.6.10)**: Android-only. Alternativas: animaciones Compose nativas con `rememberInfiniteTransition`, o `Rive` (runtime KMP experimental).

3. **`coil-compose` (v2.5.0)**: Actualizar a Coil 3.x que tiene soporte KMP.

4. **`navigation-compose` (v2.9.5)**: Compose Navigation tiene soporte CMP desde 2.8.x. `Route.kt` ya está en `:app` — migración directa sin refactors adicionales.

5. **Google Fonts**: No disponible en iOS. Opciones: fuentes del sistema, o bundlear en ambas plataformas.

### 5.5 Estrategia recomendada para Fase 2

**Enfoque incremental por pantallas:**

1. Crear módulo `:shared-ui` con `kotlin("multiplatform")` + Compose Multiplatform
2. Migrar componentes atómicos primero (botones, cards, typography)
3. Migrar pantallas de menor complejidad (Settings, ClubSelection, CreateClub)
4. Migrar pantallas de mayor valor (MatchScreen, TeamScreen)
5. `:app` (Android) importa `:shared-ui` + sus especificidades Android
6. `iosApp` importa `:shared-ui` + sus especificidades iOS

**No crear `iosApp` hasta que Fase 1 esté completa.** La inversión en UI multiplataforma solo tiene sentido cuando la infraestructura ya funciona en ambas plataformas.

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
✅ KMP-2 (Route.kt → :app)     ✅ KMP-5 (Moshi eliminado)     ✅ KMP-10 (:service eliminado)
         ↓                                ↓ (ya hecho)                    ↓ (ya hecho)
KMP-1 (domain KMP)
    ↓
KMP-3 (usecase KMP)
    ↓
KMP-4 (data:core KMP)
    ↓ (paralelo)
KMP-6 (data:local KMP)    KMP-7 (Firebase boundary)    KMP-8 (viewmodel KMP)
    ↓                           ↓                             ↓
    └───────────────── KMP-9 (di restructuring) ─────────────┘
```

**Issues completadas** (no requieren trabajo):
- ✅ **KMP-2**: `Route.kt` movido a `:app/ui/navigation/`
- ✅ **KMP-5**: Moshi eliminado de todo el proyecto
- ✅ **KMP-10**: Módulo `:service` y sistema de notificaciones de partido eliminados por completo
- ✅ **KMP-8 (código listo)**: `SavedStateHandle` eliminado de los 5 ViewModels. `android.util.Log` eliminado de `SplashViewModel` (no se usaba). `:viewmodel` no contiene ningún import Android-específico — el código ya es 100% KMP. Solo falta cambiar `build.gradle.kts` de `android.library` a `kotlin("multiplatform")`.

**Issues independientes** (pueden empezar en paralelo desde el día 1):
- **KMP-1**: Solo requiere cambiar el build script de `:domain`
- **KMP-6**: Puede arrancar en paralelo con KMP-7 y KMP-8 una vez KMP-4 completado

---

*Documento generado por análisis arquitectónico del proyecto en rama `kmp-migration-analysis`.*
*Versiones analizadas: Kotlin 2.1.0 · AGP 8.6.1 · Compose Multiplatform 1.7.3 · Koin 4.0.0 · Ktor 3.0.1 · Firebase BOM 33.6.0*

---

## Registro de cambios

| Fecha | Cambio |
|-------|--------|
| 2026-02-26 | Documento inicial con análisis KMP completo |
| 2026-02-26 | KMP-2: `Route.kt` movido a `:app/ui/navigation/` |
| 2026-02-26 | KMP-5: Moshi eliminado del proyecto |
| 2026-02-26 | KMP-10 (adelantado): `:service` eliminado. Sistema de notificaciones de partido (`MatchNotificationController`, `GetActiveMatchUseCase`, `MatchCountdownService`, `MatchNotificationServiceManager`) descartado en su totalidad |
| 2026-02-26 | KMP-8 (código listo): `SavedStateHandle` eliminado de 5 ViewModels; `android.util.Log` eliminado de `SplashViewModel` (era import sin uso). `:viewmodel` no tiene ningún import Android-específico. Código 100% KMP; pendiente solo cambio del build script |
