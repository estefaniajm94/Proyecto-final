# Justificacion tecnica — AntiPhishingCoach

Documento de referencia para la memoria del TFG.
Recoge las decisiones tecnicas reales del proyecto, por que se tomaron
y cuales se descartaron.

---

## 1. Decisiones de arquitectura

### Clean Architecture adaptada al MVP

El proyecto separa el codigo en cuatro capas:

- **core**: tipos primitivos compartidos (TrafficLight, SourceType, SourceApp),
  utilidades transversales (DispatcherProvider) y helpers de privacidad (LocalAuthManager).
- **data**: implementaciones de repositorios, entidades Room, DAOs, OCR con ML Kit,
  carga de datos seed desde assets y exportacion de reportes.
- **domain**: interfaces de repositorio, modelos de negocio, casos de uso,
  motor heuristico y motor de quiz.
- **presentation**: Fragments, ViewModels, factories, estados de UI y adaptadores.

La regla de dependencias se cumple: domain no depende de Android framework
(salvo limitaciones documentadas en la seccion 6).

### Service Locator en vez de Hilt

Se usa `AppContainer` como service locator manual con inicializacion lazy.
Razon: Hilt requiere plugin de compilador, anotaciones y configuracion
que aumentan la complejidad del build sin aportar valor diferencial
en un proyecto de este tamano. AppContainer cumple la misma funcion
(centralizar dependencias, permitir lazy init, facilitar testing)
con menos codigo y sin dependencias externas adicionales.

Para un proyecto comercial o con multiples modulos, Hilt seria la opcion natural.

---

## 2. Organizacion por capas

Cada capa tiene un rol claro:

| Capa | Responsabilidad | Ejemplo |
|------|----------------|---------|
| core | Tipos y utilidades sin logica de negocio | TrafficLight, DispatcherProvider |
| data | Acceso a datos, persistencia, APIs externas | IncidentRepositoryImpl, MlKitOcrRepository |
| domain | Logica de negocio pura | RuleEngine, AnalyzeInputUseCase |
| presentation | UI, navegacion, estado visual | HomeFragment, AnalyzeViewModel |

Las interfaces de repositorio viven en domain. Las implementaciones en data.
Los casos de uso reciben repositorios por constructor.

---

## 3. Decisiones de persistencia y datos

### Room con esquema en 4 tablas (3 relacionadas + users)

- **incidents**: metadatos del analisis (sin texto original).
- **analysis_results**: resultado agregado (score, semaforo, dominio sanitizado).
- **detected_signals**: senales individuales detectadas por regla.
- **users**: cuenta local del dispositivo para registro, login y avatar.

Las relaciones usan `ForeignKey` con `onDelete = CASCADE`:
al borrar un incidente, se eliminan automaticamente sus resultados y senales.

### fallbackToDestructiveMigration

Se mantiene activo aunque el esquema actual ya esta en version `3`.
En el estado actual si existe una migracion explicita `2 -> 3`,
que anade `avatarId` a la tabla `users`.
`fallbackToDestructiveMigration()` queda como red de seguridad para cambios
futuros no cubiertos por migraciones. Para el alcance del TFG sigue siendo
una decision deliberada de simplicidad, pero en un proyecto con continuidad
de datos reales habria que completar todas las migraciones necesarias.

### exportSchema = true

Room genera el esquema JSON en `/schemas`. Permite rastrear cambios de esquema
entre versiones y facilita la creacion de migraciones futuras.

### EncryptedSharedPreferences para flags sensibles

Las banderas de privacidad extrema y bloqueo local se almacenan cifradas
con AES256-GCM. Se usa la API `MasterKeys` (funcional aunque marcada
como deprecated en favor de `MasterKey.Builder`). El nivel de seguridad
real es identico; la diferencia es solo de modernidad de la API.

---

## 4. Decisiones de seguridad y privacidad

### Motor heuristico local sin IA y sin red

El analisis se ejecuta integramente en el dispositivo.
No se envian datos a ningun servidor. Razon principal: privacidad por diseno.
El usuario puede saber exactamente que datos procesa la app y donde quedan.

Ademas, un motor heuristico transparente permite explicar al usuario
*por que* algo es sospechoso, no solo *si lo es*. Cada senal tiene titulo,
explicacion y peso visible. Esto es coherente con el objetivo educativo
de la app.

### OCR on-device con ML Kit

Las imagenes se procesan localmente con ML Kit Text Recognition.
No se persisten ni se envian. La interfaz `OcrRepository` esta en domain
y la implementacion en data, cumpliendo la separacion de capas.

### Modo privacidad extrema

Cuando esta activo, el analisis se ejecuta pero no se guarda nada en base de datos.
El usuario puede usar la herramienta sin dejar rastro local.

### Autenticacion biometrica

Historial y Ajustes pueden protegerse con biometria o credencial del dispositivo.
Se usa `BiometricPrompt` con `BIOMETRIC_WEAK | DEVICE_CREDENTIAL` para que
usuarios sin lector de huellas puedan usar PIN o patron.

---

## 5. Decisiones de UI/UX relevantes

