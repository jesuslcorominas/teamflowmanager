# KMP Migration Plan — TeamFlowManager

> **Fecha de análisis inicial**: 2026-02-26
> **Última actualización**: 2026-03-02 (Fase 2 funcional; Fase 3 en curso)
> **Rama base de migración**: `migration/kmp-migration`
> **Arquitecto**: Senior KMP/Android Architect (Claude Code)

---

## Estado global de la migración

| Fase | Descripción | Estado |
|------|-------------|--------|
| **Fase 1** | Infraestructura KMP — todos los módulos convertidos a multiplatform | ✅ **COMPLETADA** |
| **Fase 2** | iOS MVP — login Firebase (Google Sign-In nativo) + listado de partidos | ✅ **COMPLETADA** |
| **Fase 3** | iOS con UI completa igual a la de Android (Compose Multiplatform) | 🚧 En curso |

### PRs pendientes de merge (Fase 2 → `migration/kmp-migration`)

Estas PRs están **completadas y verificadas** pero pendientes de aprobación y merge humano:

| PR | Branch | Contenido | Issue | Nota |
|----|--------|-----------|-------|------|
| #266 | `migration/kmp-xcode-project` | Proyecto Xcode completo + CocoaPods Firebase + todos los datasources iOS | — | ✅ Infraestructura completa |
| #267 | `migration/kmp-16b-screens` | SplashScreen, LoginScreen, MatchListScreen migradas a `:shared-ui` | #265 | ⚠️ Ver nota MVP abajo |
| #268 | `migration/kmp-17-google-signin` | Google Sign-In nativo iOS (GIDSignIn ↔ KMP bridge) | #261 | ⚠️ Ver nota estética abajo |

