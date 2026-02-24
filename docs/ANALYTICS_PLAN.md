# Plan de Analítica - TeamFlow Manager

## 📊 Objetivos

Implementar analítica en la aplicación para:
1. Entender el uso de la aplicación por parte de los usuarios
2. Detectar y registrar crashes automáticamente
3. Mejorar la experiencia de usuario basándose en datos
4. Mantener compatibilidad con futura migración a KMM

## 🔧 Tecnología

### Firebase Analytics + Crashlytics
**Selección**: Firebase Analytics y Firebase Crashlytics

**Ventajas**:
- ✅ Integración nativa en Android
- ✅ Crashlytics para detección automática de crashes
- ✅ Console web gratuita para visualización
- ✅ No requiere backend propio
- ✅ Soporte para eventos personalizados

**Compatibilidad KMM**:
- ⚠️ Firebase es específico de Android/iOS (no multiplataforma nativo)
- ✅ Solución: Capa de abstracción con interfaces
- ✅ Permite migración futura a soluciones KMP (ej: analytics multiplataforma)

## 📱 Eventos a Registrar

### 1. Gestión de Equipos
| Evento | Parámetros | Descripción |
|--------|-----------|-------------|
| `team_created` | `team_name`, `team_category` | Usuario crea un nuevo equipo |
| `team_updated` | `team_id` | Usuario edita información de equipo |
| `team_deleted` | `team_id` | Usuario elimina un equipo |
| `team_viewed` | `team_id` | Usuario visualiza detalle de equipo |

### 2. Gestión de Jugadores
| Evento | Parámetros | Descripción |
|--------|-----------|-------------|
| `player_created` | `team_id`, `player_position` | Usuario añade jugador |
| `player_updated` | `player_id`, `team_id` | Usuario edita jugador |
| `player_deleted` | `player_id`, `team_id` | Usuario elimina jugador |
| `player_photo_added` | `player_id` | Usuario añade foto a jugador |

### 3. Gestión de Partidos
| Evento | Parámetros | Descripción |
|--------|-----------|-------------|
| `match_created` | `team_id`, `match_type` | Usuario crea partido |
| `match_started` | `match_id`, `team_id` | Usuario inicia partido |
| `match_paused` | `match_id` | Usuario pausa partido |
| `match_resumed` | `match_id` | Usuario reanuda partido |
| `match_finished` | `match_id`, `duration_minutes` | Usuario finaliza partido |
| `match_archived` | `match_id` | Usuario archiva partido |
| `match_viewed` | `match_id` | Usuario ve detalle de partido |

### 4. Acciones Durante Partido
| Evento | Parámetros | Descripción |
|--------|-----------|-------------|
| `substitution_made` | `match_id`, `player_in`, `player_out` | Cambio de jugador |
| `goal_scored` | `match_id`, `player_id`, `team_type` | Gol marcado |
| `card_issued` | `match_id`, `player_id`, `card_type` | Tarjeta (amarilla/roja) |
| `starting_lineup_set` | `match_id`, `formation` | Alineación inicial definida |
| `captain_selected` | `match_id`, `player_id` | Capitán seleccionado |

### 5. Navegación y UI
| Evento | Parámetros | Descripción |
|--------|-----------|-------------|
| `screen_view` | `screen_name`, `screen_class` | Usuario navega a pantalla |
| `wizard_step_completed` | `wizard_type`, `step_number` | Usuario completa paso de wizard |
| `wizard_cancelled` | `wizard_type`, `step_number` | Usuario cancela wizard |

### 6. Estadísticas y Gráficas
| Evento | Parámetros | Descripción |
|--------|-----------|-------------|
| `stats_viewed` | `stats_type`, `player_id` | Usuario ve estadísticas |
| `chart_viewed` | `chart_type`, `time_range` | Usuario ve gráfica |

## 🚨 Crash Reporting

Firebase Crashlytics registrará automáticamente:
- ✅ Crashes no capturados
- ✅ Stack traces completas
- ✅ Información del dispositivo
- ✅ Versión de la app
- ✅ Logs personalizados opcionales

### Logs Personalizados Sugeridos
```kotlin
// Antes de operaciones críticas
crashlytics.log("Iniciando creación de partido: teamId=$teamId")

// En flujos importantes
crashlytics.setCustomKey("current_match_id", matchId)
crashlytics.setCustomKey("user_teams_count", teamsCount)
```

## 🏗️ Arquitectura de Implementación

### Capa de Abstracción (KMM-Ready)

```
┌─────────────────────────────────────────────┐
│         domain Module (Interface)           │
│                                              │
│  interface AnalyticsTracker {               │
│    fun logEvent(name, params)               │
│    fun logScreenView(name)                  │
│    fun setUserId(id)                        │
│    fun setUserProperty(key, value)          │
│  }                                           │
│                                              │
│  interface CrashReporter {                  │
│    fun recordException(throwable)           │
│    fun log(message)                         │
│    fun setCustomKey(key, value)             │
│  }                                           │
└─────────────────────────────────────────────┘
                      ▲
                      │
┌─────────────────────────────────────────────┐
│       app Module (Implementation)           │
│                                              │
│  class FirebaseAnalyticsTracker :           │
│        AnalyticsTracker                     │
│                                              │
│  class FirebaseCrashReporter :              │
│        CrashReporter                        │
└─────────────────────────────────────────────┘
```

