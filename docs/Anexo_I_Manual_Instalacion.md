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

## 5. Compilacion y tests
En terminal PowerShell en la raiz del repo:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Resultado esperado:
- `BUILD SUCCESSFUL` en ambos comandos.

## 6. Instalacion en emulador/dispositivo
1. Iniciar emulador Android (ejemplo: `Medium_Phone_API_36.1`) o conectar dispositivo.
2. Ejecutar:

```powershell
.\gradlew.bat :app:installDebug
```

Resultado esperado:
- `Installed on 1 device`.

## 7. Ejecucion de la app
La app se instala con nombre:
- `Anti-phishing Coach`

Tambien se puede lanzar con:

```powershell
adb shell am start -n com.estef.antiphishingcoach/.presentation.navigation.MainActivity
```

## 8. Problemas comunes
- Error `SDK location not found`:
1. Revisar `local.properties`.
- Emulador `offline`:
1. Reiniciar emulador.
2. Comprobar `adb devices`.
- Build lento en primera ejecucion:
1. Es normal por descarga inicial de dependencias.

## 9. Verificacion minima post-instalacion
1. Abrir pantalla Analizar y ejecutar un caso.
2. Abrir Historial y comprobar registro.
3. Activar privacidad extrema y repetir analisis (no debe guardarse).
