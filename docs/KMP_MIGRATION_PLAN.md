# KMP Migration Plan — TeamFlowManager

> **Fecha de análisis inicial**: 2026-02-26
> **Última actualización**: 2026-02-26 (Fase 1 completada; Fase 2 y 3 planificadas)
> **Rama base de migración**: `migration/kmp-migration`
> **Arquitecto**: Senior KMP/Android Architect (Claude Code)

---

## Estado global de la migración

| Fase | Descripción | Estado |
|------|-------------|--------|
| **Fase 1** | Infraestructura KMP — todos los módulos convertidos a multiplatform | ✅ **COMPLETADA** |
| **Fase 2** | iOS MVP — login Firebase + listado de partidos (Text simple) | 🔲 Planificada |
| **Fase 3** | iOS con UI completa igual a la de Android (Compose Multiplatform) | 🔲 Planificada |

---

## 1. Estado actual del proyecto — Fase 1 COMPLETADA

### 1.1 Inventario de módulos

| Módulo | Plugin | commonMain | androidMain | iosMain | Tests | Estado |
|--------|--------|:----------:|:-----------:|:-------:|:-----:|--------|
| `:domain` | `kotlin.multiplatform` | ✓ | ✓ | ✓ | ✓ | ✅ Completo |
| `:usecase` | `kotlin.multiplatform` | ✓ | — | — | ✓ | ✅ Completo |
| `:data:core` | `kotlin.multiplatform` | ✓ | — | — | ✓ | ✅ Completo |
| `:data:local` | `kotlin.multiplatform` | ✓ | ✓ | ✓ | ✓ | ✅ Completo |
| `:data:remote` | `kotlin.multiplatform` | ✓ | ✓ | ✓ stub | ✓ | ✅ Completo (iOS = stub) |
| `:viewmodel` | `kotlin.multiplatform` | — | ✓ | — | ✓ | ✅ Completo (Android-only) |
| `:di` | `kotlin.multiplatform` | — | ✓ | — | — | ✅ Completo (Android-only) |
| `:app` | `android.application` | — | ✓ | — | ✓ | ✅ Android-only (por diseño) |
| ~~`:service`~~ | — | — | — | — | — | ✅ Eliminado |

### 1.2 Boundaries `expect/actual` establecidas

| Archivo | commonMain (expect) | androidMain (actual) | iosMain (actual) |
|---------|--------------------|-----------------------|------------------|
| `domain/.../Platform.kt` | `expect fun currentTimeMillis(): Long` | `System.currentTimeMillis()` | POSIX `gettimeofday` |
| `data/local/.../di/DataLocalModule.kt` | `expect val dataLocalModule: Module` | SharedPreferences impl | NSUserDefaults impl |
| `data/remote/.../di/DataRemoteModule.kt` | `expect val dataRemoteModule: Module` | Firebase + Ktor full | Stub vacío (Fase 2) |

### 1.3 Issues completadas

| Issue | Título | PR |
|-------|--------|----|
| KMP-2 | Mover `Route.kt` a `:app` | Pre-migración |
| KMP-5 | Eliminar Moshi del proyecto | Pre-migración |
| KMP-10 | Eliminar módulo `:service` | Pre-migración |
| KMP-8 (prep) | Eliminar `SavedStateHandle` de 5 ViewModels | Pre-migración |
| KMP-1 | Migrar `:domain` a KMP | #244 |
| KMP-3 | Migrar `:usecase` a KMP | #245 |
| KMP-4 | Migrar `:data:core` a KMP | #246 |
| KMP-6 | Migrar `:data:local` a KMP (+ iOS NSUserDefaults) | #247 |
| KMP-7 | Boundary `expect/actual` Firebase en `:data:remote` | #248 |
| KMP-8 | Migrar `:viewmodel` a KMP | #249 |
| KMP-9 | Migrar `:di` a KMP | #250 |

### 1.4 Deuda técnica identificada en Fase 1

