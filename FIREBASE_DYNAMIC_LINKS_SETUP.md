# Firebase Dynamic Links - Guía de Configuración

## ¿Qué es Firebase Dynamic Links?

Firebase Dynamic Links son URLs inteligentes que funcionan en cualquier plataforma:
- **URLs cortas y clicables**: `https://teamflowmanager.page.link/xxxxx`
- **Funciona en WhatsApp, Email, SMS**: Los enlaces son reconocidos como URLs y son clicables
- **Redirección automática al Play Store**: Si la app no está instalada
- **Apertura automática de la app**: Tras la instalación, abre el deep link
- **Sin servidor necesario**: Firebase maneja todo

## Ventajas sobre esquemas personalizados

| Característica | `teamflowmanager://` | Firebase Dynamic Links |
|---|---|---|
| Clicable en WhatsApp/Email | ❌ No | ✅ Sí |
| Redirección a Play Store | ❌ No | ✅ Automática |
| URLs cortas | ❌ No | ✅ Sí |
| Funciona sin app instalada | ❌ No | ✅ Sí |
| Requiere configuración | ❌ No | ✅ Sí (una vez) |

## Pasos de Configuración en Firebase Console

### 1. Acceder a Firebase Dynamic Links

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto **Team Flow Manager**
3. En el menú lateral izquierdo, busca **Engagement** → **Dynamic Links**
4. Si es la primera vez, haz clic en **Get started**

### 2. Crear un Dominio de Dynamic Links

1. En la pantalla de Dynamic Links, haz clic en **Add URL prefix**
2. Te ofrecerán un dominio gratuito de Firebase: **`teamflowmanager.page.link`**
   - Este dominio ya está configurado en el código
   - Es completamente gratuito
   - No necesitas configurar DNS ni servidor
3. Acepta los términos y haz clic en **Finish**

> **Nota**: Si ya tienes un dominio propio (ej: `teamflowmanager.com`), puedes usarlo, pero requiere configuración DNS adicional. Para empezar, usa `teamflowmanager.page.link`.

### 3. Configurar la Aplicación Android

1. Mientras estás en la sección de Dynamic Links, haz clic en el ícono de **Settings** (⚙️)
2. Ve a la pestaña **Android app information**
3. Verifica que la información sea correcta:
   - **Package name**: `com.jesuslcorominas.teamflowmanager`
   - **SHA-256 certificate fingerprint**: Necesitas añadirlo

#### 3.1. Obtener la Huella SHA-256

**Opción A: Desde Android Studio**
1. Abre Android Studio
2. Ve al panel **Gradle** (lado derecho)
3. Navega a: `teamflowmanager > app > Tasks > android > signingReport`
4. Haz doble clic en `signingReport`
5. En la consola, busca la línea que dice **SHA-256**
6. Copia el valor (ej: `AA:BB:CC:...`)

**Opción B: Desde la terminal**
```bash
cd android/app
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Para producción (Release):**
```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your-alias
```

#### 3.2. Añadir SHA-256 a Firebase

1. En Firebase Console, ve a **Project Settings** (⚙️ en la barra lateral)
2. Selecciona tu app Android
3. Desplázate hasta **SHA certificate fingerprints**
4. Haz clic en **Add fingerprint**
5. Pega tu SHA-256 (sin los dos puntos `:`)
6. Haz clic en **Save**

> **Importante**: Necesitas añadir AMBAS huellas:
> - SHA-256 del **debug keystore** (para desarrollo)
> - SHA-256 del **release keystore** (para producción/Play Store)

### 4. Configurar Deep Links en la App

Esta configuración ya está hecha en el código, pero verifica que esté presente:

**AndroidManifest.xml** (ya configurado):
```xml
<!-- HTTPS deep link for team invitations -->
<intent-filter android:autoVerify="true">
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="https"
        android:host="teamflowmanager.app"
        android:pathPrefix="/team/accept" />
</intent-filter>
```

**GenerateTeamInvitationUseCaseImpl.kt** (ya configurado):
- Usa Firebase Dynamic Links SDK
- Dominio: `https://teamflowmanager.page.link`
- Deep link: `https://teamflowmanager.app/team/accept`
- Fallback al Play Store si la app no está instalada

### 5. Descargar y Actualizar google-services.json

Después de añadir la huella SHA-256:

1. En Firebase Console, ve a **Project Settings** → **General**
2. En la sección de tu app Android, haz clic en **google-services.json**
3. Descarga el archivo
4. Reemplaza el archivo existente en: `app/google-services.json`
5. Haz commit del nuevo archivo

### 6. Verificar la Configuración

#### 6.1. Compilar y ejecutar la app

```bash
./gradlew clean
./gradlew build
```

#### 6.2. Probar la generación de Dynamic Links

