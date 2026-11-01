# Publicar Rimas Diccionario en Google Play

Id de la app: `com.davidgonzalez.rimasdiccionario` (no se puede cambiar después de publicar).
Correo de contacto: david.gonzalez.esquerra@gmail.com

## Lo que ya está hecho
- Lista de palabras: **el build la genera sola** desde RLA-ES (no hay que ejecutar nada aparte).
- Licencias, política de privacidad (`docs/PRIVACIDAD.md` y `docs/privacidad.html`), ficha, icono 512 y gráfico 1024×500.
- Llave de subida (keystore) y valores de los secretos: en el archivo aparte `Credenciales-Play-NO-SUBIR-A-GITHUB.zip`.

## 1. Subir el proyecto a GitHub
Repositorio **privado** → sube todo el contenido del proyecto (incluida la carpeta `.github`). **No subas** el zip de credenciales.

## 2. Crear los 4 secretos
Repositorio → Settings → Secrets and variables → Actions → New repository secret:
`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` (valores en `secretos.txt` y `upload.jks.b64` del zip de credenciales).

## 3. Generar APK + AAB
Actions → **Compilar Rimas Diccionario** → Run workflow. Descarga `rimas-diccionario-release`: el **.aab** (lo que subes a Play), el **.apk** firmado y `mapping.txt`. Instala el APK de depuración (`rimas-diccionario-debug-apk`) para probar y tomar capturas.
El build falla a propósito si la lista de palabras quedó de muestra, para que nunca publiques una versión incompleta.

## 4. Play Console (primera publicación)
1. Cuenta de desarrollador: https://play.google.com/console (25 USD, verificación de identidad; confirma que tu país esté disponible).
2. Crear app → **Rimas Diccionario**, español, gratuita. Aceptar **Play App Signing**.
3. Pruebas internas (o Producción) → subir el `.aab`.
4. Ficha: textos de `docs/ficha_play_store.md`, icono `docs/play-store/icono-512.png`, gráfico `docs/play-store/grafico-funciones-1024x500.png`, mínimo 2 capturas de teléfono.
5. Política de privacidad: publica `docs/privacidad.html` en una URL (por ejemplo `versometro.com/privacidad-rimas`) y pega esa URL.
6. Formularios: `docs/FORMULARIOS_PLAY_CONSOLE.md`.
7. Cuentas personales nuevas: Google suele exigir una prueba cerrada con testers durante varios días antes de producción; confirma las cifras en la consola.
8. Enviar a revisión.

## 5. Actualizaciones
Sube `versionCode`/`versionName` en `app/build.gradle.kts`, haz commit y crea la etiqueta `v1.0.1`: el workflow genera el AAB firmado y lo adjunta a *Releases*.
**Opcional:** con una cuenta de servicio de Play guardada como secreto `PLAY_SERVICE_ACCOUNT_JSON`, cada etiqueta sube el AAB a la pista interna como borrador.

## Notas
- Desde el 31-ago-2026 Google Play exige apuntar a Android 16 (API 36): ya está configurado.
- Guarda el keystore y sus contraseñas en un lugar privado. Con Play App Signing, si pierdes la llave de subida se puede restablecer vía soporte.
- Licencias de la lista de palabras: `docs/LICENCIAS_TERCEROS.md`.