- **`:data:remote` iosMain**: Stub vacío. Firebase y Ktor no están implementados en iOS. Bloqueante para Fase 2.
- **ViewModels en `androidMain`**: Todos los ViewModels están en `androidMain` porque `lifecycle-viewmodel-ktx` no se añadió a `commonMain`. `lifecycle-viewmodel` soporta KMP desde la versión 2.8.0 (versión actual: 2.8.6). Bloqueante para Fase 2.
- **`:di` sin `iosMain`**: `TeamFlowManagerModule` solo existe en `androidMain`. iOS necesita su propio módulo Koin y punto de entrada. Bloqueante para Fase 2.
- **Ingen `iosApp`**: No existe proyecto Xcode ni módulo iOS. Bloqueante para Fase 2.
- **Google Sign-In en iOS**: `play-services-auth` es Android-only. iOS requiere Google Sign-In iOS SDK (nativo Swift) + adaptador KMP. Requiere diseño específico.
- **`ktor-client-darwin`**: No está en `libs.versions.toml`. Necesario para Ktor en iOS.
- **GitLive Firebase SDK**: No está en `libs.versions.toml`. Necesario para Firebase en iOS (`dev.gitlive:firebase-auth`, `dev.gitlive:firebase-firestore`).
- **`compose-charts` (v0.2.0)**: No es KMP. Bloqueante para Fase 3 (`AnalysisScreen`). Alternativa: `koalaplot` o Canvas nativo CMP.
- **`coil-compose` (v2.5.0)**: Solo Android. Para Fase 3 actualizar a Coil 3.x (KMP).
- **`compose-google-fonts`**: Android-only. Para Fase 3 bundlear fuentes o usar fuentes del sistema.

---

## 2. Fase 2 — iOS MVP: Login Firebase + Listado de partidos

### 2.1 Objetivo

Tener una app iOS funcional que:
1. Permita hacer **login con Firebase** (Google Sign-In via SDK nativo iOS)
2. Muestre el **listado de partidos del usuario** con rival y resultado como `Text` simple
3. No tiene UI refinada — es un MVP técnico para validar la pila KMP end-to-end en iOS

### 2.2 Arquitectura iOS en Fase 2

```
iosApp (Xcode / Compose Multiplatform iOS)
    ↓
:di (iosMain) — initKoin() con módulos iOS
    ↓
:viewmodel (commonMain) — LoginViewModel, SplashViewModel, MatchListViewModel
    ↓
:usecase (commonMain) — SignInWithGoogleUseCase, GetAllMatchesUseCase
    ↓
:data:core (commonMain) — AuthRepository, MatchRepository
    ↓
:data:remote (iosMain) — GitLive Firebase Auth + Firestore
:data:local (iosMain)  — NSUserDefaults (ya implementado)
```

### 2.3 Google Sign-In en iOS

Google Sign-In en iOS **no usa `play-services-auth`** (Android-only). La estrategia:

```
iOS Layer (Swift/SwiftUI):
    GoogleSignIn iOS SDK → obtiene idToken
    ↓
KMP Layer (iosMain):
    interface GoogleSignInProvider { suspend fun signIn(): String } // devuelve idToken
    ↓
actual class GoogleSignInProviderImpl — llama al SDK nativo via expect/actual o interop
    ↓
Firebase Auth (GitLive) — FirebaseAuth.signInWithCredential(GoogleAuthProvider(idToken))
```

Alternativa más sencilla para el MVP: **no usar Google Sign-In en iOS en Fase 2**. Usar **Firebase Auth con email/password** para el MVP técnico. El objetivo de Fase 2 es validar la pila de datos (Firestore → ViewModel → UI), no la UX de login. Google Sign-In iOS puede añadirse en Fase 3 cuando se construya la UI completa.

> **Decisión recomendada para Fase 2**: Login con email/password en iOS (Firebase Auth nativo GitLive). Google Sign-In iOS en Fase 3.

