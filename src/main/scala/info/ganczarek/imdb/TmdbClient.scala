package info.ganczarek.imdb

import com.uwetrottmann.tmdb2.Tmdb
import info.ganczarek.model.MovieRate

import scala.collection.JavaConverters._

object TmdbClient {

  def apply(tmdbApiKey: String): TmdbClient = new TmdbClient(new Tmdb(tmdbApiKey))
}

class TmdbClient(tmdb: Tmdb) {

  def getImdbIdFromTmdb(movieRate: MovieRate): Option[String] = {
    tmdb.searchService().movie(movieRate.title, null, null, null, movieRate.year, null, null)
      .execute().body().results.asScala
      .headOption
      .flatMap(movie => Some(tmdb.moviesService().summary(movie.id, null, null).execute().body()))
      .map(_.imdb_id)
  }

}