1. Inicia sesión como **Presidente**
2. Ve a la pantalla de **Equipos**
3. Selecciona un equipo sin coach
4. Haz clic en el botón **Compartir** (icono de share)
5. Deberías ver un enlace como: `https://teamflowmanager.page.link/xxxxx`

#### 6.3. Probar el enlace

**Escenario 1: App instalada**
1. Envía el enlace por WhatsApp/Email a otro dispositivo con la app instalada
2. Haz clic en el enlace
3. La app debería abrirse directamente en la pantalla de aceptación de invitación

**Escenario 2: App NO instalada**
1. Envía el enlace a un dispositivo sin la app
2. Haz clic en el enlace
3. Deberías ser redirigido al Play Store
4. Tras instalar la app, ábrela
5. Deberías ver la pantalla de aceptación de invitación (requiere guardar el deep link en la sesión)

### 7. Depuración de Problemas

#### Problema: El enlace no se genera (error de Firebase)

**Causa**: Firebase Dynamic Links no está habilitado o mal configurado

**Solución**:
1. Verifica que Dynamic Links esté habilitado en Firebase Console
2. Verifica que el dominio `teamflowmanager.page.link` esté creado
3. Verifica que la SHA-256 esté correctamente añadida
4. Descarga de nuevo `google-services.json`

**Fallback**: Si falla, el código genera un enlace personalizado `teamflowmanager://...` que también funciona (pero no es clicable en WhatsApp)

#### Problema: El enlace no abre la app

**Causa**: La huella SHA-256 no coincide

**Solución**:
1. Verifica que la SHA-256 en Firebase Console coincida con tu keystore
2. Para desarrollo, usa debug keystore
3. Para producción, usa release keystore del Play Store

#### Problema: El enlace abre el navegador en lugar de la app

**Causa**: `android:autoVerify="true"` no funciona sin assetlinks.json

**Solución**:
- Si tienes un servidor en `teamflowmanager.app`, configura assetlinks.json
- Si no, ignora este problema - Firebase Dynamic Links maneja la redirección correctamente

## Configuración para Producción

### 1. Obtener SHA-256 del Release Keystore

Cuando subas la app al Play Store:

1. Ve a Play Console
2. Navega a: **Setup** → **App Signing**
3. Copia la **SHA-256 certificate fingerprint** del **App signing key certificate**
4. Añádela a Firebase Console (Project Settings → Tu app Android → SHA fingerprints)

### 2. Actualizar google-services.json

Después de añadir la SHA-256 de producción, descarga de nuevo `google-services.json` y actualiza el archivo en el proyecto.

## Testing de Dynamic Links

Firebase proporciona una herramienta de testing:

1. Ve a: https://goo.gl/app-links-debug
2. Ingresa tu Dynamic Link de prueba
3. Verifica que la configuración sea correcta

## Estructura del Dynamic Link Generado

```
https://teamflowmanager.page.link/xxxxx
  ↓ Redirige a ↓
https://teamflowmanager.app/team/accept?teamId=ABC123&teamName=Infantil
  ↓ Abre la app ↓
AcceptTeamInvitationScreen (teamId: ABC123)
```

## Límites y Costos

- **Gratis**: Hasta 25,000 enlaces por día
- **Sin costo adicional**: Por encima del límite gratuito

Para Team Flow Manager, esto es más que suficiente.

## Recursos Adicionales

- [Firebase Dynamic Links Documentation](https://firebase.google.com/docs/dynamic-links)
- [Android Integration Guide](https://firebase.google.com/docs/dynamic-links/android/create)
- [Testing Dynamic Links](https://firebase.google.com/docs/dynamic-links/android/receive)

## Resumen de Archivos Modificados

En esta implementación se modificaron:

1. **gradle/libs.versions.toml**: Añadida dependencia de `firebase-dynamic-links-ktx`
2. **app/build.gradle.kts**: Añadida implementación de Dynamic Links
3. **GenerateTeamInvitationUseCaseImpl.kt**: Implementación completa con Firebase Dynamic Links
4. **app/src/main/res/values/strings.xml**: Corregida puntuación española (¡)
5. **AndroidManifest.xml**: Ya tenía la configuración de deep links (sin cambios)

## Checklist Final

- [ ] Habilitar Firebase Dynamic Links en Firebase Console
- [ ] Crear dominio `teamflowmanager.page.link`
- [ ] Obtener SHA-256 del debug keystore
- [ ] Añadir SHA-256 a Firebase Console
- [ ] Descargar y actualizar `google-services.json`
- [ ] Compilar y ejecutar la app
- [ ] Probar generación de enlaces (como Presidente)
- [ ] Probar apertura de enlaces (en otro dispositivo)
- [ ] (Producción) Añadir SHA-256 del release keystore
- [ ] (Producción) Actualizar `google-services.json` de nuevo

---

**¿Necesitas ayuda?** Consulta la documentación oficial o revisa los logs en Firebase Console → Dynamic Links → Analytics
