#!/usr/bin/env python3
"""Prepara los datos de Rimas Diccionario.

Uso:
  python tools/preparar_datos.py formas.txt            # -> app/src/main/assets/palabras.txt
  python tools/preparar_datos.py a.txt b.txt           # une varias listas
  python tools/preparar_datos.py --dic mi_dic.tsv      # valida y copia a diccionario.tsv

Opciones de limpieza (para listas generadas con `unmunch` de RLA-ES):
  --sin-propios      descarta formas con mayuscula inicial (nombres propios, siglas)
  --sin-encliticos   descarta verbo+pronombre (darselo, amarte, dandole...)

Formato de diccionario.tsv (TABULADORES, 5 columnas):
  palabra  sinonimos(;)  antonimos(;)  significado  usos(|)
"""
import argparse, re, sys
from pathlib import Path

ASSETS = Path(__file__).resolve().parent.parent / "app/src/main/assets"
LETRAS = re.compile(r"^[a-z\u00e1\u00e9\u00ed\u00f3\u00fa\u00fc\u00f1]+$")
T = str.maketrans("\u00e1\u00e9\u00ed\u00f3\u00fa\u00fc", "aeiouu")
VERBAL = re.compile(r"(ar|er|ir|ando|iendo|yendo)$")
C = ["me", "te", "se", "lo", "la", "le", "los", "las", "les", "nos", "os"]
COMBOS = sorted(set(C + [a + b for a in C for b in C]), key=len, reverse=True)


def sin_tildes(p): return p.translate(T)
def clave(p): return sin_tildes(p).replace("\u00f1", "n{")


def quitar_encliticos(palabras):
    base = {sin_tildes(p) for p in palabras}
    salida = []
    for w in palabras:
        s = sin_tildes(w)
        drop = False
        for c in COMBOS:
            if s.endswith(c) and len(s) - len(c) >= 3:
                stem = s[: -len(c)]
                if stem in base and VERBAL.search(stem):
                    drop = True
                    break
        if not drop:
            salida.append(w)
    return salida


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("entradas", nargs="*")
    ap.add_argument("--dic")
    ap.add_argument("--sin-propios", action="store_true")
    ap.add_argument("--sin-encliticos", action="store_true")
    a = ap.parse_args()
    if a.dic:
        ok = 0
        out = ["# palabra<TAB>sinonimos(;)<TAB>antonimos(;)<TAB>significado<TAB>usos(|)"]
        for ln in Path(a.dic).read_text(encoding="utf-8").splitlines():
            if not ln.strip() or ln.startswith("#"):
                continue
            c = ln.split("\t")
            if len(c) < 4:
                continue
            c += [""] * (5 - len(c))
            out.append("\t".join(x.strip() for x in c[:5]))
            ok += 1
        (ASSETS / "diccionario.tsv").write_text("\n".join(out) + "\n", encoding="utf-8")
        print(f"diccionario.tsv: {ok} entradas")
    if a.entradas:
        s = set()
        for e in a.entradas:
            for ln in Path(e).read_text(encoding="utf-8", errors="ignore").splitlines():
                p = ln.strip()
                if a.sin_propios and p != p.lower():
                    continue
                p = p.lower()
                if len(p) > 1 and LETRAS.match(p):
                    s.add(p)
        pal = sorted(s, key=clave)
        if a.sin_encliticos:
            antes = len(pal)
            pal = quitar_encliticos(pal)
            print(f"enclíticos descartados: {antes - len(pal)}")
        (ASSETS / "palabras.txt").write_text("\n".join(pal) + "\n", encoding="utf-8")
        print(f"palabras.txt: {len(pal)} palabras")
    if not a.entradas and not a.dic:
        ap.print_help()
        sys.exit(1)


if __name__ == "__main__":
    main()
