# Referencias técnicas y de dominio — AntiPhishingCoach

TFG — Ingeniería / Desarrollo de Aplicaciones
Documento de referencia para la memoria académica.

---

## 1. Lenguaje y plataforma

| Ref | Recurso | Version usada |
|-----|---------|---------------|
| [1] | Kotlin Programming Language — kotlinlang.org | 1.9.22 |
| [2] | Android Developers — developer.android.com | SDK 34 (Android 14), minSdk 24 |
| [3] | Android Gradle Plugin (AGP) — developer.android.com/build | 8.2.2 |
| [4] | Kotlin Symbol Processing (KSP) — github.com/google/ksp | 1.9.22-1.0.17 |

---

## 2. Arquitectura y patrones

| Ref | Recurso |
|-----|---------|
| [5] | Clean Architecture — Robert C. Martin, *Clean Architecture: A Craftsman's Guide to Software Structure and Design*, Prentice Hall, 2017 |
| [6] | Guide to app architecture (MVVM + repositorios) — developer.android.com/topic/architecture |
| [7] | ViewModel overview — developer.android.com/topic/libraries/architecture/viewmodel |
| [8] | StateFlow and SharedFlow — kotlinlang.org/docs/flow.html |

---

## 3. Jetpack — bibliotecas de Android

| Ref | Biblioteca | Version |
|-----|-----------|---------|
| [9]  | AndroidX Core KTX | 1.12.0 |
| [10] | AppCompat | 1.6.1 |
| [11] | Fragment KTX | 1.6.2 |
| [12] | Activity KTX | 1.8.2 |
| [13] | Lifecycle ViewModel KTX | 2.7.0 |
| [14] | Navigation Component (fragment-ktx, ui-ktx, Safe Args) | 2.7.7 |
| [15] | Room Runtime + KTX + Compiler | 2.6.1 |
| [16] | Security Crypto (EncryptedSharedPreferences) | 1.0.0 |
| [17] | Biometric | 1.1.0 |
| [18] | ConstraintLayout | 2.1.4 |
| [19] | RecyclerView | 1.3.2 |
| [20] | Material Components for Android | 1.11.0 |

Referencia general de todas las bibliotecas Jetpack:
developer.android.com/jetpack/androidx/explorer

---

## 4. Procesamiento local (OCR)

| Ref | Recurso | Version |
|-----|---------|---------|
| [21] | ML Kit Text Recognition (on-device) — developers.google.com/ml-kit/vision/text-recognition/android | 16.0.1 |

Característica relevante: el modelo de reconocimiento de texto se ejecuta
completamente en el dispositivo, sin envío de datos a servidores de Google.

---

## 5. Serialización y concurrencia

| Ref | Biblioteca | Version |
|-----|-----------|---------|
| [22] | Gson — github.com/google/gson | 2.10.1 |
| [23] | Kotlinx Coroutines Android | 1.7.3 |
| [24] | Kotlinx Coroutines Test | 1.7.3 |

---

## 6. Testing

| Ref | Biblioteca | Version | Uso |
|-----|-----------|---------|-----|
| [25] | JUnit 4 | 4.13.2 | Framework de tests unitarios |
| [26] | MockK — mockk.io | 1.13.9 | Mocking de dependencias Kotlin |
| [27] | Turbine — github.com/cashapp/turbine | 1.0.0 | Testing de Kotlin Flows |
| [28] | AndroidX Arch Core Testing | 2.2.0 | `InstantTaskExecutorRule` para LiveData/StateFlow |
| [29] | AndroidX Test JUnit | 1.1.5 | Extensiones JUnit para Android |
| [30] | Espresso Core | 3.5.1 | Tests de UI instrumentados |

---

## 7. Estándares técnicos — URLs y dominios

| Ref | Estándar | Descripción |
|-----|---------|-------------|
| [31] | RFC 3986 — *Uniform Resource Identifier (URI): Generic Syntax*, IETF, 2005 | Estructura y parsing de URIs/URLs |
| [32] | RFC 3492 — *Punycode: A Bootstring encoding of Unicode for IDNA*, IETF, 2003 | Codificación de dominios IDN en ASCII |
| [33] | RFC 5891 — *Internationalized Domain Names in Applications (IDNA): Protocol*, IETF, 2010 | Protocolo IDN, base de los ataques homoglyphic |
| [34] | Unicode Consortium — *Unicode Security Considerations (UTS #36)*, unicode.org/reports/tr36 | Confusables, homóglifos y ataques visuales en texto Unicode |
| [35] | Unicode Consortium — *Unicode IDNA Compatibility Processing (UTS #46)*, unicode.org/reports/tr46 | Procesamiento de dominios internacionalizados |

---

## 8. Seguridad y dominio — phishing y análisis de amenazas

| Ref | Recurso |
|-----|---------|
| [36] | OWASP — *Phishing*, owasp.org/www-community/attacks/Phishing | Definición y taxonomía de ataques de phishing |
| [37] | Anti-Phishing Working Group (APWG) — apwg.org | Estadísticas y tendencias globales de phishing |
| [38] | INCIBE — *Guía sobre phishing*, incibe.es | Marco de referencia en contexto español |
| [39] | NIST — *Phishing-resistant MFA*, csrc.nist.gov | Contramedidas técnicas recomendadas |
| [40] | Kucner, T. et al. — *IDN Homograph Attack Detection*, 2021 | Detección de ataques homoglyphic en dominios internacionalizados |

---

## 9. Privacidad y protección de datos

| Ref | Recurso |
|-----|---------|
| [41] | Reglamento (UE) 2016/679 — RGPD | Reglamento General de Protección de Datos, base legal del principio de minimización de datos aplicado al proyecto |
| [42] | Privacy by Design — Ann Cavoukian, *Privacy by Design: The 7 Foundational Principles*, 2009 | Marco conceptual del diseño sin persistencia del texto original |
| [43] | Android Security — EncryptedSharedPreferences, developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences | Cifrado AES256-GCM para flags sensibles |
| [44] | Android FileProvider — developer.android.com/reference/androidx/core/content/FileProvider | Compartición segura de ficheros con URI content:// y permisos temporales |

---

## 10. Organismos oficiales de referencia (en la aplicación)

Recursos enlazados directamente en la pantalla *Recursos Oficiales* de la app:

| Ref | Organismo | Recurso |
|-----|-----------|---------|
| [45] | INCIBE — Instituto Nacional de Ciberseguridad | Línea de ayuda en ciberseguridad: incibe.es/linea-de-ayuda-en-ciberseguridad |
| [46] | Policía Nacional (España) | Formulario de denuncia de delitos telemáticos: denuncias.policia.es/OVD/ |
| [47] | Guardia Civil (España) | Contacto delitos telemáticos: guardiacivil.es |

---

## 11. Herramientas de desarrollo

| Ref | Herramienta | Version usada |
|-----|------------|---------------|
| [48] | Android Studio (Hedgehog) | 2023.1.1 o superior |
| [49] | JDK | 17 |
| [50] | Gradle | Wrapper incluido en el proyecto |
| [51] | Mermaid — mermaid.js.org | Diagramas Mermaid en documentación Markdown (ER, Clases, Gantt) |
| [52] | Git | Control de versiones del proyecto |

---

## Nota sobre versiones

Las versiones indicadas corresponden al estado del proyecto en el commit final (`e9707dd`, 8 mar 2026).
Todas las dependencias se declaran en `gradle/libs.versions.toml`.
