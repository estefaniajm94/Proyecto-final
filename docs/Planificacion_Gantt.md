# Planificacion Gantt (MVP A)

## Plan por fases
| Fase | Objetivo | Semanas | Estado |
|---|---|---|---|
| F1 | Base proyecto (Gradle, navegacion, Room, estructura capas) | S1-S2 | Completada |
| F2 | Analizador heuristico + privacidad extrema + historial minimo | S3-S4 | Completada |
| F3 | Detalle de analisis + exportacion Markdown/TXT | S5 | Completada |
| F4 | Coach por escenarios (seed + checklist) | S6 | Completada |
| F5 | Training quiz (seed + feedback + resultado) | S6-S7 | Completada |
| F6 | Documentacion memoria + pruebas + anexos | S7-S8 | En curso |

## Gantt (Mermaid)
```mermaid
gantt
    title TFG Anti-phishing Coach - MVP A
    dateFormat  YYYY-MM-DD
    axisFormat  %d-%m

    section Base tecnica
    Estructura proyecto y navegacion      :done,    a1, 2026-01-06, 10d
    Room + repositorios + ajustes         :done,    a2, after a1, 8d

    section Analizador
    Heuristicas explicables y scoring      :done,    b1, 2026-01-24, 8d
    Privacidad extrema e historial         :done,    b2, after b1, 6d
    Detalle + export markdown              :done,    b3, after b2, 5d

    section Coach y training
    Coach escenarios + checklist           :done,    c1, 2026-02-10, 5d
    Quiz real con feedback                 :done,    c2, after c1, 5d
    Tests seed parser + quiz flow          :done,    c3, after c2, 3d

    section Memoria y cierre
    Casos de uso + diagrama clases         :active,  d1, 2026-02-20, 3d
    Gantt + plan de pruebas + anexos       :active,  d2, after d1, 4d
    Preparacion defensa y video            :         d3, after d2, 4d
```

## Evidencia de pruebas ejecutadas (2026-02-22)
- Build debug:
1. `.\gradlew.bat :app:assembleDebug` -> `BUILD SUCCESSFUL`
- Unit tests:
1. `.\gradlew.bat :app:testDebugUnitTest` -> `BUILD SUCCESSFUL`
2. Cobertura activa:
1. `RuleEngineTest` (analizador)
2. `SeedJsonParserTest` (parseo seed)
3. `QuizEngineTest` (flujo quiz)
- Instalacion emulador:
1. `.\gradlew.bat :app:installDebug` -> `Installed on 1 device`
