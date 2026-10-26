# Publicar en producción — guía paso a paso

> **Estado: la app ya está publicada.** Versión 61 (0.8.0), disponible en Google
> Play desde el 24 de octubre de 2026, en 178 países. Las fases 1 a 5 de esta
> guía ya están hechas; quedan los pendientes de la última sección.
>
> La guía se mantiene porque el camino sigue siendo el mismo para cada versión
> nueva, y porque explica qué hay detrás de cada declaración de la ficha.

Cada paso dice **dónde** hay que hacerlo y **cómo saber que está hecho**.

Los datos de referencia (textos de la ficha, respuestas de los formularios,
tablas de datos recogidos) están en [PLAY_STORE_LAUNCH.md](PLAY_STORE_LAUNCH.md).
Esta guía es el orden; aquella es el contenido para copiar y pegar.

Reparto aproximado: la fase 0 es una tarde de trabajo, las fases 1 a 4 son unas
dos horas en Play Console, y luego hay que esperar a la revisión de Google.

> Las etiquetas exactas de los menús de Play Console cambian de vez en cuando.
> Si alguna no coincide, busca por el nombre de la sección: la estructura
> (Política y programas / Prueba y publicación / Crecimiento) se mantiene.

---

## Fase 0 — En tu máquina, antes de tocar Play Console

### 0.1 Mergear las PRs pendientes

