package info.ganczarek.imdb

import com.uwetrottmann.tmdb2.Tmdb
import info.ganczarek.model.{ItemRate, MovieRate, SeriesRate}

import scala.collection.JavaConverters._

object TmdbClient {

  def apply(tmdbApiKey: String): TmdbClient = new TmdbClient(new Tmdb(tmdbApiKey))

}

class TmdbClient(tmdb: Tmdb) {

  def getImdbIdFromTmdb(itemRate: ItemRate): Option[String] = {
    (itemRate match {
      case movieRate: MovieRate => getImdbIdForMovie(movieRate)
      case seriesRate: SeriesRate => getImdbIdForSeries(seriesRate)
    }).filter(_.startsWith("tt"))
  }

  private def getImdbIdForMovie(movieRate: MovieRate): Option[String] = {
    tmdb.searchService().movie(movieRate.title, null, null, null, movieRate.year, null, null)
      .execute().body().results.asScala
      .headOption
      .flatMap(movie => Some(tmdb.moviesService().summary(movie.id, null, null).execute().body()))
      .map(_.imdb_id)
  }

  private def getImdbIdForSeries(seriesRate: SeriesRate): Option[String] = {
    tmdb.searchService().tv(seriesRate.title, null, null, seriesRate.year, null)
      .execute().body().results.asScala
      .headOption
      .flatMap(series => Some(tmdb.tvService().externalIds(series.id, null).execute().body()))
      .map(_.imdb_id)
  }
}