### 2.4 Plan de tareas Fase 2

---

#### KMP-11: Implementar Firebase iOS en `:data:remote`

**Módulo**: `:data:remote`
**Dependencias**: Fase 1 completa
**Complejidad**: Alta

**Objetivo**: Sustituir el stub `iosMain` por implementaciones reales usando GitLive Firebase SDK.

**Pasos técnicos**:
1. Añadir a `libs.versions.toml`:
   ```toml
   [versions]
   gitliveFirebase = "2.1.0"
   ktor-darwin = "3.0.1"  # misma versión que ktor

   [libraries]
   gitlive-firebase-auth = { module = "dev.gitlive:firebase-auth", version.ref = "gitliveFirebase" }
   gitlive-firebase-firestore = { module = "dev.gitlive:firebase-firestore", version.ref = "gitliveFirebase" }
   ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
   ```
2. En `data/remote/build.gradle.kts`, añadir a `iosMain`:
   ```kotlin
   val iosMain by creating {
       dependencies {
           implementation(libs.gitlive.firebase.auth)
           implementation(libs.gitlive.firebase.firestore)
           implementation(libs.ktor.client.darwin)
           implementation(libs.ktor.client.content.negotiation)
           implementation(libs.ktor.serialization.kotlinx.json)
       }
   }
   ```
3. Implementar en `iosMain` al menos:
   - `FirebaseAuthDataSourceImpl` usando `dev.gitlive.firebase.auth.FirebaseAuth`
   - `MatchFirestoreDataSourceImpl` usando `dev.gitlive.firebase.firestore.FirebaseFirestore`
4. Actualizar `data/remote/src/iosMain/.../di/DataRemoteModule.kt` con los módulos reales
5. Añadir `iosMain` como sourceSet en `data/remote/build.gradle.kts` (actualmente solo existe el archivo, falta el bloque kotlin)

**Criterios de aceptación**:
- [ ] `./gradlew :data:remote:compileKotlinIosArm64` pasa
- [ ] `./gradlew :data:remote:compileKotlinIosSimulatorArm64` pasa
- [ ] Firebase Auth funciona en iOS Simulator

---

#### KMP-12: Mover ViewModels clave a `commonMain`

**Módulo**: `:viewmodel`
**Dependencias**: KMP-11
**Complejidad**: Media

**Objetivo**: Mover los ViewModels necesarios para el MVP a `commonMain` para que sean accesibles desde iOS.

**ViewModels a mover en Fase 2** (mínimo para el MVP):
- `SplashViewModel` — verifica si hay sesión activa
- `LoginViewModel` — gestiona el login
- `MatchListViewModel` — listado de partidos

**Pasos técnicos**:
1. Actualizar `viewmodel/build.gradle.kts`: añadir `commonMain` con `lifecycle-viewmodel`:
   ```kotlin
   sourceSets {
       commonMain.dependencies {
           implementation(project(":domain"))
           implementation(libs.androidx.lifecycle.viewmodel.ktx) // soporta KMP desde 2.8.0
           implementation(libs.kotlinx.coroutines.core)
           implementation(libs.koin.core)
       }
       val androidMain by getting { /* resto de ViewModels + koin-android */ }
   }
   ```
2. Mover `SplashViewModel.kt`, `LoginViewModel.kt`, `MatchListViewModel.kt` de `androidMain/` a `commonMain/`
3. Mover `TimeTicker.kt` a `commonMain/` (usa solo `kotlinx.coroutines` — ya KMP)
4. Verificar que compila para `iosArm64` y `iosSimulatorArm64`

**Nota**: `koin.core:4.0.0` ya es KMP. `viewModelScope` es KMP-compatible desde `lifecycle-viewmodel 2.8.0`. No se necesitan cambios en el código de los ViewModels.

