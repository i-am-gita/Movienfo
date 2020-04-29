# Movienfo
Movienfo is partly implemented movie database application.

Things you can do:
- See watchlist and list of favourite trending, most popular, top rated and now playing movies. <br> Lists are represented as a few horizontal scroll bar lists. <br>Movies are showed as posters below which relase year is written.                      
- See movie details, which include:<br> -Movie title<br> -Release date<br>-Rating(TMDB + Filled star shapes)<br>-Short overview<br>-Movie poster<br>-Backdrop movie image in separate or same activity depending on the width of screen size
- Search for a specific movie: <br> - Input movie title in custom made action bar
- Add/Remove movie to/from the list of personal favourites
- Add/Remove movie to/from the watchlist           
- See list of 10 most recent movies               
- Find exact location of theaters that are in 10 mile radius

Techniques used while implementing application:
- Manipulating data gathered from TMDB(https://www.themoviedb.org/documentation/api) API
- Storing data in Firebase realtime database
- Storing data in SQLLite(Room lib) database
- Using fragments for displaying movie details differently depending on the width of screen size
- Fetching users current location and displaying that location on google maps fragment(Google Maps API)
- Fetching location of nearest theaters(Google Places API)

Possible future upgrades:
-Displaying trailers
-Adding more detail about movies such as information about actors, writers etc.
-Enable user to rate specific movie
-Enable user to comment
-Displaying theaters in yout neighbourhood where movie is currently showing
