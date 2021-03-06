package pmf.android.movienfo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.BuildConfig;
import pmf.android.movienfo.R;
import pmf.android.movienfo.adapters.MovieAdapter;
import pmf.android.movienfo.model.Movie;
import pmf.android.movienfo.model.Theater;
import pmf.android.movienfo.utilities.Firebase;
import pmf.android.movienfo.utilities.MovienfoRoomDatabase;
import pmf.android.movienfo.utilities.NetworkUtils;

import static pmf.android.movienfo.utilities.MovienfoUtilities.getBitmapFromVectorDrawable;

public class HomeActivity extends AppCompatActivity implements MovieInListChecker, ActionBarInitializer, MovieAdapter.OnItemClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener, MovieListInitializer {

    private static final String TAG = HomeActivity.class.getSimpleName();

    String apiKey = BuildConfig.API_KEY;

    //API call URLs
    final String upcomingMoviesURL = "https://api.themoviedb.org/3/movie/upcoming?api_key=" + apiKey + "&language=en-US";
    final String nowPlayingMoviesURL = "https://api.themoviedb.org/3/movie/now_playing?api_key=" + apiKey + "&language=en-US";
    final String topRatedURL = "https://api.themoviedb.org/3/movie/top_rated?api_key=" + apiKey + "&language=en-US";
    final String mostPopularURL = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + "&language=en-US";

    //Layout elements
    @BindView(R.id.now_playing_movies_recyclerView)
    RecyclerView nowPlayingRecycle;
    @BindView(R.id.upcoming_movies_recyclerView)
    RecyclerView upcomingRecycle;
    @BindView(R.id.popular_movies_recyclerView)
    RecyclerView mostPopularRecycle;
    @BindView(R.id.top_movies_recyclerView)
    RecyclerView topRatedRecycle;
    @BindView(R.id.find_theaters_button)
    ImageButton theatersButton;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private Toolbar toolbar;

    //Adapters for recycle views
    private MovieAdapter upcomingMoviesAdapter;
    private MovieAdapter nowPlayingMoviesAdapter;
    private MovieAdapter topRatedMoviesAdapter;
    private MovieAdapter popularMoviesAdapter;

    //Popular, Top rated, Upcoming, Now playing movies API responses
    HashMap<String, List<Movie>> fetchedMovies;

    //For searching movies based on user search query
    String movieQuery;
    List<Movie> searchResults;

    //Elements for displaying map with current location, and nearest theaters location markers
    private int PERMISSION_ID = 44;
    private double lat,lon;
    private GoogleMap mMap;
    FusedLocationProviderClient mFusedLocationClient;
    SupportMapFragment mapFragment;
    List<Theater> theaters;

    //List that are fetched from firebase and then sent to other activities in order for content to be consistent
    private List<Movie> userFavorites;
    private List<Movie> userWatchlist;

    //Room database data for last 10 movies seen by user
    private List<Movie> recentMovies;

