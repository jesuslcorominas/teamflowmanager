# US-7.1.1: Análisis de Migración a Kotlin Multiplatform Mobile (KMM)

> **📝 Actualización:** Este documento ha sido actualizado para reflejar el estado actual del proyecto en la rama `develop`:
> - ✅ El proyecto ya usa **Ktorfit + Ktor Client** (compatible con KMM)
> - ✅ **Room 2.7.0+** ahora soporta Kotlin Multiplatform (no requiere migración a SQLDelight)
> - ✅ **androidx.lifecycle.ViewModel** ahora soporta KMM desde versión 2.8.0 (no requiere reimplementación)
> - ✅ **Koin 4.0.0** ya es totalmente compatible con KMM (sin migración necesaria)
> - ✅ Ya usa **kotlinx.serialization** (compatible con KMM)
> - ⏱️ **Tiempo de migración reducido** de 10 semanas a 5-6 semanas para Opción 1

## Índice
1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Estado Actual del Proyecto](#estado-actual-del-proyecto)
3. [Opciones de Migración](#opciones-de-migración)
4. [Opción 1: Compose Multiplatform](#opción-1-compose-multiplatform)
5. [Opción 2: UI Nativa (SwiftUI para iOS)](#opción-2-ui-nativa-swiftui-para-ios)
6. [Comparación de Opciones](#comparación-de-opciones)
7. [Estrategia de Migración Recomendada](#estrategia-de-migración-recomendada)
8. [Cronograma Estimado](#cronograma-estimado)
9. [Riesgos y Mitigaciones](#riesgos-y-mitigaciones)

---

## Resumen Ejecutivo

Este documento analiza los cambios necesarios para migrar **TeamFlow Manager** de una aplicación Android nativa a una aplicación **Kotlin Multiplatform Mobile (KMM)** que pueda ejecutarse tanto en Android como en iOS.

### Hallazgos Clave:
- El proyecto está **bien estructurado** siguiendo Clean Architecture, lo que facilita la migración
- Los módulos **domain**, **usecase** y **data:core** ya son compatibles con KMM con cambios mínimos
- **El proyecto ya usa Ktorfit** (compatible con KMM), simplificando la migración de red
- **Room ahora soporta KMM** (desde v2.7.0-alpha), sin necesidad de migración
- **androidx.lifecycle.ViewModel soporta KMM** (desde v2.8.0), manteniendo la API familiar
- **Koin 4.0.0 es totalmente compatible con KMM**, sin migración necesaria
- Se identifican **dos opciones principales** de UI: Compose Multiplatform vs SwiftUI nativo
- Estimación de esfuerzo: **2-4 semanas** dependiendo de la opción elegida
- **Recomendación**: Opción 1 (Compose Multiplatform) para maximizar código compartido

---

## Estado Actual del Proyecto

### Arquitectura Actual

```
TeamFlowManager (Android App)
├── :app (Android Application) - UI con Jetpack Compose
├── :viewmodel (Android Library) - ViewModels Android-específicos
├── :usecase (Kotlin JVM Library) - Lógica de negocio
├── :domain (Kotlin JVM Library) - Modelos de dominio
├── :data:core (Kotlin JVM Library) - Repositorios
├── :data:local (Android Library) - Room Database
├── :data:remote (Kotlin JVM Library) - Ktorfit + Ktor Client
└── :di (Android Library) - Koin DI
```

### Tecnologías Utilizadas

| Capa | Tecnología | Compatibilidad KMM |
|------|-----------|-------------------|
| UI | Jetpack Compose | ⚠️ Requiere migración a Compose Multiplatform o UI nativa |
| Presentación | androidx.lifecycle.ViewModel | ✅ **Compatible con KMM** (lifecycle 2.8.0+) |
| Lógica de Negocio | Kotlin Coroutines | ✅ Compatible |
| Dominio | Kotlin puro | ✅ Compatible |
| BD Local | Room | ✅ **Compatible con KMM** (Room 2.7.0+) |
| API Remote | Ktorfit, Ktor Client, kotlinx.serialization | ✅ **Ya compatible con KMM** |
| DI | Koin 4.0.0 | ✅ **Ya compatible con KMM** |

### Estadísticas del Proyecto
- **63 archivos Kotlin** en el módulo :app (UI)
- **~3,951 líneas** de código UI en Compose
- **7 módulos** en total
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 36 (Android 14)
- **Kotlin**: 2.1.0
- **Gradle**: 8.4

---

## Opciones de Migración

## Opción 1: Compose Multiplatform

### Descripción
Utilizar **Compose Multiplatform** para compartir tanto la lógica de negocio como la UI entre Android e iOS. Esta opción maximiza la reutilización de código.

### Arquitectura Propuesta

```
TeamFlowManager (KMM Project)
├── shared/
│   ├── commonMain/
│   │   ├── domain/          ✅ Código compartido
│   │   ├── usecase/         ✅ Código compartido
│   │   ├── data/            ✅ Código compartido
│   │   ├── viewmodel/       ✅ Código compartido (con StateFlow)
│   │   └── ui/              ✅ Compose Multiplatform compartido
│   ├── androidMain/
│   │   └── Platform-specific (si necesario)
│   └── iosMain/
│       └── Platform-specific (si necesario)
├── androidApp/              📱 Android Application Module
└── iosApp/                  🍎 iOS Xcode Project
    └── ContentView.swift (wrapper para Compose)
```

### Cambios Necesarios

#### 1. Configuración del Proyecto

**1.1. Modificar `settings.gradle.kts`:**
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "TeamFlowManager"

include(":androidApp")
include(":shared")
```

**1.2. Actualizar `gradle/libs.versions.toml`:**
```toml
[versions]
kotlin = "2.1.0"
agp = "8.6.1"
compose = "1.7.1"
compose-multiplatform = "1.7.1"
lifecycle = "2.8.0"  # Lifecycle con soporte KMM (ViewModel)
room = "2.7.0-alpha01"  # Room con soporte KMM
ksp = "2.1.0-1.0.28"
ktor = "3.0.1"
ktorfit = "2.6.0"
koin = "4.0.0"
kotlinx-coroutines = "1.9.0"
kotlinx-serialization = "1.7.3"
kotlinx-datetime = "0.6.1"

[libraries]
# Compose Multiplatform
compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "compose-multiplatform" }
compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "compose-multiplatform" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "compose-multiplatform" }
compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "compose-multiplatform" }

# Lifecycle (ViewModel multiplatform)
lifecycle-viewmodel = { module = "androidx.lifecycle:lifecycle-viewmodel", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }

# Room Multiplatform
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

# Ktorfit (ya en uso en el proyecto)
ktorfit-lib = { module = "de.jensklingenberg.ktorfit:ktorfit-lib", version.ref = "ktorfit" }
ktorfit-ksp = { module = "de.jensklingenberg.ktorfit:ktorfit-ksp", version.ref = "ktorfit" }

# Ktor Client (ya en uso en el proyecto)
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }

# Kotlinx
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

# Koin Multiplatform
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
```

**1.3. Crear `shared/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            // Lifecycle (ViewModel multiplatform)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Ktorfit (ya en uso en el proyecto)
            implementation(libs.ktorfit.lib)
            
            // Ktor Client (ya en uso en el proyecto)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // Room Multiplatform
            implementation(libs.room.runtime)
            
            // Kotlinx
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.jesuslcorominas.teamflowmanager.shared"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 29
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // KSP para Ktorfit y Room
    add("kspCommonMainMetadata", libs.ktorfit.ksp)
    add("kspAndroid", libs.ktorfit.ksp)
    add("kspIosX64", libs.ktorfit.ksp)
    add("kspIosArm64", libs.ktorfit.ksp)
    add("kspIosSimulatorArm64", libs.ktorfit.ksp)
    
    add("kspCommonMainMetadata", libs.room.compiler)
    add("kspAndroid", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
```

#### 2. Migración de Módulos

**2.1. Módulo Domain (✅ Sin cambios o mínimos)**
- Ya es Kotlin puro, solo necesita moverse a `shared/commonMain/kotlin/domain`
- Mantener las clases de datos como están
- Ya usa `@Serializable` de kotlinx.serialization

**2.2. Módulo UseCase (✅ Cambios menores)**
- Mover a `shared/commonMain/kotlin/usecase`
- Ya usa Coroutines, que es compatible con KMM
- Sin cambios necesarios en la lógica

**2.3. Módulo Data (✅ Cambios mínimos)**

**Data:Core**
- Mover interfaces de repositorio a `shared/commonMain/kotlin/data/core`
- Ya usa `kotlinx.serialization` en develop
- Mantener la estructura actual de repositorios

**Data:Local (✅ **ROOM MULTIPLATFORM - Sin migración necesaria**)**

**Buenas noticias:** Room ahora soporta Kotlin Multiplatform desde la versión 2.7.0-alpha01. **No es necesario migrar a SQLDelight**.

- Room funcionará tanto en Android como en iOS con el mismo código
- Mantener las entidades `@Entity` actuales
- Mantener los DAOs `@Dao` actuales
- Solo necesita ajustar la configuración de KSP para multiplatform

Ejemplo de configuración Room KMM:

```kotlin
// shared/src/commonMain/kotlin/data/local/TeamDao.kt
@Dao
interface TeamDao {
    @Query("SELECT * FROM teams")
    suspend fun getAllTeams(): List<TeamEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)
    
    @Delete
    suspend fun deleteTeam(team: TeamEntity)
}

// shared/src/commonMain/kotlin/data/local/AppDatabase.kt
@Database(entities = [TeamEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teamDao(): TeamDao
}

// shared/src/commonMain/kotlin/data/local/DatabaseBuilder.kt
expect object DatabaseBuilder {
    fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
}

fun getDatabase(): AppDatabase {
    return DatabaseBuilder.getDatabaseBuilder()
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .build()
}
```

**Platform-specific Database Builders:**

```kotlin
// shared/src/androidMain/kotlin/data/local/DatabaseBuilder.android.kt
actual object DatabaseBuilder {
    actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        val appContext = TODO("Get context from DI")
        val dbFile = appContext.getDatabasePath("teamflow.db")
        return Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}

// shared/src/iosMain/kotlin/data/local/DatabaseBuilder.ios.kt
actual object DatabaseBuilder {
    actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        val dbFilePath = NSHomeDirectory() + "/teamflow.db"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFilePath
        )
    }
}
```

**Ventajas de usar Room KMM:**
- ✅ No hay migración de datos necesaria
- ✅ Mantener todo el código existente de Room
- ✅ API familiar para el equipo de desarrollo
- ✅ Mejor soporte y documentación que SQLDelight
- ✅ Mantenido oficialmente por Google/JetBrains

**Data:Remote (✅ **YA USA KTORFIT - Sin migración necesaria**)**

**El proyecto ya migró de Retrofit a Ktorfit** en la rama develop, que es totalmente compatible con KMM.

- Ktorfit ya está configurado con KSP
- Ya usa Ktor Client (compatible con KMM)
- Ya usa `kotlinx.serialization`

Ejemplo del estado actual (ya en develop):

**Antes (Retrofit):**
```kotlin
```kotlin
// shared/src/commonMain/kotlin/data/remote/api/SampleApi.kt
interface SampleApi {
    @GET("resource/{id}")
    suspend fun getResource(@Path("id") id: String): ResourceDto
    
    @POST("teams")
    suspend fun createTeam(@Body team: TeamDto): TeamDto
}

// Configuración del cliente Ktorfit
fun createKtorfit(): Ktorfit {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    return Ktorfit.Builder()
        .httpClient(client)
        .baseUrl("https://api.example.com/")
        .build()
}
```

**Ventajas de tener Ktorfit ya implementado:**
- ✅ No hay migración de APIs necesaria
- ✅ Sintaxis similar a Retrofit, familiar para el equipo
- ✅ Totalmente compatible con KMM
- ✅ Genera código multiplataforma automáticamente
- ✅ Soporta todas las plataformas (Android, iOS, JVM, JS)

**2.4. Módulo ViewModel (✅ **SIN CAMBIOS NECESARIOS - ViewModel ya es compatible con KMM**)**

**Buenas noticias:** `androidx.lifecycle.ViewModel` ahora soporta Kotlin Multiplatform desde la versión 2.8.0. **No es necesario reimplementar ViewModels**.

- ViewModel funcionará tanto en Android como en iOS con el mismo código
- Mantener la herencia de `ViewModel()`
- `viewModelScope` está disponible en KMM
- Solo se recomienda reemplazar `LiveData` con `StateFlow` para mejor compatibilidad

**Migración mínima recomendada (LiveData → StateFlow):**

**Antes (Android ViewModel con LiveData):**
```kotlin
class TeamViewModel(
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {
    
    private val _teams = MutableLiveData<List<Team>>()
    val teams: LiveData<List<Team>> = _teams
    
    fun loadTeams() {
        viewModelScope.launch {
            _teams.value = getTeamsUseCase()
        }
    }
}
```

**Después (ViewModel Multiplatform con StateFlow):**
```kotlin
// shared/src/commonMain/kotlin/viewmodel/TeamViewModel.kt
class TeamViewModel(
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {  // ✅ Mantener herencia de ViewModel
    
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadTeams() {
        viewModelScope.launch {  // ✅ viewModelScope funciona en KMM
            _isLoading.value = true
            try {
                _teams.value = getTeamsUseCase()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // ✅ onCleared() ya existe en ViewModel base
}
```

**Ventajas de usar androidx.lifecycle.ViewModel en KMM:**
- ✅ API familiar para el equipo de desarrollo
- ✅ `viewModelScope` funciona automáticamente
- ✅ Manejo de ciclo de vida integrado
- ✅ No requiere implementación manual de cancelación
- ✅ Compatible con Compose Multiplatform
- ✅ Mantenido oficialmente por Google/JetBrains

**2.5. Módulo UI (✅ Código compartido con Compose Multiplatform)**

El código de UI con Jetpack Compose puede ser **reutilizado casi al 100%** en Compose Multiplatform con cambios mínimos:

```kotlin
// shared/src/commonMain/kotlin/ui/team/TeamScreen.kt
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = viewModel { TeamViewModel(get()) }  // ✅ Usar viewModel() de lifecycle
) {
    val teams by viewModel.teams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadTeams()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Teams") })
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(teams) { team ->
                    TeamItem(team = team)
                }
            }
        }
    }
}
```

**Cambios en UI:**
- Usar `viewModel()` de `lifecycle-viewmodel-compose` en lugar de `koinInject()` o `koinViewModel()`
- StateFlow con `collectAsState()` funciona perfectamente en Compose Multiplatform
- Algunos componentes de Material Design pueden necesitar ajustes menores
- La navegación requiere una biblioteca multiplataforma como Voyager o Decompose
- Las fuentes de Google pueden necesitar manejarse de forma específica por plataforma

**2.6. Módulo DI (✅ **SIN CAMBIOS - Koin 4.0.0 ya es compatible con KMM**)**

**Buenas noticias:** Koin 4.0.0 ya es **totalmente compatible con Kotlin Multiplatform**. No se requiere migración.

- Koin 4.x fue diseñado desde cero como framework multiplataforma
- La API es la misma en todas las plataformas
- Solo necesita reorganizar módulos entre commonMain y platform-specific

**Configuración actual (Koin Android - compatible con KMM):**
```kotlin
val dataModule = module {
    single { TeamRepository(get(), get()) }
    single { TeamLocalDataSource(get()) }
}
```

**Adaptación a KMM (mismo código, diferente organización):**
```kotlin
// shared/src/commonMain/kotlin/di/CommonModule.kt
val commonModule = module {
    // Domain
    single { GetTeamsUseCase(get()) }
    
    // Data
    single { TeamRepository(get(), get()) }
    
    // ViewModel
    single { TeamViewModel(get()) }
}

// shared/src/androidMain/kotlin/di/AndroidModule.kt
val androidModule = module {
    single { getDatabase(androidContext()) }  // Room database
}

// shared/src/iosMain/kotlin/di/IosModule.kt
val iosModule = module {
    single { getDatabase() }  // Room database
}
```

**Ventajas de Koin 4.0.0 en KMM:**
- ✅ API idéntica en todas las plataformas
- ✅ Sin curva de aprendizaje adicional
- ✅ Soporte oficial para Compose Multiplatform
- ✅ Inyección de dependencias type-safe
- ✅ Documentación extensa para KMM

#### 3. Aplicación Android

**3.1. Crear `androidApp/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.multiplatform)
}

android {
    namespace = "com.jesuslcorominas.teamflowmanager.android"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.jesuslcorominas.teamflowmanager"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
}
```

**3.2. MainActivity.kt:**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            App() // Composable compartido desde shared module
        }
    }
}
```

#### 4. Aplicación iOS

**4.1. Crear proyecto Xcode**

Estructura:
```
iosApp/
├── iosApp.xcodeproj
└── iosApp/
    ├── ContentView.swift
    ├── iOSApp.swift
    └── Info.plist
