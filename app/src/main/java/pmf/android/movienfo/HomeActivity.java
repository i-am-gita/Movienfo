package pmf.android.movienfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.movie_details.MovieDetailsActivity;
import pmf.android.movienfo.utilities.NetworkUtils;

public class HomeActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener, SearchView.OnQueryTextListener, View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    String apiKey = BuildConfig.API_KEY;
    String mShow;

    @BindView(R.id.upcoming_movies_recyclerView)
    RecyclerView upcomingRecycle;

    @BindView(R.id.trending_movies_recyclerView)
    RecyclerView trendingRecycle;

    @BindView(R.id.find_theaters_button)
    Button theatersButton;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    String upcomingMoviesURL;
    String trendingMoviesURL;

    ArrayList<Movie> mUpcomingList;
    ArrayList<Movie> mTrending;

    String movieQuery;
    ArrayList<Movie> searchResults;

    private MovieAdapter mAdapter;

    private int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    private double lat,lon;
    String places_api_key = "AIzaSyDqCVRF02vBoHPLSXV2VL2DloslV1v0JO0";
    ArrayList<Theater> theaters;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;

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
        actionBar.setIcon(R.drawable.home_icon);

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

        upcomingRecycle.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));
        trendingRecycle.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL, false));
        mAdapter = new MovieAdapter(new ArrayList<Movie>(), this, "movie_item_home");
        trendingRecycle.setAdapter(mAdapter);
        upcomingRecycle.setAdapter(mAdapter);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();

        theatersButton.setOnClickListener(this);

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

      //  if(theaters != null && !theaters.isEmpty()){
        //    MapFragment mapFragment = (MapFragment) getFragmentManager()
          //          .findFragmentById(R.id.map);
           // mapFragment.getMapAsync(this);
        //}
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), 17));
        mMap.setOnCameraMoveListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
      //  for (Theater th : theaters) {
      //      LatLng coordinates = new LatLng(th.getLat(), th.getLon());
      //      mMap.addMarker(new MarkerOptions().position(coordinates).title(th.getName()));
      //  }
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
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    lat = location.getLatitude();
                                    lon = location.getLongitude();
                                }
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
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

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
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(this);
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate;
        searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate!=null) {
            searchPlate.setBackgroundColor(Color.BLACK);
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText!=null) {
                searchText.setTextColor(Color.parseColor("#ef1e3c"));
                searchText.setHintTextColor(Color.parseColor("#ef1e3c"));
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void send_details(Movie movie, int position) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("selectedMovie", movie);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onQueryTextSubmit(String query) {
        if(query == null || query == "")
            return false;
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



    //AsyncTask
    public class MovieSearch extends AsyncTask<Void, Void, Void>{


        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

            String movieSearchURL = "https://api.themoviedb.org/3/search/movie?api_key="+apiKey+"&language=en-US&query="+ movieQuery +"&page=1&include_adult=false";

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

            Intent searchIntent = new Intent(HomeActivity.this, MovieSearchActivity.class);
            searchIntent.putExtra("searchResults", searchResults);
            searchIntent.putExtra("query", movieQuery);
            startActivity(searchIntent);

        }
    }

    public class FetchTheatres extends AsyncTask<Void, Void, Void> {


        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

            String nearbyTheatresURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + lat + "," + lon + "&radius=4000&type=movie_theater&key=" + places_api_key;

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
            mMap.addMarker(new MarkerOptions().position(new LatLng(20.933517,44.665894)));
            for (Theater th : theaters) {
                double latt = th.getLat();
                double lonn = th.getLon();
                  mMap.addMarker(new MarkerOptions().position(new LatLng(th.getLat(),th.getLon())).title(th.getName()));
              }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), 13));
        }
    }

    //AsyncTask
    public class FetchMovies extends AsyncTask<Void, Void, Void>{


        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... voids) {

            String upcomingMoviesURL = "https://api.themoviedb.org/3/movie/upcoming?api_key="+apiKey+"&language=en-US&page=1";
            String trendingMoviesURL = "https://api.themoviedb.org/3/movie/now_playing?api_key="+apiKey+"&language=en-US&page=1";

            mTrending = new ArrayList<>();
            mUpcomingList = new ArrayList<>();

            try{
                if(NetworkUtils.networkStatus(HomeActivity.this)){
                    mTrending = NetworkUtils.fetchData(trendingMoviesURL);
                    mUpcomingList = NetworkUtils.fetchData(upcomingMoviesURL);
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

            mAdapter = new MovieAdapter(mTrending,HomeActivity.this, "movie_item_home");
            trendingRecycle.setAdapter(mAdapter);

            mAdapter = new MovieAdapter(mUpcomingList, HomeActivity.this, "movie_item_home");
            upcomingRecycle.setAdapter(mAdapter);

        }
    }
}


