# Rimas Diccionario — buscador de rimas 100% sin conexión (Android)

Creada por **David González Esquerra**, creador de Versometro.

## Funciones
- **Rimas**: consonantes y asonantes de cualquier palabra, con filtro por sílabas.
- **Finales**: palabras que terminan en letras exactas (con o sin tildes).
- **Mapa de rimas**: familia de rimas agrupada por sílabas; toca una palabra para recentrar.
- **Palabra**: significado, sinónimos, antónimos y formas de uso.
- **Más**: recomienda **Versometro** (con su icono, enlace a versometro.com), agradecimientos al creador y licencias.
- Sin permisos, sin red, sin anuncios, sin recopilar datos.

## Datos
La app lee de `app/src/main/assets/`:
| Archivo | Contenido |
|---|---|
| `palabras.txt` | una palabra por línea |
| `diccionario.tsv` | `palabra⇥sinónimos(;)⇥antónimos(;)⇥significado⇥usos(\|)` |
| `licencias/rla-es/` | textos de licencia de RLA-ES (los copia el workflow) |

Vienen **datos de muestra** (182 palabras, 24 entradas). Para cargar la lista real:
1. **Automático**: cada build (workflow *Compilar Rimas Diccionario*) descarga RLA-ES, expande las formas con `unmunch`, limpia y genera `palabras.txt` antes de compilar. No tienes que hacer nada.
   (Opcional: el workflow *Generar palabras…* las guarda en el repositorio.)
2. Sinónimos, antónimos y significados: solo salen para palabras con fila en `diccionario.tsv`. Valida el tuyo con `python tools/preparar_datos.py --dic mi_dic.tsv`.

## Fuentes y licencias
Ver `NOTICE.md` y `docs/LICENCIAS_TERCEROS.md` (RLA-ES, MPL 1.1).

## Compilar
- **GitHub**: Actions → *Compilar Rimas Diccionario*. Siempre genera el APK de depuración; con los secretos configurados genera también APK + AAB firmados.
- **Local** (JDK 17 + Android SDK 36): `gradle assembleDebug` (o `gradle wrapper` una vez para crear `./gradlew`).

## Publicar
Todo en `docs/PUBLICAR_GOOGLE_PLAY.md`. Materiales de la tienda en `docs/play-store/`.
