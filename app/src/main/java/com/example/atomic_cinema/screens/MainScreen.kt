@file:OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)

package com.example.atomic_cinema.screens



import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.atomic_cinema.R
import com.example.atomic_cinema.events.MainUIEvent
import com.example.atomic_cinema.events.TicketUIEvent
import com.example.atomic_cinema.navigation.NavRoutes
import com.example.atomic_cinema.notification.UserNotificationService
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.server.cinema.CinemaResults
import com.example.atomic_cinema.server.movie.MovieResult
import com.example.atomic_cinema.server.news.NewsResults
import com.example.atomic_cinema.server.ticket.TicketResults
import com.example.atomic_cinema.stateClasses.MovieState
import com.example.atomic_cinema.stateClasses.NewsState
import com.example.atomic_cinema.stateClasses.RevenueDetails
import com.example.atomic_cinema.utils.LoadingScreen
import com.example.atomic_cinema.viewModel.AuthViewModel
import com.example.atomic_cinema.viewModel.MainViewModel
import com.example.atomic_cinema.viewModel.MovieViewModel
import com.example.atomic_cinema.viewModel.TicketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navHostController: NavHostController,
    viewModelMovie: MovieViewModel = hiltViewModel(),
    viewModelAuth: AuthViewModel = hiltViewModel(),
    viewModelMain: MainViewModel = hiltViewModel(),
    viewModelTicket: TicketViewModel = hiltViewModel(),
){

    val context = LocalContext.current
    val stateMovie = viewModelMovie.stateMovie
    val stateAuth = viewModelAuth.state
    val listStateMovie = viewModelMovie.listMovie
    val stateNews = viewModelMain.state
    val listStateNews = viewModelMain.listStateNews
    val scope = rememberCoroutineScope()
    val stateVisibleArrow = remember {
        MutableTransitionState(false).apply {
            scope.launch {
                while (true){
                    delay(3000L)
                    targetState = true
                    delay(3000L)
                    targetState = false
                }
            }
        }
    }
    val stateRevenueForm = remember { MutableTransitionState(initialState = false) }


    LaunchedEffect(viewModelMovie, context){
        viewModelMovie.movieResults.collect{ result ->
            when(result){
                is MovieResult.Unauthorized -> {

                }
                is MovieResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "Неизвестная ошибка, попробуйте снова позже",
                        Toast.LENGTH_LONG).show()
                }
                else -> {

                }
            }
        }
    }

    LaunchedEffect(viewModelAuth, context){
        viewModelAuth.authResults.collect{ result ->
            when(result){
                is AuthResult.Authorized -> {
                }
                is AuthResult.Unauthorized ->{
                    viewModelMain.onEvent(MainUIEvent.GetNews)
                }
                else -> {

                }
            }
        }
    }

    LaunchedEffect(viewModelTicket, context){
        viewModelTicket.ticketResults.collect{ result ->
            when(result){
                is TicketResults.NotifyCustomer -> {
                    UserNotificationService(context).showNotification()
                }
                else ->{

                }
            }
        }
    }

    LaunchedEffect(viewModelMain, context){
        viewModelMain.newsResults.collect{ result ->
            when(result){
                is NewsResults.UnknownError -> {
                    Toast.makeText(context, "Неизвестная ошибка", Toast.LENGTH_LONG).show()
                }
                else -> {

                }
            }
        }
    }
    
    LaunchedEffect(viewModelMain, context){
        viewModelMain.cinemaResults.collect{ result ->
            when(result){
                is CinemaResults.NotRevenue -> {
                    Toast.makeText(
                        context, "За этот месяц нет выручки.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is CinemaResults.OK -> {
                    
                }
                is CinemaResults.Unauthorized -> {
                    Toast.makeText(
                        context, "Авторизируйтесь в системе.",
                        Toast.LENGTH_SHORT
                    ).show()

                    navHostController.navigate(NavRoutes.Authorization.route,
                        NavOptions.Builder().setPopUpTo(NavRoutes.Main.route, true).build())
                }
                is CinemaResults.UnknownError -> {
                    Toast.makeText(
                        context, "Неизвестная ошибка.",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }

    LaunchedEffect(stateAuth){
        if(stateAuth.role == "customer"){
            viewModelMain.onEvent(MainUIEvent.GetNews)
            viewModelTicket.onEvent(TicketUIEvent.CheckTicketsOnSeances)
        }
    }

    when(stateAuth.role){
        "employee" -> {
            
        }
        "admin" -> {
            Column {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)){
                    Image(
                        painter = painterResource(id = R.mipmap.main_cinema),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                )
                            )
                    )
                    ListMovie(listStateMovie)
                }

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f), contentAlignment = Alignment.Center)
                {
                    Column{
                        RevenueForm(viewModelMain = viewModelMain,
                            stateRevenueAnimate = stateRevenueForm)

                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                            Button(onClick = {
                                viewModelMain.onEvent(MainUIEvent.GetRevenueReport)
                                stateRevenueForm.targetState = true
                            }) {
                                Text(text = "Сформировать отчёт по выручке")
                            }
                        }
                    }
                }

            }
        }
        else -> {
            ModalBottomSheetNewsList(
                listStateNews,
                stateNews,
                viewModelMain
            ) {
                if(stateMovie.isLoading){
                    LoadingScreen()
                }else{
                    Image(
                        painter = painterResource(id = R.mipmap.main_cinema),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f)
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                )
                            )
                    )
                    Column(){
                        ListMovie(listStateMovie)
                        AnimatedVisibility(
                            visibleState = stateVisibleArrow,
                            enter = scaleIn(),
                            exit = slideOutVertically(),
                        ) {
                            Box(Modifier
                                .fillMaxSize()
                                .padding(bottom = 10.dp), contentAlignment = Alignment.BottomCenter) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_baseline_vertical_align_top_24),
                                    "")
                            }
                        }
                    }
                }
            }
        }
        
    }
}

