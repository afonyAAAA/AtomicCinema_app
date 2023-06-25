package com.example.atomic_cinema.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic_cinema.events.MainUIEvent
import com.example.atomic_cinema.notification.UserNotificationService
import com.example.atomic_cinema.server.auth.AuthResult
import com.example.atomic_cinema.server.cinema.CinemaRepository
import com.example.atomic_cinema.server.cinema.CinemaResults
import com.example.atomic_cinema.server.news.NewsRepository
import com.example.atomic_cinema.server.news.NewsResults
import com.example.atomic_cinema.stateClasses.NewsState
import com.example.atomic_cinema.stateClasses.RevenueDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val repository : NewsRepository,
    val repositoryCinema: CinemaRepository
) : ViewModel() {


    var state by mutableStateOf(NewsState())
    var listStateNews = mutableListOf<NewsState>()
    var stateDetailsRevenue by mutableStateOf(RevenueDetails())

    private val resultChannel = Channel<NewsResults<Unit>>()
    private val resultChannelCinema = Channel<CinemaResults<Unit>>()
    val newsResults = resultChannel.receiveAsFlow()
    val cinemaResults = resultChannelCinema.receiveAsFlow()

    fun onEvent(event : MainUIEvent){
        when(event){
            is MainUIEvent.BottomSheetValue -> {
                state = state.copy(bottomSheetValue = event.value)
            }
            MainUIEvent.GetNews -> {
                getNews()
            }
            MainUIEvent.GetRevenueReport -> {
                getRevenueReport()
            }
        }
    }

    private fun getRevenueReport(){
        viewModelScope.launch {
            stateDetailsRevenue = stateDetailsRevenue.copy(isLoading = true)

            val result = repositoryCinema.getRevenueReport()

            stateDetailsRevenue = stateDetailsRevenue.copy(
                sumMoney = result.data?.sumMoney ?: "",
                sumTickets = result.data?.sumTickets ?: ""
            )

            if(stateDetailsRevenue.sumMoney != ""){
                resultChannelCinema.send(CinemaResults.OK())
            }else{
                resultChannelCinema.send(CinemaResults.NotRevenue())
            }

            stateDetailsRevenue = stateDetailsRevenue.copy(isLoading = false)
        }
    }



    private fun getNews(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            if(listStateNews.isNotEmpty()){
                listStateNews.clear()
            }

            val news = repository.getTopHeadlines()

            if(news.data != null){
                news.data.articles.filter { it.source!!.name != "Lenta" }.forEach{ news ->
                    listStateNews.add(NewsState(
                        title = news.title!!,
                        linkImage = news.urlToImage.toString(),
                        description = news.description!!,
                        linkNews = news.url!!
                    ))
                }
            }else{
                resultChannel.send(NewsResults.UnknownError())
            }

            state = state.copy(isLoading = false)
        }
    }
}