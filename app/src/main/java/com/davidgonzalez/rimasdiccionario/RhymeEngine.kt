package com.davidgonzalez.rimasdiccionario

/** Clave de rima de una palabra: consonante (fonética), asonante (vocales) y nº de sílabas. */
data class RhymeKey(val consonante: String, val asonante: String, val syllables: Int)

object RhymeEngine {
    private const val VOWELS = "aeiouáéíóúü"
    private const val STRONG = "aeoáéó"   // vocales fuertes
    private const val WEAK = "iuü"        // vocales débiles sin tilde

    /** Quita tildes y diéresis (conserva la ñ). Espera texto en minúsculas. */
    fun strip(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
            sb.append(
                when (ch) {
                    'á' -> 'a'
                    'é' -> 'e'
                    'í' -> 'i'
                    'ó' -> 'o'
                    'ú', 'ü' -> 'u'
                    else -> ch
                }
            )
        }
        return sb.toString()
    }

    private fun joins(a: Char, b: Char): Boolean {
        val aw = a in WEAK
        val bw = b in WEAK
        val aStrong = a in STRONG
        val bStrong = b in STRONG
        return (aw && bw) || (aStrong && bw) || (aw && bStrong)
    }

    private fun phonetic(s: String): String {
        var r = s.replace("ch", "#").replace("ll", "y").replace("h", "")
            .replace("v", "b").replace("z", "s")
        r = r.replace(Regex("c(?=[ei])"), "s")
        return r.replace("#", "ch")
    }

    fun analyze(raw: String): RhymeKey? {
        val w = raw.trim().lowercase()
        if (w.isEmpty() || w.any { !(it in 'a'..'z' || it in "áéíóúüñ") }) return null
        val n = w.length
        val c = w.toCharArray()
        val endsWithY = n > 1 && c[n - 1] == 'y' && c[n - 2] in VOWELS
        if (endsWithY) c[n - 1] = 'i'

        val v = BooleanArray(n) { c[it] in VOWELS }
        val silent = BooleanArray(n)
        for (i in 1 until n) {
            if (c[i] == 'u') {
                if (c[i - 1] == 'q' || (c[i - 1] == 'g' && i + 1 < n && c[i + 1] in "eiéí")) {
                    v[i] = false
                    silent[i] = true
                }
            }
        }

        val starts = ArrayList<Int>()
        val ends = ArrayList<Int>()
        var i = 0
        while (i < n) {
            if (!v[i]) { i++; continue }
            var e = i
            while (e + 1 < n && v[e + 1] && joins(c[e], c[e + 1])) e++
            starts.add(i)
            ends.add(e)
            i = e + 1
        }
        if (starts.isEmpty()) return null
        val last = starts.size - 1

        var sn = -1
        var sp = -1
        for (k in 0..last) for (p in starts[k]..ends[k]) {
            if (c[p] in "áéíóú") { sn = k; sp = p }
        }
        if (sn < 0) {
            val llana = w[n - 1] in "aeiouns"
            sn = if (llana && last >= 1) last - 1 else last
            var p = -1
            for (q in starts[sn]..ends[sn]) if (c[q] in STRONG) { p = q; break }
            if (p < 0) p = ends[sn]
            sp = p
        }

        val tail = StringBuilder()
        for (p in sp until n) if (!silent[p]) tail.append(c[p])
        val cons = phonetic(strip(tail.toString()))

        val aso = StringBuilder()
        aso.append(strip(c[sp].toString()))
        for (k in (sn + 1)..last) {
            var p = -1
            for (q in starts[k]..ends[k]) if (c[q] in STRONG) { p = q; break }
            if (p < 0) p = ends[k]
            aso.append(strip(c[p].toString()))
        }
        return RhymeKey(cons, aso.toString(), starts.size)
    }
}
