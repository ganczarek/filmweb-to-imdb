package info.ganczarek.filmweb

import java.util
import java.util.Collections

import info.ganczarek.model.MovieRate
import info.talacha.filmweb.api.FilmwebApi
import info.talacha.filmweb.models.{Film, ItemType, User, Vote}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class FilmwebClientTest extends FlatSpec with MockitoSugar with Matchers {

  trait ClientWithMockedFilmwebApi {
    val filmwebApi: FilmwebApi = mock[FilmwebApi]
    val userMock: User = mock[User]
    val filmwebClient: FilmwebClient = new FilmwebClient("testLogin", "testPassword", filmwebApi)
    val userId = 1234L
    when(userMock.getId).thenReturn(userId)
    when(filmwebApi.login("testLogin", "testPassword")).thenReturn(userMock)
  }

  behavior of "Filmweb client"

  it should "throw exception when incorrect login or password" in {
    val filmwebApi: FilmwebApi = mock[FilmwebApi]
    when(filmwebApi.login("testLogin", "incorrectPass")).thenThrow(new RuntimeException("mocked incorrect password exception"))
    val filmwebClient: FilmwebClient = new FilmwebClient("testLogin", "incorrectPass", filmwebApi)

    intercept[RuntimeException] { filmwebClient.userMovieRates() }
  }

  it should "return empty movie rate sequence, when no votes" in new ClientWithMockedFilmwebApi {
    when(filmwebApi.getUserVotes(org.mockito.Matchers.eq(userId), anyInt(), anyInt())).thenReturn(Collections.emptyList[Vote]())

    filmwebClient.userMovieRates() shouldBe Seq()
  }

  it should "ignore votes with type different than a movie" in new ClientWithMockedFilmwebApi {
    val votes: util.List[Vote] = Seq(vote(ItemType.GAME, 1L, 5), vote(ItemType.SERIES, 2L, 7)).asJava
    when(filmwebApi.getUserVotes(org.mockito.Matchers.eq(userId), anyInt(), anyInt())).thenReturn(votes)

    filmwebClient.userMovieRates() shouldBe Seq()
  }

  it should "return movie rates for film votes from Filmweb" in new ClientWithMockedFilmwebApi {
    private val votes = Seq(vote(ItemType.FILM, 1L, 10), vote(ItemType.FILM, 2L, 3)).asJava
    when(filmwebApi.getUserVotes(org.mockito.Matchers.eq(userId), anyInt(), anyInt())).thenReturn(votes)
    private val filmData1 = film("movie 1", 2001)
    when(filmwebApi.getFilmData(1L)).thenReturn(filmData1)
    private val filmData2 = film("movie 2", 2002)
    when(filmwebApi.getFilmData(2L)).thenReturn(filmData2)

    filmwebClient.userMovieRates() should contain theSameElementsAs Seq(MovieRate("movie 1", 2001, 10), MovieRate("movie 2", 2002, 3))
  }

  it should "it should return movie rate with Polish title when no Englishther title" in new ClientWithMockedFilmwebApi {
    private val votes = Seq(vote(ItemType.FILM, 1L, 10)).asJava
    when(filmwebApi.getUserVotes(org.mockito.Matchers.eq(userId), anyInt(), anyInt())).thenReturn(votes)
    private val filmData = film(null, "Polish title", 2001)
    when(filmwebApi.getFilmData(1L)).thenReturn(filmData)

    filmwebClient.userMovieRates() should contain theSameElementsAs Seq(MovieRate("Polish title", 2001, 10))
  }

  it should "it should return movie rate with English title when both Polish and English titles available" in new ClientWithMockedFilmwebApi {
    private val votes = Seq(vote(ItemType.FILM, 1L, 10)).asJava
    when(filmwebApi.getUserVotes(org.mockito.Matchers.eq(userId), anyInt(), anyInt())).thenReturn(votes)
    private val filmData = film("English title", "Polish title", 2001)
    when(filmwebApi.getFilmData(1L)).thenReturn(filmData)

    filmwebClient.userMovieRates() should contain theSameElementsAs Seq(MovieRate("English title", 2001, 10))
  }

  private def vote(itemType: ItemType, itemId: Long, rate: Int): Vote = {
    val vote = mock[Vote]
    when(vote.getItemId).thenReturn(itemId)
    when(vote.getType).thenReturn(itemType)
    when(vote.getRate).thenReturn(rate)
    vote
  }

  private def film(title: String, year: Int): Film = {
    film(title, null, year)
  }

  private def film(title: String, polishTitle: String, year: Int): Film = {
    val film = mock[Film]
    when(film.getYear).thenReturn(year)
    when(film.getTitle).thenReturn(title)
    when(film.getPolishTitle).thenReturn(polishTitle)
    film
  }

}
