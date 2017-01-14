package info.ganczarek.imdb

import java.net.HttpCookie

import com.omertron.omdbapi.OmdbApi
import info.ganczarek.model.MovieRate
import org.slf4j.{Logger, LoggerFactory}

import scalaj.http.{Http, HttpResponse}
import scala.collection.JavaConverters._

object ImdbClient {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

}

class ImdbClient(cookieId: String, omdbApi: OmdbApi) {

  import ImdbClient._

  private val authToken = getAuthToken(cookieId)

  def submitMovieRates(movieRates: Seq[MovieRate]): Unit = {
    movieRates
      .flatMap(movieRate => getImdbIdForMovieRate(movieRate) match {
        case Some(imdbId) => Some((imdbId, movieRate))
        case None => logger.warn("Failed to find IMDB ID for {}", movieRate); None
      })
      .foreach { case (imdbId, movieRate) =>
        submitRating(imdbId, movieRate)
      }
  }

  def getImdbIdForMovieRate(movieRate: MovieRate): Option[String] = {
    Option(omdbApi.search(movieRate.title, movieRate.year).getResults).map(_.asScala).getOrElse(List())
      .map(_.getImdbID)
      .headOption
  }

  private def getAuthToken(cookieId: String): String = {
    val response = Http("http://www.imdb.com/title/tt3631112/")
      .cookie(new HttpCookie("id", cookieId))
      .asString
    "data-auth=\"(.*)\" ".r.findFirstMatchIn(response.body).get.group(1)
  }

  private def submitRating(imdbId: String, movieRate: MovieRate): HttpResponse[String] = {
    logger.info("Submit {}", movieRate)
    Http("http://www.imdb.com/ratings/_ajax/title")
      .postForm(Seq(
        "tconst" -> imdbId,
        "rating" -> movieRate.rate.toString,
        "auth" -> authToken,
        "tracking_tag" -> "title-maindetails"
      ))
      .cookie(new HttpCookie("id", cookieId))
      .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
      .header("Accept", "application/json, text/javascript, */*; q=0.01")
      .asString
  }

}
