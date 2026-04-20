# Agents Index

Agentes disponibles en este proyecto. Se invocan automáticamente por Claude cuando la tarea encaja con su descripción.

## Workflow de implementación

Para tareas de implementación de código, el flujo es siempre en dos pasos:

1. **android-senior-dev** (Opus) crea el spec técnico
2. **android-mid-dev** (Sonnet) implementa siguiendo el spec

No invoques android-mid-dev sin haber obtenido primero el spec del senior.

---

| Agente                  | Modelo | Fichero | Cuándo usarlo |
|-------------------------|--------|---|---|
| **android-senior-dev**  | Opus   | `agents/android-senior-dev.md` | Crear el spec técnico antes de cualquier implementación: analiza el código existente, toma decisiones arquitectónicas y produce un plan paso a paso. |
| **android-mid-dev**     | Sonnet | `agents/android-mid-dev.md` | Implementar código siguiendo el spec del senior-dev. No toma decisiones de arquitectura. |
| **android-testing**     | Haiku  | `agents/android-testing.md` | Añadir o mejorar tests: cobertura real ≥ 90%, tests en el source set correcto (`commonTest` / `androidUnitTest`), sin modificar comportamiento productivo. |
| **android-pr-creator**  | Haiku  | `agents/android-pr-creator.md` | Crear una Pull Request: analiza coherencia entre tarea e implementación, evalúa testing, arquitectura KMP/CMP y genera la PR lista para GitHub. |
| **android-pr-reviewer** | Sonnet | `agents/android-pr-reviewer.md` | Revisar una Pull Request existente: corrección funcional, arquitectura, source sets KMP/CMP, calidad de tests y veredicto final. |
| **github-task-writer**  | Haiku  | `agents/github-task-writer.md` | Escribir una tarea de GitHub: descompone una idea en un issue ejecutable, con alcance, criterios de aceptación y notas técnicas KMP/CMP. |

---

## Cuándo usar senior-dev para review arquitectónico

Además del flujo de implementación, invoca android-senior-dev cuando:
- La PR toca múltiples módulos y la revisión requiere razonamiento arquitectónico profundo
- Hay una decisión de diseño ambigua que el PR reviewer no puede resolver solo

---

## Cómo añadir un nuevo agente

1. Crea `.claude/agents/{nombre}.md` con el rol, objetivo y proceso del agente
2. Añade una fila a la tabla de este fichero