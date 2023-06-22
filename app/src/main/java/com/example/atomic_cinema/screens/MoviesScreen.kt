package com.example.atomic_cinema.screens

import android.graphics.Movie
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navArgument
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.atomic_cinema.events.MovieFilterUIEvent
import com.example.atomic_cinema.events.MovieUIEvent
import com.example.atomic_cinema.events.SeanceUIEvent
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.server.movie.MovieResult
import com.example.atomic_cinema.server.seance.SeanceResult
import com.example.atomic_cinema.stateClasses.*
import com.example.atomic_cinema.utils.LoadingScreen
import com.example.atomic_cinema.utils.RoundedTextField
import com.example.atomic_cinema.utils.Support
import com.example.atomic_cinema.utils.toMyDateFormat
import com.example.atomic_cinema.viewModel.AuthViewModel
import com.example.atomic_cinema.viewModel.MovieViewModel
import com.example.atomic_cinema.viewModel.SeanceViewModel
import com.himanshoe.kalendar.Kalendar
import com.himanshoe.kalendar.color.KalendarThemeColor
import com.himanshoe.kalendar.model.KalendarDay
import com.himanshoe.kalendar.model.KalendarEvent
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


@Composable
fun MoviesScreen(
    navHostController: NavHostController,
    viewModelM: MovieViewModel = hiltViewModel(),
    viewModelS: SeanceViewModel = hiltViewModel(),
    viewModelA: AuthViewModel = hiltViewModel(),
){

    val stateMovie = viewModelM.stateMovie
    val stateAuth = viewModelA.state
    val stateFilterMovie = viewModelM.stateFilterMovie
    val stateSeance = viewModelS.state
    val listStateMovie = viewModelM.listMovie
    val searchedMovie = viewModelM.searchListMovie
    val context = LocalContext.current

    LaunchedEffect(viewModelM, context){
        viewModelM.movieResults.collect{ result ->
            when(result){
                is MovieResult.Unauthorized -> {}
                is MovieResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()
                }
                is MovieResult.MovieIsDeleted -> {
                    Toast.makeText(
                        context,
                        "Фильм удалён.",
                        Toast.LENGTH_LONG).show()

                    navHostController.navigate(NavRoutes.Movies.route, NavOptions.Builder()
                        .setPopUpTo(NavRoutes.Main.route, true).build())
                }
                else -> {

                }
            }
        }
    }

    LaunchedEffect(viewModelS, context){
        viewModelS.seanceResults.collect{ result ->
            when(result){
                is SeanceResult.Unauthorized -> {
                    Toast.makeText(
                        context, "Авторизируйтесь в системе.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navHostController.navigate(NavRoutes.Authorization.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is SeanceResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()
                }
                is SeanceResult.SeanceIsDeleted -> {
                    Toast.makeText(
                        context,
                        "Сеанс удалён",
                        Toast.LENGTH_LONG).show()

                    viewModelS.onEvent(SeanceUIEvent.PlaceHolderChanged(false))
                }
                else -> {

                }
            }
        }
    }

    LaunchedEffect(viewModelA, context){
        viewModelA.authResults.collect{ result ->
//            when(result){
//               else -> {}
//            }
        }
    }

    if(stateMovie.isLoading){
        LoadingScreen()
    }else{
        if(stateMovie.showDetailsMovie){
            DetailsMovie(
                viewModelM = viewModelM,
                movieS = stateMovie,
                viewModelS = viewModelS,
                seanceS = stateSeance,
                navHostController = navHostController,
                authS = stateAuth
            )
        }else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally){
                Box(modifier = Modifier.fillMaxWidth()){
                    SearchView(state = stateFilterMovie.searchText, viewModel = viewModelM)
                }
                    AnimatedVisibility(
                        visibleState = stateFilterMovie.animateVisibilityFilter,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Box(Modifier.padding(start = 15.dp)){
                            FilterTab(state = stateFilterMovie, viewModel = viewModelM)
                        }
                    }
                if(!stateFilterMovie.filterIsActivated){
                    Button(onClick = { viewModelM.onEventFilterMovie(MovieFilterUIEvent.FilterIsChanged(true))}) {
                        Text(text = "Фильтр поиска")
                    }
                }
                LazyColumn(contentPadding = PaddingValues(5.dp)) {
                    item {
                        if(viewModelM.listMovie.isEmpty()){
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "По вашему запросу ничего не найдено :(")
                            }
                        }
                    }
                    items(items = if(stateFilterMovie.searchText.text.isEmpty())
                        listStateMovie
                    else
                        searchedMovie)  { movie ->

                        val painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(movie.linkImage.replace("https", "http"))
                                .size(coil.size.Size.ORIGINAL)
                                .crossfade(true)
                                .build()
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .clickable(onClick = {
                                    viewModelM.onEventMovie(MovieUIEvent.MovieChanged(movie))
                                    viewModelM.onEventMovie(MovieUIEvent.ShowDetailsMovieChanged(
                                        true))
                                }),
                            shape = RoundedCornerShape(10.dp),
                            elevation = 5.dp,
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {

                                when (painter.state) {
                                    is AsyncImagePainter.State.Loading -> {
                                        CircularProgressIndicator(modifier = Modifier.padding(10.dp))
                                    }
                                    is AsyncImagePainter.State.Error -> {
                                        Icon(Icons.Filled.ErrorOutline, "", modifier = Modifier.width(150.dp))
                                    }
                                    else -> {
                                        Image(
                                            painter = painter,
                                            contentDescription = "",
                                            modifier = Modifier
                                                .height(200.dp).width(150.dp)
                                                .clip(RoundedCornerShape(topStart = 10.dp,
                                                    bottomStart = 10.dp)),
                                            Alignment.CenterStart,
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(start = 10.dp)) {
                                    Text(text = movie.nameMovie,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp)

                                    Spacer(Modifier.height(50.dp))

                                    Text(text = movie.genreList.joinToString())

                                    Spacer(Modifier.height(50.dp))

                                    Text(text = movie.director)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DetailsMovie(
    viewModelM: MovieViewModel,
    viewModelS: SeanceViewModel,
    authS : AuthState,
    movieS : MovieState,
    seanceS: SeanceState,
    navHostController: NavHostController,
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(movieS.linkImage.replace("https", "http"))
            .size(coil.size.Size.ORIGINAL)
            .crossfade(true)
            .build()
    )
    fun onDayClicked(day: KalendarDay, events: List<KalendarEvent>) {
        viewModelS.onEvent(SeanceUIEvent.SelectedDateChanged(day.localDate.toJavaLocalDate()))
    }

    var movieIsDelete by rememberSaveable{mutableStateOf(false)}

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )

    LaunchedEffect(seanceS.placeHolderIsOpen){
        if(seanceS.placeHolderIsOpen){
            scope.launch {
                bottomSheetState.show()
            }
        }else{
            scope.launch {
                bottomSheetState.hide()
            }
        }
    }
    LaunchedEffect(bottomSheetState.isVisible){
        if(!bottomSheetState.isVisible){
            viewModelS.onEvent(SeanceUIEvent.PlaceHolderChanged(false))
        }
    }

    val state = rememberLazyListState()

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        sheetContent = {
            Box(modifier = Modifier
                .background(Color.White)
                .height(500.dp)
                .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp)),
                contentAlignment = Alignment.TopCenter) {
                if(seanceS.isLoading){
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter){
                        CircularProgressIndicator()
                    }
                }else{
                    LazyColumn(state = state) {
                        item {
                            Kalendar(
                                kalendarThemeColor = KalendarThemeColor(
                                    MaterialTheme.colors.primary,
                                    Color.Transparent,
                                    Color.White),
                                kalendarEvents = seanceS.calendarEvents.toList(),
                                onCurrentDayClick = ::onDayClicked,
                                takeMeToDate = if(viewModelS.listSeance.isNotEmpty()) seanceS.calendarEvents.last().date else null,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                        items(items = viewModelS.listSeance) { seance ->
                            if (seanceS.selectedDateSeance >= seance.dateStart && seanceS.selectedDateSeance <= seance.dateEnd) {

                                if(authS.role == "admin"){
                                    ElementSeanceAdmin(seance = seance, viewModelS = viewModelS, navHostController = navHostController)
                                }else{

                                    val presentTime = LocalTime.now()

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                            .border(5.dp,
                                                MaterialTheme.colors.primary,
                                                RoundedCornerShape(5.dp))
                                            .clickable {
                                                Support.copyMovie = movieS
                                                Support.copySeance =
                                                    seance.copy(selectedDateSeance = seanceS.selectedDateSeance)
                                                navHostController.navigate(NavRoutes.Tickets.route)
                                            }
                                    ) {
                                        Column(Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.SpaceBetween) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()) {
                                                Text(seance.price.toString() + " ₽",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp)
                                                Text(text = seanceS.selectedDateSeance.toString().toMyDateFormat(seanceS.selectedDateSeance),
                                                    color = Color.Gray,
                                                    modifier = Modifier.padding(top = 5.dp))
                                            }
                                            Text(seance.addressCinema)

                                            Spacer(Modifier.height(10.dp))

                                            Text("${seance.timeStart} - ${seance.timeEnd.toString()}",
                                                fontSize = 16.sp)

                                            Text("Тип зала: ${seance.typeHall}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxHeight(0.5f)
                    .fillMaxWidth()
                    .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceEvenly) {

                    Row{
                        Image(
                            painter = painter,
                            contentDescription = "",
                            modifier = Modifier.height(250.dp),
                            Alignment.Center,
                            contentScale = ContentScale.Fit,
                        )
                        Column(modifier = Modifier.padding(10.dp)){

                            Text(text = movieS.nameMovie,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp)

                            Spacer(Modifier.height(25.dp))

                            Text("Жанр", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                            Text(text = movieS.genreList.toString()
                                .replace("[", "")
                                .replace("]", ""))

                            Spacer(Modifier.height(25.dp))

                            Text("Продюсер", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                            Text(text = movieS.director)

                        }
                    }
                    Spacer(Modifier.height(25.dp))

                    Text("Описание", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                    Text(text = movieS.description)

                    Spacer(Modifier.height(25.dp))

                    Text("Год производства", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                    Text(text = movieS.yearOfIssue)

                    Spacer(Modifier.height(25.dp))

                    Text("Возрастное ограничение", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                    Text(text = movieS.ageRating)

                    Spacer(Modifier.height(25.dp))

                    Text("Продолжительность", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                    Text(text = "${movieS.duration} мин. / ${"%.1f".format(movieS.duration.toDouble() / 60)} ч.")

                    Spacer(Modifier.height(25.dp))

                    if(movieIsDelete){
                        AlertDialog(
                            onDismissRequest = {
                                movieIsDelete = false
                            },
                            title = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                                    Text(text = "Удаление фильма")
                                }
                            },
                            text = {
                                Text(text = "Вы действительно хотите удалить фильм?")
                            },
                            buttons = {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Button(onClick = {
                                        viewModelM.onEventMovie(MovieUIEvent.DeleteMovie)
                                    }) {
                                        Text(text = "Да")
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(onClick = {
                                        movieIsDelete = false
                                    }) {
                                        Text(text = "Отмена")
                                    }
                                }
                            }
                        )
                    }

                    if(authS.role == "admin"){
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                            Column(horizontalAlignment = Alignment.CenterHorizontally){
                                Button(onClick = {
                                    viewModelS.onEvent(SeanceUIEvent.PlaceHolderChanged(true))
                                    viewModelS.onEvent(SeanceUIEvent.ShowSeance(movieS.idMovie))
                                }) {
                                    Text("Просмотр сеансов")
                                }
                                Button(onClick = {
                                    navHostController.navigate(route = NavRoutes.Job.route,
                                        NavOptions.Builder().setPopUpTo(NavRoutes.Movies.route, true).build())

                                    Support.copyMovieUpdate = movieS.copy(
                                        showDetailsMovie = false
                                    )
                                    Support.copyFilterUpdate = FilterMovieState()
                                    Support.copySeanceUpdate = SeanceState()
                                    Support.copyCinemaState = CinemaState()

                                }){
                                    Text("Редактировать")
                                }
                                Button(onClick = { movieIsDelete = true}) {
                                    Text("Удалить")
                                }
                            }
                        }
                    }else{
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                            Button(onClick = {
                                viewModelS.onEvent(SeanceUIEvent.PlaceHolderChanged(true))
                                viewModelS.onEvent(SeanceUIEvent.ShowSeance(movieS.idMovie))
                            }) {
                                Text("Выбрать сеанс")
                            }
                        }
                    }

                    Spacer(Modifier.height(25.dp))
                }
            }
            Box(
                contentAlignment = Alignment.BottomStart,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 5.dp)
            ){
                IconButton(onClick = {
                    viewModelM.onEventMovie(MovieUIEvent.ShowDetailsMovieChanged(false))
                }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "")
                }
            }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewElementListSeance() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {

            }
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween){
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()){
                Text("Четверг", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("08.05.2023", color = Color.Gray, modifier = Modifier.padding(top = 5.dp))
            }
            Text("ул. Пролетарская 16")
            Spacer(Modifier.height(10.dp))
            Text("15:20-16:30", fontSize = 16.sp)
            Text("Тип зала: classic")
        }
    }
}

@Composable
fun ElementSeanceAdmin(seance : SeanceState, viewModelS: SeanceViewModel, navHostController: NavHostController){

    var seanceIsDelete by rememberSaveable{ mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(5.dp,
                MaterialTheme.colors.primary,
                RoundedCornerShape(
                    topStart = 15.dp, topEnd = 15.dp, bottomStart = 60.dp, bottomEnd = 60.dp
                )),
        shape = RoundedCornerShape(
            topStart = 15.dp, topEnd = 15.dp, bottomStart = 60.dp, bottomEnd = 60.dp
        )
    ) {
        Column(Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Text(seance.price.toString() + " ₽",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp)
                Text(text = viewModelS.state.selectedDateSeance.toString().toMyDateFormat(viewModelS.state.selectedDateSeance),
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 5.dp))
            }
            Text(seance.addressCinema)

            Spacer(Modifier.height(10.dp))

            Text("${seance.timeStart} - ${seance.timeEnd.toString()}",
                fontSize = 16.sp)

            Text("Тип зала: ${seance.typeHall}")

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Button(onClick = {
                    navHostController.navigate(NavRoutes.Job.route, NavOptions.Builder().setPopUpTo(NavRoutes.Movies.route, true).build())

                    Support.copySeanceUpdate = seance
                    Support.copyMovieUpdate = MovieState()
                    Support.copyFilterUpdate = FilterMovieState()
                }) {
                    Text(text = "Редактировать")
                }
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Button(onClick = {
                    seanceIsDelete = true
                }) {
                    Text(text = "Удалить")
                }
            }
        }
    }
    if(seanceIsDelete){
        AlertDialog(
            onDismissRequest = {
                seanceIsDelete = false
            },
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    Text(text = "Удаление сеанса")
                }
            },
            text = {
                Text(text = "Вы действительно хотите удалить сеанс?")
            },
            buttons = {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Button(onClick = {
                        viewModelS.onEvent(SeanceUIEvent.DeleteSeance(seance.id))
                    }) {
                        Text(text = "Да")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        seanceIsDelete = false
                    }) {
                        Text(text = "Отмена")
                    }
                }
            }
        )
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewElementListMovie() {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data("http://i.pinimg.com/736x/b9/6e/7b/b96e7b60a5c1094154ee49047ee3db5e.jpg")
            .size(coil.size.Size.ORIGINAL)
            .crossfade(true)
            .build()
    )
    Box {
        Row {
            Row(modifier = Modifier.padding(10.dp)) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        CircularProgressIndicator()
                    }
                    is AsyncImagePainter.State.Error -> {
                        Icon(Icons.Filled.ErrorOutline, "")
                    }
                    else -> {
                        Image(
                            painter = painter,
                            contentDescription = "",
                            modifier = Modifier.height(200.dp),
                            Alignment.CenterStart,
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            Row(modifier = Modifier.padding(10.dp)) {
                Column {
                    Text(text = "четкий фильм")

                    Text(text = "ну что-то там")

                    Text(text = "а ну неплохо")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterTab(state : FilterMovieState, viewModel: MovieViewModel){

    val listAgeRating = listOf(0, 6, 12, 16, 18)
    val listYear = (1900.. LocalDateTime.now().year).reversed().toList()

    Column{
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {

            ExposedDropdownMenuBox(
                expanded = state.expandedListGenreTextField,
                onExpandedChange = {
                    viewModel.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldGenresChanged(!state.expandedListGenreTextField))
                }
            ) {
                RoundedTextField(
                    label = "Жанры",
                    value = state.selectedGenreList.filter { it.isChoice }.map { it.nameGenre }
                        .joinToString(), placeholder = "Выберите жанр",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = state.expandedListGenreTextField
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = state.expandedListGenreTextField,
                    onDismissRequest = {
                        viewModel.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldGenresChanged(
                            false))
                    }) {
                    if (state.isLoading) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        state.selectedGenreList.forEach { genre ->
                            DropdownMenuItem(onClick = {
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = genre.nameGenre)

                                    Checkbox(checked = genre.isChoice,
                                        onCheckedChange = {
                                            viewModel.onEventFilterMovie(MovieFilterUIEvent.GenreChanged(
                                                state.selectedGenreList.indexOf(genre), it
                                            ))
                                        },
                                        colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (state.selectedGenreList.filter { it.isChoice }.isNotEmpty()) {
                IconButton(onClick = { viewModel.onEventFilterMovie(MovieFilterUIEvent.ClearSelectedGenres) }) {
                    Icon(Icons.Filled.Close, "")
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            ExposedDropdownMenuBox(
                expanded = state.expandedListAgeTextField,
                onExpandedChange = {
                    viewModel.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldAgeChanged(!state.expandedListAgeTextField))
                }
            ) {
                RoundedTextField(
                    label = "Возратсной рейтинг",
                    value = if (state.selectedAgeRating == -1) "" else state.selectedAgeRating.toString() + "+",
                    placeholder = "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = state.expandedListAgeTextField
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = state.expandedListAgeTextField, onDismissRequest =
                    {
                        viewModel.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldAgeChanged(
                            false))
                    }
                ) {
                    listAgeRating.forEach { age ->
                        DropdownMenuItem(onClick = {
                            viewModel.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(
                                false))
                            viewModel.onEventFilterMovie(MovieFilterUIEvent.AgeRatingChanged(age))
                        }
                        ) {
                            Text(text = "$age+")
                        }
                    }
                }
            }
            if (state.selectedAgeRating != -1) {
                IconButton(onClick = {
                    viewModel.onEventFilterMovie(MovieFilterUIEvent.AgeRatingChanged(-1))
                }) {
                    Icon(Icons.Filled.Close, "")
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            ExposedDropdownMenuBox(
                expanded = state.expandedListYearTextField,
                onExpandedChange = {
                    viewModel.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(!state.expandedListYearTextField))
                }
            ) {
                RoundedTextField(
                    label = "Год выпуска",
                    value = if (state.selectedYearIssue == 0) "" else state.selectedYearIssue.toString(),
                    placeholder = "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = state.expandedListYearTextField
                        )
                    }
                )

                ExposedDropdownMenu(
                    expanded = state.expandedListYearTextField, onDismissRequest =
                    {
                        viewModel.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(
                            false))
                    }
                ) {
                    listYear.forEach { year ->
                        DropdownMenuItem(onClick = {
                            viewModel.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(
                                false))
                            viewModel.onEventFilterMovie(MovieFilterUIEvent.YearOfIssueChanged(year))
                        }) {
                            Text(text = year.toString())
                        }
                    }
                }
            }

            if (state.selectedYearIssue != 0) {
                IconButton(onClick = {
                    viewModel.onEventFilterMovie(MovieFilterUIEvent.YearOfIssueChanged(0))
                }) {
                    Icon(Icons.Filled.Close, "")
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                viewModel.onEventFilterMovie(MovieFilterUIEvent.ApplyFilter(true))
                viewModel.onEventFilterMovie(MovieFilterUIEvent.FilterIsChanged(false)) },
                enabled = state.selectedYearIssue > 0 || state.selectedAgeRating > -1 || state.selectedGenreList.isNotEmpty()
            ) {
                Text(text = "Применить")
            }
            Button(onClick = {
                viewModel.onEventFilterMovie(MovieFilterUIEvent.FilterIsChanged(false))
                viewModel.onEventFilterMovie(MovieFilterUIEvent.ApplyFilter(false))
            }) {
                Text(text = "Закрыть")
            }
        }
    }
}

@Composable
fun SearchView(state: TextFieldValue, viewModel: MovieViewModel) {
    TextField(
        value = state,
        onValueChange = { value ->
            viewModel.onEventFilterMovie(MovieFilterUIEvent.SearchTextChanged(value))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 5.dp)
            .border(border = BorderStroke(3.dp, color = MaterialTheme.colors.primary),
                shape = RoundedCornerShape(15.dp)),

        textStyle = TextStyle(fontSize = 18.sp),
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "",
                modifier = Modifier
                    .padding(15.dp)
                    .size(24.dp)
            )
        },
        trailingIcon = {
            if (state != TextFieldValue("")) {
                IconButton(
                    onClick = {
                        viewModel.onEventFilterMovie(MovieFilterUIEvent.SearchTextChanged(TextFieldValue("")))
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(15.dp)
                            .size(24.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RectangleShape, // The TextFiled has rounded corners top left and right by default
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}


