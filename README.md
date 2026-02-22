# Anti-phishing & ciberfraude coach (MVP A)

Aplicacion Android educativa para ayudar a detectar senales de phishing/ciberfraude con reglas heuristicas explicables, sin IA generativa y con enfoque offline-first.

## 1. Problema
El usuario recibe mensajes sospechosos por SMS, email o chat y necesita una ayuda rapida para:

- detectar senales de riesgo,
- entender por que un mensaje parece fraude,
- tomar acciones seguras,
- entrenarse con casos practicos.

## 2. Solucion implementada (MVP A)
- Analizador de texto/enlaces con score 0-100 y semaforo.
- Explicacion de senales detectadas y recomendaciones concretas.
- Coach por escenarios con checklist local.
- Entrenamiento tipo quiz con feedback inmediato.
- Historial privado de metadatos (sin texto original).
- OCR local desde captura con revision manual editable antes de analizar.
- Exportacion de reporte Markdown (compartir por intent).
- Recursos oficiales informativos.
- Ajustes de privacidad extrema, bloqueo local y borrado de datos.

## 3. Arquitectura
Patron: `MVVM` con separacion por capas.

`UI(Fragment) -> ViewModel -> UseCase -> Repository -> Room/DataSource`

Estructura principal:
- `core/`: modelos comunes, utilidades, export, privacidad.
- `data/`: Room, repositorios, preferencias cifradas, seed local.
- `domain/`: modelos de dominio, casos de uso, heuristicas y quiz engine.
- `presentation/`: Fragments, ViewModels, adapters, navegacion.

## 4. Stack tecnico
- Kotlin + Android XML (sin Compose).
- Navigation Component + SafeArgs.
- Room + Coroutines + Flow.
- Material Components.
- `androidx.security:security-crypto`.
- Tests unitarios con JUnit.

## 5. Privacidad y seguridad
Medidas aplicadas:
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
- Ajustes sensibles en almacenamiento cifrado local.
- Bloqueo local opcional (biometria/credencial) enforced al entrar en Historial y Ajustes.

Disclaimer visible en la app:
`Herramienta educativa. No garantiza deteccion perfecta. No sustituye asesoramiento profesional.`

## 6. Modelo de datos (Room)
Cadena de 3 tablas relacionadas:

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
- keywords de suplantacion.

Semaforo:
- Verde `< 35`
- Amarillo `35-69`
- Rojo `>= 70`

## 8. Modulos funcionales
- Home
- Analizar
- Detalle analisis + export
- Coach
- Checklist escenario
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
.\gradlew.bat :app:installDebug
```

## 10. Pruebas
Matriz funcional y tecnica:
- `docs/Plan_de_Pruebas.md`

Tests unitarios activos:
- `RuleEngineTest`
- `SeedJsonParserTest`
- `QuizEngineTest`

## 11. Documentacion de memoria
- `docs/Modelo_ER.md`
- `docs/Casos_de_Uso.md`
- `docs/Diagrama_Clases.md`
- `docs/Planificacion_Gantt.md`
- `docs/Plan_de_Pruebas.md`
- `docs/Anexo_I_Manual_Instalacion.md`
- `docs/Anexo_II_Manual_Usuario.md`
- `docs/Privacidad_y_Seguridad.md`
- `docs/devlog/`

## 12. Capturas (placeholder)
Se recomienda anadir capturas en `docs/images/`:
- home.png
- analizar_resultado_rojo.png
- historial.png
- coach_checklist.png
- quiz_feedback.png
- ajustes_privacidad.png

## 13. Limitaciones del MVP A
- No integra backend ni deteccion remota.
- No incluye timeline de incidentes ni PDF avanzado.
- Reglas heuristicas locales, no cobertura exhaustiva de todos los fraudes.

## 14. Trabajo futuro
- Mejorar cobertura de reglas y calibracion por perfiles.
- Exportacion avanzada adicional (por ejemplo PDF).