**Criterios de aceptación**:
- [ ] `./gradlew :viewmodel:compileKotlinIosArm64` pasa con los 3 ViewModels en commonMain
- [ ] `./gradlew :viewmodel:testDebugUnitTest` — todos los tests existentes pasan
- [ ] `./gradlew :app:assembleDevDebug` — la app Android sigue funcionando

---

#### KMP-13: iOS DI bootstrapping en `:di`

**Módulo**: `:di`
**Dependencias**: KMP-11, KMP-12
**Complejidad**: Media

**Objetivo**: Exponer un punto de entrada Koin para iOS y crear los módulos iOS-específicos.

**Pasos técnicos**:
1. Añadir `commonMain` a `di/build.gradle.kts`:
   ```kotlin
   commonMain.dependencies {
       implementation(libs.koin.core)
       add("commonMainImplementation", project(":data:local"))
       add("commonMainImplementation", project(":data:remote"))
       add("commonMainImplementation", project(":usecase"))
       add("commonMainImplementation", project(":data:core"))
   }
   ```
2. Crear `di/src/commonMain/.../di/KoinInit.kt`:
   ```kotlin
   // commonMain
   expect fun platformModules(): List<Module>

   fun initKoin(additionalModules: List<Module> = emptyList()) {
       startKoin {
           modules(
               dataLocalModule,    // expect/actual
               dataRemoteModule,   // expect/actual
               dataCoreModule,
               useCaseModule,
               *platformModules().toTypedArray(),
               *additionalModules.toTypedArray(),
           )
       }
   }
   ```
3. Crear `di/src/iosMain/.../di/KoinInit.kt`:
   ```kotlin
   // iosMain
   actual fun platformModules(): List<Module> = listOf(
       module {
           // viewModel equivalente en iOS (koin-core, no koin-android)
           factory { SplashViewModel(get(), get(), get(), get()) }
           factory { LoginViewModel(get(), get()) }
           factory { (teamId: String?) -> MatchListViewModel(get(), ...) }
       }
   )
   ```
4. Mantener `androidMain` con `TeamFlowManagerModule` usando `viewModelModule` de Koin Android

**Criterios de aceptación**:
- [ ] `./gradlew :di:compileKotlinIosArm64` pasa
- [ ] `./gradlew :app:assembleDevDebug` — app Android compila y arranca
- [ ] `initKoin()` exportable a Swift

---

#### KMP-14: Crear proyecto iOS (`iosApp`)

**Módulo**: Nuevo — `iosApp/`
**Dependencias**: KMP-13
**Complejidad**: Alta (setup inicial)

**Objetivo**: Xcode project con Compose Multiplatform iOS que muestre login + listado de partidos.

**Estructura**:
```
iosApp/
├── iosApp.xcodeproj
├── iosApp/
│   ├── iOSApp.swift          # Entry point Swift
│   └── ContentView.swift     # Embeds CMP
└── ...

# O bien usar módulo KMP con composeApp target:
shared/
└── src/iosMain/kotlin/
    └── MainViewController.kt  # exports UIViewController
```

**Opción recomendada**: Usar la plantilla KMP de JetBrains (`Kotlin Multiplatform Wizard`) para generar el scaffolding con Compose Multiplatform. Añadir un módulo `:composeApp` o `:iosApp` con:

```kotlin
// composeApp/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.application)  // o library si :app sigue siendo la app Android
}
kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    // ...
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(project(":viewmodel"))
            implementation(project(":di"))
        }
    }
}
```

**Pantallas del MVP iOS** (Phase 2 — UI mínima):
```kotlin
// commonMain — solo para iOS MVP
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    Column {
        Text("TeamFlow Manager")
        Button(onClick = { viewModel.signIn() }) { Text("Sign In") }
    }
}

@Composable
fun MatchListScreen(viewModel: MatchListViewModel) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn {
        items(state.matches) { match ->
            Text("vs ${match.rivalName} — ${match.homeScore}:${match.awayScore}")
        }
    }
}
```

