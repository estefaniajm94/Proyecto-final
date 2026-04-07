# Anexo I - Manual de Instalacion

## 1. Alcance
Este anexo describe como preparar y ejecutar el proyecto Android del TFG en entorno local.

## 2. Requisitos
- Windows 10/11 (o entorno compatible con Android Studio).
- Android Studio (Hedgehog o superior).
- SDK Android 34 instalado.
- JDK 17 (incluido por Android Studio recomendado).
- Emulador o dispositivo Android (`minSdk 24`).

## 3. Apertura del proyecto
1. Abrir Android Studio.
2. Seleccionar `Open` y elegir carpeta raiz del repositorio:
   `c:\Users\Estef\Desktop\ProyectoFinal`
3. Esperar sincronizacion de Gradle.

## 4. Configuracion SDK local
Si aparece error de SDK, crear/editar `local.properties` en la raiz:

```properties
sdk.dir=C\:\\Users\\Estef\\AppData\\Local\\Android\\Sdk
```

## 5. Compilacion y validacion tecnica
En terminal PowerShell en la raiz del repo:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Resultado esperado:
- `BUILD SUCCESSFUL` en los tres comandos.

## 6. Instalacion en emulador/dispositivo
1. Iniciar emulador Android (ejemplo: `Medium_Phone_API_36.1`) o conectar dispositivo.
2. Ejecutar:

```powershell
.\gradlew.bat :app:installDebug
```

Resultado esperado:
- `Installed on 1 device`.

Si no hay emulador/dispositivo conectado:
- Gradle devolvera `No connected devices!`.
- No es un fallo del proyecto, sino del entorno de ejecucion.

## 7. Ejecucion de la app
La app se instala con nombre:
- `Anti-phishing Coach`

Tambien se puede lanzar con:

```powershell
adb shell am start -n com.estef.antiphishingcoach/.presentation.navigation.MainActivity
```

En la primera apertura:
1. La app muestra `Login`.
2. Si aun no existe cuenta local, pulsar `Crear cuenta local`.
3. Completar nombre, correo y contrasena.
4. Tras el alta, se crea sesion local automaticamente.

## 8. Problemas comunes
- Error `SDK location not found`:
1. Revisar `local.properties`.
- Emulador `offline`:
1. Reiniciar emulador.
2. Comprobar `adb devices`.
- `adb` no disponible en terminal:
1. Lanzar el emulador desde Android Studio.
2. Verificar que el SDK Platform-Tools este instalado.
3. Anadir `platform-tools` al `PATH` si se quiere usar `adb` desde consola.
- Build lento en primera ejecucion:
1. Es normal por descarga inicial de dependencias.

## 9. Utilidad auxiliar para pegar texto en el emulador
Si se usa el emulador Android en Windows, el proyecto incluye:
- `scripts/paste_clipboard_to_emulator.ps1`

Uso:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\paste_clipboard_to_emulator.ps1
```

Funcion:
1. Lee el portapapeles de Windows.
2. Comprueba que el emulador esta arrancado.
3. Envia el texto al campo enfocado del emulador mediante `adb shell input text`.

## 10. Verificacion minima post-instalacion
1. Crear una cuenta local o iniciar sesion.
2. Abrir pantalla Analizar y ejecutar un caso.
3. Abrir Historial y comprobar registro.
4. Activar privacidad extrema y repetir analisis (no debe guardarse).
5. Compartir un texto de prueba desde otra app o usar el script de portapapeles en emulador.

## 11. Estado del cierre tecnico
Validacion realizada en este workspace el `2026-04-07`:
- `:app:assembleDebug` -> OK
- `:app:testDebugUnitTest` -> OK
- `:app:lintDebug` -> OK
- `:app:installDebug` -> bloqueado por ausencia de dispositivo conectado

## 12. Nota sobre datos locales existentes
El esquema Room actual esta en version `2` e incorpora la tabla `users`.
Como la base usa `fallbackToDestructiveMigration()`, al cambiar desde una version previa
pueden perderse datos locales antiguos del emulador/dispositivo durante la migracion.