    @SuppressLint({"ResourceType", "ClickableViewAccessibility"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);

        //Custom action bar
        setActionBarElements(Objects.requireNonNull(getSupportActionBar()));

        //Fetching movies from TMDB API
        getMoviesFromApi(savedInstanceState);

        //Populating recycles with data fetched from TMDB API
        initializeMoviesList();

        //Google map initialization
        initializeGoogleMap();
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setActionBarElements(ActionBar customActionBar){
        customActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        customActionBar.setDisplayShowCustomEnabled(true);
        customActionBar.setCustomView(R.layout.action_bar_custom);
        customActionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        View view = Objects.requireNonNull(customActionBar.getCustomView());

        toolbar = view.findViewById(R.id.home_toolbar);
        ImageButton searchIcon = view.findViewById(R.id.search_bar_hint_icon);
        ImageView homeIcon = view.findViewById(R.id.icon_home);
        EditText searchField = view.findViewById(R.id.search_bar_edit_text);
        searchField.setVisibility(View.GONE);

        homeIcon.setOnClickListener(v -> {
            Intent homeIntent = new Intent(v.getContext(), HomeActivity.class);
            startActivity(homeIntent);
        });

        searchIcon.setOnClickListener(v -> {
            searchField.setVisibility(View.VISIBLE);
            searchField.requestFocus();
        });

        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if(!isOpen){
                searchIcon.setVisibility(View.VISIBLE);
                searchField.clearFocus();
                homeIcon.setVisibility(View.VISIBLE);
            }
        });
        //When keyboard is closed(if user pressed return button on keyboard) action bar elements visibility changes / When keyboard is opened, edit text gets focus and other elements on action bar are being disabled
        searchField.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                homeIcon.setVisibility(View.GONE);
                searchIcon.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT);
                }
            }else{
                searchField.setVisibility(View.GONE);
                searchIcon.setVisibility(View.VISIBLE);
            }
        });

        //Fetching movies for query user provided
        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_DONE){
                String userInput = searchField.getText().toString();
                if(userInput.equals("")) {
                    Toast.makeText(getApplicationContext() , "You need to type at lease one letter in order to see movie results", Toast.LENGTH_SHORT).show();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    homeIcon.setVisibility(View.VISIBLE);
                    searchIcon.setVisibility(View.VISIBLE);
                    searchField.setVisibility(View.GONE);
                }
                else{
                    if(NetworkUtils.networkStatus(HomeActivity.this))
                    {
                        new MovieSearch().execute();
                        movieQuery = searchField.getText().toString();
                    }
                    else{
                        AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                        dialog.setTitle(getString(R.string.title_network_alert));
                        dialog.setMessage(getString(R.string.message_network_alert));
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                }
            }
            return false;
        });

        toolbar.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){
                case R.id.watchlist:
                    Intent watchlistIntent = new Intent(view.getContext(), MovieListActivity.class);
                    watchlistIntent.putParcelableArrayListExtra("userFavs", new ArrayList<>(userFavorites));
                    watchlistIntent.putParcelableArrayListExtra("userWatch", new ArrayList<>(userWatchlist));
                    watchlistIntent.putParcelableArrayListExtra("recent", new ArrayList<>(recentMovies));
                    watchlistIntent.putExtra("stringData","watchlist");
                    startActivity(watchlistIntent);
                    return false;

                case R.id.favourites:
                    Intent favIntent = new Intent(view.getContext(), MovieListActivity.class);
                    favIntent.putParcelableArrayListExtra("userFavs", new ArrayList<>(userFavorites));
                    favIntent.putParcelableArrayListExtra("userWatch", new ArrayList<>(userWatchlist));
                    favIntent.putParcelableArrayListExtra("recent", new ArrayList<>(recentMovies));
                    favIntent.putExtra("stringData","favourites");
                    startActivity(favIntent);
                    return true;

                case R.id.recent:
                    Intent recIntent = new Intent(view.getContext(), MovieListActivity.class);
                    recIntent.putParcelableArrayListExtra("userFavs", new ArrayList<>(userFavorites));
                    recIntent.putParcelableArrayListExtra("userWatch", new ArrayList<>(userWatchlist));
                    recIntent.putExtra("stringData","recent");
                    startActivity(recIntent);
                    return true;

                default:
                    return true;
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getMoviesFromApi(Bundle savedInstanceState){
        progressBar.setVisibility(View.GONE);
        if(savedInstanceState == null){
            if(NetworkUtils.networkStatus(HomeActivity.this))
                new FetchMovies().execute();
            else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                dialog.setTitle(getString(R.string.title_network_alert));
                dialog.setMessage(getString(R.string.message_network_alert));
                dialog.setCancelable(false);
                dialog.show();
            }
        }
    }

    @Override
    public void initializeMoviesList() {
        nowPlayingRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        upcomingRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        topRatedRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mostPopularRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        popularMoviesAdapter = new MovieAdapter(this, Collections.emptyList(), R.layout.item_movie_home);
        popularMoviesAdapter.setOnItemClickListener(this);
        topRatedMoviesAdapter = new MovieAdapter(this, Collections.emptyList(), R.layout.item_movie_home);
        topRatedMoviesAdapter.setOnItemClickListener(this);
        nowPlayingMoviesAdapter = new MovieAdapter(this, Collections.emptyList(), R.layout.item_movie_home);
        nowPlayingMoviesAdapter.setOnItemClickListener(this);
        upcomingMoviesAdapter = new MovieAdapter(this, Collections.emptyList(), R.layout.item_movie_home);
        upcomingMoviesAdapter.setOnItemClickListener(this);

        upcomingRecycle.setAdapter(upcomingMoviesAdapter);
        nowPlayingRecycle.setAdapter(nowPlayingMoviesAdapter);
        topRatedRecycle.setAdapter(topRatedMoviesAdapter);
        mostPopularRecycle.setAdapter(popularMoviesAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar.inflateMenu(R.menu.menu_home);
        Bitmap menuBitmap = getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.icon_menu);
        Drawable menuIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(menuBitmap,toolbar.getWidth()/13,toolbar.getHeight()/2 ,true));
        toolbar.setOverflowIcon(menuIcon);
        return true;
    }


    public void onResume() {
        super.onResume();
        getFirebaseData(Firebase.getInstance().getReference());
        recentMovies = MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().getAll();
    }

    public void getFirebaseData(DatabaseReference reference){
        userFavorites = new ArrayList<>();
        reference.child("favorites").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                userFavorites.add(dataSnapshot.getValue(Movie.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                userFavorites.remove(dataSnapshot.getValue(Movie.class));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //Fetching watchlist from Firebase
        userWatchlist = new ArrayList<>();
        reference.child("watchlist").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                userWatchlist.add(dataSnapshot.getValue(Movie.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                userWatchlist.remove(dataSnapshot.getValue(Movie.class));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addToRoomDatabase(Movie movie){
        if(!recentMovies.contains(movie)){
            if(recentMovies.size() < 10){
                recentMovies.add(movie);
                MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().insert(movie);
            }else{
                MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().delete(recentMovies.get(0));
                recentMovies.remove(recentMovies.get(0));

                MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().insert(movie);
                recentMovies.add(movie);
            }
        }else{
            Log.w(TAG, "Error adding recent movie!");
            Toast.makeText(getApplicationContext() , movie + " has been recently seen so it is not added to the recent list",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void sendDetails(Movie movie, int position) {
        if(!listContainsMovie(movie, new ArrayList<>(recentMovies)))
            addToRoomDatabase(movie);

        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("selectedMovie", movie);
        intent.putParcelableArrayListExtra("favourites", new ArrayList<>(userFavorites));
        intent.putParcelableArrayListExtra("watchlist", new ArrayList<>(userWatchlist));
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initializeGoogleMap(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();

        theatersButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(v.getContext());
            getLastLocation();

            if(NetworkUtils.networkStatus(HomeActivity.this))
                new FetchTheatres().execute();
            else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                dialog.setTitle(getString(R.string.title_network_alert));
                dialog.setMessage(getString(R.string.message_network_alert));
                dialog.setCancelable(false);
                dialog.show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(marker -> false);
    }

    @Override
    public void onCameraMove() {
        findViewById(R.id.map).getParent().requestDisallowInterceptTouchEvent(true);
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        task -> {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();
        }
    };

    private boolean checkPermissions(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public boolean listContainsMovie(Movie movie, ArrayList<Movie> list) {
        for(Movie m : list){
            if(m.getId().toString().equals(movie.getId().toString()))
                return true;
        }
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    public class MovieSearch extends AsyncTask<Void, Void, Void>{

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

            final String movieSearchURL = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&language=en-US&query="+ movieQuery +"&page=1&include_adult=false";
            searchResults = new ArrayList<>();
            if(NetworkUtils.networkStatus(HomeActivity.this)){
                searchResults = NetworkUtils.fetchData(movieSearchURL);
            }else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                dialog.setTitle(getString(R.string.title_network_alert));
                dialog.setMessage(getString(R.string.message_network_alert));
                dialog.setCancelable(false);
                dialog.show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void  s) {
            super.onPostExecute(s);
            Intent searchIntent = new Intent(HomeActivity.this, MovieListActivity.class);
            searchIntent.putParcelableArrayListExtra("searchResults", new ArrayList<>(searchResults));
            searchIntent.putParcelableArrayListExtra("userFavs", new ArrayList<>(userFavorites));
            searchIntent.putParcelableArrayListExtra("userWatch", new ArrayList<>(userWatchlist));
            searchIntent.putParcelableArrayListExtra("recent", new ArrayList<>(recentMovies));
            searchIntent.putExtra("stringData", movieQuery);
            startActivity(searchIntent);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class FetchTheatres extends AsyncTask<Void, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

            final String nearbyTheatresURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lon + "&radius=10000&type=movie_theater&key=" + BuildConfig.PLACES_API_KEY;

            theaters = new ArrayList<>();

            if(NetworkUtils.networkStatus(HomeActivity.this)){
                theaters = NetworkUtils.fetchDataTheaters(nearbyTheatresURL);
            }else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                dialog.setTitle(getString(R.string.title_network_alert));
                dialog.setMessage(getString(R.string.message_network_alert));
                dialog.setCancelable(false);
                dialog.show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void  s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat ,lon)).title("Your location"));
            for (Theater th : theaters) {
                  mMap.addMarker(new MarkerOptions().position(new LatLng(th.getLat(),th.getLon())).title(th.getName()));
              }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), 14));
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class FetchMovies extends AsyncTask<Void, Void, Void>{

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

           fetchedMovies = new HashMap<>();

            if(NetworkUtils.networkStatus(HomeActivity.this)){
                fetchedMovies.put("upcoming", NetworkUtils.fetchData(upcomingMoviesURL));
                fetchedMovies.put("now_playing", NetworkUtils.fetchData(nowPlayingMoviesURL));
                fetchedMovies.put("top_rated", NetworkUtils.fetchData(topRatedURL));
                fetchedMovies.put("popular", NetworkUtils.fetchData(mostPopularURL));
            }else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                dialog.setTitle(getString(R.string.title_network_alert));
                dialog.setMessage(getString(R.string.message_network_alert));
                dialog.setCancelable(false);
                dialog.show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void  s) {
            super.onPostExecute(s);

            upcomingMoviesAdapter.updateMoviesList(fetchedMovies.get("upcoming"));
            nowPlayingMoviesAdapter.updateMoviesList(fetchedMovies.get("now_playing"));
            topRatedMoviesAdapter.updateMoviesList(fetchedMovies.get("top_rated"));
            popularMoviesAdapter.updateMoviesList(fetchedMovies.get("popular"));
        }
    }
}


