# Firebase Hosting + Cloud Functions Setup Guide
## Custom Short Links for Team Invitations

**Fecha:** Enero 2026  
**Estado:** ✅ Implementado y Listo para Desplegar

## 📋 Índice

1. [Introducción](#introducción)
2. [Arquitectura](#arquitectura)
3. [Prerequisitos](#prerequisitos)
4. [Paso 1: Instalar Firebase CLI](#paso-1-instalar-firebase-cli)
5. [Paso 2: Inicializar Firebase en el Proyecto](#paso-2-inicializar-firebase-en-el-proyecto)
6. [Paso 3: Desplegar Cloud Functions y Hosting](#paso-3-desplegar-cloud-functions-y-hosting)
7. [Paso 4: Configurar Android App](#paso-4-configurar-android-app)
8. [Paso 5: Probar la Funcionalidad](#paso-5-probar-la-funcionalidad)
9. [Troubleshooting](#troubleshooting)
10. [Mantenimiento](#mantenimiento)

---

## Introducción

Esta guía explica cómo configurar **custom short links** para las invitaciones de equipo en Team Flow Manager utilizando **Firebase Hosting + Cloud Functions**.

### ¿Por qué esta solución?

Firebase Dynamic Links está **deprecated** (dejará de funcionar en agosto 2025). Esta solución personalizada:

- ✅ **100% Firebase** - No requiere servidor externo
- ✅ **Completamente gratuito** - Tier gratuito de Firebase
- ✅ **URLs clicables** - Funcionan en WhatsApp, Email, SMS
- ✅ **Redirección automática** - A Play Store si la app no está instalada
- ✅ **Short Links** - URLs cortas y profesionales: `https://teamflowmanager.web.app/l/abc123`
- ✅ **Analytics** - Track de clicks en Firestore
- ✅ **Open Graph tags** - Vista previa enriquecida en redes sociales

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                  Team Flow Manager App                      │
│  (Genera invitación → Llama Cloud Function)                │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ HTTP POST /api/createShortLink
                     │ { teamId, teamName }
                     ▼
┌─────────────────────────────────────────────────────────────┐
│           Firebase Cloud Function (Node.js 18)              │
│  • Genera ID único (6 caracteres)                          │
│  • Guarda en Firestore: shortLinks/{id}                    │
│  • Retorna: https://teamflowmanager.web.app/l/{id}         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Almacena en Firestore
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Firestore Database                        │
│  Collection: shortLinks/{id}                                │
│  {                                                           │
│    teamId: "abc123",                                         │
│    teamName: "Juvenil Masculino",                           │
│    createdAt: timestamp,                                     │
│    clicks: 0                                                 │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘

┌──────────────── Usuario recibe link ─────────────────────────┐

Usuario hace clic: https://teamflowmanager.web.app/l/abc123
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              Firebase Hosting (Rewrite Rule)                │
│  /l/**  →  redirectShortLink Cloud Function                │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│          Cloud Function: redirectShortLink                  │
│  1. Lee datos de Firestore                                  │
│  2. Incrementa contador de clicks                           │
│  3. Sirve HTML con:                                          │
│     • Open Graph meta tags                                   │
│     • JavaScript que intenta abrir app                       │
│     • Fallback a Play Store tras 3 segundos                 │
└─────────────────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Usuario Final                             │
│  • Si tiene app: Abre app con deep link                     │
│  • Si no tiene app: Redirige a Play Store                   │
└─────────────────────────────────────────────────────────────┘
```

---

## Prerequisitos

Antes de empezar, asegúrate de tener:

- ✅ **Proyecto de Firebase** activo (ya lo tienes)
- ✅ **Node.js 18+** instalado en tu máquina
- ✅ **npm** o **yarn**
- ✅ **Cuenta de Google** con permisos de editor en Firebase Console

---

## Paso 1: Instalar Firebase CLI

### 1.1 Instalar Firebase Tools

Abre tu terminal y ejecuta:

```bash
npm install -g firebase-tools
```

### 1.2 Verificar Instalación

```bash
firebase --version
# Debería mostrar: 13.x.x o superior
```

### 1.3 Iniciar Sesión en Firebase

```bash
firebase login
```

Esto abrirá un navegador para autenticarte con tu cuenta de Google.

### 1.4 Seleccionar tu Proyecto

```bash
firebase projects:list
```

Anota el **Project ID** de Team Flow Manager (debería ser algo como `teamflowmanager` o similar).

---

## Paso 2: Inicializar Firebase en el Proyecto

### 2.1 Navegar al Directorio de Cloud Functions

```bash
cd /ruta/a/teamflowmanager/firebase-functions
```

El directorio ya está creado con los siguientes archivos:
- `index.js` - Cloud Functions
- `package.json` - Dependencias de Node.js
- `firebase.json` - Configuración de Hosting y Functions
- `public/index.html` - Página principal

### 2.2 Vincular el Proyecto de Firebase

```bash
firebase use --add
```

Selecciona tu proyecto de Firebase de la lista y asígnale un alias (por ejemplo: `default`).

### 2.3 Instalar Dependencias de Node.js

```bash
npm install
```

Esto instalará:
- `firebase-admin` - SDK de Firebase para Cloud Functions
- `firebase-functions` - Framework para Cloud Functions

---

## Paso 3: Desplegar Cloud Functions y Hosting

### 3.1 Desplegar por Primera Vez

Ejecuta el siguiente comando desde el directorio `firebase-functions`:

```bash
firebase deploy --only functions,hosting
```

**¿Qué hace este comando?**
1. Sube las Cloud Functions a Firebase
2. Despliega los archivos de Hosting
3. Configura las rewrites rules

### 3.2 Confirmar Despliegue Exitoso

Deberías ver un output similar a:

```
✔  Deploy complete!

Project Console: https://console.firebase.google.com/project/teamflowmanager/overview
Hosting URL: https://teamflowmanager.web.app

Functions:
  createShortLink(us-central1): https://us-central1-teamflowmanager.cloudfunctions.net/createShortLink
  redirectShortLink(us-central1): https://us-central1-teamflowmanager.cloudfunctions.net/redirectShortLink
```

### 3.3 Probar las URLs

Abre en tu navegador:

1. **Hosting URL**: `https://teamflowmanager.web.app`
   - Deberías ver la página de bienvenida de Team Flow Manager

2. **Crear Short Link** (desde terminal con curl):
```bash
curl -X POST https://teamflowmanager.web.app/api/createShortLink \
  -H "Content-Type: application/json" \
  -d '{"teamId":"test123","teamName":"Test Team"}'
```

Deberías recibir una respuesta como:
```json
{
  "shortLink": "https://teamflowmanager.web.app/l/abc123",
  "linkId": "abc123"
}
```

3. **Probar Redirección**: Abre el `shortLink` en tu navegador
   - Deberías ver una página con "Únete a Test Team"
   - La página intentará abrir la app
   - Después de 3 segundos, te redirigirá a Play Store

---

## Paso 4: Configurar Android App

### 4.1 Verificar Implementación Android

El código Android ya está implementado en:
- `data/remote/src/main/java/.../FirebaseDynamicLinkDataSourceImpl.kt`

Esta implementación:
- Hace HTTP POST a `/api/createShortLink`
- Recibe el short link
- Lo comparte via Intent de Android

### 4.2 Actualizar URL si es Necesaria

Si tu dominio de Firebase Hosting es diferente a `teamflowmanager.web.app`, actualiza la constante:

```kotlin
// En FirebaseDynamicLinkDataSourceImpl.kt
private const val CLOUD_FUNCTION_URL = "https://TU-DOMINIO.web.app/api/createShortLink"
```

**¿Cómo encontrar tu dominio?**
```bash
firebase hosting:sites:list
```

### 4.3 Verificar Deep Link Configuration

Asegúrate de que `AndroidManifest.xml` tiene el intent-filter para deep links:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="teamflowmanager"
        android:host="team"
        android:pathPrefix="/accept" />
</intent-filter>
```

Esto permite que la app capture links como: `teamflowmanager://team/accept?teamId=XXX`

---

## Paso 5: Probar la Funcionalidad

### 5.1 Compilar la App Android

```bash
./gradlew assembleProdDebug
```

### 5.2 Instalar en Dispositivo/Emulador

```bash
./gradlew installProdDebug
```

### 5.3 Probar Flujo Completo

1. **Como Presidente:**
   - Inicia sesión como Presidente
   - Ve a la pestaña "Equipos"
   - Encuentra un equipo sin coach (debe mostrar botón de compartir)
   - Haz clic en el botón compartir (icono compartir)
   - Selecciona WhatsApp o Email

2. **El enlace generado debería ser:**
   ```
   https://teamflowmanager.web.app/l/abc123
   ```

3. **Como Coach (receptor):**
   - Abre el enlace en otro dispositivo/emulador
   - Si la app está instalada: Se abre directamente la pantalla de aceptación
   - Si no está instalada: Redirige a Play Store

4. **Verificar en Firestore:**
   - Ve a Firebase Console → Firestore Database
   - Busca la colección `shortLinks`
   - Deberías ver documentos con:
     ```
     {
       teamId: "...",
       teamName: "...",
       createdAt: timestamp,
       clicks: 1  // incrementa con cada clic
     }
     ```

---

## Troubleshooting

### Problema 1: Error 404 al Acceder al Link

**Síntoma:** Al abrir `https://teamflowmanager.web.app/l/abc123` aparece "404 Not Found"

**Solución:**
1. Verifica que el despliegue fue exitoso:
   ```bash
   firebase deploy --only hosting,functions
   ```

2. Verifica las rewrites en `firebase.json`:
   ```json
   "rewrites": [
     {
       "source": "/l/**",
       "function": "redirectShortLink"
     }
   ]
   ```

3. Espera 2-3 minutos después del despliegue (propagación DNS)

### Problema 2: Cloud Function Timeout

**Síntoma:** La app tarda mucho y luego usa el fallback scheme

**Solución:**
1. Verifica que las Cloud Functions están desplegadas:
   ```bash
   firebase functions:list
   ```

2. Revisa los logs:
   ```bash
   firebase functions:log
   ```

3. Aumenta el timeout en `index.js` si es necesario:
   ```javascript
   exports.createShortLink = functions
     .runWith({ timeoutSeconds: 30 })
     .https.onRequest(async (req, res) => { ... });
   ```

### Problema 3: El Link No Se Abre en la App

**Síntoma:** El link abre el navegador pero no la app

**Solución:**
1. Verifica que el intent-filter está en `AndroidManifest.xml`
2. Verifica que el esquema `teamflowmanager://` es correcto
3. Prueba manualmente:
   ```bash
   adb shell am start -W -a android.intent.action.VIEW \
     -d "teamflowmanager://team/accept?teamId=test123"
   ```

### Problema 4: CORS Error en la App

**Síntoma:** Error de CORS al llamar a la Cloud Function

**Solución:**
Ya está configurado en `index.js`:
```javascript
res.set('Access-Control-Allow-Origin', '*');
```

Si sigue fallando, actualiza a origen específico:
```javascript
res.set('Access-Control-Allow-Origin', 'https://teamflowmanager.web.app');
```

---

## Mantenimiento

### Actualizar Cloud Functions

Si necesitas actualizar el código de las Cloud Functions:

1. Edita `firebase-functions/index.js`
2. Despliega cambios:
   ```bash
   firebase deploy --only functions
   ```

### Ver Logs en Tiempo Real

```bash
firebase functions:log --only createShortLink,redirectShortLink
```

### Limpiar Links Antiguos

Crea una Cloud Function scheduled para limpiar links viejos:

```javascript
// En index.js
exports.cleanOldLinks = functions.pubsub
  .schedule('every 24 hours')
  .onRun(async (context) => {
    const cutoff = admin.firestore.Timestamp.fromDate(
      new Date(Date.now() - 90 * 24 * 60 * 60 * 1000) // 90 días
    );
    
    const snapshot = await admin.firestore()
      .collection('shortLinks')
      .where('createdAt', '<', cutoff)
      .get();
    
    const batch = admin.firestore().batch();
    snapshot.docs.forEach(doc => batch.delete(doc.ref));
    await batch.commit();
  });
```

### Monitorear Uso

1. **Firebase Console → Functions → Dashboard**
   - Invocaciones por día
   - Errores
   - Latencia

2. **Firestore Console**
   - Colección `shortLinks`
   - Total de documentos
   - Storage usado

### Costos Esperados

Con el **tier gratuito de Firebase**:

- ✅ **Cloud Functions**: 2M invocaciones/mes gratis
- ✅ **Hosting**: 10 GB transferencia/mes gratis
- ✅ **Firestore**: 50K lecturas/día gratis

Para una app con **1000 usuarios activos**:
- ~500 invitaciones/mes = Bajo costo ($0-5/mes)
- Hosting: Mínimo (solo redirects)
- **Total estimado: GRATIS** (dentro del tier gratuito)

---

## Resumen de Comandos

```bash
# Instalación inicial
npm install -g firebase-tools
firebase login
cd firebase-functions
firebase use --add
npm install

# Despliegue
firebase deploy --only functions,hosting

# Desarrollo local (opcional)
firebase emulators:start --only functions,hosting

# Logs
firebase functions:log

# Ver proyectos
firebase projects:list

# Cambiar proyecto
firebase use <project-id>
```

---

## Próximos Pasos

1. ✅ **Desplegar a producción** siguiendo esta guía
2. ⏳ **Configurar dominio personalizado** (opcional):
   - Firebase Console → Hosting → Add custom domain
   - Ejemplo: `https://invite.teamflowmanager.com`
3. ⏳ **Añadir analytics avanzados** (opcional):
   - Track de conversiones
   - A/B testing de mensajes
4. ⏳ **Implementar caché** (opcional):
   - Firebase Hosting CDN (automático)
   - Cache headers en Cloud Functions

---

## Soporte

Si encuentras problemas:

1. **Revisa los logs**:
   ```bash
   firebase functions:log --only createShortLink,redirectShortLink
   ```

2. **Verifica el estado de Firebase**:
   https://status.firebase.google.com/

3. **Documentación oficial**:
   - [Cloud Functions](https://firebase.google.com/docs/functions)
   - [Firebase Hosting](https://firebase.google.com/docs/hosting)

---

**✅ Guía completa - Actualizada: Enero 2026**
