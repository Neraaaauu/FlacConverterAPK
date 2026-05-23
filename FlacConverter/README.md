# 🎵 FLAC Converter para Android

Convierte archivos FLAC a MP3, OGG Vorbis, OGG Opus y AAC directamente en tu Android.

## ✨ Funcionalidades

- ✅ Conversión por lotes (múltiples archivos a la vez)
- ✅ Formatos de salida: **MP3 · OGG Vorbis · OGG Opus · AAC**
- ✅ Bitrate ajustable: 128 / 192 / 256 / 320 kbps
- ✅ Calidad ajustable para OGG (slider 0–10)
- ✅ Selector de carpeta de destino
- ✅ Barra de progreso en tiempo real
- ✅ Log de conversión por archivo
- ✅ Historial de conversiones con Room DB
- ✅ Modo cancelar en cualquier momento
- ✅ Abre archivos FLAC directamente desde el explorador
- ✅ Diseño Material 3

## 📦 Peso estimado de la APK

| Versión | Peso |
|---------|------|
| Debug | ~25-35 MB |
| Release (minificada) | ~18-22 MB |

El peso principal lo aporta `ffmpeg-kit-audio` (~15 MB), que incluye los codecs de audio.

---

## 🛠️ Cómo compilar (paso a paso)

### 1. Instala Android Studio
Descarga gratis desde: https://developer.android.com/studio

### 2. Abre el proyecto
- Abre Android Studio
- Click en **"Open"**
- Selecciona la carpeta `FlacConverter`
- Espera a que Gradle sincronice (descarga dependencias, puede tardar unos minutos la primera vez)

### 3. Conecta tu Android o usa emulador
- **Opción A**: Conecta tu teléfono por USB con depuración USB activada
  - En el teléfono: Ajustes → Acerca del teléfono → toca "Número de compilación" 7 veces → vuelve a Ajustes → Opciones de desarrollador → activa "Depuración USB"
- **Opción B**: Usa el emulador de Android Studio (Tools → Device Manager → Create Device)

### 4. Genera la APK
- **Para instalar directo**: Click en ▶️ Run (o Shift+F10)
- **Para generar APK instalable**:
  - Menú: Build → Build Bundle(s) / APK(s) → Build APK(s)
  - La APK quedará en: `app/build/outputs/apk/debug/app-debug.apk`

### 5. Instala en tu teléfono
- Copia el archivo `.apk` a tu teléfono
- Abre el archivo desde el explorador de archivos
- Acepta instalar desde fuentes desconocidas si te lo pide

---

## 📁 Estructura del proyecto

```
FlacConverter/
├── app/
│   ├── src/main/
│   │   ├── java/com/flacconverter/
│   │   │   ├── MainActivity.kt          ← Pantalla principal
│   │   │   ├── ConversionActivity.kt    ← Pantalla de conversión
│   │   │   ├── HistoryActivity.kt       ← Historial
│   │   │   ├── adapter/FileAdapter.kt   ← Lista de archivos
│   │   │   ├── db/HistoryDatabase.kt    ← Base de datos Room
│   │   │   └── model/Models.kt          ← Modelos de datos
│   │   ├── res/layout/                  ← Pantallas XML
│   │   └── AndroidManifest.xml
│   └── build.gradle                     ← Dependencias
├── build.gradle
└── settings.gradle
```

---

## ⚠️ Notas importantes

- **minSdk 24** → Compatible con Android 7.0 y superior
- Necesita permiso de lectura de audio (se solicita automáticamente)
- Los archivos convertidos se guardan en **Descargas** por defecto, o en la carpeta que elijas
- La librería `ffmpeg-kit-audio` ya incluye soporte para: libmp3lame, libvorbis, libopus, aac

## 🔧 Íconos faltantes

Agrega estos drawables vectoriales en `res/drawable/`:
- `ic_audio_file.xml`
- `ic_check_circle.xml`
- `ic_error.xml`
- `ic_arrow_back.xml`

Puedes usar los íconos de Material Symbols: https://fonts.google.com/icons
O en Android Studio: File → New → Vector Asset → busca el ícono.