- [ ] [#449](https://github.com/jesuslcorominas/teamflowmanager/pull/449) — duraciones por tipo de equipo
- [ ] [#447](https://github.com/jesuslcorominas/teamflowmanager/pull/447) — job de build en macOS
- [ ] [#448](https://github.com/jesuslcorominas/teamflowmanager/pull/448) — esta guía y el job `promote`

Ojo con el orden: **mergea #448 el último**, porque a partir de ese momento
cualquier rama `release/*` que llegue a `main` publica en producción de forma
automática. Mientras no esté mergeada, la promoción sigue siendo manual.

### 0.2 Desplegar Firebase Hosting y Functions

Sin esto, las tres URLs legales devuelven 404 y Play rechaza la ficha, porque
valida que respondan. Además la función de borrado de cuenta no existe todavía
en el proyecto de Firebase: la app la llamaría y fallaría.

```bash
firebase deploy --only hosting,functions
```

Comprobar que las tres responden:

```bash
for u in privacy terms delete-account; do curl -s -o /dev/null -w "$u: %{http_code}\n" "https://teamflow-manager-897a3.web.app/$u.html"; done
```

- [ ] Las tres devuelven `200`

### 0.3 Probar el borrado de cuenta de verdad

Esto nunca se ha ejecutado contra Firebase. Es el requisito que más rechazos
provoca, así que conviene verlo funcionar antes de declararlo en la ficha.

1. Instala el flavor `dev` en un dispositivo o emulador.
2. Inicia sesión con una cuenta de usar y tirar.
3. Crea un club, un equipo, un par de jugadores con foto y un partido.
4. Ajustes → Eliminar cuenta → confirmar.
5. En la consola de Firebase comprueba que **no queda nada**: el usuario en
   Authentication, sus documentos en Firestore y sus fotos en Storage.

- [ ] No quedan documentos huérfanos ni ficheros en Storage

### 0.4 Crear la cuenta de prueba para el revisor

La app obliga a iniciar sesión con Google. Sin credenciales, el revisor no entra
y la rechaza directamente.

1. Crea una cuenta de Google **nueva y dedicada** (no uses la tuya personal).
2. Entra en la app con ella.
3. Déjala con un club, un equipo con jugadores y al menos un partido jugado, para
   que el revisor vea contenido real nada más entrar.
4. Apunta el correo y la contraseña: los pedirá el paso 2.3.

- [ ] Cuenta creada y con datos de ejemplo

### 0.5 Hacer las capturas de pantalla

Mínimo 2, pero con 4 a 8 la ficha se ve mucho mejor. En vertical, con el
dispositivo en modo claro y sin barra de notificaciones llena de iconos.

Orden sugerido: lista de partidos → partido en curso con el cronómetro →
sustituciones → tiempos de juego por jugador → análisis de temporada →
ficha de equipo.

Usa la cuenta de prueba del paso anterior, así las capturas no enseñan datos de
jugadores reales. **Importante si en algún equipo hay menores**: no publiques
capturas con sus nombres o fotos.

- [ ] Entre 4 y 8 capturas guardadas

### 0.6 Preparar los dos gráficos

| Recurso | Formato | De dónde sacarlo |
|---|---|---|
| Icono | 512 × 512 PNG, 32 bits | Escalar `app/src/main/res/mipmap-xxxhdpi/ic_launcher` o reexportar el original |
| Gráfico de funciones | 1024 × 500 PNG o JPG | Hay que diseñarlo: el logo sobre un fondo liso ya sirve |

El gráfico de funciones es la imagen ancha que Play enseña arriba del todo. No
puede llevar texto pegado a los bordes ni capturas dentro de un marco de móvil.

- [ ] Icono 512 × 512 listo
- [ ] Gráfico de funciones 1024 × 500 listo

---

## Fase 1 — Confirmar que no te bloquean las pruebas cerradas

Google exige 12 testers durante 14 días seguidos antes de dar acceso a
producción, pero **solo a cuentas personales creadas después del 13 de noviembre
de 2023**. La tuya es de hace unos diez años, así que no debería aplicarte.

Para confirmarlo:

1. Play Console → **⚙ Configuración** → **Datos de la cuenta de desarrollador**.
   Mira la fecha de creación y si es cuenta personal o de organización.
2. Abre la app → **Prueba y publicación** → **Producción** → **Crear versión**.
   Si te deja entrar en la pantalla de crear la versión, no te aplica. Si en su
   lugar sale una tarjeta de «Solicitar acceso a producción» con un contador de
   testers y días, entonces sí.

- [ ] Confirmado que puedo crear una versión de producción

> Si resultara que sí aplica, para aquí: hay que montar una prueba cerrada con 12
> personas y esperar 14 días. No hay atajo.

---

## Fase 2 — Contenido de la app

Todo en **Política y programas → Contenido de la aplicación**. La pestaña
«Requiere tu atención» va listando lo que falta. En cada apartado se entra con
**Empezar** la primera vez y con **Gestionar** después.

### 2.1 Política de privacidad

- Pegar: `https://teamflow-manager-897a3.web.app/privacy.html`
- [ ] Guardado

### 2.2 Anuncios

- Responder **No**, la app no tiene publicidad.
- [ ] Guardado

### 2.3 Acceso a la aplicación

- Elegir que **todas o algunas funciones están restringidas**.
- **Añadir instrucciones nuevas**, y rellenar:
  - Nombre: `Cuenta de demo`
  - Usuario y contraseña: los de la cuenta del paso 0.4
  - Instrucciones: *Iniciar sesión con Google usando la cuenta indicada. La
    cuenta ya tiene un club, un equipo con jugadores y un partido creados.
    Desde la pantalla principal se accede a los partidos y al detalle del equipo.*
- [ ] Guardado

### 2.4 Contenido y audiencia objetivo

- Franja de edad: **18 y más**.
- A la pregunta de si la app puede atraer a menores: **No**.

La app la usan entrenadores y directivos; los jugadores no crean cuenta. Declarar
público infantil activaría la política de familias, con requisitos mucho más
duros para Analytics y Crashlytics.

- [ ] Guardado

### 2.5 Clasificación de contenido

- Cuestionario IARC. Categoría: **utilidad o productividad**.
- Responder **No** a todo: violencia, lenguaje, sexo, drogas, apuestas, compras.
- Resultado esperado: apta para todos los públicos.
- [ ] Cuestionario enviado

### 2.6 Seguridad de los datos

Es el formulario más largo, unos 20 minutos. Las respuestas completas están en
la sección 2 de [PLAY_STORE_LAUNCH.md](PLAY_STORE_LAUNCH.md). En resumen:

- Sí se recogen datos; **no** se comparten con terceros.
- Todos se cifran en tránsito.
- Sí se puede solicitar el borrado → URL: `https://teamflow-manager-897a3.web.app/delete-account.html`
- Tipos a marcar: nombre, correo, ID de usuario, fotos, contenido generado por el
  usuario, interacciones con la app, registros de fallos, diagnósticos e ID de
  dispositivo.

La regla de oro: **lo que declares aquí tiene que coincidir con la política de
privacidad**. Una discrepancia es motivo de rechazo.

- [ ] Formulario completado

---

## Fase 3 — Ficha de Play Store

En **Crecimiento → Presencia en Play Store → Ficha principal de Play Store**.

Los textos están redactados en la sección 5 de
[PLAY_STORE_LAUNCH.md](PLAY_STORE_LAUNCH.md), listos para copiar.

- [ ] Nombre: `TeamFlow Manager`
- [ ] Descripción corta (78 de 80 caracteres)
- [ ] Descripción completa
- [ ] Icono 512 × 512
- [ ] Gráfico de funciones 1024 × 500
- [ ] Capturas de teléfono

En **Configuración de la tienda**, aparte:

- [ ] Categoría: **Deportes**
- [ ] Correo de contacto: `jesuslcorominas@gmail.com` — tiene que ser un
      buzón que leas de verdad, porque se publica en la ficha
- [ ] Países: elegir dónde se distribuye (empezar por España es razonable)

---

## Fase 4 — Crear la versión de producción

Dos caminos. El automático solo está disponible una vez mergeada la #448.

### Camino A — automático (recomendado)

1. En el repositorio, lanzar el flujo de release:
   ```
   /publish
   ```
   Pide la versión (por ejemplo `0.8.0`) y las notas, crea la rama `release/*`,
   sube el AAB a alpha y abre una PR hacia `main`.
2. Esperar a que la CI esté verde.
3. Mergear esa PR. El job `promote` publica en producción **al 10 % de los
   usuarios**.

### Camino B — manual

1. **Prueba y publicación** → **Producción** → **Crear versión**.
2. En «App bundles», elegir **Añadir desde la biblioteca** y seleccionar el
   versionCode que ya está en alpha.
3. Pegar las notas de la versión.
4. Porcentaje de implementación: **10 %**.
5. **Guardar** y luego **Revisar versión**.

- [ ] Versión de producción creada

---

## Fase 5 — Enviar a revisión

1. **Resumen de publicación** (en el menú lateral).
2. Revisar la lista de «Cambios listos para enviar a revisión»: deben aparecer la
   ficha, las declaraciones de contenido y la versión de producción.
3. **Enviar a revisión**.

- [ ] Enviado

A partir de aquí es esperar. La primera revisión de una app suele tardar varios
días, y puede alargarse hasta una semana; las siguientes versiones van mucho más
rápido. Si la rechazan, el correo dice qué política se ha incumplido y se corrige
y se reenvía — es normal, no es un drama.

---

## Fase 6 — Después de publicar

1. Durante dos o tres días, mirar **Calidad → Crashlytics** y el panel de
   vitals de Play con el 10 % de usuarios.
2. Si no hay sorpresas, subir el porcentaje: **Producción** → la versión →
   **Aumentar el porcentaje de implementación** → 25 %, 50 %, 100 %.
3. Para las siguientes versiones, el 10 % inicial se cambia con la variable de
   repositorio `PLAY_ROLLOUT_FRACTION` (GitHub → Settings → Secrets and
   variables → Actions → Variables).

---

## Lo que no está resuelto

**Seguridad de los datos declara cosas falsas.** A 23 de octubre, la ficha
pública decía «Los datos no se cifran» y «Los datos no se pueden eliminar». Las
dos son incorrectas: Firebase va por HTTPS y el borrado de cuenta existe y está
desplegado. Hay que rehacer el cuestionario con las respuestas de la sección 2
de [PLAY_STORE_LAUNCH.md](PLAY_STORE_LAUNCH.md).

**El deep link HTTPS no verifica.** `AndroidManifest.xml` y `Navigation.kt`
declaran `teamflowmanager.app` con `autoVerify="true"`, y ese dominio no existe.
Cambiarlo a `teamflow-manager-897a3.web.app` exige además publicar un
`assetlinks.json` en ese host con la huella SHA-256 del certificado de firma
(Play Console → Integridad de la aplicación → Firma de aplicaciones). Mientras
tanto el esquema propio `teamflowmanager://` sí funciona, que es el que usan de
verdad los enlaces de invitación.

**Las invitaciones por enlace están rotas.** Dos motivos encadenados:

1. `createShortLink` generaba URLs en `teamflowmanager.web.app`, un sitio que no
   existe. Corregido en este documento y en el código, pero **la corrección no
   está desplegada**.
2. En el host correcto, `/l/**` responde **403**: las funciones desplegadas son
   de 1ª generación y no admiten invocación sin autenticar.

Redesplegarlas no es directo: el código del repositorio ya está migrado a
`firebase-functions/v2`, y Firebase rechaza el despliegue con
«Upgrading from 1st Gen to 2nd Gen is not yet supported». Hay que borrar las
tres funciones antiguas y recrearlas, asumiendo un corte. `deleteAccount` sí
está desplegada, porque es nueva y nació en 2ª generación.

**iOS no se publica**: la CI genera un IPA ad-hoc como artefacto, sin subida a
App Store Connect, y no está previsto a corto plazo.
