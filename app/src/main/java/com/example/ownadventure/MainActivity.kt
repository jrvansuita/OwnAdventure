package com.example.ownadventure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var pokemonName by remember { mutableStateOf("Pikachu") }
    var pokemonImageUrl by remember { mutableStateOf("") }
    var pokemonAbilities by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var userInput by remember { mutableStateOf(pokemonName) } // State to track user input

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text("Enter Pokémon Name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        GlideImage(
            imageModel = { pokemonImageUrl },
            modifier = Modifier.size(200.dp)
        )

        Text(text = pokemonName, fontSize = 24.sp)
        Text(text = pokemonAbilities, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.Red
            )
        }

        Button(
            onClick = {
                if (userInput.isNotBlank()) {
                    fetchPokemonData(userInput.lowercase(), onSuccess = {
                        pokemonName = it.name.capitalize()
                        pokemonAbilities = it.abilities.joinToString(", ") { ability -> ability.ability.name }
                        pokemonImageUrl = it.sprites.front_default
                        errorMessage = ""
                    }, onFailure = {
                        errorMessage = "Failed to load Pokémon data"
                    })
                } else {
                    errorMessage = "Please enter a Pokémon name"
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Fetch Pokémon")
        }
    }
}

fun fetchPokemonData(
    pokemonName: String,
    onSuccess: (PokemonResponse) -> Unit,
    onFailure: () -> Unit
) {
    val client = AsyncHttpClient()
    val url = "https://pokeapi.co/api/v2/pokemon/$pokemonName"

    client[url, null, object : TextHttpResponseHandler() {
        override fun onSuccess(statusCode: Int, headers: Headers, response: String) {
            response.let {
                val pokemon = Gson().fromJson(it, PokemonResponse::class.java)
                onSuccess(pokemon)
            }
        }

        override fun onFailure(
            statusCode: Int,
            headers: Headers?,
            errorResponse: String,
            t: Throwable?
        ) {
            onFailure()
        }
    }]
}