### Navegacion jerarquica desde Home

Se usa Navigation Component con Safe Args. Home actua como hub central
y todas las secciones parten de ahi. No se usa Bottom Navigation Bar
porque el flujo de la app es de tarea (analizar, entrenar, consultar),
no de navegacion lateral continua. El usuario completa una accion
y vuelve al panel principal.

### BaseFragment con ViewBinding

Todos los Fragments extienden `BaseFragment<T>`, que gestiona el ciclo de vida
del binding (inflate en onCreateView, limpieza en onDestroyView).
Evita leaks de memoria por ViewBinding retenido y elimina boilerplate.

### StateFlow + sealed interface para estado de UI

Los ViewModels exponen `StateFlow<UiState>` inmutable.
El flujo de analisis usa una `sealed interface AnalyzeFlowState`
que modela explicitamente cada paso (idle, OCR, analisis, resultado, error).
Esto hace imposible representar combinaciones de estado invalidas.

### StringResolver para testabilidad de ViewModels

La interfaz `StringResolver` desacopla los ViewModels de `Context.getString()`.
Permite escribir tests unitarios de ViewModels que verifican mensajes
sin necesidad de Robolectric ni emulador. `AndroidStringResolver` es la
implementacion real; en tests se usa una implementacion que devuelve claves.

---

## 6. Limitaciones aceptadas conscientemente

### isMinifyEnabled = false en release

El build de release no aplica ProGuard/R8. El APK no esta ofuscado.
Para un TFG sin publicacion en Play Store no supone riesgo.
En un proyecto real habria que configurar reglas de ProGuard
para Room, Gson y ML Kit.

### MasterKeys API legada

`MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)` esta deprecated
en favor de `MasterKey.Builder`. El cifrado subyacente es el mismo (AES256-GCM).
No se cambia porque no aporta mejora de seguridad real y el cambio
podria introducir problemas de compatibilidad en dispositivos antiguos
sin beneficio tangible.

### Duplicacion parcial entre AnalysisDetailFragment y HistoryDetailFragment

Ambos Fragments muestran el detalle de un incidente con logica de renderizado
similar. Se mantienen separados porque:
- Tienen layouts distintos (analisis incluye boton de exportar, historial no).
- Representan flujos distintos (post-analisis vs. consulta de historial).
- Unificarlos requeriria un layout condicional y argumentos extra en Safe Args
  que anadiririan complejidad sin eliminar del todo la duplicacion.

Si el proyecto creciera con mas variantes de detalle, la unificacion
seria recomendable. Con dos consumidores, la duplicacion es tolerable.

### Duplicacion de logica biometrica entre HistoryFragment y SettingsFragment

Ambos implementan `enforceProtectedAccess()` y `requestAuthentication()`
con estructura similar pero diferencias reales (estado locked visible
vs. controles deshabilitados, callbacks distintos). Extraer un helper
requeriria parametrizar esas diferencias con lambdas, generando
una abstraccion mas compleja que la duplicacion actual.
Con dos consumidores, no compensa.

### Progreso de entrenamiento no persistido

El quiz funciona en memoria. No se guarda el resultado del ultimo quiz
ni estadisticas de entrenamiento. Es una funcionalidad pendiente
documentada explicitamente en el estado de UI.

---

## 7. Cambios realizados tras la revision tecnica

### ExportReportToFileUseCase movido de domain a data

**Antes:** `domain.usecase.ExportReportToFileUseCase` importaba
`android.content.Context` y `FileProvider`, violando la regla
de que domain no depende de Android framework.

**Despues:** movido a `data.export.ExportReportToFileUseCase`.
Los imports en AppContainer, IncidentDetailViewModel y su factory
se actualizaron. Sin cambio de interfaz ni de comportamiento.

**Por que:** el dominio debe ser testable en JVM puro.
Una clase que necesita Context y FileProvider pertenece a la capa de datos.

### android.util.Log eliminado de AnalyzeAndPersistUseCase

Se eliminaron las trazas de depuracion (`logDebug`, `Log.d`)
y las mediciones de tiempo que solo servian para desarrollo.
El dominio queda libre de dependencias Android.

### UiState.kt eliminado

Existia una `sealed interface UiState<T>` generica en `presentation.common`
que no se usaba en ningun ViewModel del proyecto. Se elimino como codigo muerto.

### StringResolver inyectado en SettingsViewModel

Los mensajes de estado (`statusMessage`) estaban hardcodeados como literales
de String. Se movieron a `strings.xml` y se inyecta `StringResolver`
como en el resto de ViewModels del proyecto.

### StringResolver inyectado en HomeViewModel

Los textos de fallback (`"Analisis sin titulo"`, `"Fecha: ..."`, `"Dominio: ..."`)
y el mensaje de progreso de entrenamiento (`"Sin progreso persistido..."`)
se movieron a recursos de strings. Se inyecta `StringResolver`
para mantener coherencia con el patron del resto del proyecto.

### Textos de HomeFragment movidos a strings.xml

