package com.example.ownadventure

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.TextHttpResponseHandler
import com.google.gson.Gson
import com.skydoves.landscapist.glide.GlideImage
import okhttp3.Headers


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokemonApp()
        }
    }
}

@Composable
fun PokemonApp() {
    var pokemonList by remember { mutableStateOf<List<PokemonSummary>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    val errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        fetchPokemonList { fetchedList ->
            pokemonList = fetchedList
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search Pokémon") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // Handle search action if needed
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = androidx.compose.ui.graphics.Color.Red)
        }

        LazyColumn {
            val filteredList =
                pokemonList.filter { it.name.contains(searchText, ignoreCase = true) }
            items(filteredList) { pokemonSummary ->
                PokemonListItem(pokemonSummary) { name ->
                    Toast.makeText(context, "$name clicked!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

fun fetchPokemonList(onSuccess: (List<PokemonSummary>) -> Unit) {
    val client = AsyncHttpClient()
    val url = "https://pokeapi.co/api/v2/pokemon?limit=100"

    client[url, null, object : TextHttpResponseHandler() {
        override fun onSuccess(statusCode: Int, headers: Headers, response: String) {
            val pokemonListResponse = Gson().fromJson(response, PokemonListResponse::class.java)
            onSuccess(pokemonListResponse.results)
        }

        override fun onFailure(
            statusCode: Int,
            headers: Headers?,
            errorResponse: String,
            t: Throwable?
        ) {
            onSuccess(emptyList())
        }
    }]
}

@Composable
fun PokemonListItem(pokemonSummary: PokemonSummary, onItemClick: (String) -> Unit) {
    var pokemonResponse by remember { mutableStateOf<PokemonResponse?>(null) }

    LaunchedEffect(pokemonSummary.url) {
        fetchPokemonData(pokemonSummary.url) { response, _ ->
            pokemonResponse = response
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick(pokemonSummary.name) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        pokemonResponse?.let { pokemon ->
            GlideImage(
                imageModel = { pokemon.sprites.front_default ?: "" },
                modifier = Modifier.size(100.dp)
            )
            Text(text = pokemon.name.capitalize(), fontSize = 18.sp)
            Text(text = pokemon.abilities.joinToString(", ") { it.ability.name }, fontSize = 14.sp)
        } ?: run {
            Text(text = "Loading...", fontSize = 18.sp)
        }
    }
}

fun fetchPokemonData(url: String, onComplete: (PokemonResponse?, String?) -> Unit) {
    val client = AsyncHttpClient()

    client[url, null, object : TextHttpResponseHandler() {
        override fun onSuccess(statusCode: Int, headers: Headers, response: String) {
            val pokemon = Gson().fromJson(response, PokemonResponse::class.java)
            onComplete(pokemon, null)
        }

        override fun onFailure(
            statusCode: Int,
            headers: Headers?,
            errorResponse: String,
            t: Throwable?
        ) {
            onComplete(null, "Failed to load Pokémon details")
        }
    }]
}
