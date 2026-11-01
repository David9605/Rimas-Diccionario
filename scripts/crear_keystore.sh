#!/usr/bin/env bash
# Crea tu llave de subida para Google Play (hazlo una vez y respáldala).
set -e
keytool -genkeypair -v -keystore upload.jks -alias rimasdiccionario \
  -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 upload.jks > upload.jks.b64
echo "Listo. Sube el contenido de upload.jks.b64 al secreto KEYSTORE_BASE64 en GitHub."