**Pasos técnicos**:
1. Generar scaffolding con KMP Wizard (jetbrains.com/lp/compose-multiplatform)
2. Añadir proyecto Xcode a `iosApp/`
3. Configurar `GoogleService-Info.plist` para Firebase iOS
4. Llamar a `initKoin()` en `iOSApp.swift` al inicio de la app
5. Implementar las 3 pantallas mínimas: Splash → Login → MatchList

**Criterios de aceptación**:
- [ ] App arranca en iOS Simulator
- [ ] Login con Firebase funciona (email/password para MVP)
- [ ] MatchListScreen muestra lista de partidos con `rival — score`
- [ ] App Android no se ha roto

---

### 2.5 Dependencia entre tareas Fase 2

```
KMP-11 (Firebase iOS en data:remote)
    ↓
KMP-12 (ViewModels → commonMain)
    ↓
KMP-13 (DI iOS bootstrapping)
    ↓
KMP-14 (iosApp + pantallas MVP)
```

### 2.6 Librerías nuevas necesarias en Fase 2

| Librería | Versión | Uso | Añadir a |
|---------|---------|-----|----------|
| `dev.gitlive:firebase-auth` | ~2.1.0 | Firebase Auth iOS | `data:remote` iosMain |
| `dev.gitlive:firebase-firestore` | ~2.1.0 | Firestore iOS | `data:remote` iosMain |
| `io.ktor:ktor-client-darwin` | 3.0.1 | HTTP engine iOS | `data:remote` iosMain |
| `org.jetbrains.compose` (plugin) | 1.7.3 | CMP UI en iOS | `:composeApp` |
| Koin Compose (CMP) | 4.0.0 | `koinViewModel` en CMP | `:composeApp` commonMain |

> **Nota GitLive Firebase**: La API de GitLive es similar a Firebase Android pero no idéntica. Los DataSources iOS de `:data:remote` deberán usar `dev.gitlive.firebase.auth.FirebaseAuth` y `dev.gitlive.firebase.firestore.FirebaseFirestore` en lugar de los `com.google.firebase.*` de Android.

---

## 3. Fase 3 — iOS con UI completa igual a Android

### 3.1 Objetivo

Tener la app iOS con exactamente las mismas pantallas y funcionalidad que la app Android, implementadas con Compose Multiplatform compartido.

### 3.2 Estrategia: módulo `:shared-ui`

```
:app (Android)          iosApp (iOS)
      ↓                       ↓
      └─────── :shared-ui ────┘
              (CMP commonMain)
                    ↓
              :viewmodel (commonMain)
                    ↓
              :usecase, :data:*
```

Crear un nuevo módulo `:shared-ui` con todo el código Compose Multiplatform compartido. Tanto `:app` como `iosApp` lo importan y añaden solo lo que es platform-specific.

### 3.3 Plan de tareas Fase 3

---

#### KMP-15: Configurar módulo `:shared-ui` con Compose Multiplatform

**Pasos**:
1. Crear `shared-ui/build.gradle.kts` con `kotlin.multiplatform` + `compose.multiplatform`
2. Mover a `commonMain`: componentes atómicos (buttons, cards, typography system)
3. Actualizar `libs.versions.toml`: añadir dependencias CMP que faltan
4. Resolver bloqueantes:
   - `compose-charts`: reemplazar por `koalaplot-core` (KMP) o implementación Canvas CMP
   - `coil-compose`: actualizar a Coil 3.x (soporta KMP)
   - `compose-google-fonts`: bundlear fuentes o usar fuentes del sistema en iOS

---

#### KMP-16: Migrar pantallas por orden de complejidad

**Orden recomendado** (de menor a mayor complejidad):

