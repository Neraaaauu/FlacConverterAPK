# 🎵 FLAC Converter para Android

¡Transforma tus archivos de audio sin pérdida! **FLAC Converter** es una aplicación nativa para Android que te permite convertir archivos `.flac` a formatos más ligeros como MP3, OGG y AAC directamente desde tu dispositivo, sin depender de la nube.

---

## ✨ Características Principales

* **⚡ Conversión por Lotes:** Selecciona múltiples archivos FLAC y conviértelos de una sola vez.
* **🎧 Multi-Formato de Salida:** Soporta conversión a **MP3, OGG Vorbis, OGG Opus y AAC**.
* **🎛️ Calidad Personalizable:** * Ajusta el *Bitrate* (128 / 192 / 256 / 320 kbps) para MP3, Opus y AAC.
    * Ajusta la calidad con un control deslizante (0–10) para OGG Vorbis.
* **📂 Gestión de Directorios:** Guarda tus archivos convertidos en la carpeta de *Descargas* por defecto o elige tu propio directorio de destino usando la API de Android.
* **📊 Monitoreo en Tiempo Real:** Barra de progreso interactiva y un *log* de conversión detallado por cada archivo.
* **📜 Historial Integrado:** Revisa tus conversiones pasadas (exitosas y fallidas) gracias a la base de datos local.
* **🎨 Diseño Moderno:** Interfaz de usuario limpia e intuitiva basada en **Material Design 3**, con soporte para abrir archivos FLAC directamente desde tu explorador de archivos habitual.

---

## 🛠️ Stack Tecnológico

Esta aplicación está construida utilizando los estándares y librerías modernas del desarrollo en Android:

* **Lenguaje:** [Kotlin](https://kotlinlang.org/)
* **UI:** Views estándar con [ViewBinding](https://developer.android.com/topic/libraries/view-binding) y componentes de **Material Design 3**.
* **Motor de Audio:** [FFmpeg-Kit-Audio](https://github.com/arthenica/ffmpeg-kit) para un procesamiento de audio rápido y confiable.
* **Asincronismo:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) (`Dispatchers.IO`) para mantener la interfaz fluida durante las conversiones.
* **Base de Datos:** [Room Database](https://developer.android.com/training/data-storage/room) para gestionar el historial de conversiones localmente.
* **SDK:** `compileSdk 34` | `minSdk 24` (Android 7.0 Nougat o superior).

---

## 📦 Peso de la Aplicación

El motor de conversión de FFmpeg incluye los códecs necesarios (`libmp3lame`, `libvorbis`, `libopus`, `aac`), lo que aporta alrededor de ~15 MB al peso total de la app.

| Tipo de Build | Peso Estimado |
| :--- | :--- |
| **Debug** | ~60 MB - 75 MB |
| **Release** *(Se expandio XD)* | ~80 MB - 85 MB |

---

## 🚀 Cómo Compilar el Proyecto

### 1. Preparación
1. Descarga e instala [Android Studio](https://developer.android.com/studio).
2. Clona este repositorio y ábrelo en Android Studio.
3. Espera a que Gradle sincronice las dependencias (puede tomar unos minutos la primera vez).

### 2. Ejecución
* **En un dispositivo físico:** Conecta tu celular con la *Depuración USB* activada.
* **En un emulador:** Crea e inicia un dispositivo virtual desde el *Device Manager*.
* Haz clic en el botón de **▶️ Run** (o presiona `Shift + F10`) para compilar e instalar la app directamente.

### 3. Generar el APK Instalable
1. En el menú superior, ve a **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
2. Una vez finalizado, Android Studio te mostrará una notificación. Haz clic en "Locate" para ver tu archivo `app-debug.apk`.
3. Pasa ese archivo a tu teléfono e instálalo (recuerda aceptar la instalación desde fuentes desconocidas si se requiere).

---

## 📁 Arquitectura y Estructura

El código fuente sigue un principio de separación de responsabilidades e incluye los siguientes módulos principales en `app/src/main/java/com/flacconverter/`:

* `MainActivity.kt`: Pantalla inicial, gestión de permisos, selección de archivos FLAC y configuración de la conversión.
* `ConversionActivity.kt`: Servicio de conversión. Ejecuta los comandos de FFmpeg a través de corrutinas y guarda resultados en Room.
* `HistoryActivity.kt`: Interfaz que lee y muestra el historial de conversiones utilizando un `RecyclerView`.
* `db/HistoryDatabase.kt`: Configuración de Room y DAOs para la persistencia de datos.
* `model/Models.kt`: Clases de datos (`AudioFile`, `ConversionSettings`, `ConversionHistory`, `OutputFormat`).

---

## ⚠️ Notas Adicionales

* **Permisos:** La app solicitará permisos de `READ_EXTERNAL_STORAGE` o `READ_MEDIA_AUDIO` (dependiendo de la versión de tu Android) para poder acceder a los archivos de origen.
* **Archivos Temporales:** Durante el proceso, el sistema utiliza el `cacheDir` para procesar de forma segura el audio sin afectar tu archivo original, moviéndolo al destino final solo si la conversión resulta exitosa.

---
*¡Hecho con ☕ y Kotlin!*
