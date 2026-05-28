# LIVETRANSCRIBE ANDROID — DOC 02 — Permisos, captura de audio y transcripción base

## Objetivo
Implementar la primera versión funcional de captura de voz y transcripción en vivo usando APIs nativas de Android, dejando la app lista para mostrar texto recibido en tiempo real.

## Instrucción para Antigravity
Actúa como Android engineer senior. En este documento debes implementar una base estable de captura y reconocimiento de voz. Prioriza robustez, manejo de errores, lifecycle y estados claros por encima de añadir muchas opciones visuales.

## Resultado esperado
Al terminar este documento debe ser posible:
- Solicitar permiso de micrófono.
- Iniciar escucha.
- Recibir texto parcial y final.
- Reflejar estados de reconocimiento en UI.
- Manejar errores básicos y reinicio de sesión.

## Tareas
1. Añade permisos necesarios en AndroidManifest.
2. Implementa flujo de solicitud de permiso en Compose.
3. Crea wrapper limpio para SpeechRecognizer.
4. Expón eventos mediante Flow o callback bien adaptado al ViewModel.
5. Distingue claramente estos estados:
   - Idle
   - RequestingPermission
   - Ready
   - Listening
   - PartialResult
   - FinalResult
   - Error
   - NoSpeechDetected
6. Distingue entre texto parcial y texto final.
7. Mantén buffer de transcripción en memoria.
8. Evita fugas de recursos del recognizer.
9. Maneja reinicio cuando el recognizer termine o falle recuperablemente.
10. Refleja visualmente estado actual en la pantalla principal.
11. Actualiza status_project.md.

## Consideraciones clave
- El texto parcial no debe consolidarse como definitivo hasta confirmación.
- El sistema debe quedar listo para que en el siguiente documento se aplique el motor de scroll suave.
- Diseña interfaces limpias en domain/data para no acoplar la UI a SpeechRecognizer.

## Criterios de aceptación
- Botón iniciar escucha funciona.
- Se ve texto parcial y luego texto final.
- Si se niega permiso, la UI lo comunica con claridad.
- Si ocurre error, existe mensaje y recuperación razonable.
- El recognizer se libera correctamente.

## Entregables
- Implementación base de reconocimiento.
- Manejo de permisos.
- UI de estados.
- status_project.md actualizado.
