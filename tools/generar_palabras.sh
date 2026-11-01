#!/usr/bin/env bash
# Obtiene el diccionario RLA-ES, expande las formas con unmunch y escribe app/src/main/assets/palabras.txt
# Uso: tools/generar_palabras.sh [version=2.9] [variante=es_ANY]
set -euo pipefail
VERSION="${1:-2.9}"
VARIANTE="${2:-es_ANY}"
cd "$(dirname "$0")/.."
WORK="$(mktemp -d)"
mkdir -p "$WORK/rla"
BASE="https://github.com/sbosio/rla-es/releases/download"

# En la v2.9 el paquete general se llama es.oxt; en versiones anteriores es_ANY.oxt.
if [ "$VARIANTE" = "es_ANY" ] || [ "$VARIANTE" = "es" ]; then
  CANDS=("$BASE/v$VERSION/es.oxt" "$BASE/v$VERSION/es_ANY.oxt" "$BASE/v2.2/es_ANY.oxt")
else
  CANDS=("$BASE/v$VERSION/$VARIANTE.oxt" "$BASE/v2.2/$VARIANTE.oxt")
fi

OK=0
for U in "${CANDS[@]}"; do
  echo "Probando $U"
  if curl -fL --retry 2 -o "$WORK/rla.oxt" "$U"; then
    unzip -q -o "$WORK/rla.oxt" -d "$WORK/rla"
    OK=1
    echo "Descargado: $U"
    break
  fi
done

if [ "$OK" != 1 ]; then
  echo "No se pudo descargar de GitHub; se usa el paquete hunspell-es de Ubuntu (también basado en RLA-ES)."
  for F in /usr/share/hunspell/es_ANY.dic /usr/share/hunspell/es_ES.dic /usr/share/myspell/es_ES.dic; do
    if [ -f "$F" ]; then
      cp "$F" "$WORK/rla/"
      cp "${F%.dic}.aff" "$WORK/rla/"
      break
    fi
  done
  cp /usr/share/doc/hunspell-es/copyright "$WORK/rla/LICENCIA-hunspell-es.txt" 2>/dev/null || true
fi

echo "Contenido obtenido:"; find "$WORK/rla" -type f | head -40
DIC="$(find "$WORK/rla" -name '*.dic' ! -name 'hyph*' ! -name 'th_*' | sort | head -1)"
[ -n "$DIC" ] || { echo "No se encontró ningún archivo .dic"; exit 1; }
AFF="${DIC%.dic}.aff"
echo "Usando $DIC"
unmunch "$DIC" "$AFF" > "$WORK/formas.txt"
echo "Formas expandidas: $(wc -l < "$WORK/formas.txt")"
python3 tools/preparar_datos.py "$WORK/formas.txt" --sin-propios --sin-encliticos
mkdir -p app/src/main/assets/licencias/rla-es
find "$WORK/rla" -type f -iname '*.txt' -exec cp {} app/src/main/assets/licencias/rla-es/ \;
echo "Palabras finales: $(wc -l < app/src/main/assets/palabras.txt)"
