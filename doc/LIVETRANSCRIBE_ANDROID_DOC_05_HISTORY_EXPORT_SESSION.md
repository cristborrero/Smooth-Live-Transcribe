# LIVETRANSCRIBE ANDROID — DOC 05 — Historial, sesiones y exportación

## Objetivo
Añadir valor práctico guardando sesiones de transcripción para repasar después, especialmente útil para aprendizaje de inglés y estudio posterior.

## Instrucción para Antigravity
Actúa como Android engineer con enfoque en producto educativo. Implementa un sistema de sesiones simple, limpio y útil.

## Funcionalidades
1. Guardar sesión de transcripción.
2. Ver lista de sesiones pasadas.
3. Abrir detalle de una sesión.
4. Exportar texto en formato simple, por ejemplo .txt.
5. Opcionalmente permitir título editable por sesión.

## Tareas
1. Diseña modelo Session.
2. Añade persistencia local, preferiblemente Room si ya aporta valor real.
3. Crea pantallas:
   - SessionHistoryScreen
   - SessionDetailScreen
4. Implementa guardado manual o auto-guardado razonable.
5. Implementa compartir/exportar texto.
6. Mantén navegación limpia.
7. Actualiza status_project.md.

## Criterios de aceptación
- El usuario puede recuperar una sesión anterior.
- El texto exportado es usable.
- La arquitectura sigue limpia.

## Entregables
- Historial y detalle de sesiones.
- Exportación básica.
- status_project.md actualizado.
