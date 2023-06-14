package com.example.atomic_cinema.viewModel

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atomic_cinema.events.MovieFilterUIEvent
import com.example.atomic_cinema.events.MovieUIEvent
import com.example.atomic_cinema.server.movie.*
import com.example.atomic_cinema.stateClasses.FilterMovieState
import com.example.atomic_cinema.stateClasses.GenresState
import com.example.atomic_cinema.stateClasses.MovieState
import com.example.atomic_cinema.utils.Support
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel(){

    var stateMovie by mutableStateOf(MovieState())
    var stateFilterMovie by mutableStateOf(FilterMovieState())

    private val resultChannel = Channel<MovieResult<Unit>>()
    val movieResults = resultChannel.receiveAsFlow()

    var copyMovieUpdate by mutableStateOf(Support.copyMovieUpdate)
    var listMovie : MutableList<MovieState> = mutableListOf()
    var searchListMovie : MutableList<MovieState> = mutableStateListOf()

    init {
        getAllMovie()
        getAllGenre()
    }

    fun onEventMovie(event : MovieUIEvent){
        when(event){
            is MovieUIEvent.NameMovieChanged -> {
                stateMovie = stateMovie.copy(nameMovie = event.value)
            }
            is MovieUIEvent.AgeRatingChanged ->{
                stateMovie = stateMovie.copy(ageRating = event.value)
            }
            is MovieUIEvent.LinkImageChanged -> {
                stateMovie = stateMovie.copy(linkImage = event.value)
            }
            is MovieUIEvent.DescriptionChanged -> {
                stateMovie = stateMovie.copy(description = event.value)
            }
            is MovieUIEvent.DurationChanged -> {
                stateMovie = stateMovie.copy(duration = event.value)
            }
            is MovieUIEvent.DirectorChanged -> {
                stateMovie = stateMovie.copy(director = event.value)
            }
            is MovieUIEvent.YearOfIssueChanged -> {
                stateMovie = stateMovie.copy(yearOfIssue = event.value)
            }
            is MovieUIEvent.GenresChanged -> {
                stateMovie = stateMovie.copy(genreList = event.value)
            }
            is MovieUIEvent.ShowDetailsMovieChanged -> {
                stateMovie = stateMovie.copy(showDetailsMovie = event.value)
            }
            is MovieUIEvent.IdMovieChanged -> {
                stateMovie = stateMovie.copy(idMovie = event.value)
            }
            is MovieUIEvent.MovieChanged -> {
                stateMovie = event.value
            }
            MovieUIEvent.AddMovie -> {
                addMovie()
            }
            MovieUIEvent.DeleteMovie -> {
                deleteMovie()
            }
            MovieUIEvent.UpdateMovie -> {
                updateMovie()
            }
        }
    }

    fun onEventFilterMovie(event: MovieFilterUIEvent){
        when(event){
            is MovieFilterUIEvent.AgeRatingChanged -> {
                stateFilterMovie = stateFilterMovie.copy(selectedAgeRating = event.value)
            }
            is MovieFilterUIEvent.ExpandedTextFieldGenresChanged -> {
                stateFilterMovie = stateFilterMovie.copy(expandedListGenreTextField = event.value)
            }
            is MovieFilterUIEvent.GenreChanged -> {

                stateFilterMovie = stateFilterMovie.copy(isLoading = true)

                val copyList = stateFilterMovie.selectedGenreList

                copyList[event.index] = GenresState(
                    id = event.index + 1,
                    nameGenre = copyList[event.index].nameGenre,
                    isChoice = event.state)

               stateFilterMovie = stateFilterMovie.copy(
                   isLoading = false,
                   selectedGenreList = copyList
               )
            }
            is MovieFilterUIEvent.YearOfIssueChanged -> {
                stateFilterMovie = stateFilterMovie.copy(selectedYearIssue = event.value)
            }
            is MovieFilterUIEvent.FilterIsChanged -> {
                stateFilterMovie = stateFilterMovie.copy(
                    filterIsActivated = event.value,
                    animateVisibilityFilter = MutableTransitionState(false).apply { targetState = event.value }
                )
            }
            is MovieFilterUIEvent.ExpandedTextFieldAgeChanged -> {
                stateFilterMovie = stateFilterMovie.copy(expandedListAgeTextField = event.value)
            }
            is MovieFilterUIEvent.ExpandedTextFieldYearChanged -> {
                stateFilterMovie = stateFilterMovie.copy(expandedListYearTextField = event.value)
            }
            is MovieFilterUIEvent.ApplyFilter -> {
                if(event.value){
                    stateFilterMovie = stateFilterMovie.copy(searchText = TextFieldValue(""))
                    getFilteredMovie()
                }else{
                    if(
                        with(stateFilterMovie){
                            selectedGenreList.filter { it.isChoice }.isEmpty() &&
                                  selectedAgeRating == -1 && selectedYearIssue == 0
                        }
                    ) getAllMovie()
                }
            }
            is MovieFilterUIEvent.SearchTextChanged -> {
                stateFilterMovie = stateFilterMovie.copy(searchText = event.value)

                if(stateFilterMovie.searchText.text.isNotEmpty()){
                    searchListMovie = getMovieWithSearchedText(stateFilterMovie.searchText).toMutableList()
                }
            }
            MovieFilterUIEvent.ClearSelectedGenres -> {

                val copyList = mutableListOf<GenresState>()
                    stateFilterMovie.selectedGenreList.forEach{
                        copyList.add(GenresState(id = it.id, nameGenre = it.nameGenre, isChoice = false))
                    }

                stateFilterMovie = stateFilterMovie.copy(
                    selectedGenreList = copyList
                )
            }
        }
    }

    private fun getAllMovie(){
        viewModelScope.launch {
            stateMovie = stateMovie.copy(isLoading = true)

            if(listMovie.isNotEmpty()){
                listMovie.clear()
            }

            val result = repository.getAllMovie()

            result.let {

                it.data?.forEach { movie ->
                    listMovie.add(MovieState(
                        idMovie = movie.id,
                        nameMovie = movie.nameMovie,
                        director = movie.director,
                        ageRating = movie.ageRating,
                        yearOfIssue = movie.yearOfIssue,
                        genreList = movie.genresMovie.toMutableList(),
                        description = movie.description,
                        duration = movie.duration,
                        linkImage = movie.linkImage
                    ))
                }

            } ?: resultChannel.send(MovieResult.UnknownError())

            resultChannel.send(MovieResult.OK())

            stateMovie = MovieState().copy(isLoading = false)
        }
    }

    private fun getFilteredMovie(){
        viewModelScope.launch {
            stateFilterMovie = stateFilterMovie.copy(isLoading = true)

            val result = repository.getFilteredMovie(
                stateFilterMovie.selectedGenreList.filter { it.isChoice }.map { it.nameGenre },
                selectedAgeRating = if(stateFilterMovie.selectedAgeRating > 0) stateFilterMovie.selectedAgeRating else null,
                selectedYear = if(stateFilterMovie.selectedYearIssue > 0) stateFilterMovie.selectedYearIssue else null
            )

            if(listMovie.isNotEmpty()){
                listMovie.clear()
            }

            if(result.data != null){
                result.data.forEach { movie ->
                    listMovie.add(
                        MovieState(
                            idMovie = movie.id,
                            nameMovie = movie.nameMovie,
                            director = movie.director,
                            ageRating = movie.ageRating,
                            yearOfIssue = movie.yearOfIssue,
                            genreList = movie.genresMovie.toMutableList(),
                            description = movie.description,
                            duration = movie.duration,
                            linkImage = movie.linkImage
                        )
                    )
                }
            }

            stateFilterMovie = stateFilterMovie.copy(isLoading = false)
        }
    }

    private fun getMovieWithSearchedText(stateSearch : TextFieldValue) : List<MovieState>{
        val searchedText = stateSearch.text
        val resultList: MutableList<MovieState> = mutableListOf()
        var i = 0
        for(movie in listMovie){
            if(movie.nameMovie.lowercase(Locale.getDefault())
                    .contains(searchedText.lowercase(Locale.getDefault()))
            ){
                resultList.add(i,movie)
                ++i
            }
        }
        return resultList
    }

    private fun addMovie(){
        viewModelScope.launch {
            stateMovie = stateMovie.copy(isLoading = true)

            val request = MovieAddRequest(
                nameMovie = stateMovie.nameMovie,
                duration = stateMovie.duration,
                description = stateMovie.description,
                director = stateMovie.director,
                linkImage = stateMovie.linkImage,
                ageRating = stateFilterMovie.selectedAgeRating.toString() + "+",
                yearOfIssue = stateFilterMovie.selectedYearIssue.toString(),
                listGenre = stateFilterMovie.selectedGenreList.filter{it.isChoice}.map { it.id }
            )

           val result =  repository.addMovie(request)

            resultChannel.send(result)

            stateMovie = MovieState().copy(isLoading = false)

            stateFilterMovie = FilterMovieState()

            getAllGenre()
        }
    }

    private fun updateMovie(){
        viewModelScope.launch {
            stateMovie = stateMovie.copy(isLoading = true)

            val request = MovieUpdateRequest(
                id = stateMovie.idMovie,
                nameMovie = stateMovie.nameMovie,
                duration = stateMovie.duration,
                description = stateMovie.description,
                director = stateMovie.director,
                linkImage = stateMovie.linkImage,
                ageRating = stateFilterMovie.selectedAgeRating.toString() + "+",
                yearOfIssue = stateFilterMovie.selectedYearIssue.toString(),
                listGenre = stateFilterMovie.selectedGenreList.filter{it.isChoice}.map { it.id }
            )

            val result = repository.updateMovie(request)

            resultChannel.send(result)

            stateMovie = stateMovie.copy(isLoading = false)
        }
    }

    private fun deleteMovie(){
        viewModelScope.launch {
            stateMovie = stateMovie.copy(isLoading = true)

            val result = repository.deleteMovie(MovieDeleteRequest(stateMovie.idMovie))

            resultChannel.send(result)

            stateMovie = stateMovie.copy(isLoading = false)
        }
    }

    private fun getAllGenre(){
        viewModelScope.launch {
            stateFilterMovie = stateFilterMovie.copy(isLoading = true)

            val result = repository.getAllGenre()

            if(result.data != null){

                if(stateFilterMovie.selectedGenreList.isNotEmpty()){
                    stateFilterMovie.selectedGenreList.clear()
                }

                val resultListGenre : MutableList<GenresState> = mutableListOf()

                result.data.forEach {
                    resultListGenre.add(GenresState(id = it.id, nameGenre = it.nameGenre))
                }

                stateFilterMovie = stateFilterMovie.copy(
                    selectedGenreList = resultListGenre
                )

                resultChannel.send(MovieResult.OK())

            }else{
                resultChannel.send(MovieResult.UnknownError())
            }

            stateFilterMovie = stateFilterMovie.copy(isLoading = false)
        }
    }
}