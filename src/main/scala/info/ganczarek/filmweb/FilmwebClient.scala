package info.ganczarek.filmweb

import info.ganczarek.model.{ItemRate, MovieRate, SeriesRate}
import info.talacha.filmweb.api.FilmwebApi
import info.talacha.filmweb.models.{Film, ItemType, Vote}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object FilmwebClient {

  val VOTE_REQUEST_LIMIT = 10000
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(login: String, password: String) = new FilmwebClient(login, password, new FilmwebApi())

}

class FilmwebClient(login: String, password: String, fa: FilmwebApi) {
  import FilmwebClient._

  def userRates(): Iterator[ItemRate]  = {
    logger.info("Get from filmweb.pl movie ratings of user {}", login)
    val user = fa.login(login, password)
    // It seems that pagination doesn't work. Therefore, use page 0 and large limit. It worked fine for >800 votes.
    val votes = fa.getUserVotes(user.getId, 0, VOTE_REQUEST_LIMIT).asScala
    logger.info("Found {} Filmweb votes", votes.size)
    votes.toIterator.flatMap(convertToItemRate)
  }

  private def convertToItemRate(vote: Vote): Option[ItemRate] = {
    vote.getType match {
      case ItemType.FILM =>
        val filmData = fa.getFilmData(vote.getItemId)
        Some(MovieRate(getFilmwebTitle(filmData), filmData.getYear, vote.getRate))
      case ItemType.SERIES =>
        val seriesData = fa.getSeriesData(vote.getItemId)
        Some(SeriesRate(getFilmwebTitle(seriesData), seriesData.getYear, vote.getRate))
      case _ => None
    }
  }

  private def getFilmwebTitle(film: Film) = Option(film.getTitle).getOrElse(film.getPolishTitle)
}
