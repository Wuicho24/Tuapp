package com.example.tuapp.ruleta

data class WordLevel(
    val id: String,
    val clave: String,
    val opciones: List<String>,
    val correctas: List<String>
)

val niveles = listOf(
    WordLevel(
        id = "Nivel ma",
        clave = "ma",
        opciones = listOf("nzana", "ma", "riposa", "dera", "estro", "ti", "leta", "no", "sa", "ngo", "yo", "pa", "mapo"),
        correctas = listOf("manzana", "mamá", "mariposa", "madera", "maestro", "maleta", "mano", "mesa", "mango", "mapa")
    ),
    WordLevel(
        id = "Nivel re",
        clave = "re",
        opciones = listOf("loj", "ír", "galo", "petir", "trato", "baño", "ina", "dondo", "gla", "po", "co", "yo", "pollo"),
        correctas = listOf("reloj", "reír", "regalo", "repetir", "retrato", "rebaño", "reina", "redondo", "regla", "repollo")
    ),
    WordLevel(
        id = "Nivel pa",
        clave = "pa",
        opciones = listOf("pel", "dre", "red", "tio", "loma", "ís", "la", "lmera", "leta", "lo", "cífico", "tria", "nal"),
        correctas = listOf("papel", "padre", "pared", "patio", "paloma", "país", "pala", "palmera", "paleta", "palo")
    ),
    WordLevel(
        id = "Nivel co",
        clave = "co",
        opciones = listOf("mida", "che", "lor", "nejo", "llar", "razón", "cina", "rona", "fre", "pa", "dia"),
        correctas = listOf("comida", "coche", "color", "conejo", "collar", "corazón", "cocina", "corona", "cofre", "copa")
    )
)
