# Mockups y Wireframes (MVP A)

## 1. Objetivo
Documentar evidencias visuales de diseno para memoria y defensa:
- wireframes de baja fidelidad (flujo y estructura),
- mockups de media fidelidad (jerarquia visual y componentes),
- trazabilidad entre pantalla y caso de uso.

## 2. Herramienta recomendada
- Figma (rapido para exportar capturas limpias).
- Alternativa: wireframes en papel + digitalizacion en PNG.

## 3. Entregables minimos para la memoria
Se recomienda guardar imagenes en `docs/images/mockups/` con estos nombres:
1. `01_home.png`
2. `02_analizar_texto.png`
3. `03_analizar_ocr_revision.png`
4. `04_resultado_analisis.png`
5. `05_coach_lista.png`
6. `06_coach_checklist.png`
7. `07_training_quiz.png`
8. `08_historial_privado.png`
9. `09_ajustes_privacidad_bloqueo.png`
10. `10_recursos_oficiales.png`

## 4. Trazabilidad pantalla -> casos de uso
| Pantalla | Caso(s) de uso |
|---|---|
| Home | UC01, UC03, UC05, UC06, UC08 |
| Analizar texto | UC01 |
| OCR revision | UC01 (flujo alternativo A2) |
| Detalle analisis | UC02, UC07 |
| Coach | UC03 |
| Checklist | UC04 |
| Training (quiz) | UC05 |
| Historial | UC06 |
| Ajustes | UC08 |
| Recursos oficiales | Soporte informativo MVP |

## 5. Checklist de calidad visual para defensa
1. Todas las pantallas obligatorias del MVP A tienen mockup.
2. Se ve disclaimer legal en Home/Analizar.
3. En Analizar se aprecia boton "Analizar desde captura".
4. En OCR se ve texto de privacidad y edicion manual.
5. En Ajustes se ven "Privacidad extrema", "Bloqueo local" y "Borrar datos".
6. Navegacion y jerarquia visual son consistentes.

## 6. Nota para memoria
Incluir una subseccion corta por pantalla con:
1. objetivo UX,
2. decisiones de diseno,
3. relacion con privacidad/seguridad.

## 7. Nota de implementacion actual
El Home del proyecto se ha redisenado como dashboard (CTA principal Analizar, grid 2x2 de accesos y tarjetas de actividad) para mejorar jerarquia visual y defendibilidad UX en memoria.
