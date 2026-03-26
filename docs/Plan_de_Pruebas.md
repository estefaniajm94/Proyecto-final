# Plan de Pruebas (MVP A)

## 1. Objetivo
Validar que el MVP A cumple funcionalidad, privacidad y consistencia tecnica del flujo:

- Analisis heuristico explicable (score, semaforo, senales, recomendaciones).
- Lectura rapida, desglose del enlace y plan de accion contextual.
- Entrada por compartir desde otras apps (`ACTION_SEND text/plain`).
- Guardado de metadatos sin texto original.
- Respeto de privacidad extrema.
- Bloqueo local enforced en Historial y Ajustes.
- OCR local desde captura con revision editable previa al analisis.
- Historial y detalle por `incidentId`.
- Exportacion Markdown a fichero real por compartir (`FileProvider`).
- Carga local de escenarios Coach desde `assets`.
- Ejecucion de quiz con feedback desde `assets`.
- Home tipo dashboard con resumen del ultimo analisis.
- Historial con busqueda, filtros y orden.

## 2. Entorno de ejecucion
- SO: Windows 10/11.
- Android Studio: Hedgehog o superior.
- Emulador recomendado: `Medium_Phone_API_36.1`.
- SDK: compile/target 34.
- minSdk: 24.

## 3. Comandos de verificacion tecnica
```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:installDebug
```

## 4. Matriz funcional (OK/FAIL)
Usar `OK` cuando el resultado real coincide con el esperado. En caso contrario, marcar `FAIL` y anotar evidencia.

| ID | Caso | Pasos / Entrada | Resultado esperado | Estado |
|---|---|---|---|---|
| PF-01 | Analisis bajo riesgo | Entrada: `https://github.com/...` | Score 0, semaforo VERDE, sin senales | OK |
| PF-02 | Acortador aislado | Entrada: `https://bit.ly/abc` | Score 18, semaforo VERDE, senal `URL_SHORTENER` | OK |
| PF-03 | Riesgo alto compuesto | Entrada: `https://bit.ly/abc` + `http://cliente@banco-seguro-1234.top` | Score 92, semaforo ROJO, multiples senales URL/impersonacion | OK |
| PF-04 | Recomendaciones coherentes | Ejecutar PF-02 y PF-03 | Recomendaciones cambian segun codigos y score | OK |
| PF-05 | Tipo de fuente LINK | Entrada solo URL | `sourceType = LINK` | OK |
| PF-06 | Tipo de fuente MIXED | Entrada texto + URL | `sourceType = MIXED` | ok |
| PF-07 | Guardado normal | Privacidad extrema OFF + analizar | Se guarda en historial con `incidentId` | OK |
| PF-08 | No guardar en privacidad extrema | Privacidad extrema ON + analizar | No aparece nuevo registro en historial | ok |
| PF-09 | Historial lista real | Ir a Historial tras varios analisis | Lista con fecha, score, semaforo y sourceApp | OK |
| PF-10 | Detalle por SafeArgs | Abrir item de historial | Muestra datos completos del incidente seleccionado | OK |
| PF-11 | Exportar Markdown | En detalle de analisis pulsar Exportar | Abre chooser Android con reporte Markdown | OK |
| PF-12 | Borrar datos | Ajustes > Borrar datos | Historial queda vacio | PENDIENTE |
| PF-13 | No texto original persistido | Revisar modelo y datos almacenados | Solo metadatos (sin contenido original) | OK |
| PF-14 | Mensaje legal visible | Home/Analizar | Disclaimer educativo visible | OK |
| PF-15 | Lista de escenarios Coach | Home > Coach | Se muestran escenarios de `coach_scenarios.json` | OK |
| PF-16 | Checklist de escenario | Coach > Abrir checklist | Se muestran items y contador de marcados | OK |
| PF-17 | Quiz con feedback | Training > Comenzar > responder | Muestra correcto/incorrecto + explicacion por pregunta | OK |
| PF-18 | Resultado final quiz | Completar todas las preguntas | Muestra aciertos totales y porcentaje | OK |
| PF-19 | Bloqueo local en Historial | Ajustes: activar bloqueo local. Ir a Historial | Solicita biometria/credencial antes de mostrar datos | OK |
| PF-20 | Bloqueo local en Ajustes | Con bloqueo local activo, salir y volver a Ajustes | Solicita biometria/credencial al entrar | OK |
| PF-21 | Cancelacion de autenticacion | Cancelar prompt en Historial o Ajustes | Se bloquea acceso y vuelve a pantalla anterior | PENDIENTE |
| PF-22 | OCR local con captura real | Analizar > Analizar desde captura > elegir imagen legible (SMS/email/web) | Se extrae texto local, se muestra dialogo editable y permite analizar | OK |
| PF-23 | OCR con imagen borrosa | Repetir PF-22 con captura borrosa | Puede devolver texto incompleto o error claro, sin crash | PENDIENTE |
| PF-24 | Cancelar OCR o revision | Cancelar picker o pulsar Cancelar en dialogo OCR | Se descarta texto OCR y no se analiza ni se persiste nada | OK |
| PF-25 | Home dashboard visible | Abrir Home | Se muestran tarjetas y bloques de ultimo analisis/entrenamiento | OK |
| PF-26 | Ultimo analisis en Home | Tener al menos 1 incidente guardado y abrir Home | Muestra fecha, score, semaforo y boton de detalle | OK |
| PF-27 | Historial filtros y busqueda | En Historial, usar query + chips + orden | Lista reacciona en vivo sin recargar pantalla | OK |
| PF-28 | Export a fichero `.md` real | En detalle, pulsar Exportar | Comparte `content://...` con archivo Markdown real | OK |
| PF-29 | Export sin texto original | Revisar contenido del `.md` generado | Solo metadatos/senales/recomendaciones/disclaimer | OK |
| PF-30 | Compartir desde otra app | Enviar `text/plain` a la app desde navegador/WhatsApp/email | Se abre `Analizar` con texto precargado y sin autoanalizar | OK |
| PF-31 | Lectura rapida y desglose | Analizar enlace sospechoso con dominio no oficial | Muestra lectura rapida, dominio real, esquema, ruta y observaciones | OK |
| PF-32 | Resaltado de frases sospechosas | Analizar mensaje con urgencia/premio/password | El texto mostrado resalta visualmente las frases detectadas | OK |
| PF-33 | Plan de accion en Analizar | Analizar caso amarillo/rojo | Se muestran pasos numerados adaptados al riesgo | OK |
| PF-34 | Plan de accion en detalle | Guardar incidente y abrir detalle | El detalle reconstruye y muestra el plan de accion | OK |
| PF-35 | Atajo a recursos oficiales | Caso con recomendacion de verificacion oficial | Aparece boton para abrir `Recursos oficiales` | OK |

