# Privacidad y Seguridad (MVP A)

## 1. Principios aplicados
- Procesamiento local (offline-first).
- Minimizacion de datos: no se guarda texto original por defecto.
- Sin permisos invasivos (no lectura de SMS, no accesibilidad).
- Transparencia: senales explicables y advertencias educativas.

## 2. Datos que se guardan
En modo normal se guardan solo metadatos del analisis:
- fecha/hora,
- score y semaforo,
- origen/tipo de fuente,
- codigos de senales y recomendaciones,
- dominio sanitizado opcional.

No se guarda el contenido original del mensaje.

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

## 5. Bloqueo local
- Bloqueo biometrico/credencial opcional en Historial y Ajustes.
- Ajustes sensibles almacenados con cifrado local.
