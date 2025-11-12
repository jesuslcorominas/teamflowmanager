# US-7.1.8 - Pasos Finales de Configuración

## ✅ Implementación Completada

La infraestructura de analítica ha sido completamente implementada. Todo el código está listo y probado.

## ⚠️ Acción Requerida - Configuración de Firebase Console

Para activar la analítica, necesitas completar estos pasos en Firebase Console:

### 1. Crear Proyecto en Firebase (5 minutos)

1. Ve a https://console.firebase.google.com/
2. Haz clic en "Agregar proyecto"
3. Nombre: "TeamFlow Manager" (o el que prefieras)
4. Acepta términos y condiciones
5. (Opcional) Activa Google Analytics
6. Selecciona región: España

### 2. Agregar App Android (5 minutos)

1. En el proyecto de Firebase, haz clic en el icono de Android
2. **Nombre del paquete**: `com.jesuslcorominas.teamflowmanager` (¡IMPORTANTE!)
3. Alias: "TeamFlow Manager"
4. Haz clic en "Registrar app"

### 3. Descargar Configuración (1 minuto)

1. Descarga el archivo `google-services.json`
2. Reemplaza el archivo en `app/google-services.json` con el descargado
3. **NO** lo subas a git si contiene datos sensibles (opcional)

### 4. Activar Servicios (2 minutos)

1. **Analytics**: Se activa automáticamente
2. **Crashlytics**: 
   - Ve a "Crashlytics" en el menú
   - Haz clic en "Activar Crashlytics"
   - Sigue el asistente

### 5. Verificar Integración (10 minutos)

Después de compilar y ejecutar la app:

```bash
# Activar modo debug
adb shell setprop debug.firebase.analytics.app com.jesuslcorominas.teamflowmanager

# Ver eventos en tiempo real en Firebase Console → Analytics → DebugView
```

**Probar Crashlytics (opcional):**

Añade temporalmente en `MainActivity.onCreate()`:
```kotlin
throw RuntimeException("Test Crash")
```

Ejecuta la app, crasheará, espera 5-10 minutos y revisa Crashlytics.
¡No olvides eliminar este código después!

```bash
# Desactivar modo debug
adb shell setprop debug.firebase.analytics.app .none.
```

## 📚 Documentación Completa

Para más detalles, consulta:

- **ANALYTICS_PLAN.md**: Plan completo de analítica con todos los eventos
- **FIREBASE_SETUP.md**: Guía detallada paso a paso
- **US-7.1.8_IMPLEMENTATION_SUMMARY.md**: Resumen técnico de la implementación

## 🎯 Eventos Implementados

La app ya está configurada para rastrear:

### Equipos
- Creación, edición, eliminación y visualización de equipos

### Jugadores
- Creación, edición, eliminación y añadir fotos

### Partidos
- Creación, inicio, pausa, reanudación, finalización y archivo
- Cambios, goles, tarjetas, alineaciones y capitán

### Navegación
- Vista de pantallas automática (ejemplo en TeamScreen)
- Pasos de wizards

### Estadísticas
- Visualización de estadísticas y gráficas

## 🔧 Para Desarrolladores

### Añadir tracking a nuevas pantallas

```kotlin
@Composable
fun MiPantalla() {
    TrackScreenView(screenName = ScreenName.MI_PANTALLA, screenClass = "MiPantalla")
    // ... resto del contenido
}
```

### Añadir eventos en ViewModels

```kotlin
class MiViewModel(
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {
    
    fun onAccion() {
        // ... lógica de negocio
        
        analyticsTracker.logEvent(
            AnalyticsEvent.MI_EVENTO,
            mapOf(
                AnalyticsParam.MI_PARAMETRO to valor
            )
        )
    }
}
```

## 🔐 Privacidad

- Los datos son anónimos por defecto
- No se recopilan datos personales
- Firebase Analytics cumple con GDPR
- Los eventos son agregados (no rastreables a usuarios específicos)

## ❓ Problemas Comunes

**"google-services.json not found"**
→ Descarga el archivo real de Firebase Console

**"Default FirebaseApp is not initialized"**
→ Verifica que google-services.json está en `/app/` y haz Clean & Rebuild

**No aparecen eventos**
→ Activa DebugView y espera hasta 24h para producción

**Crashlytics no reporta**
→ Espera 5-10 minutos después del crash

## 📞 Soporte

Para más información, revisa la documentación completa en:
- FIREBASE_SETUP.md (guía detallada)
- ANALYTICS_PLAN.md (plan de analítica)

---

**¡La implementación está lista! Solo falta la configuración de Firebase Console.**