## 5. Matriz tecnica automatizada
Cobertura actual:
- `RuleEngineTest` (10 casos).
- `SeedJsonParserTest` (4 casos).
- `QuizEngineTest` (5 casos).
- `AnalyzeViewModelOcrTest` (2 casos de estado OCR).
- `HistoryViewModelTest` (4 casos de filtro/orden/estado vacio).
- `ReportExporterTest` (2 casos de markdown y escritura de fichero).
- `AnalyzeInputInsightBuilderTest` (2 casos de desglose y frases sospechosas).
- `AnalyzeActionPlanBuilderTest` (2 casos de plan de accion).

| ID | Test unitario | Esperado |
|---|---|---|
| PT-01 | urgencia | Detecta `URGENCY_THREAT` |
| PT-02 | premio | Detecta `PRIZE_GIFT` |
| PT-03 | datos sensibles | Detecta `SENSITIVE_DATA_REQUEST` |
| PT-04 | http inseguro | Detecta `URL_HTTP_INSECURE` |
| PT-05 | acortador | Detecta `URL_SHORTENER` |
| PT-06 | patron dominio | Detecta `URL_DOMAIN_PATTERN` |
| PT-07 | tld + arroba | Detecta `URL_SUSPICIOUS_TLD` y `URL_AT_SYMBOL` |
| PT-08 | subdominio enganoso | Detecta `URL_DECEPTIVE_SUBDOMAIN` |
| PT-09 | inferencia sourceType | Detecta `LINK` y dominio sanitizado |
| PT-10 | umbral rojo | Score >= 70 => `TrafficLight.RED` |
| PT-11 | parser coach valido | Parsea escenarios seed validos |
| PT-12 | parser coach invalido | Filtra entradas sin id/titulo/checklist |
| PT-13 | parser training valido | Parsea preguntas validas |
| PT-14 | parser training invalido | Filtra preguntas con indice fuera de rango |
| PT-15 | quiz score correcto | Suma puntos solo en respuestas correctas |
| PT-16 | quiz no doble respuesta | No permite responder dos veces misma pregunta |
| PT-17 | quiz avance y completado | Solo avanza tras responder y finaliza al final |
| PT-18 | VM OCR exito | `OcrReady` -> confirmar -> `ResultReady` |
| PT-19 | VM OCR error | Error OCR -> `AnalyzeFlowState.Error` |
| PT-20 | Historial query | Filtra por titulo/dominio en memoria |
| PT-21 | Historial filtro semaforo | Filtra por chip Verde/Amarillo/Rojo |
| PT-22 | Historial orden riesgo | Ordena por score descendente |
| PT-23 | Historial vacio extremo | Muestra mensaje de privacidad extrema |
| PT-24 | Export markdown fichero | Crea archivo > 0 bytes y sin texto original |
| PT-25 | Insight builder URL | Extrae dominio y observaciones del enlace |
| PT-26 | Insight builder frases | Detecta frases sospechosas por categorias |
| PT-27 | Action plan rojo | Prioriza bloqueo, verificacion y no compartir credenciales |
| PT-28 | Action plan verde | Mantiene guidance ligera y sin recursos forzados |

## 6. Criterios de aceptacion del bloque Analizar
- `assembleDebug`, `testDebugUnitTest` e `installDebug` finalizan en exito.
- PF-01..PF-05 y PF-07, PF-09..PF-11 en `OK`.
- Sin permisos invasivos (no lectura SMS, no accesibilidad).
- Persistencia limitada a metadatos definidos.
- PF-30..PF-35 en `OK`.

## 7. Criterios de aceptacion Coach + Training
- PF-15..PF-18 en `OK`.
- Sin acceso a red para cargar contenido educativo base.
- Quiz finaliza con resultado consistente (`score <= total`).

## 8. Criterios de aceptacion OCR local
- PF-22 y PF-24 en `OK`.
- La revision manual editable aparece antes de analizar texto OCR.
- No se persiste imagen ni texto OCR.

## 9. Criterios de aceptacion seguridad local
- PF-19 y PF-20 en `OK`.
- Si el prompt falla o se cancela, no se muestran datos protegidos.

## 10. Registro de incidencias (plantilla)
Usar esta plantilla en cada fallo:

- ID prueba:
- Fecha:
- Dispositivo:
- Resultado observado:
- Resultado esperado:
- Evidencia (captura/log):
- Causa raiz:
- Accion correctiva:
- Estado final:
