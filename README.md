# Anti-phishing & ciberfraude coach (MVP A)

Aplicacion Android educativa para ayudar a detectar senales de phishing/ciberfraude con reglas heuristicas explicables, sin IA generativa y con enfoque offline-first.

Estado de cierre tecnico verificado el `2026-04-07`:
- `:app:assembleDebug` OK
- `:app:testDebugUnitTest` OK (`125/125`)
- `:app:lintDebug` OK (`0 errores`)
- `:app:installDebug` requiere emulador o dispositivo conectado

## 1. Problema
El usuario recibe mensajes sospechosos por SMS, email o chat y necesita una ayuda rapida para:

- detectar senales de riesgo,
- entender por que un mensaje parece fraude,
- tomar acciones seguras,
- entrenarse con casos practicos,
- proteger su historial local con una cuenta minima en el dispositivo.

## 2. Solucion implementada (MVP A)
- Flujo de autenticacion local con registro, login y sesion persistida en el dispositivo.
- Analizador de texto/enlaces con score `0-100` y semaforo.
- Explicacion de senales detectadas y recomendaciones concretas.
- Lectura rapida del riesgo con foco en enlace o mensaje.
- Desglose local del enlace: dominio real, esquema, ruta, parametros y observaciones.
- Resaltado visual de frases sospechosas detectadas en el texto analizado.
- Plan de accion inmediato ("que hacer ahora") segun semaforo, senales y recomendaciones.
- Entrada por compartir desde otras apps (`ACTION_SEND` para `text/plain`).
- Guia rapida por situacion con senales, acciones recomendadas y cierre practico.
- Entrenamiento tipo quiz por niveles (principiante, intermedio y avanzado) con feedback inmediato.
- Historial privado de metadatos (sin texto original).
- OCR local desde captura con revision manual editable antes de analizar.
- Exportacion de reporte Markdown (compartir por intent).
- Recursos oficiales informativos.
- Ajustes de privacidad extrema, bloqueo local, borrado de datos y cierre de sesion.

## 3. Arquitectura
Patron: `MVVM` con separacion por capas.

`UI(Fragment) -> ViewModel -> UseCase -> Repository -> Room/DataSource`

Estructura principal:
- `core/`: modelos comunes, utilidades, export, privacidad.
- `data/`: Room, repositorios, preferencias cifradas, seed local.
- `domain/`: modelos de dominio, casos de uso, heuristicas y quiz engine.
- `presentation/`: Fragments, ViewModels, adapters, navegacion y auth local.

## 4. Stack tecnico
- Kotlin + Android XML (sin Compose).
- Navigation Component + SafeArgs.
- Room + Coroutines + Flow.
- Material Components.
- `androidx.security:security-crypto`.
- Tests unitarios con JUnit.

## 5. Privacidad y seguridad
Medidas aplicadas:
- Autenticacion local sin backend; la cuenta vive solo en el dispositivo.
- Contrasena almacenada como hash SHA-256, no en texto plano.
- No se solicitan permisos invasivos (sin lectura SMS, sin accesibilidad).
- Por defecto no se guarda texto original analizado.
- Solo se persisten metadatos:
1. fecha/hora,
2. score/semaforo,
3. origen/tipo,
4. codigos de senales y recomendaciones,
5. dominio sanitizado opcional.
- Modo `Privacidad extrema`: no se guarda ningun analisis nuevo.
- `Borrar datos`: elimina datos locales de historial.
- Ajustes sensibles y sesion local en almacenamiento cifrado.
- Bloqueo local opcional (biometria/credencial) enforced al entrar en Historial y Ajustes.
- Copias de seguridad del sistema explicitamente deshabilitadas para no extraer datos locales sensibles.

Disclaimer visible en la app:
`Herramienta educativa. No garantiza deteccion perfecta. No sustituye asesoramiento profesional.`

## 6. Modelo de datos (Room)
Cadena principal de 3 tablas de analisis mas una tabla de usuarios locales:

- `UserEntity`
- `IncidentEntity (1)`
- `AnalysisResultEntity (1)` por incidente
- `DetectedSignalEntity (N)` por resultado

Ver detalle en `docs/Modelo_ER.md`.

