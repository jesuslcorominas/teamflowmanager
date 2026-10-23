# Borrado de cuenta

Google Play exige, para cualquier app que permita crear una cuenta, dos cosas: una ruta
**dentro de la app** para borrarla, y una **URL pública** donde solicitar el borrado sin
instalarla. Este documento recoge cómo está implementado y, sobre todo, **por qué** la cascada
se ejecuta donde se ejecuta y qué pasa con los datos que no son solo del usuario que se va.

## Resumen

| Pieza | Dónde |
|---|---|
| Entrada en la app | `shared-ui/.../ui/settings/SettingsScreen.kt` (Ajustes → Cuenta → Eliminar cuenta) |
| Caso de uso | `domain/.../usecase/DeleteAccountUseCase.kt` + `usecase/.../DeleteAccountUseCaseImpl.kt` |
| Repositorio | `usecase/.../repository/AccountRepository.kt` → `data/core/.../repository/AccountRepositoryImpl.kt` |
| Datasource | `data/core/.../datasource/AccountDeletionDataSource.kt` → `data/remote/src/commonMain/.../datasource/AccountDeletionDataSourceImpl.kt` |
| Cascada | `firebase-functions/accountDeletion.js`, función `deleteAccount` |
| Endpoint | `POST https://teamflowmanager.app/api/deleteAccount`, cabecera `Authorization: Bearer <id token>` |
| Ruta web | `firebase-functions/public/delete-account.html` |
| Política de privacidad | `firebase-functions/public/privacy.html` |

## Decisión 1: la cascada va en una Cloud Function, no en el cliente

Se evaluó hacerla desde el cliente (ampliando `firestore.rules`) y se descartó por cuatro
motivos:

1. **Obligaría a abrir permisos de borrado que hoy no existen.** `goals`, `substitutions`,
   `playerTimeHistory` y `matchOperations` no tienen regla `allow delete`. Añadirla para una
   operación que ocurre una vez en la vida de una cuenta deja el permiso abierto para todas las
   demás peticiones. El Admin SDK se salta las reglas, así que la cascada no necesita ninguna.
2. **`user.delete()` desde el cliente exige login reciente.** Firebase Auth devuelve
   `requires-recent-login` si la sesión tiene más de unos minutos, lo que obligaría a un
   re-login justo antes de borrar. `admin.auth().deleteUser(uid)` no tiene esa restricción.
3. **Una cascada a medias no se puede reanudar.** Si la app muere a mitad (proceso matado, red
   caída), quedan documentos huérfanos sin nada que los recoja — exactamente lo que revisa
   Play. En el servidor se ejecuta entera en una invocación, y si algo falla la función
   devuelve 500 y la app dice que no se completó en lugar de dar un éxito falso.
4. **Storage solo se puede limpiar por prefijo desde el servidor.** `bucket.deleteFiles({ prefix })`
   es API de Admin.

La función verifica el ID token con `admin.auth().verifyIdToken()` y toma el `uid` **del token**,
nunca del cuerpo de la petición: cogerlo del body permitiría a cualquiera borrar la cuenta de
cualquiera.

## Decisión 2: qué pasa con los clubes y equipos de los que el usuario es propietario

Es la decisión de producto de verdad. Había tres opciones: **bloquear** el borrado si hay otros
miembros, **borrar** el club entero, o **transferir** la propiedad.

**Bloquear queda descartado**: Play exige que el borrado sea incondicional. Un usuario atrapado
en su propia cuenta porque su club tiene miembros no cumple la política.

**Borrar el club entero tampoco**: los datos del club son también de sus otros miembros. Que
una persona se dé de baja no puede destruir la temporada de todo el club.

La política implementada es, por tanto:

### Clubes

- **El usuario es propietario y el club tiene más miembros** → el club **sobrevive**. La
  propiedad (`clubs/{id}.ownerId`) pasa a otro miembro: se prefiere uno que ya tenga el rol
  `Presidente` y, si no hay ninguno, se promueve al primer miembro restante. Un club sin
  propietario no se puede administrar nunca más, así que alguien tiene que quedárselo.
- **El usuario es propietario y es el único miembro** → el club se **borra entero**, con sus
  equipos, jugadores, fotos, partidos e histórico. No queda nadie a quien pertenezca.
