package info.ganczarek.imdb

import com.omertron.omdbapi.OmdbApi
import com.omertron.omdbapi.model.{OmdbVideoBasic, SearchResults}
import info.ganczarek.model.MovieRate
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class ImdbClientTest extends FlatSpec with MockitoSugar with Matchers {

  behavior of "IMDB client"

  trait ClientWithMockedOmdbApi {
    val omdbApi: OmdbApi = mock[OmdbApi]
    val tmdbClient: TmdbClient = mock[TmdbClient]
    val cookieId = "testCookieId"
    val imdbClient = new ImdbClient(cookieId, omdbApi, tmdbClient)
  }

  it should "return IMDB id for a movie that exists in IMDB database" in new ClientWithMockedOmdbApi {
    private val movieRate = MovieRate("existing movie", 2016, 1)
    private val imdbId = "imdbId-1"
    private val omdbMovieData = omdbVideoBasic(imdbId)
    private val omdbSearchResults = searchResults(omdbMovieData)
    when(omdbApi.search(movieRate.title, movieRate.year)).thenReturn(omdbSearchResults)

    imdbClient.getImdbIdForMovieRate(movieRate) shouldBe Some(imdbId)
  }

  it should "return None for a movie that does not exist in IMDB database" in new ClientWithMockedOmdbApi {
    private val movieRate = MovieRate("existing movie", 2016, 1)
    private val omdbSearchResults = searchResults()
    when(omdbApi.search(movieRate.title, movieRate.year)).thenReturn(omdbSearchResults)
    when(tmdbClient.getImdbIdFromTmdb(movieRate)).thenReturn(None)

    imdbClient.getImdbIdForMovieRate(movieRate) shouldBe None
  }

  it should "return IMDB id for a first movie that was found in IMDB database" in new ClientWithMockedOmdbApi {
    private val movieRate = MovieRate("existing movie", 2016, 1)
    private val imdbId = "imdbId-1"
    private val omdbMovieData1 = omdbVideoBasic(imdbId)
    private val omdbMovieData2 = omdbVideoBasic("imdbId-2")
    private val omdbSearchResults = searchResults(omdbMovieData1, omdbMovieData2)
    when(omdbApi.search(movieRate.title, movieRate.year)).thenReturn(omdbSearchResults)

    imdbClient.getImdbIdForMovieRate(movieRate) shouldBe Some(imdbId)
  }

  it should "fallback to TMDb, if cannot find IMDB id with OMDb" in new ClientWithMockedOmdbApi {
    private val movieRate = MovieRate("existing movie", 2016, 1)
    private val imdbId = "imdbId-1"
    private val omdbSearchResults = searchResults()
    when(omdbApi.search(movieRate.title, movieRate.year)).thenReturn(omdbSearchResults)
    when(tmdbClient.getImdbIdFromTmdb(movieRate)).thenReturn(Some(imdbId))

    imdbClient.getImdbIdForMovieRate(movieRate) shouldBe Some(imdbId)
  }

  private def searchResults(omdbVideoBasic: OmdbVideoBasic*) = {
    val searchResults = mock[SearchResults]
    when(searchResults.getResults).thenReturn(omdbVideoBasic.toList.asJava)
    when(searchResults.getTotalResults).thenReturn(omdbVideoBasic.size)
    searchResults
  }

  private def omdbVideoBasic(imdbId: String) = {
    val omdbVideoBasic = mock[OmdbVideoBasic]
    when(omdbVideoBasic.getImdbID).thenReturn(imdbId)
    omdbVideoBasic
  }
}
