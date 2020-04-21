package pmf.android.movienfo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.BuildConfig;
import pmf.android.movienfo.R;
import pmf.android.movienfo.activities.movie_details.MovieDetailsActivity;
import pmf.android.movienfo.adapters.MovieAdapter;
import pmf.android.movienfo.model.Movie;
import pmf.android.movienfo.model.Theater;
import pmf.android.movienfo.utilities.NetworkUtils;

public class HomeActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener, SearchView.OnQueryTextListener, View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

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
    Button theatersButton;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    //API responses
    HashMap<String, List<Movie>> fetchedMovies;

    //For searching movies based on user input
    String movieQuery;
    List<Movie> searchResults;

    private MovieAdapter upcomingMoviesAdapter;
    private MovieAdapter nowPlayingMoviesAdapter;
    private MovieAdapter topRatedMoviesAdapter;
    private MovieAdapter popularMoviesAdapter;

    //For displaying map with location markers
    private int PERMISSION_ID = 44;
    private double lat,lon;
    private GoogleMap mMap;
    FusedLocationProviderClient mFusedLocationClient;
    SupportMapFragment mapFragment;
    List<Theater> theaters;
    List<Movie> userFavorites;
    List<Movie> userWatchlist;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.logo_render_movienfo);

        ButterKnife.bind(this);

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
        initializeMoviesList();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();

        theatersButton.setOnClickListener(this);

    }

    private void initializeMoviesList() {
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

    public void onResume() {
        super.onResume();
        getFirebaseData(FirebaseDatabase.getInstance().getReference());
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


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        progressBar.setVisibility(View.VISIBLE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        if(NetworkUtils.networkStatus(HomeActivity.this))
            new FetchTheatres().execute();
        else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.title_network_alert));
            dialog.setMessage(getString(R.string.message_network_alert));
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                return false;
            }
        });
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
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
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search");
        searchView.setSubmitButtonEnabled(true);
        searchView.setSelected(true);
        searchView.setOnQueryTextListener(this);

        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if(searchPlate != null) {
            searchPlate.setBackgroundColor(Color.BLACK);
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);

            if(searchText != null) {
                searchText.setTextColor(Color.parseColor("#303F9F"));
                searchText.setHintTextColor(Color.parseColor("#303F9F"));
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        HashMap<Long, Movie> uniqueMovies = new HashMap<>();

        switch (item.getItemId()){
            case R.id.user_pick:
                SubMenu subMenu = item.getSubMenu();
                if(subMenu.getItem().isChecked())
                    return onOptionsItemSelected(subMenu.getItem());
                else
                    return false;

            case R.id.watchlist:
                Intent watchlistIntent = new Intent(this, MovieListActivity.class);
                watchlistIntent.putParcelableArrayListExtra("list", new ArrayList(userWatchlist));
                watchlistIntent.putExtra("stringData","watchlist");
                startActivity(watchlistIntent);
                return false;

            case R.id.favourites:
                Intent favIntent = new Intent(this, MovieListActivity.class);
                favIntent.putParcelableArrayListExtra("list", new ArrayList(userFavorites));
                favIntent.putExtra("stringData","favourites");
                startActivity(favIntent);
                return true;

            default:
                return true;
        }
    }

    @Override
    public void sendDetails(Movie movie, int position) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("selectedMovie", movie);
        intent.putParcelableArrayListExtra("favourites", new ArrayList(userFavorites));
        intent.putParcelableArrayListExtra("watchlist", new ArrayList(userWatchlist));
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onQueryTextSubmit(String query) {
        if(query == null || query.equals("")) {
            return false;
        }
        else{
            movieQuery = query;
            if(NetworkUtils.networkStatus(HomeActivity.this))
                new MovieSearch().execute();
            else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                dialog.setTitle(getString(R.string.title_network_alert));
                dialog.setMessage(getString(R.string.message_network_alert));
                dialog.setCancelable(false);
                dialog.show();
            }
            return true;
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public class MovieSearch extends AsyncTask<Void, Void, Void>{

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

            final String movieSearchURL = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&language=en-US&query="+ movieQuery +"&page=1&include_adult=false";
            searchResults = new ArrayList<>();

            try{
                if(NetworkUtils.networkStatus(HomeActivity.this)){
                    searchResults = NetworkUtils.fetchData(movieSearchURL);
                }else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                    dialog.setTitle(getString(R.string.title_network_alert));
                    dialog.setMessage(getString(R.string.message_network_alert));
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void  s) {
            super.onPostExecute(s);
            Intent searchIntent = new Intent(HomeActivity.this, MovieListActivity.class);
            searchIntent.putExtra("list", new ArrayList(searchResults));
            searchIntent.putExtra("stringData", movieQuery);
            searchIntent.putParcelableArrayListExtra("userFavs", new ArrayList(userFavorites));
            searchIntent.putParcelableArrayListExtra("userWatch", new ArrayList(userWatchlist));
            startActivity(searchIntent);
        }
    }

    public class FetchDirections extends AsyncTask<Void, Void, Void>{


        @Override
        protected Void doInBackground(Void... voids) {

            //https://maps.googleapis.com/maps/api/directions/json?origin=44.6501129,20.9046823&destination=44.665384,20.917671&key=AIzaSyAh8f0b0HOJ2UrsftirpnMQPbA4hirLpes
           // final String directionsURL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + + "&destination=Montreal&key=AIzaSyAh8f0b0HOJ2UrsftirpnMQPbA4hirLpes";
            return null;
        }
    }


    public class FetchTheatres extends AsyncTask<Void, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

            final String nearbyTheatresURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lon + "&radius=10000&type=movie_theater&key=" + BuildConfig.PLACES_API_KEY;

            theaters = new ArrayList<>();

            try{
                if(NetworkUtils.networkStatus(HomeActivity.this)){
                    theaters = NetworkUtils.fetchDataTheaters(nearbyTheatresURL);
                }else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(HomeActivity.this);
                    dialog.setTitle(getString(R.string.title_network_alert));
                    dialog.setMessage(getString(R.string.message_network_alert));
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void  s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat ,lon)).title("Your location"));
            LatLng origin = new LatLng(lat,lon);
            for (Theater th : theaters) {
                LatLng dest = new LatLng(th.getLat(), th.getLon());
                  mMap.addMarker(new MarkerOptions().position(new LatLng(th.getLat(),th.getLon())).title(th.getName()));
              }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), 14));
        }
    }

    public class FetchMovies extends AsyncTask<Void, Void, Void>{

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

           fetchedMovies = new HashMap<>();

            try{
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
            }catch (IOException e){
                e.printStackTrace();
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