@Composable
fun RevenueForm(viewModelMain: MainViewModel,
                revenueDetails: RevenueDetails = viewModelMain.stateDetailsRevenue,
                stateRevenueAnimate: MutableTransitionState<Boolean>){
    AnimatedVisibility(
        visibleState = stateRevenueAnimate,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
            Column {
                if(revenueDetails.isLoading){
                    CircularProgressIndicator()
                }else{
                    Text(text = "За этот месяц:", fontWeight = FontWeight.Bold)
                    Text(text = "Сумма выручки: " + revenueDetails.sumMoney)
                    Text(text = "Количество проданных билетов: " + revenueDetails.sumTickets)
                    Spacer(modifier = Modifier.height(20.dp))
                }

            }
        }
    }

}

@Composable
private fun ListMovie(listMovie : List<MovieState>){

    val scrollState = rememberLazyListState()

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            // Прокручиваем к следующему элементу
            val itemCount = listMovie.size // Общее количество элементов в списке
            val nextItemIndex = (scrollState.firstVisibleItemIndex + 1) % itemCount
            scrollState.animateScrollToItem(nextItemIndex)

        }
    }

    LazyRow(state = scrollState, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top){
        items(items = listMovie.reversed()){ movie ->
            ListElementMovie(movie = movie)
        }
    }

}

@Composable
fun ListElementMovie(movie : MovieState){

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(movie.linkImage.replace("https", "http"))
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )

        Box(modifier = Modifier
            .padding(top = 15.dp, end = 15.dp)
            .width(500.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start){
                Text(
                    text = movie.nameMovie,
                    fontSize = 25.sp, fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier
                        .width(175.dp)
                        .padding(end = 25.dp)
                )
                when(painter.state) {
                    AsyncImagePainter.State.Empty -> {

                    }
                    is AsyncImagePainter.State.Loading -> {
                        Box(modifier = Modifier
                            .heightIn(min = 200.dp, max = 250.dp)
                            .widthIn(min = 100.dp, max = 150.dp))
                        {
                            CircularProgressIndicator()
                        }
                    }
                    is AsyncImagePainter.State.Success -> {
                        Image(
                            painter = painter,
                            contentDescription = "",
                            modifier = Modifier
                                .heightIn(min = 200.dp, max = 250.dp)
                                .widthIn(min = 100.dp, max = 150.dp),
                            Alignment.Center,
                            contentScale = ContentScale.Fit,
                        )
                    }
                    is AsyncImagePainter.State.Error -> {
                        Icon(Icons.Filled.Error,
                            "",
                            modifier = Modifier.heightIn(min = 200.dp, max = 250.dp)
                                .widthIn(min = 100.dp, max = 150.dp),
                        tint = Color.White)
                    }
                }
//                Image(
//                    painter = painter,
//                    contentDescription = "",
//                    contentScale = ContentScale.Fit,
//                    alignment = Alignment.Center,
//                    modifier = Modifier
//                        .height(250.dp)
//                        .width(200.dp)
//                        .clip(RoundedCornerShape(10.dp))
//                )
            }
        }

}




