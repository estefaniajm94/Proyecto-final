# Privacidad y Seguridad (MVP A)

## 1. Principios aplicados
- Procesamiento local (offline-first).
- Minimizacion de datos: no se guarda texto original por defecto.
- Sin permisos invasivos (no lectura de SMS, no accesibilidad).
- Integracion por compartir sin lectura automatica de otras apps.
- Transparencia: senales explicables y advertencias educativas.

## 2. Datos que se guardan
En modo normal se guardan solo metadatos del analisis:
- fecha/hora,
- score y semaforo,
- origen/tipo de fuente,
- codigos de senales y recomendaciones,
- dominio sanitizado opcional.

No se guarda el contenido original del mensaje.
Tampoco se persiste el texto recibido por `ACTION_SEND` ni el texto resaltado en pantalla.

## 3. Privacidad extrema
- Si esta activada, no se persisten nuevos analisis en historial.
- El resultado se muestra en pantalla, pero sin guardado en Room.
- El borrado de datos elimina historial y resultados previos.

## 4. OCR local desde captura
- La imagen se procesa en el dispositivo con OCR on-device (ML Kit local).
- No se envia imagen ni texto OCR a servidores.
- No se persiste la imagen.
- El texto OCR pasa por revision manual editable antes de analizar.
- Si el usuario cancela la revision, el texto OCR se descarta.

## 5. Entrada por compartir desde otras apps
- La app puede recibir contenido `text/plain` via `ACTION_SEND`.
- La recepcion es bajo accion explicita del usuario en el chooser del sistema.
- El contenido compartido solo se precarga en `Analizar`; no se guarda automaticamente.
- No se solicitan permisos de lectura masiva sobre SMS, correo o mensajeria.

## 6. Bloqueo local
- Bloqueo biometrico/credencial opcional en Historial y Ajustes.
- Ajustes sensibles almacenados con cifrado local.

## 7. Exportacion de reportes Markdown
- El reporte se genera como archivo `.md` local en cache (`cache/reports`).
- El compartido se realiza con `FileProvider` (`content://`) y permiso temporal de lectura.
- El contenido exportado incluye solo:
  - metadatos del incidente,
  - senales detectadas,
  - recomendaciones por codigo,
  - disclaimer educativo.
- El reporte no incluye texto original analizado ni texto OCR.
