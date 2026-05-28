# LIVETRANSCRIBE ANDROID — DOC 01 — Visión, alcance y arquitectura base

## Objetivo
Construir una app Android nativa enfocada en transcripción en tiempo real para lectura cómoda mientras otra persona habla (conferencista, pastor, profesor, etc.), optimizada para aprendizaje de inglés. El diferencial principal no es solo transcribir, sino presentar el texto con una experiencia visual fluida, sin saltos bruscos, con desplazamiento suave tipo teleprónter.

## Instrucción para Antigravity
Actúa como arquitecto senior Android + product engineer. Tu misión en este documento es planificar y dejar creada la base técnica del proyecto, sin implementar todavía toda la lógica final de transcripción. Debes priorizar claridad, mantenibilidad, modularidad y buena UX de lectura prolongada.

## Resultado esperado
Al terminar este documento debe existir:
- Proyecto Android nativo listo para compilar.
- Configuración moderna recomendada para 2026.
- Arquitectura limpia por capas.
- Decisiones técnicas documentadas.
- README inicial.
- status_project.md inicial con lo hecho y lo pendiente.

## Stack recomendado
Usa este stack salvo que exista una razón fuerte y documentada para mejorar algún punto:
- Kotlin
- Jetpack Compose
- MVVM + Clean Architecture ligera
- Navigation Compose
- ViewModel
- Kotlin Coroutines + Flow
- Material 3
- DataStore para preferencias
- Room solo si luego hace falta historial persistente complejo
- SpeechRecognizer de Android como base inicial
- Min SDK razonable para buena compatibilidad moderna
- Target SDK actualizado
- Gradle Kotlin DSL
- Hilt para inyección de dependencias

## Requisitos funcionales iniciales
1. Pantalla principal de transcripción.
2. Botón iniciar/pausar escucha.
3. Área grande de texto para lectura.
4. Modo lectura suave sin saltos bruscos.
5. Ajustes de tamaño de letra, velocidad de desplazamiento visual, contraste y tema.
6. Estado visible de micrófono, escuchando, error y silencio.
7. Preparada para soportar historial y exportación después.

## Requisitos UX críticos
- La app debe sentirse como un lector/teleprónter, no como un simple TextView que se refresca.
- Prohibido que al entrar nuevo texto el contenido “salte” violentamente.
- El texto debe reacomodarse con animación suave y legible.
- Debe existir una estrategia explícita para diferenciar texto provisional vs texto confirmado.
- La lectura debe minimizar cansancio visual.

## Tareas
1. Crea el proyecto Android con nombre claro, por ejemplo: Smooth Live Transcribe o similar.
2. Define package name limpio y profesional.
3. Configura módulos si lo ves útil, pero evita sobreingeniería. Puede empezar en app único módulo con paquetes bien separados.
4. Estructura paquetes sugerida:
   - app
   - core
   - data
   - domain
   - ui
   - feature/transcription
   - feature/settings
5. Implementa tema base Material 3 con modo claro/oscuro.
6. Configura Hilt.
7. Configura Navigation Compose.
8. Crea pantallas placeholder:
   - TranscriptionScreen
   - SettingsScreen
9. Crea ViewModels base y contratos UI state.
10. Documenta arquitectura en README.
11. Genera status_project.md con:
   - realizado
   - decisiones técnicas
   - pendientes próximos docs

## Criterios de aceptación
- El proyecto compila.
- Se puede abrir en Android Studio sin errores.
- Existe navegación básica.
- El código sigue una estructura clara.
- El README explica por qué Compose y por qué esta arquitectura.
- status_project.md queda actualizado.

## Restricciones
- No metas todavía hacks visuales rápidos.
- No improvises código espagueti para “solo hacer que funcione”.
- No añadas librerías innecesarias.
- No implementes features futuras si no son necesarias para esta etapa.

## Entregables
- Código funcional del setup.
- README.md.
- status_project.md.
- Comentario final con lo implementado y riesgos detectados.
