package com.example.ownadventure


data class PokemonListResponse(val count: Int, val next: String?, val previous: String?, val results: List<PokemonSummary>)
data class PokemonSummary(val name: String, val url: String)
data class PokemonResponse(val name: String, val abilities: List<Ability>, val sprites: Sprites)
data class Ability(val ability: AbilityDetails)
data class AbilityDetails(val name: String)
data class Sprites(val front_default: String?)
