# Movienfo
<h2>Movienfo is partly implemented movie database application.</h2>

<h3>Things you can do:</h3>

<ul style="list-style-type:disc;">
  <li>See watchlist and list of favourite trending, most popular, top rated and now playing movies. 
  <br>
  Lists are represented as a few horizontal scroll bar lists.
  <br>
  - Movies are showed as posters below which relase year is written.
  <br>
  <li>See movie details, which include:</li>
  <br> 
  - Movie title
  <br> 
  - Release date
  <br>
  - Rating(TMDB + Filled star shapes)
  <br>
  - Short overview in separate or same activity depending on the width of screen size
  <br>
  - Movie poster
  <br>
  - Backdrop movie image
  <br>
  <li>Search for a specific movie: </li>
  <br> 
  - Input movie title in custom made action bar
  <br>
  <li>Add/Remove movie to/from the list of personal favourites</li>
  <br>
  <li>Add/Remove movie to/from the watchlist</li>
  <br>
  <li>See list of 10 most recent movies</li>
   <br>
  <li>Find exact location of theaters that are in 10 mile radius<li>
</ul>

<h3>Techniques used while implementing application:</h3>

<ul style="list-style-type:disc;">
  <li>Manipulating data gathered from TMDB(https://www.themoviedb.org/documentation/api) API</li>
  <li>Storing data in Firebase realtime database</li>
  <li>Storing data in SQLLite(Room lib) database</li>
  <li>Using fragments for displaying movie details differently depending on the width of screen size</li>
  <li>Fetching users current location and displaying that location on google maps fragment(Google Maps API)</li>
  <li>Fetching location of nearest theaters(Google Places API)</li>
</ul>

<h3>Possible future upgrades:</h3>
<br>
<ul style="list-style-type:disc;">
  <li>Displaying trailers</li>
  <br>
  <li>Adding more detail about movies such as information about actors, writers etc.</li>
  <br>
  <li>Enable user to rate specific movie</li>
  <br>
  <li>Enable user to comment</li>
  <br>
  <li>Displaying theaters in yout neighbourhood where movie is currently showing</li>
</ul>
<br>
