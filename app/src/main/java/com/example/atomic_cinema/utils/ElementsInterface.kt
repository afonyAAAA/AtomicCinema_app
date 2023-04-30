package com.example.atomic_cinema.utils

import android.app.DatePickerDialog
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import com.example.atomic_cinema.events.AuthUIEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter


    @Composable
    fun RoundedTextField(
        label: String,
        value : String,
        placeholder: String,
        notNull : Boolean = false,
        readOnly : Boolean = false,
        modifier: Modifier? = null,
        switchFocus : Boolean = true,
        enabled : Boolean = true,
        visualTransformation: VisualTransformation = VisualTransformation.None,
        trailingIcon: @Composable (() -> Unit)? = null,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions(),
        onValueChange: (String) -> Unit
    ) {
        val focusManager = LocalFocusManager.current

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            label = { Text(text = label) },
            readOnly = readOnly,
            placeholder = { Text(text = placeholder) },
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            isError = if(notNull) value.isEmpty() else false,
            keyboardOptions = keyboardOptions,
            keyboardActions = if(switchFocus) KeyboardActions (
                onDone = {focusManager.clearFocus()}) else
                keyboardActions,
            modifier = modifier ?: Modifier.fillMaxWidth(0.7f).padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun DatePicker(onValueChange: (LocalDate) -> Unit = {}, ) {
        val date = LocalDate.now()
        val dialog = DatePickerDialog(
            LocalContext.current,
            { _, year, month, dayOfMonth ->
                onValueChange(LocalDate.of(year, month + 1, dayOfMonth))
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth,
        )
        dialog.show()
    }

    @Composable
    fun LoadingScreen(){
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
            contentAlignment = Alignment.Center){
            CircularProgressIndicator(color = Color.White)
        }
    }








