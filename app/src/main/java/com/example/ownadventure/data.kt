package com.example.ownadventure

data class PokemonResponse(
    val name: String,
    val abilities: List<AbilityEntry>,
    val sprites: Sprites
)

data class AbilityEntry(
    val ability: Ability
)

data class Ability(
    val name: String
)

data class Sprites(
    val front_default: String
)
