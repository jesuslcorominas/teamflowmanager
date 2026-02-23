# Configuración de Firebase - TeamFlow Manager

## 📋 Resumen

Este documento detalla los pasos para configurar Firebase Analytics y Crashlytics en el proyecto TeamFlow Manager.

## ⚠️ Estado Actual

- ✅ Código de integración implementado
- ✅ Dependencias de Firebase añadidas
- ✅ Capa de abstracción KMM-ready creada
- ⚠️ **PENDIENTE**: Configuración de proyecto en Firebase Console
- ⚠️ **PENDIENTE**: Archivo `google-services.json` real

## 🔧 Pasos Pendientes de Configuración

### 1. Crear Proyecto en Firebase Console

1. **Acceder a Firebase Console**
   - URL: https://console.firebase.google.com/
   - Iniciar sesión con cuenta de Google

2. **Crear Nuevo Proyecto**
   - Clic en "Agregar proyecto" o "Add project"
   - Nombre del proyecto: `TeamFlow Manager` (o el que prefieras)
   - Aceptar términos y condiciones
   - (Opcional) Activar Google Analytics para el proyecto
   - Seleccionar ubicación: España o región correspondiente

3. **Configurar Google Analytics** (si se activó)
   - Crear nueva cuenta de Analytics o usar existente
   - Aceptar términos del servicio de Analytics

### 2. Añadir App Android al Proyecto

1. **En el proyecto de Firebase:**
   - Clic en el icono de Android para añadir app
   - O ir a "Configuración del proyecto" → "Tus apps" → "Añadir app" → Android

2. **Completar información de la app:**
   - **Nombre del paquete de Android** (OBLIGATORIO): `com.jesuslcorominas.teamflowmanager`
   - **Alias de la app** (Opcional): `TeamFlow Manager`
   - **Certificado de firma SHA-1** (Opcional): 
     - Obtener con: `./gradlew signingReport`
     - Copiar SHA-1 del certificado debug o release

3. **Descargar archivo de configuración:**
   - Descargar el archivo `google-services.json`
   - Colocar en: `/app/google-services.json`
   - ⚠️ **IMPORTANTE**: Reemplazar el archivo placeholder actual

4. **Verificar en Firebase Console:**
   - Firebase mostrará que ha detectado la app
   - Continuar al siguiente paso

### 3. Activar Firebase Analytics

Firebase Analytics se activa automáticamente al crear el proyecto. Para verificar:

1. **En Firebase Console:**
   - Ir a "Analytics" → "Panel de control" (Dashboard)
   - Debería aparecer mensaje de espera de datos

2. **Configurar propiedades** (opcional):
   - Ir a "Analytics" → "Configuración"
   - Ajustar período de retención de datos
   - Configurar zona horaria

### 4. Activar Firebase Crashlytics

1. **En Firebase Console:**
   - Ir a "Crashlytics" en el menú lateral
   - Clic en "Activar Crashlytics"

2. **Configuración inicial:**
   - Seguir el asistente de configuración
   - Firebase detectará automáticamente la configuración del plugin

3. **Forzar crash de prueba** (después de desplegar):
   ```kotlin
   // Añadir temporalmente en MainActivity.onCreate()
   throw RuntimeException("Test Crash para verificar Crashlytics")
   ```
   - Ejecutar la app
   - Debería crashear inmediatamente
   - Esperar 5-10 minutos
   - Revisar en Firebase Console → Crashlytics
   - ⚠️ **Eliminar el código de prueba después**

### 5. Configuración de Privacidad y GDPR

1. **Revisión de Políticas:**
   - Ir a "Configuración del proyecto" → "Privacidad"
   - Revisar configuración de recopilación de datos
   - Ajustar según requisitos legales

2. **Retención de Datos:**
   - Por defecto: 14 meses
   - Ajustar según necesidades

3. **Consideraciones GDPR:**
   - Firebase Analytics es anónimo por defecto
   - No se recopilan datos personales identificables
   - Los eventos son agregados y anónimos

### 6. Configuración de Usuarios y Permisos

1. **Gestión de Acceso:**
   - Ir a "Configuración del proyecto" → "Usuarios y permisos"
   - Añadir colaboradores si es necesario
   - Asignar roles apropiados

### 7. Integración con BigQuery (Opcional - Avanzado)

Si necesitas análisis más profundos:

1. **Vincular a BigQuery:**
   - Ir a "Configuración del proyecto" → "Integraciones"
   - Clic en "BigQuery" → "Vincular"
   - Seguir el asistente

2. **Beneficios:**
   - Consultas SQL personalizadas
   - Análisis de datos sin procesar
   - Exportación de datos

## 📱 Verificación de Integración

### 1. Verificar que la App Envía Eventos

1. **Habilitar modo de depuración:**
   ```bash
   adb shell setprop debug.firebase.analytics.app com.jesuslcorominas.teamflowmanager
   ```

