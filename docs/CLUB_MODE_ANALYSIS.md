# Club Mode — Análisis del Estado Actual

> **Fecha**: 2026-03-27
> **Rama activa**: `feature/club-mode`

---

## 1. ¿Qué es "Club Mode"?

Club Mode es una capa jerárquica sobre los equipos. La app soporta dos perfiles de usuario completamente distintos:

| Perfil | Condición | Navegación principal |
|--------|-----------|----------------------|
| **Coach** | Tiene un `Team` con `clubFirestoreId` definido | Bottom nav: Partidos / Jugadores / Análisis / Equipo |
| **Presidente** | Tiene un `ClubMember` con rol `PRESIDENT` | Bottom nav: Equipos del club / Staff |
| **Sin club** | Sin `ClubMember` ni `Team` con club | Redirige a `ClubSelectionScreen` |

La arquitectura jerárquica es **Club → Team → Player**, donde el Club agrupa múltiples equipos y tiene miembros con roles específicos (Presidente, Coach, Staff).

---

## 2. Modelo de Datos

### Dominio

```
Club
  ├── id: Long                    (convertido de Firestore)
  ├── ownerId: String             (Firebase UID del propietario)
  ├── name: String                (3-50 caracteres)
  ├── invitationCode: String      (6-10 chars alfanuméricos, único)
  └── firestoreId: String?        (ID del doc en Firestore)

ClubMember
  ├── id: Long
  ├── userId: String              (Firebase UID)
  ├── name: String
  ├── email: String
  ├── clubId: Long
  ├── roles: List<String>         ("Presidente", "Coach", "Staff")
  ├── firestoreId: String?        (= "${userId}_${clubFirestoreId}")
  └── clubFirestoreId: String?

ClubRole (enum)
  ├── PRESIDENT("Presidente")
  ├── COACH("Coach")
  └── STAFF("Staff")

Team (campos relevantes para clubs)
  ├── clubId: Long?
  └── clubFirestoreId: String?
```

### Firestore Collections

```
/clubs/{clubId}
  - ownerId, name, invitationCode

/clubMembers/{userId}_{clubId}     ← ID predictible (importante para security rules)
  - userId, name, email, clubId, roles[]
```

---

## 3. Funcionalidades Implementadas

### 3.1 Flujo de onboarding de club ✅

**Crear un club** (`CreateClub` flow):
- Usuario sin club → `ClubSelectionScreen` → `CreateClubScreen`
- Valida nombre (3-50 chars)
- Crea doc en `/clubs/{id}` con código de invitación generado automáticamente
- Crea `ClubMember` con rol `Presidente`
- Si ya tenía un team "orphan" → lo linkea al nuevo club
- Sincroniza FCM token si tiene permisos
- Tras éxito → `CreateTeamScreen`

**Unirse a un club** (`JoinClub` flow):
- `JoinClubScreen` — introduce código de 6-10 chars alfanumérico
- Busca el club en Firestore por `invitationCode`
- Si tiene un team orphan → lo linkea, asigna rol `Coach`
- Si no tiene team → asigna rol `Staff`
- Crea `ClubMember` correspondiente
- Sincroniza FCM token
- Tras éxito → vuelve al Splash (re-evalúa estado)

### 3.2 Routing en Splash ✅

`SplashViewModel.loadTeam()` decide el destino con esta lógica:

```
1. ¿Tiene ClubMember con rol PRESIDENT?
   → UiState.ClubPresident → TeamList (vista del presidente)

2. ¿Tiene Team?
   ├── ¿Con clubFirestoreId? → UiState.TeamExists → Matches
   └── Sin clubFirestoreId  → UiState.NoClub → ClubSelection

3. Sin Team → UiState.NoClub → ClubSelection
```

**Punto ciego detectado**: Si el usuario tiene un `ClubMember` con rol `Coach` o `Staff` pero NO tiene equipo, el flujo cae en `NoClub` (intenta ir a `ClubSelection`). No hay estado `UiState.NoTeam` que diferencie "usuario de club sin team" de "usuario sin club".

### 3.3 Vista del Presidente ✅

Cuando `isPresident = true` en `MainViewModel`:
- Bottom nav cambia a: **Equipos** (`TeamList`) + **Staff** (`ClubMembers`)
- `TeamListScreen`: muestra todos los equipos del club (via `GetTeamsByClubUseCase`)
- `ClubMembersScreen`: lista miembros del club con nombre, email y roles

### 3.4 Notificaciones FCM ✅

- Tras crear/unirse al club, se suscribe al tópico `club-{clubFirestoreId}`
- En Splash, si ya tiene membership, sincroniza el token de nuevo
- Fire-and-forget: no bloquea el flujo principal

---

## 4. Casos de Uso Disponibles

