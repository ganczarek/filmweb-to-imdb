package info.ganczarek

import info.ganczarek.filmweb.FilmwebClient
import info.ganczarek.imdb.ImdbClient

object CopyFilmwebRatesToImdbApp {

  def main(args: Array[String]): Unit = {
    val tmdbApiKey = args(0)
    val imdbCookieId = args(1)
    val filmwebLogin = args(2)
    val filmwebPassword = args(3)
    val omdbApiKey = args(4)

    val filmwebClient = FilmwebClient(filmwebLogin, filmwebPassword)
    val imdbClient = ImdbClient(imdbCookieId, tmdbApiKey, omdbApiKey)

    val notFoundMovieRates = imdbClient.submitMovieRates(filmwebClient.userRates()).toList
    println("Movies that could not be found in IMDb database:")
    notFoundMovieRates.foreach(println)
  }

}
