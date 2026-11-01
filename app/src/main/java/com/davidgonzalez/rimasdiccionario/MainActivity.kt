package com.davidgonzalez.rimasdiccionario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = (application as RimarioApplication).repo
        setContent { RimarioTheme { RimarioRoot(repo) } }
    }
}

private class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Rimas", Icons.Default.Search),
    Tab("Finales", Icons.Default.Edit),
    Tab("Mapa", Icons.Default.Place),
    Tab("Palabra", Icons.AutoMirrored.Filled.List),
    Tab("Más", Icons.Default.Info),
)

@Composable
fun RimarioRoot(repo: Repository) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var qRima by rememberSaveable { mutableStateOf("") }
    var qFinal by rememberSaveable { mutableStateOf("") }
    var qMapa by rememberSaveable { mutableStateOf("") }
    var qDict by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { repo.ensureLoaded() }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label, maxLines = 1) },
                    )
                }
            }
        },
    ) { pad ->
        val mod = Modifier.padding(pad).statusBarsPadding().imePadding().fillMaxSize()
        if (!repo.ready) {
            Box(mod, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Preparando la galería de palabras…")
                }
            }
        } else {
            when (tab) {
                0 -> RimasScreen(
                    repo, qRima, { qRima = it },
                    onDict = { qDict = it; tab = 3 },
                    onMap = { qMapa = it; tab = 2 },
                    modifier = mod,
                )
                1 -> FinalesScreen(
                    repo, qFinal, { qFinal = it },
                    onWord = { qRima = it; tab = 0 },
                    modifier = mod,
                )
                2 -> MapaScreen(
                    repo, qMapa, { qMapa = it },
                    onRhymes = { qRima = it; tab = 0 },
                    modifier = mod,
                )
                3 -> DiccionarioScreen(
                    repo, qDict, { qDict = it },
                    onRhymes = { qRima = it; tab = 0 },
                    modifier = mod,
                )
                else -> MasScreen(repo, mod)
            }
        }
    }
}