```

**4.2. iOSApp.swift:**
```swift
import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        KoinKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

**4.3. ContentView.swift:**
```swift
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

**4.4. Crear MainViewController en shared:**
```kotlin
// shared/src/iosMain/kotlin/MainViewController.kt
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    App() // Tu Composable principal
}
```

#### 5. Navegación Multiplataforma

Reemplazar Android Navigation Compose con una solución multiplataforma como **Voyager**:

```kotlin
// shared/build.gradle.kts
dependencies {
    implementation("cafe.adriel.voyager:voyager-navigator:1.1.0")
    implementation("cafe.adriel.voyager:voyager-transitions:1.1.0")
}
```

```kotlin
// shared/src/commonMain/kotlin/ui/navigation/Navigation.kt
@Composable
fun App() {
    Navigator(TeamListScreen()) { navigator ->
        SlideTransition(navigator)
    }
}

class TeamListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TeamScreen(
            onTeamClick = { team ->
                navigator.push(TeamDetailScreen(team.id))
            }
        )
    }
}
```

#### 6. Manejo de Recursos

**6.1. Imágenes y Assets:**
```kotlin
// Usar compose resources
// shared/src/commonMain/composeResources/drawable/
// shared/src/commonMain/composeResources/values/strings.xml

@Composable
fun TeamIcon() {
    Image(
        painter = painterResource(Res.drawable.team_icon),
        contentDescription = null
    )
}
```

**6.2. Strings localizados:**
```kotlin
// shared/src/commonMain/composeResources/values/strings.xml
<resources>
    <string name="app_name">TeamFlow Manager</string>
    <string name="teams">Equipos</string>
