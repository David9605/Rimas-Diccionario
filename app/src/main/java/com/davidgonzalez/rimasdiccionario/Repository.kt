package com.davidgonzalez.rimasdiccionario

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RimarioApplication : Application() {
    val repo: Repository by lazy { Repository(this) }
}

data class RhymeResult(val key: RhymeKey, val consonantes: List<Int>, val asonantes: List<Int>)

class DictEntry(
    val palabra: String,
    val sinonimos: List<String>,
    val antonimos: List<String>,
    val significado: String,
    val usos: List<String>,
)

/**
 * Todo se lee de assets/ (sin red):
 *  - palabras.txt: una palabra por línea (la galería de ~250 mil palabras)
 *  - diccionario.tsv: palabra<TAB>sinónimos(;)<TAB>antónimos(;)<TAB>significado<TAB>usos(|)
 */
class Repository(private val context: Context) {
    var ready by mutableStateOf(false)
        private set

    var words: Array<String> = emptyArray()
        private set
    var dictionarySize: Int = 0
        private set

    private var syl = ByteArray(0)
    private val cons = HashMap<String, ArrayList<Int>>()
    private val aso = HashMap<String, ArrayList<Int>>()
    private val dictLines = ArrayList<String>()
    private val dictIndex = HashMap<String, Int>()
    private val mutex = Mutex()

    fun syllables(i: Int): Int = syl[i].toInt()

    suspend fun ensureLoaded() {
        mutex.withLock {
            if (ready) return
            withContext(Dispatchers.Default) {
                loadWords()
                loadDictionary()
            }
            ready = true
        }
    }

    private fun loadWords() {
        val list = ArrayList<String>(260_000)
        context.assets.open("palabras.txt").bufferedReader().useLines { seq ->
            seq.forEach { line ->
                val t = line.trim()
                if (t.isNotEmpty()) list.add(t)
            }
        }
        words = list.toTypedArray()
        syl = ByteArray(words.size)
        for (i in words.indices) {
            val k = RhymeEngine.analyze(words[i]) ?: continue
            syl[i] = k.syllables.coerceAtMost(127).toByte()
            cons.getOrPut(k.consonante) { ArrayList() }.add(i)
            aso.getOrPut(k.asonante) { ArrayList() }.add(i)
        }
    }

    private fun loadDictionary() {
        try {
            context.assets.open("diccionario.tsv").bufferedReader().useLines { seq ->
                seq.forEach { line ->
                    if (line.isBlank() || line.startsWith("#")) return@forEach
                    val head = line.substringBefore('\t').trim().lowercase()
                    if (head.isEmpty()) return@forEach
                    dictIndex[RhymeEngine.strip(head)] = dictLines.size
                    dictLines.add(line)
                }
            }
        } catch (_: Exception) {
            // diccionario opcional
        }
        dictionarySize = dictLines.size
    }

    fun rhymes(word: String): RhymeResult? {
        val key = RhymeEngine.analyze(word) ?: return null
        val w = word.trim()
        val consIds = cons[key.consonante].orEmpty()
        val consSet = HashSet(consIds)
        val c = consIds.filter { !words[it].equals(w, ignoreCase = true) }
        val a = aso[key.asonante].orEmpty()
            .filter { it !in consSet && !words[it].equals(w, ignoreCase = true) }
        return RhymeResult(key, c, a)
    }

    /** Palabras que terminan exactamente con las letras dadas. */
    fun endingSearch(query: String, respectAccents: Boolean): List<Int> {
        val s = query.trim().lowercase()
        if (s.isEmpty()) return emptyList()
        val target = if (respectAccents) s else RhymeEngine.strip(s)
        val k = target.length
        val out = ArrayList<Int>()
        for (i in words.indices) {
            val w = words[i]
            if (w.length < k) continue
            val tail = w.substring(w.length - k).lowercase()
            val cmp = if (respectAccents) tail else RhymeEngine.strip(tail)
            if (cmp == target) out.add(i)
        }
        return out
    }

    fun entry(word: String): DictEntry? {
        val idx = dictIndex[RhymeEngine.strip(word.trim().lowercase())] ?: return null
        val p = dictLines[idx].split('\t')
        fun list(i: Int, sep: Char) =
            p.getOrElse(i) { "" }.split(sep).map { it.trim() }.filter { it.isNotEmpty() }
        return DictEntry(
            palabra = p[0].trim(),
            sinonimos = list(1, ';'),
            antonimos = list(2, ';'),
            significado = p.getOrElse(3) { "" }.trim(),
            usos = list(4, '|'),
        )
    }
}
