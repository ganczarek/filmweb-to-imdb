package info.ganczarek.imdb

import java.net.HttpCookie

import com.omertron.omdbapi.OmdbApi
import info.ganczarek.model.MovieRate
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scalaj.http.{Http, HttpResponse}

object ImdbClient {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(imdbCookieId: String, tmdbApiKey: String): ImdbClient = new ImdbClient(imdbCookieId, new OmdbApi(), TmdbClient(tmdbApiKey))

}

class ImdbClient(cookieId: String, omdbApi: OmdbApi, tmdbClient: TmdbClient) {

  import ImdbClient._

  def submitMovieRates(movieRates: Iterator[MovieRate]): Seq[MovieRate] = {
    val movieRatesWithImdbId = movieRates
      .map(movieRate => (getImdbIdForMovieRate(movieRate), movieRate))

    movieRatesWithImdbId.flatMap {
        case (Some(imdbId), movieRate) => submitRating(imdbId, movieRate); None
        case (None, movieRate) => logger.warn("Failed to find IMDB ID for {}", movieRate); Some(movieRate)
      }.toSeq
  }

  def getImdbIdForMovieRate(movieRate: MovieRate): Option[String] = {
    Option(omdbApi.search(movieRate.title, movieRate.year).getResults).map(_.asScala).getOrElse(List())
      .map(_.getImdbID)
      .headOption
      .orElse(tmdbClient.getImdbIdFromTmdb(movieRate))
  }

  private def getAuthToken(cookieId: String, imdbId: String): String = {
    val response = Http(s"http://www.imdb.com/title/$imdbId/")
      .cookie(new HttpCookie("id", cookieId))
      .asString
    "data-auth=\"(.*)\" ".r.findFirstMatchIn(response.body).get.group(1)
  }

  private def submitRating(imdbId: String, movieRate: MovieRate): HttpResponse[String] = {
    logger.info(s"Submit rate for IMDB id: $imdbId. $movieRate")
    val authToken = getAuthToken(cookieId, imdbId)
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
