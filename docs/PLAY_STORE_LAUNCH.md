# Publicación en Google Play — producción

Guía para pasar la app del track `alpha` a producción abierta. Reúne lo que hay
que rellenar en Play Console (que no se puede automatizar desde el repositorio)
y los textos ya redactados para copiar y pegar.

Estado de referencia: versión `0.7.3`, versionCode `59`, publicada solo en alpha.

---

## 1. Requisito de pruebas cerradas — no aplica

Google Play exige 12 testers durante 14 días consecutivos en closed testing antes
de dar acceso a producción, pero **solo a las cuentas de desarrollador personales
creadas después del 13 de noviembre de 2023**. El requisito va atado a la cuenta,
no a la app.

Cómo confirmarlo:

1. Play Console → ⚙ Configuración → Datos de la cuenta de desarrollador: fecha de
   creación y tipo de cuenta (personal u organización).
2. Abrir la app → Prueba y publicación → Producción → Crear versión. Si el
   requisito aplicara, aparecería una tarjeta de «Solicitar acceso a producción»
   con el progreso de testers y días. Si deja crear la versión, no aplica.

> El umbral fue de 20 testers desde noviembre de 2023 y se rebajó a 12 en
> diciembre de 2024. Las cifras distintas que circulan por la web son de la
> versión antigua de la norma.

---

## 2. Seguridad de los datos (Data Safety)

Respuestas derivadas de [la política de privacidad](../firebase-functions/public/privacy.html).
Mantener ambas coherentes: una discrepancia es motivo de rechazo.

**Declaraciones generales**

| Pregunta | Respuesta |
|---|---|
| ¿Se recogen o comparten datos? | Sí, se recogen. No se comparten con terceros. |
| ¿Se cifran en tránsito? | Sí (HTTPS/TLS en todo Firebase). |
| ¿Se puede solicitar el borrado? | Sí — in-app (Ajustes → Eliminar cuenta) y web (`/delete-account.html`). |
| ¿Cumple la Families Policy? | No aplica: público objetivo 18+. |
| ¿Hay publicidad? | No. |
| ¿Hay compras integradas? | No. |

**Tipos de datos a declarar**

| Tipo de dato | Recogido | Obligatorio | Finalidad |
|---|---|---|---|
| Nombre | Sí | Sí | Funciones de la app, gestión de la cuenta |
| Dirección de correo | Sí | Sí | Funciones de la app, gestión de la cuenta |
| ID de usuario | Sí | Sí | Funciones de la app, gestión de la cuenta |
| Fotos | Sí | No | Funciones de la app (ficha del jugador) |
| Otra información generada por el usuario | Sí | No | Funciones de la app (clubes, equipos, partidos) |
| Interacciones con la app | Sí | No | Analíticas |
| Registros de fallos | Sí | No | Analíticas, diagnóstico |
| Diagnósticos | Sí | No | Analíticas, diagnóstico |
| ID del dispositivo o de otro tipo | Sí | No | Funciones de la app (notificaciones push) |

No se recogen: datos de ubicación, financieros, de salud, contactos, mensajes,
archivos del dispositivo ni historial de navegación.

**Permisos a justificar en la ficha**

- `CAMERA` — tomar la fotografía del jugador desde la propia app.
- `POST_NOTIFICATIONS` — avisos de partido, goles y asignaciones de equipo.

---

## 3. Acceso a la app (credenciales para el revisor)

La app obliga a iniciar sesión con Google, así que sin esto el revisor no puede
entrar y la rechaza. En Play Console → Política y programas → Contenido de la app
→ Acceso a la app, declarar que **todas las funciones requieren credenciales** y
aportar:

- Una cuenta de Google de prueba (crear una dedicada, no usar la personal).
- Instrucciones: iniciar sesión con Google → crear un club → crear un equipo →
  añadir jugadores → crear un partido.
- Dejar la cuenta con un club y un equipo ya creados, para que el revisor vea
  contenido real nada más entrar.

---

## 4. Público objetivo y clasificación de contenido

| Pregunta | Respuesta | Razón |
|---|---|---|
| Público objetivo | 18 y más | La usan entrenadores, delegados y directivos; los jugadores no crean cuenta |
| ¿Atrae a menores? | No | Herramienta de gestión deportiva, sin contenido infantil |
| Categoría | Deportes | |
| Clasificación de contenido | Para todos los públicos (IARC) | Sin violencia, lenguaje, apuestas ni contenido sexual |

Aunque el público sea 18+, en la app se tratan datos de jugadores que pueden ser
menores. Eso ya está cubierto en la sección 4 de la política de privacidad: los
introduce el adulto responsable, que debe contar con el consentimiento de los
tutores.

---

## 5. Ficha de Play Store

### Nombre de la app (30 caracteres máx.)

```
TeamFlow Manager
```

Es el valor de `app_name` en `strings.xml`, sin espacio en «TeamFlow». Las
páginas legales decían «Team Flow Manager» y se han alineado con este nombre.

### Descripción corta (80 caracteres máx.)

```
Gestiona tu equipo de fútbol base: convocatorias, partidos y tiempos de juego.
```

### Descripción completa (4000 caracteres máx.)