| Use Case | Estado |
|----------|--------|
| `CreateClubUseCase` | ✅ Completo |
| `JoinClubByCodeUseCase` | ✅ Completo |
| `GetClubMembersUseCase` | ✅ Completo |
| `GetUserClubMembershipUseCase` | ✅ Completo |
| `GetTeamsByClubUseCase` | ✅ Completo |
| `SubscribeToClubNotificationsUseCase` | ✅ Completo |
| `UnsubscribeFromClubNotificationsUseCase` | ✅ Completo |

---

## 5. Estado por Capa

| Capa | Android | iOS |
|------|---------|-----|
| Domain / UseCase | ✅ Completo (commonMain) | ✅ Completo (commonMain) |
| Repositories (data/core) | ✅ Completo | ✅ Completo |
| ClubDataSource (Firestore) | ✅ Lectura + escritura | ❌ No implementado |
| ClubMemberDataSource (Firestore) | ✅ Lectura + escritura | ⚠️ Solo lectura (NotImplementedError en escrituras) |
| ViewModels | ✅ KMP commonMain | ✅ KMP commonMain |
| UI / Screens | ✅ shared-ui commonMain | ✅ shared-ui commonMain |
| DI / Koin | ✅ Completo | ⚠️ Parcial (falta ClubDataSource en iosMain) |

---

## 6. Pendiente / En Riesgo

### 6.1 iOS — Escrituras de ClubMember no implementadas ⚠️

Los métodos de escritura en `data/remote/src/iosMain/.../ClubMemberFirestoreDataSourceImpl.kt` lanzan `NotImplementedError`:
- `createOrUpdateClubMember()`
- `updateClubMemberRoles()`
- `addClubMemberRole()`

En iOS actualmente **no se puede** crear ni unirse a un club. Solo lectura.

### 6.2 ClubFirestoreDataSourceImpl no existe en iOS ❌

No hay implementación de `ClubDataSource` en `iosMain`. Esto implica que en iOS no se puede crear un club. El DI de iOS no registra ningún `ClubDataSource`.

### 6.3 Firebase Dynamic Links — Pendiente de activar en consola ⚠️

El doc `FIREBASE_DYNAMIC_LINKS_SETUP.md` describe la configuración necesaria, pero **no está claro si está hecha en producción**. Los pasos pendientes serían:
- Activar Firebase Dynamic Links en la consola (si no está ya hecho)
- Crear el dominio `teamflowmanager.page.link`
- Añadir SHA-256 del release keystore a Firebase Console
- Actualizar `google-services.json`

> **NOTA**: Los Dynamic Links en el código actual son para **invitaciones de equipo** (`/team/accept`), NO para unirse a clubs. Las invitaciones de club van por **código manual** (6-10 chars). Esto puede ser suficiente para el MVP pero limita la viralidad.

### 6.4 Reglas de Firestore — ¿Deployadas? ⚠️

Existe el fichero `FIRESTORE_RULES_DEPLOYMENT.md` y otros docs de rules, pero no hay certeza de que las reglas para `clubs` y `clubMembers` estén aplicadas en producción. Sin las reglas correctas:
- Usuarios no autorizados podrían leer clubMembers ajenos
- La lógica de security rules usa IDs predictibles (`{userId}_{clubId}`) que deben estar desplegadas para que funcionen correctamente

Ver `docs/CLUB_STRUCTURE_DATA_MODEL.md` para la definición exacta de las reglas.

### 6.5 Bug: Coach/Staff sin team → loop en ClubSelection ⚠️

Si un usuario tiene rol `Coach` o `Staff` (sin ser Presidente) y NO tiene equipo asociado:
1. `SplashViewModel` no detecta el `ClubMember` para roles distintos a `PRESIDENT`
2. Cae en `NoClub` → navega a `ClubSelectionScreen`
3. El usuario puede intentar crear/unirse a otro club, pero ya tiene membership

La lógica actual en `SplashViewModel` solo trata el rol `PRESIDENT` de manera especial. Los roles `COACH` y `STAFF` deben estar asociados a un `Team` con `clubFirestoreId` para llegar a `TeamExists`. Si el `clubFirestoreId` no está correctamente seteado en el team, el usuario queda atrapado en `ClubSelection`.

### 6.6 Vista del Presidente — Funcionalidades básicas aún limitadas

La vista del Presidente tiene:
- ✅ Lista de equipos del club (`TeamListScreen`)
- ✅ Lista de staff/miembros (`ClubMembersScreen`)
- ❌ Sin acciones sobre equipos (no puede crear equipo desde aquí)
- ❌ Sin acciones sobre miembros (no puede cambiar roles, expulsar)
- ❌ Sin detalle de equipo clickeable desde `TeamListScreen` en modo Presidente
- ❌ Sin pantalla de configuración del club (cambiar nombre, ver código de invitación)
- ❌ Sin pantalla para compartir el código de invitación

---

## 7. Infraestructura Firebase Pendiente

### Para que las invitaciones de club sean compartibles (nice to have)

Actualmente el código de invitación se muestra en pantalla y el usuario lo copia manualmente. Para hacerlo compartible como enlace:

