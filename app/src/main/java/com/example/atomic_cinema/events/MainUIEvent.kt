package com.example.atomic_cinema.events

import androidx.compose.ui.text.input.TextFieldValue

sealed class MainUIEvent {
    data class BottomSheetValue(val value: Boolean) : MainUIEvent()
    object GetNews : MainUIEvent()
    object GetRevenueReport : MainUIEvent()
}