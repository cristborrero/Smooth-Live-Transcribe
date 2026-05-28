# LIVETRANSCRIBE ANDROID — DOC 03 — Motor de lectura suave y anti-saltos

## Objetivo
Resolver el problema principal del producto: evitar que el texto “salte” abruptamente mientras se completa la transcripción. Diseñar e implementar una experiencia de lectura progresiva, suave y cómoda para seguir un discurso en tiempo real.

## Instrucción para Antigravity
Actúa como especialista en UX reading systems y Android Compose animations. Aquí debes concentrarte en la lógica visual y de presentación del texto. Este documento define el corazón del producto. No hagas soluciones superficiales. Piensa en lectura humana real durante 30-60 minutos.

## Problema a resolver
Cuando el motor de reconocimiento añade texto nuevo, el layout cambia y empuja el contenido de forma brusca. Eso rompe la concentración, fatiga la vista y vuelve incómoda la lectura continua.

## Principios UX obligatorios
- El usuario debe poder fijar la vista en una zona estable.
- El contenido debe moverse suavemente, no a tirones.
- El texto nuevo debe incorporarse con ritmo predecible.
- El cambio de línea no debe provocar reposicionamientos violentos.
- La lectura debe parecer un teleprónter suave, no un chat que brinca.

## Estrategia propuesta
Implementa y evalúa una estrategia basada en estos conceptos:
1. Separar texto confirmado y texto parcial.
2. Renderizar el bloque confirmado como cuerpo principal.
3. Renderizar parcial con estilo visual secundario para indicar que aún puede cambiar.
4. Mantener una ventana de lectura centrada o ligeramente superior al centro de la pantalla.
5. Aplicar auto-scroll animado con spring/tween suave solo cuando el contenido realmente exceda el área visible.
6. Evitar llamar scroll instantáneo salvo acciones manuales del usuario.
7. Debounce inteligente para actualizaciones parciales muy frecuentes.
8. Considerar “append batching”: agrupar pequeños cambios para reducir micro-saltos.

## Tareas
1. Diseña un ReadingEngine o PresentationEngine desacoplado de la UI.
2. Define modelo de datos para:
   - confirmedText
   - partialText
   - mergedVisibleText
   - scrollTarget
   - readingAnchor
3. Implementa algoritmo de actualización visual con suavizado.
4. Usa Compose de forma eficiente para no recomponer más de la cuenta.
5. Implementa contenedor de lectura con desplazamiento animado.
6. Permite modo auto-scroll ON/OFF.
7. Si el usuario toca o desplaza manualmente, pausa temporalmente el auto-follow.
8. Añade indicador para volver al modo seguir en vivo.
9. Estiliza parcial y final de forma distinta pero elegante.
10. Deja comentarios técnicos explicando por qué esta estrategia reduce fatiga visual.
11. Actualiza status_project.md.

## Criterios de aceptación
- El texto ya no da saltos abruptos al crecer.
- La lectura se siente progresiva y estable.
- Los resultados parciales no destruyen el flujo visual.
- El usuario puede retomar el seguimiento automático.
- El comportamiento es notoriamente mejor que una caja de texto simple.

## Entregables
- Motor de lectura suave funcionando.
- UI con auto-follow y pausa manual.
- status_project.md actualizado.
