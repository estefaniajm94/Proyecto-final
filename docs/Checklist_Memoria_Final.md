# Checklist memoria final y anexos

Estado base revisado contra el cierre tecnico del `2026-04-07`.

## 1. Documentacion ya alineada en el repo

- [x] `docs/Modelo_ER.md` actualizado a esquema Room `v3`.
- [x] `docs/Modelo_ER.md` refleja `users.avatarId` y la migracion `2 -> 3`.
- [x] `docs/Modelo_ER.md` usa los enums persistidos reales (`sourceType` y `scenarioType`).
- [x] `docs/Anexo_I_Manual_Instalacion.md` refleja esquema `v3`.
- [x] `docs/Anexo_I_Manual_Instalacion.md` deja `installDebug` como `FAIL` por entorno en el cierre.
- [x] `docs/Planificacion_Gantt.md` alinea su evidencia final con el cierre tecnico del `2026-04-07`.

## 2. Pendientes reales fuera del repo o dependientes de dispositivo

- [ ] Ejecutar PF-12 `Borrar datos` en emulador/dispositivo y guardar evidencia.
- [ ] Ejecutar PF-22 `Cancelacion de autenticacion` en emulador/dispositivo y guardar evidencia.
- [ ] Ejecutar PF-24 `OCR con imagen borrosa` en emulador/dispositivo y guardar evidencia.
- [ ] Obtener una ejecucion real de `:app:installDebug` con dispositivo conectado si se quiere cerrar el anexo con instalacion validada extremo a extremo.

## 3. Material pendiente para memoria/anexos

- [ ] Versionar capturas finales en `docs/images/`.
- [ ] Preparar al menos estas capturas: `login_local.png`, `register_local.png`, `home.png`, `analizar_resultado_rojo.png`, `analizar_resultado_destacado.png`, `historial.png`, `guia_rapida_lista.png`, `guia_rapida_detalle.png`, `quiz_feedback.png`, `ajustes_privacidad.png`.
- [ ] Insertar en la memoria una referencia explicita a que el modelo de datos vigente es Room `v3`.
- [ ] Mencionar en la memoria que `avatarId` forma parte de `users` desde la migracion `2 -> 3`.

## 4. Criterio de consistencia antes de entregar

- [ ] Mantener la misma fecha de cierre (`2026-04-07`) en memoria, anexos y plan de pruebas.
- [ ] Mantener el mismo estado final de `installDebug` en todos los documentos: `FAIL` por entorno si no hubo dispositivo conectado, o actualizar todos a la vez si se repite la instalacion con exito.
- [ ] No volver a citar esquema `v2` en documentos de estado actual.