## 7. Heuristicas actuales del analizador
Reglas explicables implementadas:
- urgencia/amenaza,
- premios/regalos,
- peticion de datos sensibles,
- enlace HTTP sin HTTPS,
- acortadores,
- dominio con patrones sospechosos,
- TLD sospechoso,
- simbolo `@` en URL,
- subdominio enganoso,
- esquemas no web / deep links (`intent:`, `javascript:`, `data:`...),
- archivos ejecutables o instalables,
- host en IP,
- punycode / caracteres no ASCII / mezcla de alfabetos,
- URL anidada en parametros,
- URL muy larga, con muchos parametros o tokens de alta entropia,
- keywords de suplantacion.

Semaforo:
- Verde `< 35`
- Amarillo `35-69`
- Rojo `>= 70`

## 8. Modulos funcionales
- AuthGate
- Login
- Registro local
- Home
- Analizar
- Detalle analisis + export
- Guia rapida
- Detalle de guia
- Training inicio
- Quiz
- Resultado quiz
- Historial
- Detalle historial
- Ajustes
- Recursos oficiales

## 9. Instalacion y ejecucion rapida
Pre-requisitos:
- Android Studio + SDK Android 34.
- Emulador/dispositivo Android 7.0+.

Comandos:
```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:installDebug
```

Primera ejecucion:
- Si no existe sesion local activa, la app abre `Login`.
- Si no existe cuenta local previa, el usuario puede crearla desde `Crear cuenta local`.

Utilidad auxiliar para emulador:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\paste_clipboard_to_emulator.ps1
```
Permite enviar el texto actual del portapapeles de Windows al campo enfocado del emulador.

## 10. Pruebas
Matriz funcional y tecnica:
- `docs/Plan_de_Pruebas.md`

Validacion automatizada verificada en el cierre:
- `125` tests unitarios en verde.
- `lintDebug` en verde (`0` errores).
- `installDebug` probado: requiere dispositivo conectado.

Suites unitarias activas:
- `RuleEngineTest`
- `SeedJsonParserTest`
- `QuizEngineTest`
- `TrainingQuestionFiltersTest`
- `AnalyzeInputInsightBuilderTest`
- `AnalyzeActionPlanBuilderTest`
- `AnalyzeViewModelOcrTest`
- `ClearLocalDataUseCaseTest`
- `HistoryViewModelTest`
- `ReportExporterTest`
- `SettingsViewModelTest`
- `TrafficLightUiTest`
- `UrlNormalizerTest`
- `UrlHeuristicsExtendedTest`
- `PhishingSmishingSemanticTest`
- `IdnHomoglyphRulesTest`
- `AvatarCatalogTest`

## 11. Documentacion de memoria
- `docs/Modelo_ER.md`
- `docs/Casos_de_Uso.md`
- `docs/Diagrama_Clases.md`
- `docs/Planificacion_Gantt.md`
- `docs/Plan_de_Pruebas.md`
- `docs/Mockups_Wireframes.md`
- `docs/Anexo_I_Manual_Instalacion.md`
- `docs/Anexo_II_Manual_Usuario.md`
- `docs/Privacidad_y_Seguridad.md`
- `docs/devlog/`

## 12. Capturas para memoria
La carpeta `docs/images/` no contiene aun capturas versionadas en este workspace.
Para la memoria/entrega final se recomienda adjuntar al menos:
- login_local.png
- register_local.png
- home.png
- analizar_resultado_rojo.png
- analizar_resultado_destacado.png
- historial.png
- guia_rapida_lista.png
- guia_rapida_detalle.png
- quiz_feedback.png
- ajustes_privacidad.png

## 13. Limitaciones del MVP A
- Sin backend, sincronizacion ni recuperacion de cuenta.
- Un unico contexto local por dispositivo/emulador.
- No integra backend ni deteccion remota.
- No incluye timeline de incidentes ni PDF avanzado.
- Reglas heuristicas locales, no cobertura exhaustiva de todos los fraudes.
- El progreso del entrenamiento no se persiste entre sesiones.
- No hay `androidTest` instrumentados en esta iteracion final.

## 14. Trabajo futuro
- Mejorar cobertura de reglas y calibracion por perfiles.
- Anadir tests unitarios especificos para autenticacion local.
- Incorporar pruebas instrumentadas en emulador para OCR, biometria y borrado local.
- Persistir progreso del quiz y resumen de entrenamiento en Home.
- Exportacion avanzada adicional (por ejemplo PDF).
- Valorar sincronizacion o multiusuario solo si el alcance del producto lo justificara.
