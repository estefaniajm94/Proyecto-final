# Anexo II - Manual de Usuario

## 1. Objetivo de la aplicacion
Ayudar a identificar posibles fraudes digitales mediante analisis local explicable, entrenamiento y recomendaciones practicas.

## 2. Aviso importante
La app es educativa y orientativa:
- no garantiza deteccion perfecta,
- no sustituye asesoramiento profesional ni canales oficiales.

## 3. Navegacion principal
Antes de entrar en Home:
- si no hay sesion local activa, la app muestra `Login`;
- desde `Login` se puede acceder a `Crear cuenta local`.

Desde Home se accede a:
- Analizar
- Coach
- Entrenamiento
- Historial
- Ajustes
- Recursos oficiales

## 4. Registro e inicio de sesion local
### Crear cuenta local
1. Abrir la app por primera vez.
2. Pulsar `Crear cuenta local`.
3. Introducir nombre visible, correo y contrasena.
4. Confirmar contrasena.
5. Pulsar `Crear cuenta y entrar`.

### Iniciar sesion
1. Introducir correo y contrasena.
2. Pulsar `Entrar`.
3. La app abre Home o, si venia contenido compartido, entra en `Analizar`.

### Cerrar sesion
1. Ir a `Ajustes`.
2. Pulsar `Cerrar sesion`.
3. La app vuelve a `Login`.

## 5. Como usar "Analizar"
1. Abrir `Analizar texto/enlace`.
2. (Opcional) introducir etiqueta.
3. Elegir origen (`SMS`, `WhatsApp`, `Email`, `Otro`).
4. Pegar texto/enlace sospechoso.
5. Pulsar `Analizar ahora`.

Alternativa rapida:
1. Desde WhatsApp, navegador, correo u otra app, usar `Compartir`.
2. Seleccionar `Anti-phishing Coach`.
3. Si no hay sesion, la app pedira login y despues abrira `Analizar` con el contenido cargado.
4. Revisar el contenido y ejecutar el analisis.

Resultado mostrado:
- score (0-100),
- semaforo (`VERDE`, `AMARILLO`, `ROJO`),
- tipo de fuente,
- dominio sanitizado,
- texto analizado con frases sospechosas resaltadas,
- lectura rapida del riesgo,
- desglose del enlace (dominio, esquema, ruta, parametros, observaciones),
- plan de accion inmediato ("que hacer ahora"),
- senales detectadas,
- recomendaciones.

Si privacidad extrema esta desactivada:
- se guarda en historial con `incidentId`.

## 6. Interpretacion rapida del semaforo
- Verde `< 35`: bajo riesgo heuristico.
- Amarillo `35-69`: riesgo moderado; verificar antes de actuar.
- Rojo `>= 70`: riesgo alto; no compartir datos y usar canales oficiales.

## 7. Coach por escenarios
1. Ir a `Coach`.
2. Elegir escenario.
3. Abrir checklist y marcar pasos realizados.
4. Revisar progreso de marcados.

## 8. Entrenamiento (quiz)
1. Ir a `Entrenamiento`.
2. Elegir un nivel: `Principiante`, `Intermedio` o `Avanzado`.
3. Revisar la descripcion breve del nivel y el numero de preguntas disponibles.
4. Pulsar `Comenzar entrenamiento`.
5. Responder cada pregunta.
6. Revisar feedback inmediato (correcto/incorrecto + explicacion).
7. Al finalizar, consultar nivel jugado, aciertos, porcentaje y mensaje final adaptado.
8. Usar `Reiniciar quiz` para repetir el nivel o volver a elegir otro.

## 9. Historial y detalle
En modo normal:
- se listan analisis guardados por fecha.
- se puede abrir detalle de cada incidente.

En detalle:
- ver score/semaforo/senales/recomendaciones.
- ver plan de accion reconstruido a partir de senales y recomendaciones persistidas.
- abrir `Recursos oficiales` cuando el riesgo aconseja verificacion externa.
- en detalle de analisis, exportar reporte Markdown por compartir.

## 10. Ajustes de privacidad y cuenta local
### Cuenta local
- En Ajustes se muestran nombre y correo del usuario activo.
- `Cerrar sesion` elimina la sesion actual del dispositivo.

### Privacidad extrema
- Si esta activa, no se guardan nuevos analisis.

### Borrar datos locales
- Elimina historial y resultados almacenados.

## 11. Recursos oficiales
La pantalla `Recursos oficiales` ofrece accesos informativos a:
- INCIBE 017
- Policia Nacional (OVD / 091)
- Guardia Civil delitos telematicos (062)
- Emergencias 112

Uso recomendado:
- ante riesgo alto, contactar canal oficial antes de cualquier accion.

## 12. Uso en emulador Android
Si se prueba la app en emulador Android desde Windows:
1. Copiar el texto de prueba en el portapapeles del PC.
2. Enfocar el campo de entrada del emulador.
3. Ejecutar en PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\paste_clipboard_to_emulator.ps1
```

El texto se enviara al campo enfocado del emulador.