1. **Cloud Function**: Crear endpoint que reciba un `invitationCode` y redirija a la app
2. **Firebase Hosting**: Servir la web de redirección en `teamflowmanager.app` o similar
3. **Deep Link scheme**: `teamflowmanager://join?code=XXXXXX`
4. **Dynamic Link** (opcional): Envolver el deep link para que funcione en WhatsApp/email

El doc `FIREBASE_HOSTING_SETUP.md` puede tener contexto relevante sobre el dominio.

### Para que funcionen las invitaciones de equipo (ya existe código)

Los Dynamic Links de invitación de equipo (`/team/accept`) ya están implementados en el código pero requieren activación en Firebase Console (ver `FIREBASE_DYNAMIC_LINKS_SETUP.md`).

---

## 8. Sugerencias

### 8.1 Pantalla "Club Settings" para el Presidente

Pantalla accesible desde el bottom nav del Presidente que muestre:
- Nombre del club
- Código de invitación (copiable con un tap)
- Botón "Compartir código" (usando el Share Intent del sistema)
- Número de equipos y miembros

Esto eliminaría la fricción de compartir el código de invitación.

### 8.2 Rol "Coach" con acceso a su equipo sin saber el código

El flujo actual de `JoinClub` asigna rol Coach si el usuario ya tiene un team orphan. Pero si el Coach crea el equipo DESPUÉS de unirse al club con rol Staff, el team queda desconectado. Hace falta un caso de uso o pantalla donde el Staff pueda "reclamar" un team existente o crear uno ya vinculado al club.

### 8.3 Compartir código de invitación como link (iOS-friendly)

Añadir un botón "Compartir" en `ClubSelectionScreen` o en un futuro `ClubSettingsScreen` que genere un link del tipo:
```
https://teamflowmanager.app/join?code=XXXXXX
```
(requiere Cloud Function o Firebase Hosting rule + dynamic link)

### 8.4 Mostrar código de invitación al Presidente

Tras crear el club, el código de invitación desaparece. El Presidente necesita verlo para invitar a otros Coaches. Actualmente no hay ningún sitio en la UI donde el Presidente pueda consultar su código.

### 8.5 Notificaciones push específicas por club

La infraestructura FCM ya suscribe a topics `club-{id}`. Sería directo añadir:
- Notificación cuando un nuevo miembro se une al club
- Notificación cuando se crea un partido en alguno de los equipos del club

### 8.6 Desconexión de roles Coach/Staff y el team

Hoy en día si un Coach deja el club (o es expulsado), su `Team` sigue teniendo el `clubFirestoreId`. No hay ningún mecanismo de limpieza.

---

## 9. Checklist de Despliegue

Antes de considerar Club Mode "listo para producción":

- [ ] **Firestore rules**: Verificar que las reglas para `clubs` y `clubMembers` están desplegadas en producción (`firebase deploy --only firestore:rules`)
- [ ] **iOS ClubDataSource**: Implementar `ClubFirestoreDataSourceImpl` para iosMain
- [ ] **iOS escrituras ClubMember**: Implementar `createOrUpdateClubMember`, `updateClubMemberRoles`, `addClubMemberRole` en iosMain
- [ ] **Bug Coach/Staff sin team**: Añadir estado `UiState.ClubMemberNoTeam` en `SplashViewModel` para redirigir correctamente
- [ ] **Código de invitación visible**: Añadir pantalla o sección donde el Presidente pueda ver y compartir el código
- [ ] **Dynamic Links para equipo**: Si se quieren usar links de invitación de equipo (no club), activar en Firebase Console + añadir SHA-256 release

---

## 10. Archivos Clave de Referencia

| Fichero | Descripción |
|---------|-------------|
| `domain/.../Club.kt` | Modelo de dominio Club |
| `domain/.../ClubMember.kt` | Modelo de dominio ClubMember |
| `domain/.../ClubRole.kt` | Enum de roles |
| `usecase/.../CreateClubUseCaseImpl.kt` | Lógica de creación |
| `usecase/.../JoinClubByCodeUseCaseImpl.kt` | Lógica de unirse |
| `data/remote/androidMain/.../ClubFirestoreDataSourceImpl.kt` | Firestore Android |
| `data/remote/iosMain/.../ClubMemberFirestoreDataSourceImpl.kt` | Firestore iOS (parcial) |
| `viewmodel/.../SplashViewModel.kt` | Routing inicial con club mode |
| `viewmodel/.../MainViewModel.kt` | Flag `isPresident` |
| `app/.../navigation/BottomNavigationBar.kt` | Nav diferenciada Presidente vs Coach |
| `shared-ui/.../club/` | Pantallas de club (CMP) |
| `docs/CLUB_STRUCTURE_DATA_MODEL.md` | Modelo de datos Firestore detallado |
| `docs/CLUB_STRUCTURE_MIGRATION_GUIDE.md` | Guía de migración Firebase |
| `docs/FIREBASE_DYNAMIC_LINKS_SETUP.md` | Setup de Dynamic Links (team invitations) |