- **El usuario no es propietario** → el club no se toca. Solo se borra su fila en `clubMembers`
  y sus `notificationPreferences`.

### Equipos

- **Equipo con `assignedCoachId` del usuario y club que sobrevive** → el equipo se **libera**:
  `assignedCoachId = null`, y se borra el `pendingCoachAssignments/{teamId}` si apuntaba a él.
  Sus jugadores, partidos e histórico **no se tocan**: pertenecen al club, que sigue siendo el
  responsable del tratamiento de esos datos.
- **Equipo sin club, o cuyo club se borra** → el equipo se **borra entero** en cascada.

### Por qué es coherente con el RGPD

El derecho de supresión cubre los **datos personales del usuario**, no los registros de la
organización para los que el club es responsable del tratamiento. Los datos personales del
usuario (cuenta de Auth, `users/{uid}`, `clubMembers`, `fcmTokens`, preferencias) se borran
**siempre**, sobreviva o no el club. Los datos de jugadores —incluidos menores— se borran
cuando desaparece la estructura que los sostenía, y permanecen bajo la responsabilidad del club
cuando este sigue existiendo. La política de privacidad lo explica en los mismos términos, y la
página de borrado da a tutores legales una vía directa para pedir la supresión de los datos de
un menor concreto.

## Qué se borra, exactamente

Siempre, pase lo que pase con el club:

- `users/{uid}` (y subcolecciones, vía `recursiveDelete`)
- `clubMembers` donde `userId == uid`
- `clubs/{clubId}/notificationPreferences/{uid}` de cada club al que pertenecía
- `fcmTokens` donde `userId == uid`
- El prefijo `users/{uid}/` de Firebase Storage
- La cuenta de Firebase Auth (`admin.auth().deleteUser`), **lo último**: una vez borrada no hay
  forma de reintentar un paso fallido

Además, por cada equipo que se borra en cascada:

- `players` del equipo, sus subcolecciones `playerStats` y su foto en Storage
- `matches` del equipo y todas sus subcolecciones
- `goals`, `substitutions`, `playerTimes`, `playerTimeHistory`, `matchOperations` y `shortLinks`
  con ese `teamId`
- `pendingCoachAssignments/{teamId}`
- `teams/{teamId}` y su subcolección `teamStats`

Y por cada club que se borra entero:

- El resto de filas de `clubMembers` del club
- `presidentNotifications/{clubId}` completo
- `clubs/{clubId}` y sus subcolecciones

> Las colecciones por partido (`goals`, `substitutions`, `playerTimes`, `playerTimeHistory`,
> `matchOperations`) **no cuelgan del documento del partido**: están indexadas por `teamId` en la
> raíz. Cada una necesita su propia consulta, y olvidar una es justo lo que deja los huérfanos.

## El token FCM

La función borra el documento de `fcmTokens`, pero **solo el cliente puede hablar con FCM**. Por
eso `DeleteAccountUseCaseImpl` invoca `DeleteFcmTokenUseCase` antes de llamar a la función: así
el dispositivo se da de baja del topic del club. Va en `runCatching` — que un dispositivo no
pueda desuscribirse no es motivo para negar el borrado, y el topic queda inservible igualmente.

## Reglas de Firestore

**No se ha modificado ninguna.** `firestore.rules` lleva un comentario que lo explica, para que
nadie se pregunte más adelante por qué no hay `allow delete` en las colecciones de partido.

## Probar en dev

```bash
# 1. Desplegar la función y las páginas al proyecto dev
firebase use tfm-dev
firebase deploy --only functions:deleteAccount,hosting
```

Después, en el flavor dev: crear un club con equipo, jugadores (con foto), un partido terminado
con goles y sustituciones, y borrar la cuenta desde Ajustes. Comprobar en la consola de Firebase
que no quedan documentos con ese `teamId` en `goals`, `substitutions`, `playerTimes`,
`playerTimeHistory` ni `matchOperations`, que el bucket no conserva la foto del jugador, y que
el usuario ha desaparecido de Authentication.

Para el caso de transferencia: club con dos miembros, borrar al propietario y comprobar que
`clubs/{id}.ownerId` pasa al otro miembro, que este tiene el rol `Presidente` y que el equipo
que entrenaba queda con `assignedCoachId = null`.