Los titulos y descripciones de las tarjetas rapidas del panel principal
(Coach, Entrenamiento, Historial, Recursos) estaban hardcodeados
en el codigo del Fragment. Se externalizaron a `strings.xml`.

### Textos del BiometricPrompt en SettingsFragment movidos a strings.xml

Los literales de titulo y subtitulo del prompt biometrico
se externalizaron a recursos.

---

## 8. Cierre tecnico de la iteracion final (7 abril 2026)

### Correccion de tests rotos por deriva entre produccion y dobles de test

Durante el cierre final habia 4 tests fallando:
- 3 en `HistoryViewModelTest`
- 1 en `RuleEngineTest`

La causa de Historial no estaba en la logica de filtrado ni orden, sino en
`TestStringResolver`: el doble seguia devolviendo un formato antiguo
(`Score | origen | semaforo`) mientras la UI real ya solo mostraba
`Origen: %1$s`. Al alinear el doble con `strings.xml`, los tests volvieron a
medir el comportamiento real.

En `RuleEngineTest` el fallo no venia de una regresion funcional, sino de una
calibracion previa de pesos: la regla `URGENCY_THREAT` pesa `14`, mientras el
test seguia esperando `>= 16`. Se actualizo la expectativa para reflejar la
configuracion vigente del motor heuristico.

### Recolocacion del collector de Historial para cumplir ciclo de vida

`HistoryFragment` iniciaba el `collectOnStarted` del `uiState` desde un flujo
que partia de `onResume`, despues de validar biometria. Aunque el codigo estaba
protegido contra duplicados, `lint` lo marcaba correctamente como un mal punto
de inicio para `repeatOnLifecycle`.

La solucion fue mover la inicializacion de lista, controles y collector a
`onBoundView` y dejar en `onResume` solo la compuerta de acceso biometrico.
Resultado:
- desaparece el error `RepeatOnLifecycleWrongUsage`,
- el collector queda atado al ciclo de vida correcto,
- el bloqueo local sigue funcionando igual.

### Endurecimiento de calidad de UI y recursos

En el cierre se resolvieron varios problemas de calidad que no rompian el APK
pero si el gate de `lint`:
- migracion de `android:tint` a `app:tint` en toolbars,
- externalizacion de textos hardcodeados a `strings.xml`,
- anadido de `hint` en combos accesibles,
- eliminacion de recursos `mock` no usados,
- ajuste de `AvatarOptionAdapter` para evitar `notifyDataSetChanged()`
  innecesario,
- composicion i18n-safe de textos dinamicos (`QuizFragment`,
  `ResourcesAdapter`, checklist de Coach).

### Ajustes finales de privacidad y export

Se anadieron:
- `backup_rules.xml`
- `data_extraction_rules.xml`

Con ello la app deja explicita su politica de no respaldo automatico en copias
del sistema, coherente con el enfoque de privacidad local.

Tambien se corrigio `ReportExporter` para no fijar `Locale.getDefault()` en un
campo estatico, evitando un warning de `lint` y un comportamiento no deseado si
la configuracion regional cambia durante la vida de la app.

### Estado verificado del cierre

Validacion ejecutada en este workspace el `2026-04-07`:
- `:app:assembleDebug` -> OK
- `:app:testDebugUnitTest` -> OK (`125/125`)
- `:app:lintDebug` -> OK (`0 errores`, `31 warnings`)

Los `31 warnings` restantes no bloquean entrega y se concentran en:
- dependencias y `targetSdk` no actualizados a la ultima version publicada,
- recomendacion de mover avatares bitmap a `drawable-nodpi` o densidades,
- optimizaciones menores de layout (`UseCompoundDrawables`, `UselessParent`),
- optimizacion opcional del vector `ic_settings_gear_24`.

`installDebug` no pudo validarse en esta sesion por ausencia de dispositivo
conectado; el fallo observado fue `No connected devices!`, no un error de build.

---

## 9. Decisiones descartadas y motivo

| Propuesta | Motivo de descarte |
|-----------|-------------------|
| Migrar a Hilt | Complejidad excesiva para el alcance del TFG. AppContainer cumple la funcion. |
| Activar isMinifyEnabled | Requiere configurar reglas ProGuard para Room, Gson y ML Kit. Riesgo de rotura sin beneficio para TFG. |
| Migrar MasterKeys a MasterKey.Builder | Mismo cifrado, sin mejora real. Posible incompatibilidad en dispositivos antiguos. |
| Unificar AnalysisDetail y HistoryDetail | Layouts distintos, flujos distintos. La unificacion anade complejidad condicional mayor que la duplicacion actual. |
| Extraer helper de autenticacion biometrica | Solo 2 consumidores con diferencias reales en callbacks. La abstraccion seria mas compleja que la duplicacion. |
| Reutilizar instancia de ML Kit TextRecognizer | Sin impacto medible en el MVP. Optimizacion prematura. |
| Mover URLs de ResourcesFragment a assets | Pantalla estatica con 4 elementos estables. Un JSON anade complejidad sin valor. |
| Persistir progreso de entrenamiento | Fuera del alcance del MVP. Documentado como mejora futura. |