```
TeamFlow Manager es la herramienta de los entrenadores de fútbol base para
llevar el equipo durante toda la temporada, desde la plantilla hasta el minuto a
minuto del partido.

GESTIÓN DEL CLUB Y DEL EQUIPO
• Organiza tu club con varios equipos y asigna roles: propietario, presidente,
  entrenador y staff.
• Fútbol 5, 7, 8 u 11: elige el formato de cada equipo.
• Ficha de jugador completa: nombre, dorsal, posiciones, capitán y fotografía.
• Invita a otros miembros del cuerpo técnico con un enlace y compartid el equipo.

PARTIDOS EN TIEMPO REAL
• Convoca a los jugadores y prepara el once inicial antes de empezar.
• Cronómetro por periodos, con formatos de dos tiempos de 25 minutos o cuatro
  cuartos de 12:30.
• Registra sustituciones, goles y tiempos muertos sobre la marcha, sin perder el
  hilo del encuentro.
• Control del tiempo de juego de cada jugador, para repartir minutos con
  criterio y que nadie se quede en el banquillo.

ANÁLISIS DESPUÉS DEL PARTIDO
• Histórico de todos los encuentros con su desarrollo completo.
• Minutos jugados, goles y participación de cada jugador a lo largo de la
  temporada.
• Informe del partido listo para compartir con el equipo o con el club.

PENSADA PARA EL BANQUILLO
• Todo sincronizado en la nube: cambia de dispositivo y sigue donde lo dejaste.
• Avisos de partido y de goles para que el cuerpo técnico esté al día.
• Interfaz pensada para usarse con una mano y con prisa, que es como se usa de
  verdad durante un partido.

TeamFlow Manager no incluye publicidad ni compras dentro de la aplicación, y no
vende tus datos a terceros.
```

> La landing page de `teamflowmanager.app` describía la app como de voleibol.
> Era un texto heredado: el dominio está en `TeamType` (FOOTBALL_5, FOOTBALL_7,
> FOOTBALL_8, FOOTBALL_11) y el periodo por defecto es de 2 × 25 min o
> 4 × 12:30, propio del fútbol base. Corregido junto con esta guía.

### Gráficos necesarios

| Recurso | Formato | Estado |
|---|---|---|
| Icono | 512 × 512 PNG, 32 bits | Pendiente (adaptar `app/src/main/res/mipmap-*/ic_launcher`) |
| Gráfico de funciones | 1024 × 500 PNG o JPG | Pendiente |
| Capturas de teléfono | mín. 2, recomendable 4–8; 16:9 o 9:16, lado mínimo 320 px | Pendiente |
| Capturas de tablet 7" y 10" | Solo si se declara compatibilidad con tablets | Pendiente |
| Vídeo promocional | URL de YouTube | Opcional |

Capturas sugeridas, en este orden: lista de partidos, partido en curso con el
cronómetro, pantalla de sustituciones, tiempos de juego por jugador, análisis de
la temporada, ficha de equipo.

### Idiomas

La ficha y la app están solo en español (`es-ES`). Los `strings.xml` por defecto
están en inglés desde el arreglo de la PR #446, así que una ficha en inglés es
posible más adelante, pero requiere revisar que todas las cadenas estén
traducidas antes de ofrecerla.

---

## 6. Datos de contacto y páginas legales

| Campo de Play Console | Valor |
|---|---|
| Política de privacidad | `https://teamflowmanager.app/privacy.html` |
| Términos de uso | `https://teamflowmanager.app/terms.html` |
| Borrado de cuenta (URL) | `https://teamflowmanager.app/delete-account.html` |
| Email de contacto | `privacidad@teamflowmanager.app` |

> Las tres páginas se sirven desde Firebase Hosting y requieren
> `firebase deploy --only hosting,functions` antes de darlas de alta en la ficha:
> Play valida que la URL responda. Verificar también que el buzón
> `privacidad@teamflowmanager.app` existe y se lee.

---

## 7. Pipeline de release

El job `promote` de `.github/workflows/post-release.yml` ya está activo: al
mergear una rama `release/*` a `main`, la versión que está en alpha se promueve
a producción.

**Despliegue escalonado.** La variable de repositorio `PLAY_ROLLOUT_FRACTION`
controla a qué fracción de usuarios llega:

| Valor | Efecto |
|---|---|
| sin definir | 0.1 — el 10 % de los usuarios (`status: inProgress`) |
| `0.2`, `0.5`… | esa fracción (`status: inProgress`) |
| `1` | publicación directa al 100 % (`status: completed`) |

Cualquier otro valor aborta el job antes de llamar a la Play API. Se define en
GitHub → Settings → Secrets and variables → Actions → Variables.

Con un despliegue escalonado, el resto del rollout se escala a mano desde Play
Console → Producción → la versión → «Aumentar el porcentaje de implementación».
**Para la primera versión de producción conviene dejar el 0.1 por defecto** y
subir según se vean los datos de Crashlytics.

Si la Play API rechaza la actualización del track o el commit de la edición, el
job falla y no se crea el GitHub Release, para que no anuncie una publicación
que no ha llegado a Play.

---

## 8. Orden recomendado

1. Confirmar en Play Console que el requisito de testers no aplica (sección 1).
2. Desplegar hosting y functions para que las URLs legales respondan.
3. Rellenar Data Safety, acceso a la app, público objetivo y clasificación.
4. Preparar gráficos y capturas, y subir la ficha.
5. Lanzar una `release/*` con `/publish`. Al mergear su PR a `main`, el job
   `promote` publica en producción al 10 % de los usuarios (sección 7).
6. Vigilar Crashlytics un par de días y escalar el rollout desde Play Console.
