package info.ganczarek.filmweb

import info.ganczarek.model.MovieRate
import info.talacha.filmweb.api.FilmwebApi
import info.talacha.filmweb.models.{Film, ItemType, Vote}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object FilmwebClient {

  val VOTE_REQUEST_LIMIT = 10000
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

}

class FilmwebClient(login: String, password: String, fa: FilmwebApi) {
  import FilmwebClient._

  def userMovieRates(): Seq[MovieRate]  = {
    logger.info("Get user {} movie ratings from filmweb.pl service", login)
    val user = fa.login(login, password)
    // It seems that pagination doesn't work. Therefore, use page 0 and large limit. It worked fine for >800 votes.
    val votes = fa.getUserVotes(user.getId, 0, VOTE_REQUEST_LIMIT).asScala
    logger.debug("Found {} Filmweb votes", votes.size)
    val movieRates = votes.flatMap(convertToMovieRate)
    logger.info("Found {} Filmweb movie rate", movieRates.size)
    movieRates
  }

  private def convertToMovieRate(vote: Vote): Option[MovieRate] = {
    vote.getType match {
      case ItemType.FILM =>
        val filmData = fa.getFilmData(vote.getItemId)
        Some(MovieRate(getMovieTitle(filmData), filmData.getYear, vote.getRate))
      case _ => None
    }
  }

  private def getMovieTitle(film: Film) = Option(film.getTitle).getOrElse(film.getPolishTitle)
}
