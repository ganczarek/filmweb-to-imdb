filmweb-to-imdb
===============

'filmweb-to-imdb' is simple (and hacky) program to copy user's movie ratings from [filmweb.pl](http://www.filmweb.pl/) 
to [IMDb](http://www.imdb.com/). It uses private Filmweb and IMDb APIs, so your millage my vary. It worked fine in mid 
of January 2017.

This program uses Paweł Talacha's [Filmweb API](https://bitbucket.org/varabi/filmweb-api) to read user rates of movies 
and TV series from Filmweb account. For each item it searches by title and year for movie's IMDb ID with use of [OMDb API](http://www.omdbapi.com/). 
Since OMDb API searches for exact title match and there are slight differences between titles in Filmweb and IMDb databases, 
the second search in [TMDb](https://www.themoviedb.org/) is performed in case OMDb API returns nothing. TMDb's search service
handles better slight variations of the same titles.

Once IMDb ID of the movie is found, then movie rating is set in IMDb with use of private API. IMDb expects rates ranging
from 1 to 10. Therefore, Filmweb's 0 rates (watched, but not rated) are being ignored. 

In the end program will list all title ratings from Filmweb for which IMDb IDs could not be found. 

Build and run
-------------

Build
    
    sbt assembly
      
and run
      
    java -jar target/scala-2.12/filmweb-to-imdb-assembly-*.jar $TMDB_API_KEY $IMDB_COOKIE_ID $FILMWEB_LOGIN $FILMWEB_PASSWORD $OMDB_API_KEY

Requirements
------------
- [Filmweb.pl](http://www.filmweb.pl/) account credentials. Logging with Facebook or Google+ accounts is not supported.
- [IMDb](http://www.imdb.com/) session cookie id. Log into IMDb with your browser and get assigned session cookied id.
- [TMDb](https://www.themoviedb.org/) API key. Read [FAQ](https://www.themoviedb.org/faq/api) to see how to apply for a key.
- [OMDb](https://omdbapi.com/) API key. Generate [here](http://omdbapi.com/apikey.aspx).

Limitations
-----------
- Works only in one direction, i.e., it doesn't copy ratings from IMDb to Filmweb
- Overwrites existing movie ratings in IMDb account with ratings from Filmweb
- Copies only movie and TV series ratings

Known Issues
------------
- Release years can differ between Filmweb and IMDb databases; especially for Polish productions. Better heuristic could be
implemented, but currently these cases are ignored and listed as not found in IMDb.
- If a movie has a game with a title same as in Filmweb database, then the game might be rated.  