@Composable
fun ModalBottomSheetNewsList(
    listNews: List<NewsState>,
    stateNews: NewsState,
    viewModelMain: MainViewModel,
    content: @Composable () -> Unit
){
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.HalfExpanded,
        skipHalfExpanded = false
    )

    val swipeableModifier = Modifier.swipeable(
        state = bottomSheetState,
        anchors = mapOf(
            0f to ModalBottomSheetValue.Hidden,
            0.3f to ModalBottomSheetValue.HalfExpanded,
            1f to ModalBottomSheetValue.Expanded
        ),
        thresholds = { _, _ -> FractionalThreshold(0.3f) },
        orientation = Orientation.Vertical
    )


//    if(stateNews.bottomSheetValue){
//        scope.launch { bottomSheetState.animateTo(ModalBottomSheetValue.HalfExpanded) }
//        viewModelMain.onEvent(MainUIEvent.BottomSheetValue(false))
//    }
    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            if(stateNews.isLoading){
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }else{
                ListNews(listNews = listNews)
            }
        },
        sheetElevation = 0.dp,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp)
                    .then(swipeableModifier)
            ) {
                content()
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun PreviewMain(){
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter){
           // MainImageCinema()
        }
    }
}

@Composable
fun ListElementNews(news : NewsState){

    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(news.linkImage.replace("https", "http"))
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.linkNews))
                context.startActivity(intent)
            },
        elevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column{
            Box(
                modifier = Modifier.background(color = Color.Black.copy(0.5f)),
                    contentAlignment = Alignment.BottomStart
            ){
                Image(
                    painter = painter,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxWidth(),
                    colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.4f), BlendMode.Darken)
                )
                Text(
                    text = news.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom =5.dp))
            }

            Text(text = news.description, Modifier.padding(10.dp))

        }
    }
}

@Composable
fun ListNews(listNews : List<NewsState>){
    LazyColumn(
        verticalArrangement = Arrangement.SpaceBetween
    ){
        items(listNews){ news ->
            ListElementNews(news = news)
        }
    }

}



@Preview
@Composable
fun PreviewList(){
    Box(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(0.5f))
        ListNews(listNews = listOf<NewsState>(
            NewsState(
                false,
                linkImage ="https://cdn.lifehacker.ru/wp-content/uploads/2023/05/beau-is-afraid-banner-1681166847980_1683013525-1024x512.jpg",
                title = "ААААА",
                description = "АААААААААА"
            ),
            NewsState(
                false,
                linkImage ="https://cdn.lifehacker.ru/wp-content/uploads/2023/05/beau-is-afraid-banner-1681166847980_1683013525-1024x512.jpg",
                title = "ААААА",
                description = "АААААААААА"
            ),
            NewsState(
                false,
                linkImage ="https://cdn.lifehacker.ru/wp-content/uploads/2023/05/beau-is-afraid-banner-1681166847980_1683013525-1024x512.jpg",
                title = "ААААА",
                description = "АААААААААА"
            ),
            NewsState(
                false,
                linkImage ="https://cdn.lifehacker.ru/wp-content/uploads/2023/05/beau-is-afraid-banner-1681166847980_1683013525-1024x512.jpg",
                title = "ААААА",
                description = "АААААААААА"
            )

        ))
    }
}
