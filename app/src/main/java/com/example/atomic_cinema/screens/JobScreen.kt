package com.example.atomic_cinema.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.atomic_cinema.R
import com.example.atomic_cinema.events.*
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.server.movie.MovieResult
import com.example.atomic_cinema.server.seance.SeanceResult
import com.example.atomic_cinema.server.ticket.TicketResults
import com.example.atomic_cinema.stateClasses.FilterMovieState
import com.example.atomic_cinema.stateClasses.MovieState
import com.example.atomic_cinema.stateClasses.SeanceState
import com.example.atomic_cinema.utils.*
import com.example.atomic_cinema.viewModel.*
import java.time.LocalDateTime

@Composable
fun JobScreen(
    navHostController: NavHostController,
    viewModelM: MovieViewModel = hiltViewModel(),
    viewModelS: SeanceViewModel = hiltViewModel(),
    viewModelA : AuthViewModel = hiltViewModel()
){

    val context = LocalContext.current
    val stateAuth = viewModelA.state

    LaunchedEffect(viewModelA, context) {
        viewModelA.authResults.collect { result ->
            when (result) {
                is AuthResult.Authorized -> {

                }
                is AuthResult.Unauthorized -> {
                    Toast.makeText(
                        context, "Авторизируйтесь в системе.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navHostController.navigate(NavRoutes.Authorization.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                else -> {

                }
            }
        }
    }

    LaunchedEffect(viewModelS, context) {
        viewModelS.seanceResults.collect { result ->
            when (result) {
                is SeanceResult.ConflictSeances -> {
                    Toast.makeText(
                        context, "Сеанс конфликтует с другими сеансами.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is SeanceResult.NotCorrectDateOrTime -> {
                    Toast.makeText(
                        context, "Ошибка. Проверьте корректность выбранной даты и времени",
                        Toast.LENGTH_LONG
                    ).show()
                }
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
                        context, "Неизвестная ошибка.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is SeanceResult.SeanceIsAdded -> {
                    Toast.makeText(
                        context, "Сеанс добавлен",
                        Toast.LENGTH_SHORT
                    ).show()
                    navHostController.navigate(NavRoutes.Job.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is SeanceResult.SeanceIsUpdated -> {
                    Toast.makeText(
                        context, "Сеанс обновлён",
                        Toast.LENGTH_SHORT
                    ).show()

                    Support.copySeanceUpdate = SeanceState()

                    navHostController.navigate(NavRoutes.Job.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is SeanceResult.NotFoundSeances -> {
                    Toast.makeText(
                        context, "Сейчас сеансов нет",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(viewModelM, context) {
        viewModelM.movieResults.collect { result ->
            when (result) {
                is MovieResult.Unauthorized -> {
                    Toast.makeText(
                        context, "Авторизируйтесь в системе.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navHostController.navigate(NavRoutes.Authorization.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is MovieResult.UnknownError -> {
                    Toast.makeText(
                        context, "Произошла неизвестная ошибка. Операция не удалась.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is MovieResult.MovieIsAdded -> {
                    Toast.makeText(
                        context, "Фильм добавлен.",
                        Toast.LENGTH_SHORT
                    ).show()

                    viewModelM.onEventMovie(MovieUIEvent.ShowDetailsMovieChanged(false))
                }
                is MovieResult.MovieIsUpdated -> {
                    Toast.makeText(
                        context, "Фильм обновлён.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navHostController.navigate(NavRoutes.Movies.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        if(stateAuth.isLoading){
            LoadingScreen()
        }else{
            if(viewModelM.stateMovie.showDetailsMovie) {
                AddMovie(
                    movieS = viewModelM.stateMovie,
                    viewModelM = viewModelM
                )
            }
            if (
                viewModelM.copyMovieUpdate.idMovie != 0
                && Support.copySeanceUpdate.id == 0
                && !viewModelM.stateMovie.showDetailsMovie
                && !viewModelS.state.showDetailsSeance
            ) {

                LaunchedEffect(viewModelM.copyMovieUpdate){
                    viewModelM.onEventMovie(MovieUIEvent.MovieChanged(viewModelM.copyMovieUpdate))
                    viewModelM.onEventFilterMovie(MovieFilterUIEvent.YearOfIssueChanged(viewModelM.copyMovieUpdate.yearOfIssue.toInt()))
                    viewModelM.onEventFilterMovie(MovieFilterUIEvent.AgeRatingChanged(viewModelM.copyMovieUpdate.ageRating.replace(
                        "+",
                        "").toInt()))

                    viewModelM.stateFilterMovie.selectedGenreList.forEach { genre ->
                        viewModelM.copyMovieUpdate.genreList.forEach {
                            if (genre.nameGenre == it) {
                                viewModelM.onEventFilterMovie(MovieFilterUIEvent.GenreChanged(
                                    viewModelM.stateFilterMovie.selectedGenreList.indexOf(genre),
                                    true))
                            }
                        }
                    }

                    Support.copyFilterUpdate = viewModelM.stateFilterMovie
                }
                UpdateMovie(viewModelM = viewModelM)
            }

            if (viewModelS.state.showDetailsSeance){
                AddSeance(viewModelM, viewModelS)
            }

            if (
                viewModelS.copySeanceUpdate.id != 0
                && Support.copyCinemaState.id == 0
                && !viewModelM.stateMovie.showDetailsMovie
                && !viewModelS.state.showDetailsSeance
                && Support.copyMovieUpdate.idMovie == 0
                && Support.copyFilterUpdate.selectedAgeRating == -1
            ) {
                val viewModelC: CinemaViewModel = hiltViewModel()
                LaunchedEffect(viewModelS.copySeanceUpdate){
                    viewModelS.onEvent(SeanceUIEvent.SeanceChanged(viewModelS.copySeanceUpdate))
                    viewModelM.onEventMovie(MovieUIEvent.MovieChanged(viewModelM.listMovie.find { it.idMovie == viewModelS.copySeanceUpdate.idMovie }
                        ?: MovieState()))
                    viewModelC.onEvent(CinemaUIEvent.AddressCinemaChanged(viewModelS.copySeanceUpdate.addressCinema))
                }
                UpdateSeance(viewModelM = viewModelM, viewModelS = viewModelS, viewModelC = viewModelC)
            }


            if(
                Support.copyCinemaState.id != 0
                && !viewModelM.stateMovie.showDetailsMovie
                && !viewModelS.state.showDetailsSeance
                && Support.copyMovieUpdate.idMovie == 0
                && Support.copyFilterUpdate.selectedAgeRating == -1
                && Support.copySeanceUpdate.id == 0
            ){
                ViewSeancesCinema(viewModelS = viewModelS, viewModelM = viewModelM)
            }

            if(
                !viewModelM.stateMovie.showDetailsMovie
                && !viewModelS.state.showDetailsSeance
                && Support.copyMovieUpdate.idMovie == 0
                && Support.copyCinemaState.id == 0
                && Support.copyFilterUpdate.selectedAgeRating == -1
                && Support.copySeanceUpdate.id == 0
            ) {
                MenuAdminJob(viewModelM, viewModelS)
            }
        }
    }
}

@Composable
fun MenuAdminJob(
    viewModelM: MovieViewModel,
    viewModelS: SeanceViewModel
){
    Card(
        elevation = 5.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable {
                viewModelM.onEventMovie(MovieUIEvent.ShowDetailsMovieChanged(true))
            }
    ) {
        Column(
            modifier = Modifier
                .heightIn(150.dp)
                .widthIn(min = 150.dp, max = 200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Icon(
                Icons.Filled.VideoFile,
                "",
                modifier = Modifier.height(50.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(text = "Добавить фильм")
        }
    }

    Spacer(modifier = Modifier.height(30.dp))

    Card(
        elevation = 5.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable {
                viewModelS.onEvent(SeanceUIEvent.ShowDetailSeance(true))
            },
    ) {
        Column(
            modifier = Modifier
                .heightIn(150.dp)
                .widthIn(min = 150.dp, max = 200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Icon(
                Icons.Filled.Add,
                "",
                modifier = Modifier.height(50.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(text = "Добавить сеанс")
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UpdateMovie(
    viewModelM : MovieViewModel,
    movieS: MovieState = viewModelM.stateMovie,
    filterS : FilterMovieState = viewModelM.stateFilterMovie
){
    val listAgeRating = listOf(0, 6, 12, 16, 18)
    val listYear = (1900.. LocalDateTime.now().year).reversed().toList()
    val maxCharDescription = 1000
    val maxCharNameMovie = 50
    val maxCharDirector = 50
    val maxCharLinkImage = 100
    var buttonIsEnabled by remember { mutableStateOf(false) }
    var alertDialogIsVisible by rememberSaveable{ mutableStateOf(false) }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(movieS.linkImage.replace("https", "http"))
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )

    LaunchedEffect(movieS, filterS){
        buttonIsEnabled = movieS.nameMovie.isNotBlank() &&
                movieS.duration >= 10 &&
                movieS.director.isNotBlank() &&
                movieS.description.isNotBlank() &&
                filterS.selectedAgeRating != -1 &&
                filterS.selectedYearIssue != 0 &&
                filterS.selectedGenreList.filter { it.isChoice }.isNotEmpty() &&
                movieS.linkImage.isNotEmpty()
    }

    if(alertDialogIsVisible){
        AlertDialog(
            onDismissRequest = {alertDialogIsVisible = false},
            title = { Text(text = "Добавление постера фильма")},
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    Column{
                        RoundedTextField(
                            label = "Вставьте сслыку изображения",
                            value = movieS.linkImage,
                            placeholder = "" ,
                            onValueChange = {
                                if(it.length <= maxCharLinkImage){
                                    viewModelM.onEventMovie(MovieUIEvent.LinkImageChanged(it))
                                }
                            })
                        Text(text = "Длина ссылки не должна превышать 100 символов", color = Color.Gray)
                    }

                }
            },
            buttons = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    Button(onClick = {
                        alertDialogIsVisible = false
                    }) {
                        Text(text = "Подтвердить")
                    }
                }
            }
        )
    }


    Column(modifier = Modifier
        .verticalScroll(rememberScrollState())
        .fillMaxHeight(0.5f)
        .fillMaxWidth()
        .padding(10.dp),
        verticalArrangement = Arrangement.SpaceEvenly) {


        Text("Постер фильма", color = Color.Gray, textAlign = TextAlign.Center)

        Spacer(Modifier.height(25.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clickable {
                    alertDialogIsVisible = true
                },
            contentAlignment = Alignment.Center){

            Image(
                painter = if(movieS.linkImage.isNotEmpty()){
                    when(painter.state){
                        AsyncImagePainter.State.Empty -> {
                            painterResource(id = R.drawable.ic_baseline_add_24)
                        }
                        is AsyncImagePainter.State.Loading -> {
                            painter
                        }
                        is AsyncImagePainter.State.Success -> {
                            painter
                        }
                        is AsyncImagePainter.State.Error -> {
                            painterResource(id = R.drawable.ic_baseline_add_24)
                        }
                    }
                }
                else
                    painterResource(id = R.drawable.ic_baseline_add_24),
                contentDescription = "",
                modifier = Modifier
                    .height(250.dp)
                    .widthIn(min = 50.dp),
                Alignment.Center,
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(Modifier.height(25.dp))

        if(movieS.linkImage.isNotEmpty()) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    Text(text = "Изображение загружается...")
                }
                is AsyncImagePainter.State.Success -> {
                    Text(text = "Изображение загружено!")
                }
                is AsyncImagePainter.State.Error -> {
                    Text(text = "Ошибка. Не удалось загрузить изображение попробуйте вставить другую ссылку.")
                }
                else -> {

                }
            }
        }


        Spacer(Modifier.height(25.dp))

        Text("Название", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        RoundedTextField(
            label = "Название фильма",
            value = movieS.nameMovie,
            notNull = true,
            placeholder = "Введите название фильма",
            onValueChange = {
                if(it.length <= maxCharNameMovie){
                    viewModelM.onEventMovie(MovieUIEvent.NameMovieChanged(it))
                }
            })

        Spacer(Modifier.height(25.dp))

        Text("Жанр", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        ExposedDropdownMenuBox(
            expanded = filterS.expandedListGenreTextField,
            onExpandedChange = {
                viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldGenresChanged(!filterS.expandedListGenreTextField))
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                RoundedTextField(
                    label = "Жанры",
                    value = filterS.selectedGenreList.filter { it.isChoice }.map { it.nameGenre }.joinToString(), placeholder = "Выберите жанр",
                    onValueChange ={},
                    notNull = true,
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = filterS.expandedListGenreTextField
                        )
                    }
                )
                if(filterS.selectedGenreList.filter { it.isChoice }.isNotEmpty()){
                    IconButton(onClick = {viewModelM.onEventFilterMovie(MovieFilterUIEvent.ClearSelectedGenres)}) {
                        Icon(Icons.Filled.Close, "")
                    }
                }
            }
            ExposedDropdownMenu(
                expanded = filterS.expandedListGenreTextField,
                onDismissRequest = {
                    viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldGenresChanged(false))
                }) {
//                if(filterS.isLoading){
//                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
//                        CircularProgressIndicator()
//                    }
//                }else{
                filterS.selectedGenreList.forEach { genre ->
                    DropdownMenuItem(onClick = {
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = genre.nameGenre)

                            Checkbox(checked = genre.isChoice,
                                onCheckedChange = {
                                    viewModelM.onEventFilterMovie(MovieFilterUIEvent.GenreChanged(
                                        filterS.selectedGenreList.indexOf(genre), it
                                    ))
                                },
                                colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                            )
                        }
                    }
                }
            }
//            }


        }
        Spacer(Modifier.height(25.dp))

        Text("Продюсер", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        RoundedTextField(
            label = "Имя продюсера",
            value = movieS.director,
            placeholder = "Введите имя продюсера",
            notNull = true,
            onValueChange = {
                if(it.length <= maxCharDirector){
                    viewModelM.onEventMovie(MovieUIEvent.DirectorChanged(it))
                }
            })

        Spacer(Modifier.height(25.dp))

        Text("Описание", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        RoundedTextField(
            label = "Описание фильма",
            value = movieS.description,
            singleLine = false,
            placeholder = "Введите описание фильма",
            notNull = true,
            onValueChange = {
                if(it.length <= maxCharDescription){
                    viewModelM.onEventMovie(MovieUIEvent.DescriptionChanged(it))
                }
            })

        Spacer(Modifier.height(25.dp))

        Text("Год производства", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            ExposedDropdownMenuBox(
                expanded = filterS.expandedListYearTextField,
                onExpandedChange = {
                    viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(!filterS.expandedListYearTextField))
                }
            ) {
                RoundedTextField(
                    label = "Год выпуска",
                    value = if(filterS.selectedYearIssue == 0) "" else filterS.selectedYearIssue.toString(),
                    placeholder = "",
                    onValueChange = {},
                    notNull = true,
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = filterS.expandedListYearTextField
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = filterS.expandedListYearTextField, onDismissRequest =
                    {
                        viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(false))
                    }
                ) {
                    listYear.forEach { year ->
                        DropdownMenuItem(onClick = {
                            viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(false))
                            viewModelM.onEventFilterMovie(MovieFilterUIEvent.YearOfIssueChanged(year))
                        }) {
                            Text(text = year.toString())
                        }
                    }
                }
            }
            if(filterS.selectedYearIssue != 0){
                IconButton(
                    onClick = {
                        viewModelM.onEventFilterMovie(MovieFilterUIEvent.YearOfIssueChanged(0))
                    }

                ) {
                    Icon(Icons.Filled.Close, "")
                }
            }
        }


        Spacer(Modifier.height(25.dp))

        Text("Возрастное ограничение",
            color = Color.Gray,
            modifier = Modifier.padding(start = 5.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            ExposedDropdownMenuBox(
                expanded = filterS.expandedListAgeTextField,
                onExpandedChange = {viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldAgeChanged(!filterS.expandedListAgeTextField))}
            ) {
                RoundedTextField(
                    label = "Возратсной рейтинг",
                    value = if(filterS.selectedAgeRating == -1) "" else "+" + filterS.selectedAgeRating,
                    placeholder = "",
                    onValueChange = {},
                    notNull = true,
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = filterS.expandedListAgeTextField
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = filterS.expandedListAgeTextField, onDismissRequest =
                    {
                        viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldAgeChanged(false))
                    }
                ) {
                    listAgeRating.forEach { age ->
                        DropdownMenuItem(onClick = {
                            viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldAgeChanged(false))
                            viewModelM.onEventFilterMovie(MovieFilterUIEvent.AgeRatingChanged(age))}
                        ) {
                            Text(text = "+$age")
                        }
                    }
                }
            }
            if(filterS.selectedAgeRating != -1){
                IconButton(onClick = {viewModelM.onEventFilterMovie(MovieFilterUIEvent.AgeRatingChanged(-1))}) {
                    Icon(Icons.Filled.Close, "")
                }
            }
        }

        Spacer(Modifier.height(25.dp))

        Text("Продолжительность", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        RoundedTextField(
            label = "Продолжительность фильма",
            value = if(movieS.duration == 0) "" else movieS.duration.toString(),
            placeholder = "Введите продолжительность фильма в минутах",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            notNull = true,
            onValueChange = {
                if(movieS.duration.toString().isDigitsOnly() && it.length <= 3 && movieS.duration.toString().isNotBlank()){
                    if(it.isNotBlank() || movieS.duration.toString().length != 1){
                        viewModelM.onEventMovie(MovieUIEvent.DurationChanged(it.toInt()))
                    }
                }
            })

        if(movieS.duration < 10 && movieS.duration != 0){
            Text(text = "Продолжительность фильма должно быть больше 10 минут")
        }

        Spacer(Modifier.height(25.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
            Button(onClick = {
                viewModelM.onEventMovie(MovieUIEvent.UpdateMovie)
            },
                enabled = movieS != Support.copyMovieUpdate && buttonIsEnabled || viewModelM.stateFilterMovie != Support.copyFilterUpdate && buttonIsEnabled) {
                Text(text = "Редактировать")
            }
        }

        if(!buttonIsEnabled){
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Text(text = "Все поля должны быть корректно заполнены", color = Color.Gray)
            }

        }

        Spacer(Modifier.height(50.dp))
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddMovie(
    movieS : MovieState,
    viewModelM : MovieViewModel,
    filterS : FilterMovieState = viewModelM.stateFilterMovie
){

    val listAgeRating = listOf(0, 6, 12, 16, 18)
    val listYear = (1900.. LocalDateTime.now().year).reversed().toList()
    val maxCharDescription = 1000
    val maxCharNameMovie = 50
    val maxCharDirector = 50
    val maxCharLinkImage = 100
    var buttonIsEnabled by remember { mutableStateOf(false) }
    var alertDialogIsVisible by rememberSaveable{ mutableStateOf(false) }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(movieS.linkImage.replace("https", "http"))
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )

    LaunchedEffect(movieS, filterS){
        buttonIsEnabled = movieS.nameMovie.isNotBlank() &&
                movieS.duration >= 10 &&
                movieS.director.isNotBlank() &&
                movieS.description.isNotBlank() &&
                filterS.selectedAgeRating != -1 &&
                filterS.selectedYearIssue != 0 &&
                filterS.selectedGenreList.filter { it.isChoice }.isNotEmpty() &&
                movieS.linkImage.isNotEmpty()
    }

    if(alertDialogIsVisible){
        AlertDialog(
            onDismissRequest = {alertDialogIsVisible = false},
            title = { Text(text = "Добавление постера фильма")},
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    Column{
                        RoundedTextField(
                            label = "Вставьте сслыку изображения",
                            value = movieS.linkImage,
                            placeholder = "" ,
                            onValueChange = {
                                if(it.length <= maxCharLinkImage){
                                    viewModelM.onEventMovie(MovieUIEvent.LinkImageChanged(it))
                                }
                            })
                        Text(text = "Длина ссылки не должна превышать 100 символов", color = Color.Gray)
                    }

                }
            },
            buttons = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    Button(onClick = {
                        alertDialogIsVisible = false
                    }) {
                        Text(text = "Подтвердить")
                    }
                }
            }
        )
    }


    Column(modifier = Modifier
        .verticalScroll(rememberScrollState())
        .fillMaxHeight(0.5f)
        .fillMaxWidth()
        .padding(10.dp),
        verticalArrangement = Arrangement.SpaceEvenly) {


        Text("Постер фильма", color = Color.Gray, textAlign = TextAlign.Center)

        Spacer(Modifier.height(25.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clickable {
                    alertDialogIsVisible = true
                },
            contentAlignment = Alignment.Center){

            Image(
                painter = if(movieS.linkImage.isNotEmpty()){
                    when(painter.state){
                        AsyncImagePainter.State.Empty -> {
                            painterResource(id = R.drawable.ic_baseline_add_24)
                        }
                        is AsyncImagePainter.State.Loading -> {
                            painter
                        }
                        is AsyncImagePainter.State.Success -> {
                            painter
                        }
                        is AsyncImagePainter.State.Error -> {
                            painterResource(id = R.drawable.ic_baseline_add_24)
                        }
                    }
                }
                else
                    painterResource(id = R.drawable.ic_baseline_add_24),
                contentDescription = "",
                modifier = Modifier
                    .height(250.dp)
                    .widthIn(min = 50.dp),
                Alignment.Center,
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(Modifier.height(25.dp))

        if(movieS.linkImage.isNotEmpty()) {
            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    Text(text = "Изображение загружается...")
                }
                is AsyncImagePainter.State.Success -> {
                    Text(text = "Изображение загружено!")
                }
                is AsyncImagePainter.State.Error -> {
                    Text(text = "Ошибка. Не удалось загрузить изображение попробуйте вставить другую ссылку.")
                }
                else -> {

                }
            }
        }


        Spacer(Modifier.height(25.dp))

        Text("Название", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        RoundedTextField(
            label = "Название фильма",
            value = movieS.nameMovie,
            notNull = true,
            placeholder = "Введите название фильма",
            onValueChange = {
                if(it.length <= maxCharNameMovie){
                    viewModelM.onEventMovie(MovieUIEvent.NameMovieChanged(it))
                }
        })

        Spacer(Modifier.height(25.dp))

        Text("Жанр", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        ExposedDropdownMenuBox(
            expanded = filterS.expandedListGenreTextField,
            onExpandedChange = {
                viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldGenresChanged(!filterS.expandedListGenreTextField))
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                RoundedTextField(
                    label = "Жанры",
                    value = filterS.selectedGenreList.filter { it.isChoice }.map { it.nameGenre }.joinToString(), placeholder = "Выберите жанр",
                    onValueChange ={},
                    notNull = true,
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = filterS.expandedListGenreTextField
                        )
                    }
                )
                if(filterS.selectedGenreList.filter { it.isChoice }.isNotEmpty()){
                    IconButton(onClick = {viewModelM.onEventFilterMovie(MovieFilterUIEvent.ClearSelectedGenres)}) {
                        Icon(Icons.Filled.Close, "")
                    }
                }
            }
            ExposedDropdownMenu(
                expanded = filterS.expandedListGenreTextField,
                onDismissRequest = {
                    viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldGenresChanged(false))
                }) {
                    filterS.selectedGenreList.forEach { genre ->
                        DropdownMenuItem(onClick = {
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = genre.nameGenre)

                                Checkbox(checked = genre.isChoice,
                                    onCheckedChange = {
                                        viewModelM.onEventFilterMovie(MovieFilterUIEvent.GenreChanged(
                                            filterS.selectedGenreList.indexOf(genre), it
                                        ))
                                    },
                                    colors = CheckboxDefaults.colors(MaterialTheme.colors.primary)
                                )
                            }
                        }
                    }
                }

        }
        Spacer(Modifier.height(25.dp))

        Text("Продюсер", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        RoundedTextField(
            label = "Имя продюсера",
            value = movieS.director,
            placeholder = "Введите имя продюсера",
            notNull = true,
            onValueChange = {
                if(it.length <= maxCharDirector){
                    viewModelM.onEventMovie(MovieUIEvent.DirectorChanged(it))
                }
            })

        Spacer(Modifier.height(25.dp))

        Text("Описание", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        RoundedTextField(
            label = "Описание фильма",
            value = movieS.description,
            singleLine = false,
            placeholder = "Введите описание фильма",
            notNull = true,
            onValueChange = {
                if(it.length <= maxCharDescription){
                    viewModelM.onEventMovie(MovieUIEvent.DescriptionChanged(it))
                }
            })

        Spacer(Modifier.height(25.dp))

        Text("Год производства", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            ExposedDropdownMenuBox(
                expanded = filterS.expandedListYearTextField,
                onExpandedChange = {
                    viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(!filterS.expandedListYearTextField))
                }
            ) {
                RoundedTextField(
                    label = "Год выпуска",
                    value = if(filterS.selectedYearIssue == 0) "" else filterS.selectedYearIssue.toString(),
                    placeholder = "",
                    onValueChange = {},
                    notNull = true,
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = filterS.expandedListYearTextField
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = filterS.expandedListYearTextField, onDismissRequest =
                    {
                        viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(false))
                    }
                ) {
                    listYear.forEach { year ->
                        DropdownMenuItem(onClick = {
                            viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldYearChanged(false))
                            viewModelM.onEventFilterMovie(MovieFilterUIEvent.YearOfIssueChanged(year))
                        }) {
                            Text(text = year.toString())
                        }
                    }
                }
            }
            if(filterS.selectedYearIssue != 0){
                IconButton(
                    onClick = {
                        viewModelM.onEventFilterMovie(MovieFilterUIEvent.YearOfIssueChanged(0))
                    }
                ) {
                    Icon(Icons.Filled.Close, "")
                }
            }
        }


        Spacer(Modifier.height(25.dp))

        Text("Возрастное ограничение",
            color = Color.Gray,
            modifier = Modifier.padding(start = 5.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            ExposedDropdownMenuBox(
                expanded = filterS.expandedListAgeTextField,
                onExpandedChange = {viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldAgeChanged(!filterS.expandedListAgeTextField))}
            ) {
                RoundedTextField(
                    label = "Возратсной рейтинг",
                    value = if(filterS.selectedAgeRating == -1) "" else "+" + filterS.selectedAgeRating,
                    placeholder = "",
                    onValueChange = {},
                    notNull = true,
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = filterS.expandedListAgeTextField
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = filterS.expandedListAgeTextField, onDismissRequest =
                    {
                        viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldAgeChanged(false))
                    }
                ) {
                    listAgeRating.forEach { age ->
                        DropdownMenuItem(onClick = {
                            viewModelM.onEventFilterMovie(MovieFilterUIEvent.ExpandedTextFieldAgeChanged(false))
                            viewModelM.onEventFilterMovie(MovieFilterUIEvent.AgeRatingChanged(age))}
                        ) {
                            Text(text = "+$age")
                        }
                    }
                }
            }
            if(filterS.selectedAgeRating != -1){
                IconButton(onClick = {viewModelM.onEventFilterMovie(MovieFilterUIEvent.AgeRatingChanged(-1))}) {
                    Icon(Icons.Filled.Close, "")
                }
            }
        }

        Spacer(Modifier.height(25.dp))

        Text("Продолжительность", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

        RoundedTextField(
            label = "Продолжительность фильма",
            value = if(movieS.duration == 0) "" else movieS.duration.toString(),
            placeholder = "Введите продолжительность фильма в минутах",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            notNull = true,
            onValueChange = {
                if(movieS.duration.toString().isDigitsOnly() && it.length <= 3 && movieS.duration.toString().isNotBlank()){
                    if(it.isNotBlank() || movieS.duration.toString().length != 1){
                        viewModelM.onEventMovie(MovieUIEvent.DurationChanged(it.toInt()))
                    }
                }
            })

        if(movieS.duration < 10 && movieS.duration != 0){
            Text(text = "Продолжительность фильма должно быть больше 10 минут")
        }

        Spacer(Modifier.height(25.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
            Button(onClick = {
                viewModelM.onEventMovie(MovieUIEvent.AddMovie)
            },
            enabled = buttonIsEnabled) {
                Text(text = "Добавить")
            }
        }

        if(!buttonIsEnabled){
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Text(text = "Все поля должны быть корректно заполнены", color = Color.Gray)
            }
        }

        Spacer(Modifier.height(50.dp))
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddSeance(
    viewModelM: MovieViewModel,
    viewModelS: SeanceViewModel,
    viewModelC: CinemaViewModel = hiltViewModel()
){

    val context = LocalContext.current
    val movieS = viewModelM.stateMovie
    val seanceS = viewModelS.state
    val cinemaS = viewModelC.state
    var datePickerIsVisible by rememberSaveable { mutableStateOf(false) }
    var priceSeance by rememberSaveable { mutableStateOf("") }
    var pickIsDateStart by rememberSaveable { mutableStateOf(false) }
    var timePickerIsVisible by rememberSaveable { mutableStateOf(false) }
    var pickIsTimeStart by rememberSaveable { mutableStateOf(false) }
 //   var isChoiceFilm by rememberSaveable { mutableStateOf(false) }
    var expandedListCinemas by rememberSaveable{ mutableStateOf(false) }
    var expandedListHalls by rememberSaveable{ mutableStateOf(false) }
    var buttonIsEnabled by remember { mutableStateOf(false) }
    val visibleStateListMovie = remember{
        MutableTransitionState(initialState = false).apply {
            targetState = false
        }
    }
        AnimatedVisibility(
            visibleState = visibleStateListMovie,
            enter = expandHorizontally(),
            exit = shrinkVertically()
        ) {
            ListMovie(viewModelM = viewModelM, viewModelS = viewModelS, mutableTransitionState = visibleStateListMovie)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                RoundedTextField(
                    label = "Дата начала",
                    value = if(seanceS.dateStart == null) "" else seanceS.dateStart.toString().toMyDateFormat(seanceS.dateStart),
                    placeholder = "Дата начала",
                    onValueChange = {},
                    modifier = Modifier.widthIn(150.dp, 200.dp),
                    readOnly = true,
                    notNull = true)

                Spacer(modifier = Modifier.width(5.dp))

                Text(text = "-", fontSize = 20.sp)

                Spacer(modifier = Modifier.width(5.dp))

                RoundedTextField(
                    label = "Дата конца",
                    value = if(seanceS.dateEnd == null) "" else seanceS.dateEnd.toString().toMyDateFormat(seanceS.dateEnd),
                    placeholder = "Дата конца",
                    modifier = Modifier.widthIn(150.dp, 200.dp),
                    onValueChange = {},
                    readOnly = true,
                    notNull = true)

            }

            Spacer(Modifier.height(25.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Button(onClick = {
                    pickIsDateStart = true
                    datePickerIsVisible = true
                }, modifier = Modifier.widthIn(max = 150.dp)) {
                    Text(
                        text = "Выбрать дату начала",
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(30.dp))

                Button(onClick = {
                    datePickerIsVisible = true
                }, modifier = Modifier.widthIn(max = 150.dp)
                ) {
                    Text(
                        text = "Выбрать дату конца",
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                RoundedTextField(
                    label = "Время начала",
                    value = if(seanceS.timeStart == null) "" else seanceS.timeStart.toString(),
                    placeholder = "Время начала",
                    onValueChange = {},
                    modifier = Modifier.widthIn(150.dp, 200.dp),
                    readOnly = true,
                    notNull = true)

                Spacer(modifier = Modifier.width(5.dp))

                Text(text = "-", fontSize = 20.sp)

                Spacer(modifier = Modifier.width(5.dp))

                RoundedTextField(
                    label = "Время конца",
                    value = if(seanceS.timeEnd == null) "" else seanceS.timeEnd.toString(),
                    placeholder = "Время конца",
                    modifier = Modifier.widthIn(150.dp, 200.dp),
                    onValueChange = {},
                    readOnly = true,
                    notNull = true
                )
            }

            Spacer(Modifier.height(25.dp))

            Button(onClick = {
                pickIsTimeStart = true
                timePickerIsVisible = true
            }, modifier = Modifier.widthIn(max = 150.dp)) {
                Text(
                    text = "Выбрать время начала",
                    textAlign = TextAlign.Center
                )
            }

            if(datePickerIsVisible && pickIsDateStart) {
                DatePicker(onValueChange = {viewModelS.onEvent(
                    SeanceUIEvent.DateStartChanged(it))
                })
                datePickerIsVisible = false
                pickIsDateStart = false
            }else if(datePickerIsVisible){
                DatePicker(onValueChange = {viewModelS.onEvent(
                    SeanceUIEvent.DateEndChanged(it))
                })
                datePickerIsVisible = false
            }else{

            }

            if(timePickerIsVisible && pickIsTimeStart) {
                TimePicker(onValueChange = {viewModelS.onEvent(
                    SeanceUIEvent.TimeStartChanged(it))
                    if(movieS.idMovie != 0){
                        viewModelS.onEvent(
                            SeanceUIEvent.TimeEndChanged(it.plusMinutes((movieS.duration + 15).toLong()))
                        )
                    }
                })



                timePickerIsVisible = false
                pickIsDateStart = false
            }

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .heightIn(max = 250.dp),
                contentAlignment = Alignment.Center
            ){
                InfoMovie(movieState = movieS)
            }
            Button(onClick = {
                //isChoiceFilm
                visibleStateListMovie.targetState = true
            }) {
                Text(text = "Выбрать фильм")
            }

            Box(Modifier
                .fillMaxSize()
                .padding(5.dp)){
                Column{
                    Text("Кинотеатр", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                        ExposedDropdownMenuBox(
                            expanded = expandedListCinemas,
                            onExpandedChange = {expandedListCinemas = !expandedListCinemas}
                        ) {
                            RoundedTextField(
                                label = "Адрес кинотеатра",
                                value = cinemaS.addressCinema,
                                placeholder = "Выберите адрес",
                                onValueChange = {},
                                notNull = true,
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expandedListCinemas
                                    )
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = expandedListCinemas, onDismissRequest =
                                {
                                    expandedListCinemas = false
                                }
                            ) {
                                viewModelC.listStateCinema.forEach { cinema ->
                                    DropdownMenuItem(onClick = {
                                        expandedListCinemas = false
                                        viewModelC.onEvent(CinemaUIEvent.IdCinemaChanged(cinema.id))
                                        viewModelC.onEvent(CinemaUIEvent.GetHallsCinema)
                                        viewModelC.onEvent(CinemaUIEvent.AddressCinemaChanged(cinema.addressCinema))
                                    }) {
                                        Text(text = cinema.addressCinema)
                                    }
                                }
                            }
                        }
                        if(cinemaS.addressCinema != ""){
                            IconButton(onClick = {
                                viewModelC.onEvent(CinemaUIEvent.IdCinemaChanged(0))
                                viewModelS.onEvent(SeanceUIEvent.IdHallChanged(0))
                                viewModelC.onEvent(CinemaUIEvent.AddressCinemaChanged(""))
                            }) {
                                Icon(Icons.Filled.Close, "")
                            }
                        }
                    }

                    Spacer(Modifier.height(25.dp))

                    Text("Номер зала", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                        ExposedDropdownMenuBox(
                            expanded = expandedListHalls,
                            onExpandedChange = {expandedListHalls = !expandedListHalls}
                        ) {
                            RoundedTextField(
                                label = "Номер зала",
                                value = if(seanceS.idHall == 0) "" else seanceS.idHall.toString(),
                                placeholder = "Выберите номер зала",
                                onValueChange = {},
                                notNull = true,
                                enabled = cinemaS.id != 0,
                                readOnly = true,
                                trailingIcon = {
                                    if(cinemaS.id != 0){
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = expandedListHalls
                                        )
                                    }
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = if(cinemaS.id != 0) expandedListHalls else false, onDismissRequest =
                                {
                                    expandedListHalls = false
                                }
                            ) {
                                if(cinemaS.isLoading){
                                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                                        CircularProgressIndicator()
                                    }
                                }else{
                                    viewModelC.listStateHall.forEach { hall ->
                                        DropdownMenuItem(onClick = {
                                            expandedListHalls = false
                                            viewModelS.onEvent(SeanceUIEvent.IdHallChanged(hall.id))
                                        }) {
                                            Text(text = "${hall.id}, ${hall.nameTypeHall}")
                                        }
                                    }
                                }
                            }
                        }
                        if(seanceS.idHall != 0){
                            IconButton(onClick = {
                                viewModelS.onEvent(SeanceUIEvent.IdHallChanged(0))
                            }) {
                                Icon(Icons.Filled.Close, "")
                            }
                        }
                    }

                    Spacer(Modifier.height(25.dp))

                    Text("Цена", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                    RoundedTextField(
                        label = "Цена билета на сеанс",
                        value = priceSeance,
                        placeholder = "Цена билета",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        onValueChange = {
                            if(it != "0" && it.isDigitsOnly() ||
                                it.indexOf(",") != -1 && it.groupBy{","}.count() < 2){
                                val maxNum = if(it.indexOf(",") != -1){
                                    when(it.substringAfter(",").length){
                                        0 -> {
                                            it.length + 2
                                        }
                                        1 -> {
                                            it.length + 1
                                        }
                                        2 -> {
                                            it.length
                                        }else -> {
                                        it.length - 1
                                    }
                                    }
                                }else{
                                    6
                                }
                                if (it.length <= maxNum){
                                    priceSeance = it
                                    if(it.isDigitsOnly() && it.length > 1){
                                        viewModelS.onEvent(SeanceUIEvent.PriceChanged(it.toDouble()))
                                    }
                                }
                            }
                        })
                }
            }

                LaunchedEffect(seanceS){
                    buttonIsEnabled = seanceS.idHall != 0 &&
                                      seanceS.dateStart != null &&
                                      seanceS.dateEnd != null &&
                                      seanceS.idMovie != 0 &&
                                      seanceS.price != 0.0 &&
                                      seanceS.timeStart != null &&
                                      seanceS.timeEnd != null
                }

            Spacer(Modifier.height(50.dp))

                Button(onClick = {
                    viewModelS.onEvent(SeanceUIEvent.AddSeance)
                }, enabled = buttonIsEnabled
                ) {
                    Text(text = "Добавить")
                }

            Spacer(Modifier.height(50.dp))

        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UpdateSeance(
    viewModelM: MovieViewModel,
    viewModelS: SeanceViewModel,
    viewModelC: CinemaViewModel,
){

    val movieS = viewModelM.stateMovie
    val seanceS = viewModelS.state
    val cinemaS = viewModelC.state
    var datePickerIsVisible by rememberSaveable { mutableStateOf(false) }
    var priceSeance by rememberSaveable { mutableStateOf(Support.copySeanceUpdate.price.toString())}
    var pickIsDateStart by rememberSaveable { mutableStateOf(false) }
    var timePickerIsVisible by rememberSaveable { mutableStateOf(false) }
    var pickIsTimeStart by rememberSaveable { mutableStateOf(false) }
    var expandedListCinemas by rememberSaveable{ mutableStateOf(false) }
    var expandedListHalls by rememberSaveable{ mutableStateOf(false) }
    var buttonIsEnabled by remember { mutableStateOf(false) }
    val visibleStateListMovie = remember{
        MutableTransitionState(initialState = false).apply {
            targetState = false
        }
    }
    AnimatedVisibility(
        visibleState = visibleStateListMovie,
        enter = expandHorizontally(),
        exit = shrinkVertically()
    ) {
        ListMovie(viewModelM = viewModelM, viewModelS = viewModelS, mutableTransitionState = visibleStateListMovie)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(50.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            RoundedTextField(
                label = "Дата начала",
                value = if(seanceS.dateStart == null) "" else seanceS.dateStart.toString().toMyDateFormat(seanceS.dateStart),
                placeholder = "Дата начала",
                onValueChange = {},
                modifier = Modifier.widthIn(150.dp, 200.dp),
                readOnly = true,
                notNull = true)

            Spacer(modifier = Modifier.width(5.dp))

            Text(text = "-", fontSize = 20.sp)

            Spacer(modifier = Modifier.width(5.dp))

            RoundedTextField(
                label = "Дата конца",
                value = if(seanceS.dateEnd == null) "" else seanceS.dateEnd.toString().toMyDateFormat(seanceS.dateEnd),
                placeholder = "Дата конца",
                modifier = Modifier.widthIn(150.dp, 200.dp),
                onValueChange = {},
                readOnly = true,
                notNull = true)

        }

        Spacer(Modifier.height(25.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Button(onClick = {
                pickIsDateStart = true
                datePickerIsVisible = true
            }, modifier = Modifier.widthIn(max = 150.dp)) {
                Text(
                    text = "Выбрать дату начала",
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(30.dp))

            Button(onClick = {
                datePickerIsVisible = true
            }, modifier = Modifier.widthIn(max = 150.dp)
            ) {
                Text(
                    text = "Выбрать дату конца",
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(25.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            RoundedTextField(
                label = "Время начала",
                value = if(seanceS.timeStart == null) "" else seanceS.timeStart.toString(),
                placeholder = "Время начала",
                onValueChange = {},
                modifier = Modifier.widthIn(150.dp, 200.dp),
                readOnly = true,
                notNull = true)

            Spacer(modifier = Modifier.width(5.dp))

            Text(text = "-", fontSize = 20.sp)

            Spacer(modifier = Modifier.width(5.dp))

            RoundedTextField(
                label = "Время конца",
                value = if(seanceS.timeEnd == null) "" else seanceS.timeEnd.toString(),
                placeholder = "Время конца",
                modifier = Modifier.widthIn(150.dp, 200.dp),
                onValueChange = {},
                readOnly = true,
                notNull = true
            )
        }

        Spacer(Modifier.height(25.dp))

        Button(onClick = {
            pickIsTimeStart = true
            timePickerIsVisible = true
        }, modifier = Modifier.widthIn(max = 150.dp)) {
            Text(
                text = "Выбрать время начала",
                textAlign = TextAlign.Center
            )
        }

        if(datePickerIsVisible && pickIsDateStart) {
            DatePicker(onValueChange = {viewModelS.onEvent(
                SeanceUIEvent.DateStartChanged(it))
            })
            datePickerIsVisible = false
            pickIsDateStart = false
        }else if(datePickerIsVisible){
            DatePicker(onValueChange = {viewModelS.onEvent(
                SeanceUIEvent.DateEndChanged(it))
            })
            datePickerIsVisible = false
        }else{

        }

        if(timePickerIsVisible && pickIsTimeStart) {
            TimePicker(onValueChange = {viewModelS.onEvent(
                SeanceUIEvent.TimeStartChanged(it))
                if(movieS.idMovie != 0){
                    viewModelS.onEvent(
                        SeanceUIEvent.TimeEndChanged(it.plusMinutes((movieS.duration + 15).toLong()))
                    )
                }
            })



            timePickerIsVisible = false
            pickIsDateStart = false
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .heightIn(max = 250.dp),
            contentAlignment = Alignment.Center
        ){
            InfoMovie(movieState = movieS)
        }
        Button(onClick = {
            //isChoiceFilm
            visibleStateListMovie.targetState = true
        }) {
            Text(text = "Выбрать фильм")
        }

        Box(Modifier
            .fillMaxSize()
            .padding(5.dp)){
            Column{
                Text("Кинотеатр", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    ExposedDropdownMenuBox(
                        expanded = expandedListCinemas,
                        onExpandedChange = {expandedListCinemas = !expandedListCinemas}
                    ) {
                        RoundedTextField(
                            label = "Адрес кинотеатра",
                            value = cinemaS.addressCinema,
                            placeholder = "Выберите адрес",
                            onValueChange = {},
                            notNull = true,
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedListCinemas
                                )
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedListCinemas, onDismissRequest =
                            {
                                expandedListCinemas = false
                            }
                        ) {
                            viewModelC.listStateCinema.forEach { cinema ->
                                DropdownMenuItem(onClick = {
                                    expandedListCinemas = false
                                    viewModelC.onEvent(CinemaUIEvent.IdCinemaChanged(cinema.id))
                                    viewModelC.onEvent(CinemaUIEvent.GetHallsCinema)
                                    viewModelC.onEvent(CinemaUIEvent.AddressCinemaChanged(cinema.addressCinema))
                                }) {
                                    Text(text = cinema.addressCinema)
                                }
                            }
                        }
                    }
                    if(cinemaS.addressCinema != ""){
                        IconButton(onClick = {
                            viewModelC.onEvent(CinemaUIEvent.IdCinemaChanged(0))
                            viewModelS.onEvent(SeanceUIEvent.IdHallChanged(0))
                            viewModelC.onEvent(CinemaUIEvent.AddressCinemaChanged(""))
                        }) {
                            Icon(Icons.Filled.Close, "")
                        }
                    }
                }

                Spacer(Modifier.height(25.dp))

                Text("Номер зала", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                    ExposedDropdownMenuBox(
                        expanded = expandedListHalls,
                        onExpandedChange = {expandedListHalls = !expandedListHalls}
                    ) {
                        RoundedTextField(
                            label = "Номер зала",
                            value = if(seanceS.idHall == 0) "" else seanceS.idHall.toString(),
                            placeholder = "Выберите номер зала",
                            onValueChange = {},
                            notNull = true,
                            enabled = cinemaS.id != 0 || cinemaS.addressCinema != "",
                            readOnly = true,
                            trailingIcon = {
                                if(cinemaS.id != 0){
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expandedListHalls
                                    )
                                }
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = if(cinemaS.id != 0 || cinemaS.addressCinema != "") expandedListHalls else false, onDismissRequest =
                            {
                                expandedListHalls = false
                            }
                        ) {
                            if(cinemaS.isLoading){
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                                    CircularProgressIndicator()
                                }
                            }else{
                                viewModelC.listStateHall.forEach { hall ->
                                    DropdownMenuItem(onClick = {
                                        expandedListHalls = false
                                        viewModelS.onEvent(SeanceUIEvent.IdHallChanged(hall.id))
                                    }) {
                                        Text(text = "${hall.id}, ${hall.nameTypeHall}")
                                    }
                                }
                            }
                        }
                    }
                    if(seanceS.idHall != 0){
                        IconButton(onClick = {
                            viewModelS.onEvent(SeanceUIEvent.IdHallChanged(0))
                        }) {
                            Icon(Icons.Filled.Close, "")
                        }
                    }
                }

                Spacer(Modifier.height(25.dp))

                Text("Цена", color = Color.Gray, modifier = Modifier.padding(start = 5.dp))

                RoundedTextField(
                    label = "Цена билета на сеанс",
                    value = priceSeance,
                    placeholder = "Цена билета",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    onValueChange = {
                        if(it != "0" && it.isDigitsOnly() || it.indexOf(",") != -1 && it.split(",").size == 2){
                            val maxNum = if(it.indexOf(",") != -1){
                                when(it.substringAfter(",").length){
                                    0 -> {
                                        it.length + 2
                                    }
                                    1 -> {
                                        it.length + 1
                                    }
                                    2 -> {
                                        it.length
                                    }else -> {
                                    it.length - 1
                                }
                                }
                            }else{
                                6
                            }
                            if (it.length <= maxNum){
                                priceSeance = it
                                if(it.replace(",", ".").isDigitsOnly() && it.length > 1){
                                    viewModelS.onEvent(SeanceUIEvent.PriceChanged(it.toDouble()))
                                }
                            }
                        }
                    })
            }
        }

        LaunchedEffect(seanceS){
            buttonIsEnabled = seanceS.idHall != 0 &&
                    seanceS.dateStart != null &&
                    seanceS.dateEnd != null &&
                    seanceS.idMovie != 0 &&
                    seanceS.price != 0.0 &&
                    seanceS.timeStart != null &&
                    seanceS.timeEnd != null
        }

        Spacer(Modifier.height(50.dp))

        Button(onClick = {
            viewModelS.onEvent(SeanceUIEvent.UpdateSeance)
        }, enabled = buttonIsEnabled && seanceS != Support.copySeanceUpdate
        ) {
            Text(text = "Редактировать")
        }

        Spacer(Modifier.height(50.dp))

    }
}

@Composable
fun ViewSeancesCinema(
    viewModelS: SeanceViewModel,
    viewModelM: MovieViewModel,
    viewModelT: TicketViewModel = hiltViewModel(),
    viewModelC: CinemaViewModel = hiltViewModel(),
){
    val context = LocalContext.current
    val stateCinema = viewModelC.state
    val stateSeance = viewModelS.state
    val listSeanceState = viewModelS.listSeance
    val listMovieState = viewModelM.listMovie
    var loginCustomer by rememberSaveable{ mutableStateOf("") }
    var alertDialogISVisible by rememberSaveable{ mutableStateOf(false) }

    LaunchedEffect(Support.copyCinemaState){
        viewModelS.onEvent(SeanceUIEvent.GetSeancesCinema(Support.copyCinemaState.addressCinema))
        viewModelC.onEvent(CinemaUIEvent.AddressCinemaChanged(Support.copyCinemaState.addressCinema))
    }

    LaunchedEffect(viewModelT, context) {
        viewModelT.ticketResults.collect { result ->
            when (result) {
                is TicketResults.NotFoundTickets -> {
                    Toast.makeText(
                        context, "Доступных билетов нет",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else ->{

                }
            }
        }
    }

    Column(Modifier
        .fillMaxSize()
        .padding(top = 15.dp)){
        if(stateSeance.isLoading){
            LoadingScreen()
        }else{
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier
                    .heightIn(min = 50.dp, max = 100.dp)
                    .fillMaxWidth(0.8f)
                    .background(MaterialTheme.colors.primary,
                        RoundedCornerShape(bottomEnd = 10.dp, topEnd = 10.dp))

            ){
                Text(text = stateCinema.addressCinema, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Column(horizontalAlignment = Alignment.CenterHorizontally){

                    Button(onClick = {
                        alertDialogISVisible = true
                    }) {
                        Text(text = "Проверить билеты")
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    if(viewModelT.listStateTicket.isNotEmpty()){
                        Box(modifier = Modifier.clickable {
                            viewModelT.onEvent(TicketUIEvent.ClearTicketsUser)
                        }){
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically)
                            {
                                Icon(Icons.Filled.Close, "")
                                Text(text = "Очистить билеты", color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Text(text = "Доступные сеансы:")
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            if(viewModelT.state.isLoading){
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }else{
                if(viewModelT.listStateTicket.isNotEmpty()){

                    ListSeance(listSeances = listSeanceState
                        .filter { seance ->
                            viewModelT.listStateTicket.any { ticket ->
                                seance.id == ticket.idSeance

                            }}, listMovie = listMovieState)
                }else{
                    ListSeance(listSeances = listSeanceState, listMovie = listMovieState)
                }
            }
        }
    }
    if(alertDialogISVisible){
        AlertDialog(
            onDismissRequest = {
                alertDialogISVisible = false
            },
            title = {Text("Проверка билетов")},
            text = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Введите логин клиента, чтобы проверить билеты на сегодняшние сеансы")
                    RoundedTextField(
                        label = "Введите логин",
                        value = loginCustomer,
                        placeholder = "Логин клиента",
                        onValueChange = {
                            loginCustomer = it
                        })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        alertDialogISVisible = false
                        viewModelT.onEvent(TicketUIEvent.GetTicketsByLogin(loginCustomer))
                        loginCustomer = ""
                              },
                    enabled = loginCustomer.isNotBlank())
                {
                    Text(text = "Подтвердить")
                }
            },
            dismissButton = {
                Button(onClick = {
                    alertDialogISVisible = false
                    loginCustomer = ""
                }

                ) {
                    Text(text = "Отмена")
                }
            }
        )
    }
}

@Composable
private fun ListSeance(listSeances : List<SeanceState>, listMovie : List<MovieState>){
    LazyColumn{
        items(items = listSeances){ seance ->
            ListElementSeance(seance = seance, movie = listMovie.find { it.idMovie == seance.idMovie }!!)
        }
    }
}

@Composable
private fun ListElementSeance(seance : SeanceState, movie : MovieState){

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(movie.linkImage.replace("https", "http"))
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column(Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painter,
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(100.dp)
                        .clip(RoundedCornerShape(15.dp)))

                Column(Modifier.padding(start = 10.dp)){
                    Text(movie.nameMovie,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp)
                    Text(text = "${seance.timeStart}-${seance.timeEnd}")
                    Text(text = "Номер зала: ${seance.idHall}")
                }
            }
        }
    }
}

@Composable
fun ListMovie(
    viewModelM: MovieViewModel,
    viewModelS: SeanceViewModel,
    mutableTransitionState: MutableTransitionState<Boolean>,
    listStateMovie: List<MovieState> = viewModelM.listMovie
) {
    LazyColumn(contentPadding = PaddingValues(5.dp)) {
        item {
            if(viewModelM.listMovie.isEmpty()){
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "По вашему запросу ничего не найдено :(")
                }
            }
        }
        items(items = listStateMovie)  { movie ->
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.linkImage.replace("https", "http"))
                    .size(Size.ORIGINAL)
                    .crossfade(true)
                    .build()
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clickable(onClick = {
                        mutableTransitionState.targetState = false
                        viewModelM.onEventMovie(MovieUIEvent.MovieChanged(movie))
                        viewModelS.onEvent(SeanceUIEvent.IdMovieChanged(movie.idMovie))

                        with(viewModelS.state) {
                            if (viewModelS.state.timeStart != null) {
                                viewModelS.onEvent(
                                    SeanceUIEvent.TimeEndChanged(timeStart!!.plusMinutes(
                                        (viewModelM.stateMovie.duration + 15).toLong())
                                    )
                                )
                            }
                        }

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
                            Icon(Icons.Filled.ErrorOutline, "")
                        }
                        else -> {
                            Image(
                                painter = painter,
                                contentDescription = "",
                                modifier = Modifier
                                    .height(200.dp)
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