</resources>

// Uso en Compose
Text(stringResource(Res.string.teams))
```

### Ventajas de Compose Multiplatform

✅ **Máximo código compartido** (80-95%)
✅ **Desarrollo más rápido** - una sola UI para mantener
✅ **Consistencia perfecta** entre plataformas
✅ **Reutilización del código Compose existente**
✅ **Un solo equipo de desarrollo** puede manejar ambas plataformas
✅ **Testing más simple** - tests compartidos
✅ **Actualizaciones sincronizadas** en ambas plataformas

### Desventajas de Compose Multiplatform

❌ **Rendimiento potencialmente inferior** en iOS vs nativo
❌ **Tamaño de app mayor** en iOS
❌ **Curva de aprendizaje** para desarrolladores iOS nativos
❌ **Dependencia de JetBrains** para Compose iOS (aún en beta)
❌ **Menos control** sobre APIs específicas de plataforma
❌ **Debugging más complejo** en iOS
❌ **Limitaciones en componentes** muy específicos de iOS

### Desglose de Código Compartido

| Capa | % Compartido | Descripción |
|------|-------------|-------------|
| Domain | 100% | Modelos y entidades |
| UseCase | 100% | Lógica de negocio |
| Data (Core) | 100% | Interfaces de repositorio |
| Data (Local) | 95% | Room KMM - solo builders específicos |
| Data (Remote) | 100% | Ktorfit ya es multiplataforma |
| ViewModel | 100% | androidx.lifecycle.ViewModel KMM compatible |
| UI | 90-95% | Con Compose Multiplatform |
| DI | 100% | Koin 4.0.0 ya es multiplataforma |
| **TOTAL** | **96-99%** | |

---

## Opción 2: UI Nativa (SwiftUI para iOS)

### Descripción
Compartir **solo la lógica de negocio y datos** (domain, usecase, data) en KMM, mientras se mantiene Jetpack Compose para Android y se desarrolla una UI nativa en **SwiftUI** para iOS.

### Arquitectura Propuesta

```
TeamFlowManager (KMM Project)
├── shared/
│   ├── commonMain/
│   │   ├── domain/          ✅ Código compartido
│   │   ├── usecase/         ✅ Código compartido
│   │   └── data/            ✅ Código compartido
│   ├── androidMain/
│   │   └── Platform implementations
│   └── iosMain/
│       └── Platform implementations
├── androidApp/              📱 Jetpack Compose UI (existente)
│   └── ui/ (Mantener código actual)
└── iosApp/                  🍎 SwiftUI UI (nuevo)
    └── Views/ (Desarrollar desde cero)
