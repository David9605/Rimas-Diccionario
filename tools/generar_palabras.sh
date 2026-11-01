#!/usr/bin/env bash
# Descarga RLA-ES, expande las formas con unmunch y escribe app/src/main/assets/palabras.txt
# Uso: tools/generar_palabras.sh [version=2.9] [variante=es_ANY]
set -euo pipefail
VERSION="${1:-2.9}"
VARIANTE="${2:-es_ANY}"
cd "$(dirname "$0")/.."
WORK="$(mktemp -d)"
curl -fL --retry 3 -o "$WORK/rla.oxt" \
  "https://github.com/sbosio/rla-es/releases/download/v${VERSION}/${VARIANTE}.oxt"
unzip -q "$WORK/rla.oxt" -d "$WORK/rla"
DIC="$(find "$WORK/rla" -name '*.dic' ! -name 'hyph*' ! -name 'th_*' | head -1)"
AFF="${DIC%.dic}.aff"
echo "Usando $DIC"
unmunch "$DIC" "$AFF" > "$WORK/formas.txt"
echo "Formas expandidas: $(wc -l < "$WORK/formas.txt")"
python3 tools/preparar_datos.py "$WORK/formas.txt" --sin-propios --sin-encliticos
mkdir -p app/src/main/assets/licencias/rla-es
find "$WORK/rla" -type f -iname '*.txt' -exec cp {} app/src/main/assets/licencias/rla-es/ \;
echo "Palabras finales: $(wc -l < app/src/main/assets/palabras.txt)"
