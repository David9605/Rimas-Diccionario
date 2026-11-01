# Licencias de terceros — qué hacer para cumplirlas

> Esto es una guía práctica, no asesoría legal.

**Qué usas:** el diccionario RLA-ES (triple licencia GPL 3+ / LGPL 3+ / MPL 1.1+). Lo usas bajo **MPL 1.1** (igual que Versometro).

**Ya incluido en el proyecto:**
- Sección "Fuentes y licencias" dentro de la app (pestaña *Más*) con el copyright de Santiago Bosio y colaboradores, el esquema de licencias, el enlace al proyecto original y al texto MPL 1.1.
- `NOTICE.md` en la raíz del repositorio.
- El workflow *Generar palabras desde RLA-ES* copia los textos de licencia del diccionario a `app/src/main/assets/licencias/rla-es/`, de modo que viajan dentro de la app.

**Recomendado para estar tranquilo con la MPL 1.1:**
1. Mantén `palabras.txt` público en tu repositorio de GitHub (es la forma "fuente" de la lista derivada) o enlaza al proyecto original RLA-ES.
2. Si modificas la lista (por ejemplo, la limpias o añades palabras), indícalo en `NOTICE.md` y no quites los avisos de copyright.
3. El texto de la MPL 1.1 viaja dentro de la app: el build lo copia desde el paquete del diccionario a `app/src/main/assets/licencias/rla-es/`. Si quieres, guarda además una copia en `docs/` (https://www.mozilla.org/MPL/1.1/).
4. En la ficha de Google Play, menciona la fuente en la descripción (ya está en `docs/ficha_play_store.md`).

El código de la app (Kotlin) es tuyo; elige la licencia que quieras para él o déjalo privado. La MPL es una licencia de "copyleft débil" por archivo: aplica a los archivos del diccionario, no obliga a abrir el código de la app.