### Beneficios de esta Arquitectura
1. ✅ **KMM Compatible**: Interfaces en módulo puro Kotlin
2. ✅ **Testeable**: Fácil crear mocks para testing
3. ✅ **Flexible**: Cambiar implementación sin afectar lógica de negocio
4. ✅ **Migración Futura**: En iOS, implementar con Firebase iOS SDK
5. ✅ **Desacoplado**: ViewModels no dependen de Firebase directamente

## 📦 Dependencias Requeridas

### build.gradle.kts (Proyecto)
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
    }
}
```

### build.gradle.kts (app)
```kotlin
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    // Firebase BOM (Bill of Materials) para gestión de versiones
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics-ktx")
}
```

## 🔑 Configuración de Firebase

### Pasos para Configuración (Post-Implementación)

1. **Crear Proyecto en Firebase Console**
   - Ir a https://console.firebase.google.com/
   - Crear nuevo proyecto "TeamFlow Manager"
   - Seleccionar región apropiada

2. **Añadir App Android**
   - Package name: `com.jesuslcorominas.teamflowmanager`
   - Descargar `google-services.json`
   - Colocar en `app/` directorio

3. **Activar Analytics**
   - Activado automáticamente al crear proyecto
   - Configurar propiedades del proyecto

4. **Activar Crashlytics**
   - En Firebase Console → Crashlytics
   - Seguir wizard de configuración
   - Forzar crash de prueba para verificar

5. **Configurar Data Collection (GDPR)**
   - Revisar políticas de privacidad
   - Configurar retención de datos
   - Considerar consentimiento de usuario si aplica

## 📝 Archivos a Crear/Modificar

### Nuevos Archivos
1. `domain/src/main/java/.../domain/analytics/AnalyticsTracker.kt`
2. `domain/src/main/java/.../domain/analytics/CrashReporter.kt`
3. `app/src/main/java/.../analytics/FirebaseAnalyticsTracker.kt`
4. `app/src/main/java/.../analytics/FirebaseCrashReporter.kt`
5. `di/src/main/java/.../di/AnalyticsModule.kt`
6. `app/google-services.json` (manual, post-implementación)

### Archivos a Modificar
1. `gradle/libs.versions.toml` - añadir versiones Firebase
2. `build.gradle.kts` (proyecto) - añadir classpath plugins
3. `app/build.gradle.kts` - añadir plugins y dependencias
4. ViewModels relevantes - inyectar y usar analytics
5. `TeamFlowManagerApplication.kt` - inicializar Crashlytics

## ✅ Criterios de Aceptación

- [x] Documento de plan de analítica creado
- [ ] Interfaces de analytics definidas en módulo domain
- [ ] Implementaciones Firebase en módulo app
- [ ] Módulo Koin para inyección de dependencias
- [ ] Analytics integrado en eventos clave de la aplicación
- [ ] Crashlytics configurado y testeado
- [ ] Documentación de pasos de configuración Firebase
- [ ] Código revisado y sin vulnerabilidades
- [ ] Build exitoso con nuevas dependencias

## 🔒 Consideraciones de Privacidad

### GDPR/Privacidad
- Firebase Analytics recopila datos de forma anónima por defecto
- No se recopilan datos personales identificables (PII)
- Los IDs de usuarios son opcionales y anónimos
- Considerar añadir consentimiento si se amplía recopilación de datos

### Datos Recopilados
- ✅ Eventos de interacción con la app (anónimos)
- ✅ Crashes y errores (sin datos personales)
- ✅ Tipo de dispositivo y versión de OS
- ❌ NO se recopilan: nombres, emails, datos de equipos reales

## 🚀 Implementación Futura (iOS/KMP)

Cuando se migre a KMP:

1. **Crear Módulo Shared Analytics**
   ```
   shared/
     commonMain/
       analytics/
         AnalyticsTracker.kt (expect)
     androidMain/
       analytics/
         AnalyticsTracker.kt (actual - Firebase)
     iosMain/
       analytics/
         AnalyticsTracker.kt (actual - Firebase iOS)
   ```

2. **Alternativas Multiplataforma Futuras**
   - [Count.ly](https://count.ly/) - KMP ready
   - [Plausible Analytics](https://plausible.io/) - Privacy-focused
   - Soluciones custom con backend propio

## 📚 Referencias

- [Firebase Analytics - Android](https://firebase.google.com/docs/analytics/get-started?platform=android)
- [Firebase Crashlytics - Android](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [Firebase BOM](https://firebase.google.com/docs/android/learn-more#bom)
- [Analytics Events Best Practices](https://firebase.google.com/docs/analytics/events)
- [KMM Analytics Patterns](https://kotlinlang.org/docs/multiplatform-mobile-samples.html)

---

**Versión**: 1.0  
**Fecha**: 2025-11-07  
**Estado**: Plan Aprobado
