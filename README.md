# Movienfo
<h2>Movienfo is partly implemented movie database application.</h2>

<h3>Things you can do:</h3>
<ul style="list-style-type:disc;">
  <li>- See watchlist and list of favourite trending, most popular, top rated and now playing movies. 
  <br>
  - Lists are represented as a few horizontal scroll bar lists.
  <br>
  - Movies are showed as posters below which relase year is written.
    <br><li>
  - See movie details, which include:<br></li> - Movie title<br> - Release date<br>- Rating(TMDB + Filled star shapes)
  <br>
  <li>- Short overview in separate or same activity depending on the width of screen size</li>
  <br>
  <li>- Movie poster
  <br>
  <li> - Backdrop movie image</li>
  <br>
  <li>- Search for a specific movie: 
  <br> 
  <li>- Input movie title in custom made action bar</li>
  <br>
  <li>- Add/Remove movie to/from the list of personal favourites
  <br>
  <li>- Add/Remove movie to/from the watchlist
  <br>
  <li>- See list of 10 most recent movies
   <br>
  <li>- Find exact location of theaters that are in 10 mile radius
</ul>

<h3>Techniques used while implementing application:</h3>
- Manipulating data gathered from TMDB(https://www.themoviedb.org/documentation/api) API
- Storing data in Firebase realtime database
- Storing data in SQLLite(Room lib) database
- Using fragments for displaying movie details differently depending on the width of screen size
- Fetching users current location and displaying that location on google maps fragment(Google Maps API)
- Fetching location of nearest theaters(Google Places API)

<h3>Possible future upgrades:</h3><br>
-Displaying trailers<br>
-Adding more detail about movies such as information about actors, writers etc.<br>
-Enable user to rate specific movie<br>
-Enable user to comment<br>
-Displaying theaters in yout neighbourhood where movie is currently showing<br>