| Prioridad | Pantalla | Complejidad | Bloqueantes |
|-----------|----------|-------------|-------------|
| 1 | `SplashScreen` | Baja | — |
| 2 | `LoginScreen` | Baja-Media | Google Sign-In iOS |
| 3 | `CreateClubScreen`, `JoinClubScreen` | Baja | — |
| 4 | `MatchListScreen` | Media | — |
| 5 | `TeamListScreen`, `PlayerScreen` | Media | Coil (imágenes) |
| 6 | `MatchCreationWizardScreen` | Media | — |
| 7 | `MatchScreen` | Alta | TimeTicker, Canvas timer |
| 8 | `AnalysisScreen` | Alta | compose-charts → alternativa KMP |

**Para cada pantalla**:
1. Mover el Composable de `:app` a `:shared-ui/commonMain`
2. Eliminar imports Android-specific (`LocalContext`, `Intent`, etc.)
3. Usar `expect/actual` para lo que sea genuinamente platform-specific
4. Verificar en Android Simulator + iOS Simulator

---

#### KMP-17: Google Sign-In nativo en iOS

**Pasos**:
1. Añadir `GoogleSignIn` iOS SDK via Swift Package Manager en `iosApp`
2. Crear `expect interface GoogleSignInHandler` en `:domain` o `:data:remote`
3. `androidMain`: implementación existente con `play-services-auth`
4. `iosMain`: implementación con GoogleSignIn iOS SDK via Swift interop
5. Actualizar `FirebaseAuthDataSourceImpl` iOS para usar el handler

---

#### KMP-18: Notificaciones de partido (reimplementación KMP)

> **Contexto**: El sistema de notificaciones fue eliminado en pre-migración. Si se quiere reimplementar para Fase 3:

- `expect/actual` en `:domain` para `MatchNotificationController`
- `androidMain`: `ForegroundService` + `NotificationManager`
- `iosMain`: `UNUserNotificationCenter` (iOS local notifications)

---

### 3.4 Dependencias entre tareas Fase 3

```
KMP-15 (shared-ui module)
    ↓
KMP-16 (migrate screens — en paralelo por pantalla)
    ↓
KMP-17 (Google Sign-In iOS)   KMP-18 (notifications — opcional)
```

### 3.5 Componentes Android-only que permanecen en `:app`

Independientemente de la migración UI, estos elementos permanecen en `:app` Android-only:

- `MainActivity.kt`
- `AndroidManifest.xml`
- Firebase Crashlytics setup
- `google-services.json` config
- Deep links / Android app links

---

## 4. Análisis de viabilidad técnica

### 4.1 GitLive Firebase SDK vs alternativas

| Opción | Ventajas | Inconvenientes |
|--------|----------|----------------|
| **GitLive Firebase** (`dev.gitlive:firebase-*`) | API casi idéntica a Firebase Android; soporta Auth, Firestore, Storage; activo en 2026 | API no 100% compatible; versión puede quedar retrasada respecto a Firebase |
| **Firebase iOS SDK nativo** (Swift interop) | Oficial, sin intermediarios | Requiere mucho boilerplate Swift ↔ Kotlin |
| **Supabase KMP** | Alternativa 100% KMP nativa | Cambio de backend — no aplica |

**Recomendación**: GitLive Firebase. Su API es suficientemente similar para que los DataSources iOS sean casi copia de los Android.

### 4.2 Compose Multiplatform 1.7.3 en iOS — Estado (feb 2026)

| Componente | Estado | Impacto |
|-----------|--------|---------|
| Material3 | ✅ Estable | Sin cambios — todo portado |
| `LazyColumn`, `LazyRow` | ✅ Estable | MatchList, PlayerList — directo |
| Navegación CMP | ✅ Estable 2.8+ | Route.kt ya en `:app` — migración directa |
| Canvas / gráficas | ✅ Estable | Alternativa a `compose-charts` |
| Animaciones | ✅ Estable | lottie ya eliminado |
| Coil 3.x | ✅ KMP | Actualizar desde coil 2.5.0 |
| Google Fonts | ❌ Android-only | Bundlear fuentes o sistema |
| `compose-charts` 0.2.0 | ❌ Android-only | Reemplazar por `koalaplot` o Canvas |