2. **Ver eventos en tiempo real:**
   - En Firebase Console → Analytics → DebugView
   - Ejecutar la app
   - Navegar por diferentes pantallas
   - Ver eventos aparecer en tiempo real

3. **Desactivar modo de depuración:**
   ```bash
   adb shell setprop debug.firebase.analytics.app .none.
   ```

### 2. Verificar Crashlytics

1. **Forzar crash de prueba** (código mencionado arriba)
2. **Esperar 5-10 minutos**
3. **Verificar en Firebase Console:**
   - Ir a Crashlytics
   - Debería aparecer el crash de prueba
   - Ver stack trace completa

### 3. Eventos Implementados

Los siguientes eventos ya están implementados en el código:

#### Gestión de Equipos
- `team_created`, `team_updated`, `team_deleted`, `team_viewed`

#### Gestión de Jugadores
- `player_created`, `player_updated`, `player_deleted`, `player_photo_added`

#### Gestión de Partidos
- `match_created`, `match_started`, `match_paused`, `match_resumed`
- `match_finished`, `match_archived`, `match_viewed`

#### Acciones de Partido
- `substitution_made`, `goal_scored`, `card_issued`
- `starting_lineup_set`, `captain_selected`

#### Navegación
- `screen_view`, `wizard_step_completed`, `wizard_cancelled`

#### Estadísticas
- `stats_viewed`, `chart_viewed`

**Nota**: Los eventos se registrarán automáticamente cuando se integren en los ViewModels (pendiente en próximos commits).

## 🔐 Seguridad

### Archivo google-services.json

- ⚠️ **Contiene claves de API**: No es altamente sensible, pero es buena práctica protegerlo
- ✅ **Puede incluirse en git**: Muchos proyectos lo incluyen
- ⚠️ **Mejor práctica**: Añadir a `.gitignore` si prefieres mayor seguridad

Si decides excluirlo de git:

```bash
# Añadir a .gitignore
echo "app/google-services.json" >> .gitignore
```

### Restricciones de API

1. **En Google Cloud Console:**
   - Ir a https://console.cloud.google.com/
   - Seleccionar el proyecto de Firebase
   - Ir a "APIs y servicios" → "Credenciales"
   - Restringir API keys a paquete de la app
   - Configurar restricciones de aplicación Android

## 📊 Usando Firebase Console

### Panel de Analytics

1. **Dashboard Principal:**
   - Vista general de usuarios activos
   - Eventos más comunes
   - Retención de usuarios

2. **Eventos:**
   - Ver todos los eventos registrados
   - Parámetros de cada evento
   - Volumen de eventos por tiempo

3. **Audiencias:**
   - Crear segmentos de usuarios
   - Basados en comportamiento o propiedades

### Panel de Crashlytics

1. **Crashes:**
   - Lista de crashes ordenados por impacto
   - Usuarios afectados
   - Versiones de app afectadas

2. **Detalle de Crash:**
   - Stack trace completa
   - Información del dispositivo
   - Logs personalizados
   - Claves personalizadas

## 🔄 Próximos Pasos

Después de configurar Firebase:

1. ✅ Verificar que `google-services.json` está en su lugar
2. ✅ Compilar y ejecutar la app
3. ✅ Verificar en DebugView que se reciben eventos
4. ✅ Forzar crash de prueba y verificar en Crashlytics
5. ⏭️ Integrar analytics en ViewModels (siguiente fase)
6. ⏭️ Añadir tracking específico para flujos críticos
7. ⏭️ Configurar alertas para crashes críticos

## 📚 Referencias

- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firebase Analytics Events](https://firebase.google.com/docs/analytics/events)
- [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics)
- [DebugView](https://firebase.google.com/docs/analytics/debugview)
- [GDPR Compliance](https://firebase.google.com/support/privacy)

## 🆘 Troubleshooting

### Error: "google-services.json not found"

**Solución**: Descargar el archivo real de Firebase Console y reemplazar el placeholder.

### Error: "Default FirebaseApp is not initialized"

**Solución**: 
1. Verificar que google-services.json está en `/app/`
2. Verificar que el plugin está aplicado en app/build.gradle.kts
3. Hacer Clean & Rebuild

### No aparecen eventos en Analytics

**Solución**:
1. Activar DebugView (comando adb arriba)
2. Esperar hasta 24h para datos en producción
3. Verificar que el dispositivo tiene conexión a internet

### Crashlytics no reporta crashes

**Solución**:
1. Verificar que Crashlytics está activado en Console
2. Esperar 5-10 minutos después del crash
3. Verificar que la app tiene permisos de internet
4. Revisar ProGuard rules si es build release

---

**Versión**: 1.0  
**Fecha**: 2025-11-07  
**Estado**: Configuración Pendiente