```

### Cambios Necesarios

#### 1. Módulo Shared (Solo lógica y datos)

**1.1. `shared/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget()
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
            
            // Exportar API para Swift
            export(libs.kotlinx.coroutines.core)
            export(libs.kotlinx.datetime)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.koin.core)
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.sqldelight.android.driver)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
    }
}
```

#### 2. ViewModels para iOS (Observable en Swift)

Crear ViewModels que sean fáciles de consumir desde SwiftUI usando **Combine** o **async/await**:

**Kotlin (shared):**
```kotlin
// shared/src/commonMain/kotlin/viewmodel/TeamViewModel.kt
class TeamViewModel(
    private val getTeamsUseCase: GetTeamsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadTeams() {
        scope.launch {
            _isLoading.value = true
            try {
                _teams.value = getTeamsUseCase()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun dispose() {
        scope.cancel()
    }
}

// shared/src/iosMain/kotlin/viewmodel/TeamViewModelHelper.kt
@Suppress("unused") // Called from Swift
fun TeamViewModel.observeTeams(
    onCollect: (List<Team>) -> Unit
): Closeable {
    val job = teams.onEach { onCollect(it) }
        .launchIn(CoroutineScope(Dispatchers.Main))
    
    return object : Closeable {
        override fun close() {
            job.cancel()
        }
    }
}
```

**Swift (iosApp):**
```swift
// iosApp/ViewModels/TeamViewModelWrapper.swift
import shared
import Combine

@MainActor
class TeamViewModelWrapper: ObservableObject {
    private let viewModel: TeamViewModel
    private var cancellables = Set<AnyCancellable>()
    private var closeable: Closeable?
    
    @Published var teams: [Team] = []
    @Published var isLoading: Bool = false
    
    init(viewModel: TeamViewModel) {
        self.viewModel = viewModel
        
        // Observar teams
        closeable = viewModel.observeTeams { [weak self] teams in
            self?.teams = teams
        }
        
        // Observar loading state
        viewModel.observeIsLoading { [weak self] isLoading in
            self?.isLoading = isLoading.boolValue
        }
    }
    
    func loadTeams() {
        viewModel.loadTeams()
    }
    
    deinit {
        closeable?.close()
        viewModel.dispose()
    }
}
```

#### 3. UI en SwiftUI

**3.1. TeamListView.swift:**
```swift
import SwiftUI
import shared

struct TeamListView: View {
    @StateObject private var viewModel: TeamViewModelWrapper
    
    init() {
        let koinViewModel = KoinKt.getTeamViewModel()
        _viewModel = StateObject(wrappedValue: TeamViewModelWrapper(viewModel: koinViewModel))
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                if viewModel.isLoading {
                    ProgressView()
                } else {
                    List(viewModel.teams, id: \.id) { team in
                        NavigationLink(destination: TeamDetailView(team: team)) {
                            TeamRow(team: team)
                        }
                    }
                }
            }
            .navigationTitle("Teams")
            .onAppear {
                viewModel.loadTeams()
            }
        }
    }
}

struct TeamRow: View {
    let team: Team
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(team.name)
                    .font(.headline)
                Text(team.category)
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .padding(.vertical, 8)
    }
}
```

**3.2. TeamDetailView.swift:**
```swift
import SwiftUI
import shared