### 4.3 ViewModels en iOS — Gestión del ciclo de vida

`androidx.lifecycle:lifecycle-viewmodel:2.8.6` soporta KMP. En iOS el ViewModel no está ligado a una `Activity` (no existe), sino a un `CoroutineScope` equivalente. Las opciones:

- **Con CMP**: `koinViewModel()` de Koin CMP gestiona el ciclo de vida automáticamente en Compose
- **Con SwiftUI**: Se puede usar `StateViewModel` de Koin o gestión manual del scope
- **Recomendado para Fase 2**: CMP + `koinViewModel()` — misma API que en Android

---

## 5. Dependencias entre todas las fases

```
✅ Fase 1 COMPLETADA
   KMP-1→3→4→6→7→8→9 (PRs #244–#250)

Fase 2 (iOS MVP):
   KMP-11 (Firebase iOS en data:remote)
       ↓
   KMP-12 (ViewModels → commonMain)
       ↓
   KMP-13 (DI iOS bootstrapping)
       ↓
   KMP-14 (iosApp + Login + MatchList como Text simple)

Fase 3 (UI completa):
   KMP-14 completado
       ↓
   KMP-15 (módulo :shared-ui con CMP)
       ↓
   KMP-16 (migrar pantallas — paralelo por pantalla)
   KMP-17 (Google Sign-In iOS)
   KMP-18 (notificaciones — opcional)
```

---

## 6. Porcentaje de reutilización de código por fase

| Capa | Fase 1 (actual) | Fase 2 (tras KMP-12) | Fase 3 (con :shared-ui) |
|------|:---------------:|:---------------------:|:------------------------:|
| `:domain` | 100% compartido | 100% | 100% |
| `:usecase` | 100% compartido | 100% | 100% |
| `:data:core` | 100% compartido | 100% | 100% |
| `:data:local` | 100% (Android real, iOS real) | 100% | 100% |
| `:data:remote` | Android real, iOS stub | Android real, iOS real | 100% |
| `:viewmodel` | 0% iOS (androidMain) | ~20% iOS (3 VMs) | 100% |
| UI (screens) | 0% iOS | 0% iOS (SwiftUI/CMP básico) | ~75% |
| **Total** | **~70% en commonMain** | **~80%** | **~90%** |

---

## 7. Registro de cambios

| Fecha | Cambio |
|-------|--------|
| 2026-02-26 | Documento inicial con análisis KMP completo |
| 2026-02-26 | KMP-2: `Route.kt` movido a `:app/ui/navigation/` |
| 2026-02-26 | KMP-5: Moshi eliminado del proyecto |
| 2026-02-26 | KMP-10 (adelantado): `:service` eliminado. Sistema de notificaciones descartado |
| 2026-02-26 | KMP-8 (prep): `SavedStateHandle` eliminado de 5 ViewModels |
| 2026-02-26 | `lottie-compose` eliminado del proyecto |
| 2026-02-26 | KMP-1 (:domain) completado — PR #244 |
| 2026-02-26 | KMP-3 (:usecase) completado — PR #245 |
| 2026-02-26 | KMP-4 (:data:core) completado — PR #246 |
| 2026-02-26 | KMP-6 (:data:local) completado — PR #247 |
| 2026-02-26 | KMP-7 (:data:remote boundary) completado — PR #248 |
| 2026-02-26 | KMP-8 (:viewmodel) completado — PR #249 |
| 2026-02-26 | KMP-9 (:di) completado — PR #250 |
| 2026-02-26 | **Fase 1 completada.** Documento actualizado con plan Fase 2 (iOS MVP) y Fase 3 (UI completa) |

---

*Versiones de referencia: Kotlin 2.1.0 · AGP 8.6.1 · Compose Multiplatform 1.7.3 · Koin 4.0.0 · Ktor 3.0.1 · Firebase BOM 33.6.0 · lifecycle-viewmodel 2.8.6 · GitLive Firebase ~2.1.0 (Fase 2)*
