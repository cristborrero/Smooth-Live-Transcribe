# LIVETRANSCRIBE ANDROID — MASTER PROMPT PARA ANTIGRAVITY

Usa este prompt como tarea principal en Antigravity para ejecutar el proyecto por documentos, en orden, con disciplina de ingeniería y documentación continua.

## Prompt

Contexto:
Estoy construyendo una app Android nativa de transcripción en tiempo real pensada para leer cómodamente mientras otra persona habla en vivo, especialmente para practicar inglés en iglesia, conferencias o clases.

El problema principal a resolver no es solo transcribir, sino evitar que el texto salte bruscamente cuando se actualiza. Quiero una experiencia de lectura suave tipo teleprónter.

En el repositorio existe una carpeta /doc con estos documentos:
- LIVETRANSCRIBE_ANDROID_DOC_01_VISION_ARCHITECTURE.md
- LIVETRANSCRIBE_ANDROID_DOC_02_AUDIO_PERMISSIONS_TRANSCRIPTION_BASE.md
- LIVETRANSCRIBE_ANDROID_DOC_03_SMOOTH_READING_ENGINE.md
- LIVETRANSCRIBE_ANDROID_DOC_04_READING_MODE_SETTINGS.md
- LIVETRANSCRIBE_ANDROID_DOC_05_HISTORY_EXPORT_SESSION.md
- LIVETRANSCRIBE_ANDROID_DOC_06_POLISH_TESTING_RELEASE.md

Tu misión:
1. Lee y ejecuta los documentos en estricto orden.
2. Antes de codificar cada etapa, resume el objetivo y valida que entiendes el alcance.
3. Implementa cada etapa sin romper lo ya construido.
4. Al finalizar cada documento, actualiza status_project.md con:
   - qué se completó
   - decisiones técnicas tomadas
   - problemas encontrados
   - deuda técnica
   - siguiente paso
5. Si falta alguna key, asset o decisión bloqueante, detente solo en ese punto y deja todo lo demás avanzado.
6. No improvises arquitectura caótica.
7. Prioriza Kotlin moderno, Compose, mantenibilidad y UX real.
8. El criterio principal del producto es que la lectura en vivo sea suave, estable y mucho más agradable que Live Transcribe estándar.
9. Al terminar todos los documentos, genera un informe final en status_project.md con:
   - porcentaje de avance
   - funcionalidades listas
   - pendientes
   - bugs conocidos
   - pasos exactos para probar en dispositivo Android
   - pasos para generar APK release

Reglas de calidad:
- No uses soluciones temporales si comprometen la UX principal.
- No acoples la UI directamente a APIs nativas sin abstracción razonable.
- Mantén arquitectura limpia pero sin sobreingeniería.
- Si propones una mejora distinta a la documentada, justifícala y documenta el cambio.
- Haz commits lógicos si el entorno lo permite.

Resultado esperado:
Proyecto Android funcional, documentado y preparado para uso real como MVP.
