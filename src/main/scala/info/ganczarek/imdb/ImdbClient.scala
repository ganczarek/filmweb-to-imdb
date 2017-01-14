package info.ganczarek.imdb

import java.net.HttpCookie

import com.omertron.omdbapi.OmdbApi
import info.ganczarek.model.MovieRate
import org.slf4j.{Logger, LoggerFactory}

import scalaj.http.{Http, HttpResponse}
import scala.collection.JavaConverters._

object ImdbClient {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(cookieId: String): ImdbClient = new ImdbClient(cookieId, new OmdbApi())

}

class ImdbClient(cookieId: String, omdbApi: OmdbApi) {

  import ImdbClient._

  private val authToken = getAuthToken(cookieId)

  def submitMovieRates(movieRates: Seq[MovieRate]): Seq[MovieRate] = {
    val movieRatesWithImdbId = movieRates
      .map(movieRate => (getImdbIdForMovieRate(movieRate), movieRate))

    movieRatesWithImdbId.flatMap {
        case (Some(imdbId), movieRate) => Some((imdbId, movieRate))
        case (None, movieRate) => logger.warn("Failed to find IMDB ID for {}", movieRate); None
      }
      .foreach { case (imdbId, movieRate) =>
        submitRating(imdbId, movieRate)
      }

    // return movie rates that were not found in IMDB database
    movieRatesWithImdbId.flatMap {
      case (Some(imdbId), movieRate) => None
      case (None, movieRate) => Some(movieRate)
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