struct TeamDetailView: View {
    let team: Team
    @StateObject private var viewModel: TeamDetailViewModelWrapper
    
    init(team: Team) {
        self.team = team
        let koinViewModel = KoinKt.getTeamDetailViewModel(teamId: team.id)
        _viewModel = StateObject(wrappedValue: TeamDetailViewModelWrapper(viewModel: koinViewModel))
    }
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Team Header
                VStack(alignment: .leading) {
                    Text(team.name)
                        .font(.largeTitle)
                        .bold()
                    
                    Text(team.category)
                        .font(.title3)
                        .foregroundColor(.secondary)
                }
                .padding()
                
                // Players Section
                VStack(alignment: .leading, spacing: 12) {
                    Text("Players")
                        .font(.headline)
                        .padding(.horizontal)
                    
                    ForEach(viewModel.players, id: \.id) { player in
                        PlayerRow(player: player)
                            .padding(.horizontal)
                    }
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.loadPlayers()
        }
    }
}
```

#### 4. Integración con Koin desde Swift

**Kotlin (shared):**
```kotlin
// shared/src/iosMain/kotlin/KoinHelper.kt
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object KoinHelper : KoinComponent {
    fun getTeamViewModel(): TeamViewModel {
        val viewModel: TeamViewModel by inject()
        return viewModel
    }
    
    fun getTeamDetailViewModel(teamId: String): TeamDetailViewModel {
        val viewModel: TeamDetailViewModel by inject()
        viewModel.setTeamId(teamId)
        return viewModel
    }
}
```

**Swift:**
```swift
import shared

extension KoinKt {
    static func getTeamViewModel() -> TeamViewModel {
        return KoinHelper.shared.getTeamViewModel()
    }
    
