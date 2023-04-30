package com.example.atomic_cinema.models

import androidx.compose.ui.graphics.ImageBitmap
import java.time.Year

data class Movie(
    val id : Int,
    val nameMovie : String,
    val yearOfIssue : Year,
    val description : String,
    val ageRating : Int,
    val director : String,
    val duration : Int,
    val image : ImageBitmap
)
