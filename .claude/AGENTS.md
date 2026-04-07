# Agents Index

Agentes disponibles en este proyecto. Se invocan automáticamente por Claude cuando la tarea encaja con su descripción.

| Agente                  | Fichero | Cuándo usarlo |
|-------------------------|---|---|
| **android-pr-creator**  | `agents/android-pr-creator.md` | Crear una Pull Request: analiza coherencia entre tarea e implementación, evalúa testing, arquitectura KMP/CMP y genera la PR lista para GitHub. |
| **android-pr-reviewer** | `agents/android-pr-reviewer.md` | Revisar una Pull Request existente: corrección funcional, arquitectura, source sets KMP/CMP, calidad de tests y veredicto final. |
| **android-senior-dev**  | `agents/android-senior-dev.md` | Implementar una tarea técnica: código listo para producción en un proyecto KMP/CMP, con lógica en `commonMain` por defecto y decisiones arquitectónicas justificadas. |
| **android-testing**     | `agents/android-testing.md` | Añadir o mejorar tests: cobertura real ≥ 90%, tests en el source set correcto (`commonTest` / `androidUnitTest`), sin modificar comportamiento productivo. |
| **github-task-writer**  | `agents/github-task-writer.md` | Escribir una tarea de GitHub: descompone una idea en un issue ejecutable, con alcance, criterios de aceptación y notas técnicas KMP/CMP. |

---

## Cómo añadir un nuevo agente

1. Crea `.claude/agents/{nombre}.md` con el rol, objetivo y proceso del agente
2. Añade una fila a la tabla de este fichero
