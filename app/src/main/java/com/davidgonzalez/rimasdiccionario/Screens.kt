package com.davidgonzalez.rimasdiccionario

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val CAP = 2000
private const val VERSOMETRO_URL = "https://versometro.com"
private const val RLA_URL = "https://github.com/sbosio/rla-es"
private const val MPL_URL = "https://www.mozilla.org/MPL/1.1/"

// ---------- Componentes comunes ----------

@Composable
private fun Title(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
    )
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SearchField(value: String, onValue: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Search,
        ),
        trailingIcon = { if (value.isNotEmpty()) TextButton(onClick = { onValue("") }) { Text("✕") } },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun WordRow(word: String, syl: Int, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(word, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            "$syl síl.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun SyllableFilter(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("Todas", "1", "2", "3", "4+")
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { i, l ->
            FilterChip(
                selected = selected == i,
                onClick = { onSelect(i) },
                label = { Text(if (i == 0) l else "$l síl.") },
            )
        }
    }
}

private fun passes(repo: Repository, id: Int, f: Int): Boolean {
    val s = repo.syllables(id)
    return f == 0 || (f < 4 && s == f) || (f == 4 && s >= 4)
}

private fun LazyListScope.wordSection(title: String, ids: List<Int>, repo: Repository, onTap: (String) -> Unit) {
    item { SectionHeader("$title (${ids.size})") }
    if (ids.isEmpty()) item { Hint("Sin resultados") }
    items(ids.take(CAP)) { id -> WordRow(repo.words[id], repo.syllables(id)) { onTap(repo.words[id]) } }
    if (ids.size > CAP) item { Hint("Mostrando $CAP de ${ids.size}. Afina la búsqueda para ver el resto.") }
}

private fun plural(n: Int) = if (n == 1) "sílaba" else "sílabas"

// ---------- Pantalla: Rimas ----------

@Composable
fun RimasScreen(
    repo: Repository,
    query: String,
    onQuery: (String) -> Unit,
    onDict: (String) -> Unit,
    onMap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val result by produceState<RhymeResult?>(null, query) {
        delay(200)
        value = withContext(Dispatchers.Default) { repo.rhymes(query) }
    }
    val consF = remember(result, filter) { result?.consonantes?.filter { passes(repo, it, filter) } ?: emptyList() }
    val asoF = remember(result, filter) { result?.asonantes?.filter { passes(repo, it, filter) } ?: emptyList() }

    LazyColumn(modifier) {
        item { Title("Buscar rimas") }
        item { SearchField(query, onQuery, "Escribe una palabra") }
        val r = result
        if (query.isBlank()) {
            item { Hint("Escribe una palabra para ver sus rimas consonantes y asonantes.") }
        } else if (r == null) {
            item { Hint("No se pudo analizar esa palabra (usa solo letras, sin espacios).") }
        } else {
            item {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(query.trim(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${r.key.syllables} ${plural(r.key.syllables)} · rima consonante en «-${r.key.consonante}»")
                        Text("Asonancia: ${r.key.asonante.toList().joinToString("-")}")
                        Row {
                            TextButton(onClick = { onMap(query.trim()) }) { Text("Ver mapa") }
                            TextButton(onClick = { onDict(query.trim()) }) { Text("Significado") }
                        }
                    }
                }
            }
            item { SyllableFilter(filter) { filter = it } }
            wordSection("Rimas consonantes", consF, repo, onDict)
            wordSection("Rimas asonantes", asoF, repo, onDict)
        }
    }
}

// ---------- Pantalla: Finales (terminaciones exactas) ----------

@Composable
fun FinalesScreen(
    repo: Repository,
    query: String,
    onQuery: (String) -> Unit,
    onWord: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var strict by rememberSaveable { mutableStateOf(false) }
    val ids by produceState(emptyList<Int>(), query, strict) {
        delay(200)
        value = withContext(Dispatchers.Default) { repo.endingSearch(query, strict) }
    }
    LazyColumn(modifier) {
        item { Title("Palabras por terminación") }
        item { SearchField(query, onQuery, "Letras finales exactas (ej. ando, ción)") }
        item {
            Row(
                Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(checked = strict, onCheckedChange = { strict = it })
                Text("  Respetar tildes", style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (query.isBlank()) {
            item { Hint("Escribe las letras con las que deben terminar las palabras.") }
        } else {
            wordSection("Terminan en «-${query.trim().lowercase()}»", ids, repo, onWord)
        }
    }
}

// ---------- Pantalla: Mapa de rimas ----------

@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.mapGroups(
    title: String,
    groups: Map<Int, List<Int>>,
    repo: Repository,
    onTap: (String) -> Unit,
) {
    item { SectionHeader(title) }
    if (groups.isEmpty()) item { Hint("Sin resultados") }
    groups.forEach { (syl, ids) ->
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    "$syl ${plural(syl)} · ${ids.size}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ids.take(40).forEach { id ->
                        AssistChip(onClick = { onTap(repo.words[id]) }, label = { Text(repo.words[id]) })
                    }
                }
                if (ids.size > 40) Hint("+${ids.size - 40} más")
            }
        }
    }
}