> **Acción requerida**: revisar y mergear las 3 PRs en orden (#266 → #267 → #268) para cerrar Fase 2 formalmente.

#### ⚠️ Nota sobre PR #267 — Pantallas MVP, no producción

Las 3 pantallas migradas a `:shared-ui` son **funcionales a nivel de MVP** pero **no tienen el mismo acabado visual que Android**:

- **SplashScreen**: muestra solo un spinner. Sin logo ni branding. El `Loading()` composable es un placeholder — falta `TeamFlowManagerIcon` y el diseño de splash definitivo.
- **LoginScreen**: botón "Sign in with Google" funcional pero con estilo mínimo (`OutlinedButton`). Falta ajustar tipografía, colores, logo y composición visual para que sea equivalente a Android.
- **MatchListScreen**: lista plana de texto (`Text` por partido). Falta el diseño de tarjeta (`MatchCard`) con resultado, rival, fecha, estadísticas visuales, etc.

Estas mejoras se abordan en **KMP-19+** cuando se migren los componentes atómicos compartidos (`AppCard`, sistema de diseño) y los strings/recursos CMP.

#### ⚠️ Nota sobre PR #268 — Google Sign-In funcional, estética pendiente

El flujo de autenticación Google funciona end-to-end en iOS (GIDSignIn → Firebase Auth → navegación a MatchList). Sin embargo:

- El botón de login es un `OutlinedButton` genérico, sin el estilo oficial de Google Sign-In.
- **Decisión pendiente**: ¿botón nativo iOS (Google Sign-In branded button del SDK) o botón custom con el estilo de la app?
  - **Opción A — Nativo iOS**: Usar `GIDSignInButton` de GoogleSignIn SDK via `UIViewRepresentable`. Experiencia auténtica pero platform-specific (Android tiene su propio `SignInWithGoogleButton`). Tiene sentido: Google no tiene una librería CMP, y la experiencia nativa es la correcta en cada plataforma.
  - **Opción B — Botón custom**: Un único Composable CMP en `:shared-ui` con el logo de Google y estilo propio de la app. Menos "oficial" pero 100% compartido.
  - **Recomendación**: Opción A (nativo por plataforma) — planificar como subtarea de **KMP-19** o issue separada.

---

## 1. Estado actual del proyecto — Fase 1 COMPLETADA

### 1.1 Inventario de módulos (estado actual)

| Módulo | Plugin | commonMain | androidMain | iosMain | Tests | Estado |
|--------|--------|:----------:|:-----------:|:-------:|:-----:|--------|
| `:domain` | `kotlin.multiplatform` | ✓ | ✓ | ✓ | ✓ | ✅ Completo |
| `:usecase` | `kotlin.multiplatform` | ✓ | — | — | ✓ | ✅ Completo |
| `:data:core` | `kotlin.multiplatform` | ✓ | — | — | ✓ | ✅ Completo |
| `:data:local` | `kotlin.multiplatform` | ✓ | ✓ (Room) | ✓ (NSUserDefaults) | ✓ | ✅ Completo |
| `:data:remote` | `kotlin.multiplatform` | ✓ | ✓ (Firebase+Ktor) | ✓ (GitLive Firebase) | ✓ | ✅ Completo |
| `:viewmodel` | `kotlin.multiplatform` | ✓ (14 VMs) | ✓ (Koin Android) | — | ✓ | ✅ Completo (todos los VMs en commonMain) |
| `:di` | `kotlin.multiplatform` | ✓ | ✓ | ✓ (initKoinIos) | — | ✅ Completo |
| `:shared-ui` | `kotlin.multiplatform` | ✓ (3 pantallas) | — | — | — | 🚧 En curso (Fase 3) |
| `:app` | `android.application` | — | ✓ | — | ✓ | ✅ Android-only (por diseño) |
| `:iosApp` | `kotlin.multiplatform` | ✓ (App.kt) | — | ✓ (bridge, MainVC) | — | ✅ Funcional (Google Sign-In + MatchList) |
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

Tener la app iOS con exactamente las mismas pantallas y funcionalidad que la app Android, implementadas con Compose Multiplatform compartido en `:shared-ui`.

### 3.2 Arquitectura objetivo (ya establecida en Fase 2)

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

El módulo `:shared-ui` ya existe (PR #267). Tiene 3 pantallas. Fase 3 completa las restantes.

### 3.3 Inventario de pantallas

#### Ya en `:shared-ui/commonMain` ✅
| Pantalla | Estado |
|----------|--------|
| `SplashScreen` | ✅ En shared-ui |
| `LoginScreen` | ✅ En shared-ui |
| `MatchListScreen` | ✅ En shared-ui |

#### Pendientes de migrar a `:shared-ui` — 15 pantallas
| Pantalla | Complejidad | Bloqueantes | Issue |
|----------|-------------|-------------|-------|
| `ClubSelectionScreen` | Baja | `R.string` → CMP, `TrackScreenView` | KMP-19 |
| `CreateClubScreen` | Baja-Media | `R.string`, `AppTextField`, `TrackScreenView` | KMP-19 |
| `JoinClubScreen` | Baja-Media | `R.string`, `AppTextField`, `TrackScreenView` | KMP-19 |
| `ClubMembersScreen` | Media | `R.string`, `AppCard`, `TrackScreenView` | KMP-19 |
| `TeamListScreen` | Media | `R.string`, Coil (imágenes) | KMP-20 |
| `TeamScreen` | Media | `R.string`, `AppTopBar`, Coil | KMP-20 |
| `PlayersScreen` | Media | `R.string`, Coil | KMP-20 |
| `PlayerWizardScreen` | Media | `R.string`, `AppTextField` | KMP-20 |
| `SettingsScreen` | Baja | `R.string`, `TrackScreenView` | KMP-21 |
| `ArchivedMatchesScreen` | Media | `R.string`, `AppCard` | KMP-21 |
| `AcceptTeamInvitationScreen` | Media | `R.string`, deep link | KMP-21 |
| `MatchCreationWizardScreen` | Media-Alta | `R.string`, `AppTextField`, wizard state | KMP-22 |
| `MainScreen` | Media | Bottom nav, `R.string` | KMP-22 |
| `MatchScreen` | Alta | TimeTicker, Canvas timer, drag-drop | KMP-23 |
| `AnalysisScreen` | Alta | `compose-charts` → reemplazar por Canvas/koalaplot | KMP-24 |

### 3.4 Bloqueantes técnicos transversales

Antes de empezar cada tanda de pantallas, resolver:

| Bloqueante | Afecta a | Solución |
|------------|----------|----------|
| `androidx.compose.ui.res.stringResource` | Todas | → `org.jetbrains.compose.resources.stringResource` + strings en `composeResources/` |
| `org.koin.androidx.compose.koinViewModel` | Todas | → `org.koin.compose.viewmodel.koinViewModel` (ya resuelto en las 3 existentes) |
| `TrackScreenView` / Analytics | 10+ pantallas | Crear `expect fun TrackScreenView(screenName, screenClass)` — no-op en iOS, Firebase en Android |
| `TeamFlowManagerIcon` | 3 pantallas | Mover a `:shared-ui` como recurso CMP (vector drawable o `painterResource`) |
| `AppTextField`, `AppCard`, `AppTopBar` | 10+ pantallas | Migrar componentes atómicos a `:shared-ui/commonMain/components/` |
| `coil-compose` (imágenes) | 5 pantallas | Actualizar a Coil 3.x (KMP-compatible) |
| `compose-charts` (AnalysisScreen) | 1 pantalla | Reemplazar por `koalaplot-core` o Canvas CMP |
| `compose-google-fonts` | Theme | Bundlear fuente o usar fuentes del sistema en iOS |

### 3.5 Plan de tareas Fase 3

---

#### KMP-19: Migrar pantallas de club a `:shared-ui`

**Pantallas**: `ClubSelectionScreen`, `CreateClubScreen`, `JoinClubScreen`, `ClubMembersScreen`

**Pasos previos** (bloqueantes compartidos para TODAS las pantallas):
1. Mover `AppTextField`, `AppCard`, componentes básicos a `:shared-ui/commonMain/components/`
2. Crear `expect fun TrackScreenView(screenName: ScreenName, screenClass: String)` en `:shared-ui`
   - `androidMain actual`: llama a Firebase Analytics
   - `iosMain actual`: no-op (analytics iOS se añade en iteración futura)
3. Migrar `TeamFlowManagerIcon` a `:shared-ui` como `painterResource` CMP
4. Añadir strings de club a `shared-ui/src/commonMain/composeResources/values/strings.xml`

**Criterios de aceptación**:
- [ ] Las 4 pantallas en `shared-ui/commonMain`
- [ ] `:app` las importa desde `:shared-ui` (sin duplicados)
- [ ] `./gradlew :shared-ui:compileDebugKotlin` pasa
- [ ] `./gradlew :iosApp:compileKotlinIosSimulatorArm64` pasa
- [ ] Android sigue funcionando

---

#### KMP-20: Migrar pantallas de equipo y jugadores

**Pantallas**: `TeamListScreen`, `TeamScreen`, `PlayersScreen`, `PlayerWizardScreen`

**Pasos previos**:
1. Actualizar `coil-compose` → Coil 3.x en `:shared-ui` (KMP-compatible)
2. Migrar `AppTopBar` a `:shared-ui`

**Criterios de aceptación**:
- [ ] Las 4 pantallas en `shared-ui/commonMain`
- [ ] Imágenes de equipo/jugador cargan en iOS con Coil 3.x

---

#### KMP-21: Migrar pantallas secundarias

**Pantallas**: `SettingsScreen`, `ArchivedMatchesScreen`, `AcceptTeamInvitationScreen`

**Nota sobre `AcceptTeamInvitationScreen`**: usa deep links (Android Intent). Crear `expect fun openDeepLink(url: String)` con `actual` Android + iOS.

---

#### KMP-22: Migrar wizard y navegación principal

**Pantallas**: `MatchCreationWizardScreen`, `MainScreen` (bottom nav)

**Nota sobre navegación**: `MainScreen` usa Jetpack Navigation. Para iOS usar la navegación CMP ya establecida en `App.kt`.

---

#### KMP-23: Migrar `MatchScreen`

La pantalla más compleja. Contiene:
- Timer canvas animado (TimeTicker)
- Drag & drop de jugadores (`DragDropContainer`, `DraggablePlayerItem`)
- Sustituciones, goles, control de tiempo por jugador

**Pasos**:
1. Verificar compatibilidad de drag & drop con CMP en iOS
2. Migrar `MatchTimeCard`, `SubstitutionCard`, componentes de partido a `:shared-ui`
3. `TimeTicker` ya está en `viewmodel/commonMain` — sin cambios

---

#### KMP-24: Migrar `AnalysisScreen`

**Bloqueante**: `compose-charts 0.2.0` es Android-only.

**Opciones**:
- Reemplazar por `koalaplot-core` (KMP nativo, gráficas básicas)
- Implementar gráficas con Canvas CMP (control total, sin dependencia externa)

**Recomendación**: Canvas CMP — las gráficas de análisis son simples (barras, líneas) y el canvas de CMP es estable.

---

#### KMP-25: Navegación iOS completa

Una vez todas las pantallas están en `:shared-ui`, actualizar `App.kt` (iosApp) con navegación completa equivalente a la Android (`Navigation.kt`).

**Actualmente** `App.kt` solo navega: SPLASH → LOGIN → MATCHES.
**Objetivo**: añadir todas las rutas.

---

#### KMP-18: Notificaciones (opcional)

> Descartado en pre-migración. Reimplementar si se requiere:
- `expect/actual` para `MatchNotificationController`
- `androidMain`: ForegroundService + NotificationManager
- `iosMain`: UNUserNotificationCenter

---

### 3.6 Dependencias entre tareas Fase 3

```
KMP-19 (club screens + bloqueantes transversales)
    ↓
KMP-20 (team/players — necesita Coil 3.x)
KMP-21 (screens secundarias — paralelo con KMP-20)
    ↓
KMP-22 (wizard + nav principal)
KMP-23 (MatchScreen — la más compleja)
KMP-24 (AnalysisScreen — compose-charts reemplazar)
    ↓
KMP-25 (navegación iOS completa)
KMP-18 (notificaciones — opcional, paralelo)
```

### 3.7 Componentes que permanecen Android-only en `:app`

| Componente | Motivo |
|------------|--------|
| `MainActivity.kt` | Entry point Android |
| `AndroidManifest.xml` | Sistema Android |
| `google-services.json` | Firebase Android config |
| `R.string` (Android) | Se mantiene en paralelo; iOS usa CMP `composeResources` |
| Deep links / Android App Links | Platform-specific |
| Firebase Crashlytics setup | SDK Android-only |
| Koin `viewModel {}` registration | Android lifecycle; iOS usa `factory {}` |

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

✅ Fase 2 COMPLETADA (PRs pendientes de merge)
   KMP-11 (Firebase iOS)    → PR #255 ✅ merged
   KMP-12 (VMs commonMain)  → PR #256 ✅ merged
   KMP-13 (DI iOS)          → PR #257 ✅ merged
   KMP-14 (iosApp CMP MVP)  → PR #258 ✅ merged
   KMP-Xcode (proyecto Xcode + CocoaPods) → PR #266 ⏳ merge pendiente
   KMP-15 (:shared-ui)      → PR #263 ✅ merged
   KMP-16 (todos los VMs)   → PR #264 ✅ merged
   KMP-16b (3 pantallas → shared-ui) → PR #267 ⏳ merge pendiente
   KMP-17 (Google Sign-In iOS)       → PR #268 ⏳ merge pendiente

🚧 Fase 3 (UI completa — iniciar tras merge de PRs pendientes):
   KMP-19 (club screens + bloqueantes transversales)
       ↓
   KMP-20 (team/players + Coil 3.x)   KMP-21 (screens secundarias)
       ↓
   KMP-22 (wizard + MainScreen)
       ↓
   KMP-23 (MatchScreen)   KMP-24 (AnalysisScreen)
       ↓
   KMP-25 (navegación iOS completa)
   KMP-18 (notificaciones — opcional)
```

---

## 6. Porcentaje de reutilización de código — estado real alcanzado

### 6.1 Por capa (estado actual, post-Fase 2)

| Capa | Archivos compartidos | Archivos platform-specific | % compartido | Notas |
|------|:--------------------:|:---------------------------:|:------------:|-------|
| `:domain` | Models, interfaces, use case interfaces | `Platform.kt` (expect/actual) | **~99%** | |
| `:usecase` | Todas las implementaciones | — | **100%** | Pure Kotlin |
| `:data:core` | Repositories, DataSource interfaces | — | **100%** | Pure Kotlin |
| `:data:local` | Room entities, DAOs (commonMain) | Android: `AppDatabase` actual · iOS: NSUserDefaults actual | **~85%** | Schema compartido |
| `:data:remote` | Interfaces, modelos Firestore (commonMain) | Android: Firebase/Ktor actual · iOS: GitLive actual | **~60%** | APIs similares, implementaciones distintas |
| `:viewmodel` | 14 ViewModels + TimeTicker (commonMain) | Android: Koin `viewModel {}` · iOS: Koin `factory {}` | **~95%** | Solo el módulo DI difiere |
| `:di` | `InitKoin.kt` (commonMain) | `TeamFlowManagerModule` (Android) · `IosModule` (iOS) | **~50%** | Por diseño: cada plataforma registra sus propios datasources |
| UI — pantallas | SplashScreen, LoginScreen, MatchListScreen (`:shared-ui`) | 15 pantallas restantes solo en `:app` | **~15%** | Fase 3 lo llevará a ~90% |
| **Total proyecto** | | | **~75%** | Sube a ~90% al completar Fase 3 |

### 6.2 Dónde vive el código compartido

```
commonMain (compartido Android + iOS):
  ├── :domain          → 100% — modelos, interfaces use cases
  ├── :usecase         → 100% — lógica de negocio
  ├── :data:core       → 100% — repositorios, interfaces datasource
  ├── :data:local      → ~85% — schema Room/SQLDelight + lógica
  ├── :data:remote     → ~60% — interfaces + modelos Firestore
  ├── :viewmodel       → ~95% — 14 ViewModels + TimeTicker
  ├── :di              → ~50% — punto de entrada Koin
  └── :shared-ui       → ~15% UI — 3 pantallas de ~18

platform-specific:
  Android (:app)       → MainActivity, Navigation, 15 pantallas, R.string
  iOS (:iosApp)        → App.kt nav shell, GoogleSignInBridge, MainViewController.swift
  iOS (Swift)          → iOSApp.swift, GoogleSignInHandler.swift, ContentView.swift
```

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
| 2026-02-26 | **Fase 1 completada.** |
| 2026-02-27 | KMP-11 (:data:remote iosMain GitLive Firebase) completado — PR #255 |
| 2026-02-27 | KMP-12 (todos los ViewModels + TimeTicker → commonMain) completado — PR #256 |
| 2026-02-27 | KMP-13 (:di iOS bootstrapping — initKoinIos) completado — PR #257 |
| 2026-02-27 | KMP-14 (:iosApp CMP MVP — Splash/Login/MatchList como Text) completado — PR #258 |
| 2026-02-27 | KMP-Xcode: Proyecto Xcode + CocoaPods Firebase + todos los datasources iOS — PR #266 |
| 2026-02-27 | KMP-15: módulo `:shared-ui` con CMP — PR #263 |
| 2026-02-27 | KMP-16 (14 VMs → commonMain, todos los VMs) — PR #264 |
| 2026-02-27 | KMP-16b: SplashScreen, LoginScreen, MatchListScreen → `:shared-ui` — PR #267 |
| 2026-03-01 | KMP-17: Google Sign-In nativo iOS (GIDSignIn ↔ KMP bridge) — PR #268 |
| 2026-03-02 | **Fase 2 completada.** App iOS funcional: Google Sign-In + listado de partidos. PRs #266/#267/#268 pendientes de merge humano. |
| 2026-03-02 | Documento actualizado con estado real de código compartido y plan Fase 3 |

---

*Versiones de referencia: Kotlin 2.1.0 · AGP 8.6.1 · Compose Multiplatform 1.7.3 · Koin 4.0.0 · Ktor 3.0.1 · Firebase BOM 33.6.0 · lifecycle-viewmodel 2.8.6 · GitLive Firebase ~2.1.0*
