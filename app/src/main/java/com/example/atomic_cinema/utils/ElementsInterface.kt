package com.example.atomic_cinema.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.example.atomic_cinema.navigation.NavRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime


@Composable
fun RoundedTextField(
    label: String,
    value : String,
    placeholder: String,
    singleLine : Boolean = true,
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
        singleLine = singleLine,
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
        modifier = modifier ?: Modifier
            .fillMaxWidth(0.7f)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePicker(onValueChange: (LocalDate) -> Unit = {}) {
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
fun TimePicker(onValueChange: (LocalTime) -> Unit = {}){
    val time = LocalTime.now()
    val dialog = TimePickerDialog(
        LocalContext.current,
        {_, mHour : Int, mMinute: Int ->
            onValueChange(LocalTime.of(mHour, mMinute))
        },
        time.hour,
        time.minute,
        true
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoadingMoneyOperationScreen(
    navHostController: NavHostController,
    stateLoading : Boolean, sum : Double,
    operationIsSuccessful: Boolean?,
    isPaying : Boolean = false,
    destination: String
) {
    val navOptions = NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build()

    val stateIndicator = remember {
        MutableTransitionState(false).apply {
            // Start the animation immediately.
            targetState = true
        }
    }
    val state = remember {
        MutableTransitionState(false).apply {
            // Start the animation immediately.
            targetState = false
        }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(stateLoading, operationIsSuccessful){
        if(!stateLoading){
            stateIndicator.targetState = false

            if(operationIsSuccessful != null){
                scope.launch {
                    delay(1000L)
                    state.targetState = true
                }
            }
        }
    }

    AnimatedVisibility(
        visibleState = stateIndicator,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically)
    ) {
        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Red)
        }
    }

    AnimatedVisibility(
        visibleState = state,
        enter = scaleIn(),
        exit = scaleOut(animationSpec = snap(10000)) + shrinkVertically(shrinkTowards = Alignment.CenterVertically)
    ) {
        Box(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
            contentAlignment = Alignment.Center) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.ConfirmationNumber, contentDescription = "")

                if (isPaying) {
                    Text(text = if(operationIsSuccessful!!) "$sum ₽" else "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White)
                    Text(text = if(operationIsSuccessful) "Оплата прошла успешно" else "Оплата не удалась :(",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White)
                    Button(onClick = { navHostController.navigate(destination, navOptions) }) {
                        Text("ОК")
                    }
                } else {
                    Text(text = if(operationIsSuccessful!!) "$sum ₽" else "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White)
                    Text(text = if(operationIsSuccessful)"Пополнение баланса прошло успешно" else "Пополнение баланса не удалось :(",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = Color.White)
                    Button(onClick = {
                        Support.viewBalance = true
                        navHostController.navigate(destination, navOptions) }) {
                        Text("ОК")
                    }
                }
            }
        }
    }
}










