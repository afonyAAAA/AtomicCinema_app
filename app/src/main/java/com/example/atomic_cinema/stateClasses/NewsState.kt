package com.example.atomic_cinema.stateClasses

data class NewsState(
    val isLoading : Boolean = false,
    val title : String = "",
    val description : String = "",
    val linkImage : String = "",
    val linkNews : String = "",
    val bottomSheetValue : Boolean = true
)