@Composable
fun MapaScreen(
    repo: Repository,
    query: String,
    onQuery: (String) -> Unit,
    onRhymes: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val result by produceState<RhymeResult?>(null, query) {
        delay(200)
        value = withContext(Dispatchers.Default) { repo.rhymes(query) }
    }
    val consG: Map<Int, List<Int>> = remember(result) {
        result?.consonantes?.groupBy { repo.syllables(it) }?.toSortedMap() ?: emptyMap()
    }
    val asoG: Map<Int, List<Int>> = remember(result) {
        result?.asonantes?.groupBy { repo.syllables(it) }?.toSortedMap() ?: emptyMap()
    }
    LazyColumn(modifier) {
        item { Title("Mapa de rimas") }
        item { SearchField(query, onQuery, "Palabra central") }
        val r = result
        if (query.isBlank()) {
            item { Hint("Escribe una palabra: verás su familia de rimas ordenada por número de sílabas. Toca cualquier palabra para recentrar el mapa.") }
        } else if (r == null) {
            item { Hint("No se pudo analizar esa palabra.") }
        } else {
            item {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(query.trim(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("«-${r.key.consonante}» · asonancia ${r.key.asonante.toList().joinToString("-")}")
                        TextButton(onClick = { onRhymes(query.trim()) }) { Text("Ver lista completa") }
                    }
                }
            }
            mapGroups("Consonantes (${r.consonantes.size})", consG, repo, onQuery)
            mapGroups("Asonantes (${r.asonantes.size})", asoG, repo, onQuery)
        }
    }
}

// ---------- Pantalla: Palabra (diccionario) ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsBlock(title: String, items: List<String>, onTap: (String) -> Unit) {
    if (items.isEmpty()) return
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { AssistChip(onClick = { onTap(it) }, label = { Text(it) }) }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
fun DiccionarioScreen(
    repo: Repository,
    query: String,
    onQuery: (String) -> Unit,
    onRhymes: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entry = remember(query) { repo.entry(query) }
    Column(modifier.verticalScroll(rememberScrollState())) {
        Title("Significado, sinónimos y antónimos")
        SearchField(query, onQuery, "Palabra")
        if (query.isBlank()) {
            Hint("Busca una palabra para ver su significado, sinónimos, antónimos y formas de uso. (${repo.dictionarySize} entradas en el diccionario)")
        } else if (entry == null) {
            Hint("Esa palabra aún no tiene entrada en el diccionario sin conexión.")
            TextButton(onClick = { onRhymes(query.trim()) }, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Ver sus rimas")
            }
        } else {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(entry.palabra, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (entry.significado.isNotEmpty()) {
                        Text("Significado", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text(entry.significado)
                        Spacer(Modifier.height(12.dp))
                    }
                    ChipsBlock("Sinónimos", entry.sinonimos, onQuery)
                    ChipsBlock("Antónimos", entry.antonimos, onQuery)
                    if (entry.usos.isNotEmpty()) {
                        Text("Formas de uso", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        entry.usos.forEach { Text("• $it", modifier = Modifier.padding(vertical = 2.dp)) }
                        Spacer(Modifier.height(8.dp))
                    }
                    TextButton(onClick = { onRhymes(entry.palabra) }) { Text("Ver rimas de «${entry.palabra}»") }
                }
            }
        }
    }
}

// ---------- Pantalla: Más (Versometro + créditos) ----------

@Composable
fun MasScreen(repo: Repository, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val version = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (_: Exception) {
            ""
        }
    }
    Column(modifier.verticalScroll(rememberScrollState())) {
        Title("Más")

        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Te recomendamos", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_versometro),
                        contentDescription = "Icono de Versometro",
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
                    )
                    Column(Modifier.padding(start = 16.dp)) {
                        Text("Versometro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("La app de David González Esquerra para escribir y trabajar tus versos.")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = { uriHandler.openUri(VERSOMETRO_URL) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Conocer Versometro en versometro.com")
                }
            }
        }

        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Agradecimientos y reconocimiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Rimas Diccionario fue creada por David González Esquerra, creador de Versometro, para ayudar a poetas, decimeros, trovadores y compositores de habla hispana a encontrar la rima justa, sin necesidad de conexión.")
                Spacer(Modifier.height(8.dp))
                Text("Reconocimiento al creador, diseñador y desarrollador: David González Esquerra.", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Gracias a ti por usar Rimas Diccionario: tu apoyo y tus comentarios hacen crecer este proyecto.")
            }
        }

        Card(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Fuentes y licencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("La lista de palabras proviene del Diccionario ortográfico libre de español RLA-ES (Hunspell / LibreOffice), © Santiago Bosio y colaboradores. Ese diccionario se distribuye bajo un triple esquema de licencias a elección de quien lo usa: GNU GPL 3 o posterior, GNU LGPL 3 o posterior o Mozilla Public License 1.1 o posterior. Rimas Diccionario lo utiliza bajo los términos de la Mozilla Public License 1.1.")
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { uriHandler.openUri(RLA_URL) }) { Text("Proyecto original (RLA-ES)") }
                TextButton(onClick = { uriHandler.openUri(MPL_URL) }) { Text("Texto de la licencia MPL 1.1") }
            }
        }

        Hint("Funciona 100% sin conexión · ${repo.words.size} palabras · ${repo.dictionarySize} entradas de diccionario · v$version")
        Spacer(Modifier.height(24.dp))
    }
}