    static func getTeamDetailViewModel(teamId: String) -> TeamDetailViewModel {
        return KoinHelper.shared.getTeamDetailViewModel(teamId: teamId)
    }
}
```

#### 5. Migración de Datos (Idéntica a Opción 1)

La migración de Room a SQLDelight y de Retrofit a Ktor es **idéntica a la Opción 1**.

#### 6. Módulo Android App

**Mantener la estructura actual:**
- Continuar usando Jetpack Compose
- Usar Android ViewModels con LiveData o StateFlow
- Dependencia en el módulo `shared` para lógica de negocio

```kotlin
// androidApp/src/main/java/com/jesuslcorominas/teamflowmanager/ui/team/TeamScreen.kt
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = koinViewModel() // Android ViewModel
) {
    val teams by viewModel.teams.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadTeams()
    }
    
    // ... UI code (mantener código existente)
}
```

### Ventajas de UI Nativa

✅ **Rendimiento óptimo** en cada plataforma
✅ **Look & Feel 100% nativo** en iOS
✅ **Acceso completo** a APIs específicas de plataforma
✅ **Mejor integración** con componentes nativos
✅ **Equipos especializados** pueden trabajar independientemente
✅ **Tamaño de app optimizado** en iOS
✅ **Debugging más familiar** para desarrolladores iOS
✅ **Sin dependencias en tecnologías beta** (Compose iOS)

### Desventajas de UI Nativa

❌ **Menos código compartido** (~60-70%)
❌ **Desarrollo más lento** - dos UIs para mantener
❌ **Mayor esfuerzo de testing** - tests en dos plataformas
❌ **Posibles inconsistencias** entre plataformas
❌ **Necesita equipos especializados** en cada plataforma
❌ **Duplicación de lógica de UI** en ambos lados
❌ **Mayor complejidad** en coordinación de features

### Desglose de Código Compartido

| Capa | % Compartido | Descripción |
|------|-------------|-------------|
| Domain | 100% | Modelos y entidades |
| UseCase | 100% | Lógica de negocio |
| Data (Core) | 100% | Interfaces de repositorio |
| Data (Local) | 95% | Room KMM - solo builders específicos |
| Data (Remote) | 100% | Ktorfit ya es multiplataforma |
| ViewModel | 100% | androidx.lifecycle.ViewModel KMM compatible |
| UI | 0% | Completamente separado |
| DI | 100% | Koin 4.0.0 ya es multiplataforma |
| **TOTAL** | **78-82%** | |

---

## Comparación de Opciones

### Comparación Técnica

| Aspecto | Compose Multiplatform | UI Nativa (SwiftUI) |
|---------|----------------------|---------------------|
| **Código compartido** | 96-99% | 78-82% |
| **Rendimiento iOS** | Bueno | Excelente |
| **Experiencia UX iOS** | Muy buena | Perfecta |
| **Velocidad desarrollo** | Rápida | Moderada |
| **Mantenimiento** | Más simple | Más complejo |
| **Curva de aprendizaje** | Moderada | Alta (dos stacks) |
| **Tamaño app iOS** | Mayor (~5-10MB más) | Optimizado |
| **Testing** | Más simple | Más complejo |
| **Acceso APIs nativas** | Via expect/actual | Directo |
| **Madurez tecnológica** | Beta en iOS | Madura |
| **Consistencia UI** | Perfecta | Requiere esfuerzo |
| **Debugging iOS** | Más complejo | Más simple |
| **Migración de datos** | Sin migración (Room KMM) | Sin migración (Room KMM) |
| **Migración de red** | Sin migración (Ktorfit) | Sin migración (Ktorfit) |
| **Migración de ViewModel** | Sin migración (ViewModel KMM) | Sin migración (ViewModel KMM) |
| **Migración de DI** | Sin migración (Koin 4.0.0 KMM) | Sin migración (Koin 4.0.0 KMM) |

### Comparación de Esfuerzo

| Tarea | Compose Multiplatform | UI Nativa |
|-------|----------------------|-----------|
| Configuración inicial | 2-3 días | 2-3 días |
| Adaptación Room a KMM | 2-3 días | 2-3 días |
| Adaptación Ktorfit (ya compatible) | 0-1 días | 0-1 días |
| Adaptación ViewModels (LiveData → StateFlow) | 1-2 días | 2-3 días |
| Reorganización Koin (ya compatible) | 0-1 días | 0-1 días |
| Migración/Adaptación UI | 6-8 días | 14-18 días |
| Configuración iOS | 2-3 días | 3-4 días |
| Testing e integración | 2-4 días | 6-10 días |
| Pulido y optimización | 1-2 días | 3-5 días |
| **TOTAL** | **2-3.5 semanas** | **5-7 semanas** |

### Comparación de Costos

| Concepto | Compose Multiplatform | UI Nativa |
|----------|----------------------|-----------|
| **Desarrollo inicial** | € | €€ |
| **Mantenimiento continuo** | € | €€ |
| **Equipo requerido** | 1-2 devs Kotlin | 2-3 devs (Android + iOS) |
| **Herramientas** | Estándar | Estándar + Mac |
| **Testing** | € | €€ |
| **Actualizaciones** | € | €€ |

---

## Estrategia de Migración Recomendada

### Recomendación: **Opción 1 - Compose Multiplatform**

#### Justificación:

1. **Maximiza ROI**: 90-95% de código compartido reduce costos a largo plazo
2. **Desarrollo más rápido**: Un solo codebase de UI acelera el desarrollo
3. **Reutilización**: El proyecto ya usa Jetpack Compose, minimizando la migración
4. **Equipo pequeño**: No requiere especialistas iOS separados
5. **Consistencia**: Garantiza UX idéntica en ambas plataformas
6. **Testing simplificado**: Tests compartidos reducen esfuerzo de QA
7. **Tendencia del mercado**: Compose Multiplatform está ganando adopción

#### Fases de Implementación

### Fase 1: Preparación y Configuración (Semana 1-2)

**Objetivos:**
- Configurar estructura de proyecto KMM
- Migrar configuración de dependencias
- Preparar entorno de desarrollo

**Tareas:**
1. ✅ Crear módulo `shared` con source sets (commonMain, androidMain, iosMain)
2. ✅ Actualizar `gradle/libs.versions.toml` con dependencias KMM
3. ✅ Configurar plugins de Kotlin Multiplatform y Compose Multiplatform
4. ✅ Configurar SQLDelight
5. ✅ Crear proyecto Xcode para iOS
6. ✅ Configurar integración de framework shared en Xcode
7. ✅ Validar compilación en ambas plataformas

**Entregables:**
- Proyecto KMM base funcional
- Compilación exitosa en Android e iOS
- Documentación de setup

### Fase 2: Migración de Capa de Dominio y Lógica (Semana 2-3)

**Objetivos:**
- Migrar domain models
- Migrar use cases
- Establecer interfaces de repositorio

**Tareas:**
1. ✅ Mover módulo `domain` a `shared/commonMain/domain`
2. ✅ Agregar `@Serializable` a modelos de datos
3. ✅ Mover módulo `usecase` a `shared/commonMain/usecase`
4. ✅ Mover interfaces de repositorio de `data:core` a `shared/commonMain/data/core`
5. ✅ Crear tests unitarios compartidos
6. ✅ Validar lógica de negocio

**Entregables:**
- Módulos domain, usecase y data:core en shared
- Tests pasando en commonTest
- Sin regresiones en lógica de negocio

### Fase 3: Adaptación de Capa de Datos a KMM (Semana 3)

**Objetivos:**
- Adaptar Room a Room Multiplatform
- Validar Ktorfit en configuración KMM
- Implementar repositorios compartidos

**Tareas:**

**3.1. Adaptación de Room a KMM:**
1. ✅ Actualizar plugin de Room a versión KMM (2.7.0+)
2. ✅ Crear DatabaseBuilder expect/actual
3. ✅ Implementar builders Android e iOS
4. ✅ Mover DAOs y Entities a commonMain
5. ✅ Testing de persistencia en ambas plataformas

**3.2. Validación Ktorfit:**
1. ✅ Verificar Ktorfit en shared module
2. ✅ Configurar KSP para todas las plataformas
3. ✅ Mover APIs a commonMain
4. ✅ Testing de API calls en ambas plataformas

**3.3. Repositorios:**
1. ✅ Mover repositorios a commonMain
2. ✅ Integrar LocalDataSource y RemoteDataSource
3. ✅ Testing de repositorios

**Entregables:**
- Room database funcional en Android e iOS
- Ktorfit API funcional en ambas plataformas
- Repositorios compartidos con tests

**Nota:** Esta fase es mucho más simple que la originalmente prevista porque Room y Ktorfit ya son compatibles con KMM, eliminando migraciones complejas.

### Fase 4: Adaptación de ViewModels y DI (Semana 3)

**Objetivos:**
- Adaptar ViewModels para KMM (ya compatibles desde lifecycle 2.8.0)
- Reemplazar LiveData con StateFlow
- Reorganizar DI con Koin (ya compatible, v4.0.0)

**Tareas:**
1. ✅ Mover ViewModels a commonMain (mantener herencia de ViewModel)
2. ✅ Reemplazar LiveData con StateFlow
3. ✅ Agregar lifecycle-viewmodel-compose a dependencias
4. ✅ Reorganizar módulos Koin entre commonMain y platform-specific
5. ✅ Configurar inyección de contexto para Android
6. ✅ Testing de ViewModels y DI

**Entregables:**
- ViewModels funcionando en ambas plataformas con la API familiar
- DI configurado correctamente con Koin
- Tests de ViewModels pasando

**Nota:** Esta fase es extremadamente simple porque tanto androidx.lifecycle.ViewModel como Koin 4.0.0 ya son totalmente compatibles con KMM. Solo se requiere reorganización de código, no reimplementación.

### Fase 5: Migración de UI (Semana 4-5)

**Objetivos:**
- Migrar UI de Jetpack Compose a Compose Multiplatform
- Implementar navegación multiplataforma
- Adaptar recursos y assets

**Tareas:**

**5.1. Configuración UI:**
1. ✅ Configurar Compose Multiplatform en shared
2. ✅ Migrar tema (TFMAppTheme) a commonMain
3. ✅ Migrar colores y typography
4. ✅ Configurar recursos compartidos

**5.2. Navegación:**
1. ✅ Implementar Voyager (o similar)
2. ✅ Migrar rutas de navegación
3. ✅ Crear Screens multiplataforma
4. ✅ Testing de navegación

**5.3. Pantallas y Componentes:**
1. ✅ Migrar componentes compartidos (buttons, cards, etc.)
2. ✅ Migrar pantallas principales:
   - TeamScreen
   - TeamDetailScreen
   - PlayerScreen
   - MatchScreen
   - etc.
3. ✅ Adaptar código específico de plataforma (expect/actual)
4. ✅ Testing de UI (Screenshot tests)

**Entregables:**
- UI completamente funcional en Android
- UI funcional en iOS
- Navegación working en ambas plataformas

### Fase 6: Aplicaciones Nativas y Testing (Semana 6)

**Objetivos:**
- Configurar aplicación Android
- Configurar aplicación iOS
- Integrar módulo shared
- Testing y optimización

**Tareas:**

**6.1. Android App:**
1. ✅ Simplificar módulo androidApp (solo shell)
2. ✅ Configurar MainActivity para usar shared UI
3. ✅ Configurar AndroidManifest
4. ✅ Testing en diferentes dispositivos

**6.2. iOS App:**
1. ✅ Crear MainViewController.kt en iosMain
2. ✅ Implementar ContentView.swift
3. ✅ Configurar Info.plist
4. ✅ Configurar permisos y capabilities
5. ✅ Testing en simuladores y dispositivos

**6.3. Testing y Pulido:**
1. ✅ Testing end-to-end en Android
2. ✅ Testing end-to-end en iOS
3. ✅ Optimización de rendimiento
4. ✅ Corrección de bugs
5. ✅ Actualizar documentación
6. ✅ Setup CI/CD para ambas plataformas

**Entregables:**
- APK Android funcional
- IPA iOS funcional
- Apps completamente funcionales y optimizadas
- Documentación actualizada
- Pipeline CI/CD configurado
- Apps listas para release

---

**Entregables:**
- Apps completamente funcionales y optimizadas
- Documentación actualizada
- Pipeline CI/CD configurado
- Apps listas para release

---

## Cronograma Estimado

### Opción 1: Compose Multiplatform

```
Semana 1:    ████████████████ Preparación y configuración
Semana 2:    ████████████████ Migración dominio, lógica y datos
Semana 3:    ████████████████ Adaptación ViewModels y DI
Semana 4-5:  ████████████████████████████████ Migración UI
Semana 6:    ████████████████ Apps nativas, testing y pulido
---------------------------------------------------------
TOTAL: 5-6 semanas (1.5 meses)
```

**Nota:** El tiempo se redujo significativamente porque:
- ✅ Room ya es compatible con KMM (no necesita migración a SQLDelight)
- ✅ Ktorfit ya está implementado (no necesita migración desde Retrofit)
- ✅ ViewModel ya es compatible con KMM (solo LiveData → StateFlow)
- ✅ Koin 4.0.0 ya es compatible con KMM (solo reorganización)
- ✅ kotlinx.serialization ya en uso (no necesita migración desde Gson)

### Opción 2: UI Nativa

```
Semana 1:    ████████████████ Preparación y configuración
Semana 2:    ████████████████ Migración dominio, lógica y datos
Semana 3:    ████████████████ Adaptación ViewModels y DI
Semana 4-5:  ████████████████████████████████ Mantener UI Android
Semana 6-9:  ████████████████████████████████████████████████████████ Desarrollar UI iOS
Semana 10:   ████████████████████████ Testing y pulido
---------------------------------------------------------
TOTAL: 9-10 semanas (2.5 meses)
```

---

## Riesgos y Mitigaciones

### Riesgos Técnicos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|-----------|
| **Compose iOS es Beta** | Alta | Alto | Validar con prototipos; plan B con UI nativa |
| **Rendimiento en iOS** | Media | Medio | Optimización temprana y profiling |
| **Problemas de interop Kotlin-Swift** | Media | Medio | Diseñar API clara; documentar patrones |
| **Incompatibilidades de bibliotecas** | Media | Alto | Evaluar alternativas KMM-ready antes |
| **Curva de aprendizaje del equipo** | Alta | Medio | Training y documentación extensa |
| **Bugs en SQLDelight** | Baja | Medio | Tests exhaustivos; community support |
| **Diferencias en navegación** | Media | Medio | Usar bibliotecas KMM probadas (Voyager) |
| **Problemas con recursos** | Media | Bajo | Usar Compose Resources oficial |

### Riesgos de Proyecto

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|-----------|
| **Subestimación de esfuerzo** | Alta | Alto | Buffer de 20-30% en estimaciones |
| **Falta de expertise iOS** | Media | Alto | Contratar consultor iOS para fase inicial |
| **Cambios en requirements** | Media | Medio | Desarrollo iterativo con demos frecuentes |
| **Falta de devices para testing** | Baja | Medio | Cloud testing (Firebase Test Lab, BrowserStack) |
| **Dependencia de JetBrains** | Media | Medio | Monitorear roadmap; relación con comunidad |

### Riesgos de Negocio

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|-----------|
| **Retraso en lanzamiento iOS** | Media | Alto | Release escalonado (Android first) |
| **Costos excedidos** | Media | Alto | Revisiones de presupuesto cada sprint |
| **Usuarios reportan bugs iOS** | Alta | Medio | Beta testing extensivo con usuarios reales |
| **Experiencia subóptima en iOS** | Media | Alto | User testing early; ajustes basados en feedback |

---

## Recomendaciones Adicionales

### Durante la Migración

1. **Desarrollo Incremental:**
   - Migrar feature por feature, no todo de una vez
   - Mantener ambas versiones funcionales durante transición
   - Usar feature flags para cambiar entre implementaciones

2. **Testing Continuo:**
   - Configurar CI/CD desde día 1
   - Tests automatizados en cada commit
   - Testing manual en dispositivos reales semanalmente

3. **Documentación:**
   - Documentar decisiones arquitectónicas (ADRs)
   - Mantener guía de estilo para código compartido
   - Documentar patrones expect/actual

4. **Equipo:**
   - Training en KMM para todo el equipo
   - Code reviews rigurosos
   - Pair programming en áreas críticas

### Post-Migración

1. **Monitoreo:**
   - Implementar analytics en ambas plataformas
   - Monitorear crashes y errores (Firebase Crashlytics)
   - Tracking de métricas de rendimiento

2. **Optimización Continua:**
   - Revisar tamaño de apps cada release
   - Profiling de performance regularmente
   - Actualizar dependencias frecuentemente

3. **Community:**
   - Contribuir a comunidad KMM
   - Compartir aprendizajes (blog posts, talks)
   - Mantenerse actualizado con roadmap de KMM

---

## Conclusión

La migración de TeamFlow Manager a **Kotlin Multiplatform Mobile** es **técnicamente viable y recomendada**. 

**Recomendación Final: Opción 1 - Compose Multiplatform**

### Por qué Compose Multiplatform:
- ✅ Maximiza la reutilización de código (90-95%)
- ✅ Desarrollo más rápido y económico
- ✅ Aprovecha la inversión actual en Jetpack Compose
- ✅ Garantiza consistencia entre plataformas
- ✅ Reduce complejidad de mantenimiento

### Siguiente Paso:
**Crear un MVP en iOS** con 2-3 pantallas clave para validar:
- Rendimiento en dispositivos iOS reales
- Experiencia de desarrollo
- Viabilidad técnica de Compose iOS

**Estimación:** 1-2 semanas

Si el MVP es exitoso, proceder con la migración completa según el plan de 10 semanas.

---

## Recursos Adicionales

### Documentación:
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Ktor](https://ktor.io/)
- [Koin Multiplatform](https://insert-koin.io/docs/reference/koin-mp/kmp)

### Tutoriales:
- [KMM Hands-On](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [Compose Multiplatform Tutorial](https://github.com/JetBrains/compose-multiplatform-template)

### Comunidad:
- [Kotlin Slack](https://kotlinlang.slack.com) - #multiplatform channel
- [KotlinConf talks](https://www.youtube.com/@Kotlin/playlists)

---

**Documento creado:** 2025-11-03  
**Versión:** 1.0  
**Autor:** Copilot Analysis Tool  
**Para:** US-7.1.1 - Análisis cambios KMM
