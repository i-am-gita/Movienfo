package pmf.android.movienfo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.adapters.MovieAdapter;
import pmf.android.movienfo.fragments.MovieDetailsListFragment;
import pmf.android.movienfo.model.Movie;
import pmf.android.movienfo.utilities.MovienfoRoomDatabase;
import pmf.android.movienfo.utilities.MovienfoUtilities;

public class MovieListActivity extends AppCompatActivity implements MovieInListChecker, ActionBarInitializer, MovieAdapter.OnItemClickListener, MovieAdapter.OnFragmentItemClickListener, MovieListInitializer {

    private static final String TAG = MovieListActivity.class.getSimpleName();

    @BindView(R.id.info_text)
    TextView infoTextview;

    @BindView(R.id.movie_list_recycle)
    RecyclerView movieListRecycle;

    private String movieListType;
    private ArrayList<Movie> searchedMovieList;

    //Firebase data
    private ArrayList<Movie> userFavorites;
    private ArrayList<Movie> userWatchlist;

    //Roomdatabase data
    private ArrayList<Movie> recent;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_movies_list);

        setActionBarElements(Objects.requireNonNull(getSupportActionBar()));

        ButterKnife.bind(this);

        getDataFromHomeActivityIntent();

        initializeMoviesList();
    }

    @Override
    public void setActionBarElements(ActionBar customActionBar) {
        customActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        customActionBar.setDisplayShowCustomEnabled(true);
        customActionBar.setCustomView(R.layout.action_bar_custom);
        customActionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        View view = Objects.requireNonNull(getSupportActionBar()).getCustomView();
        ImageButton searchIcon = view.findViewById(R.id.search_bar_hint_icon);
        ImageView homeIcon = view.findViewById(R.id.icon_home);
        EditText searchField = view.findViewById(R.id.search_bar_edit_text);
        searchField.setVisibility(View.GONE);

        homeIcon.setOnClickListener(v -> {
            Intent homeIntent = new Intent(v.getContext(), HomeActivity.class);
            startActivity(homeIntent);
        });

        searchIcon.setVisibility(View.GONE);
        searchField.setVisibility(View.GONE);
    }

    private void getDataFromHomeActivityIntent(){
        Intent wantedData = getIntent();
        searchedMovieList = wantedData.getParcelableArrayListExtra("searchResults");
        userFavorites = wantedData.getParcelableArrayListExtra("userFavs");
        userWatchlist = wantedData.getParcelableArrayListExtra("userWatch");
        recent = (ArrayList<Movie>) MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().getAll();
        movieListType = wantedData.getStringExtra("stringData");
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void initializeMoviesList() {
        movieListRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        //So 2 scroll views can scroll without messing with one-another
        movieListRecycle.setOnTouchListener((v, event) -> {
            findViewById(R.id.overviewScroll).getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });
        //Inflating recycle on MovieListActivity with specific list type.
        MovieAdapter mAdapter;
        switch (movieListType){
            case "favourites":
                mAdapter = new MovieAdapter(this, userFavorites, R.layout.item_movie_list, movieListType);
                infoTextview.setText("Personal favourites");
                break;
            case "watchlist":
                mAdapter = new MovieAdapter(this, userWatchlist, R.layout.item_movie_list, movieListType);
                infoTextview.setText("Watchlist");
                break;
            case "recent":
                mAdapter = new MovieAdapter(this, recent, R.layout.item_movie_list, movieListType);
                infoTextview.setText("Recently viewed movies");
                break;
            default:
                mAdapter = new MovieAdapter(this, searchedMovieList, R.layout.item_movie_list, movieListType);
                infoTextview.setText("Results for " + movieListType);
                break;
        }
        if(MovienfoUtilities.showMovieOnCurrentActivity(this, (float) getResources().getDisplayMetrics().densityDpi)){
            mAdapter.setOnFragmentItemClickListener(this);
        }
        mAdapter.setOnItemClickListener(this);
        movieListRecycle.setAdapter(mAdapter);
    }

    //Interface method that has 2 types of usage:
    // 1. Showing movie details in MovieDetailsActivity using MovieDetailsFragment
    // 2. Removing selected movie from favourites list or from watchlist
    @Override
    public void sendDetails(Movie movie, int position) {

        //Value -100 is assigned to the position parameter when selected movie should be opened in another activity instead of displaying it as a fragment
        //on the same activity movie is selected (MovieListActivity). This happens when screen width in dpi is less then 600dp
        if(position == -100 || (!movieListType.equals("favourites") && !movieListType.equals("watchlist"))){
            Intent movieIntent = new Intent(this, MovieDetailsActivity.class);
            movieIntent.putExtra("selectedMovie", movie);
            movieIntent.putParcelableArrayListExtra("favourites", userFavorites);
            movieIntent.putParcelableArrayListExtra("watchlist", userWatchlist);
            startActivity(movieIntent);

        }else if(movieListType.equals("favourites")){
            FirebaseDatabase.getInstance().getReference().child("favorites").child(movie.getId().toString()).removeValue();
            userFavorites.remove(movie);
            Toast.makeText(getApplicationContext(), movie.getOriginalTitle() + " removed from favourites!", Toast.LENGTH_SHORT).show();
            refreshMovieList();

        }else {
            FirebaseDatabase.getInstance().getReference().child("watchlist").child(movie.getId().toString()).removeValue();
            userWatchlist.remove(movie);
            Toast.makeText(getApplicationContext(), movie.getOriginalTitle() + " removed from watchlist!", Toast.LENGTH_SHORT).show();
            refreshMovieList();
        }
    }

    //Interface method which is responsible for displaying movie details on the same activity in which user selected that specific movie(MovieListActivity)
    //Another duty of this method is adding movie to recently seen list if the conditions are fulfilled

    @Override
    public void sendData(Movie movie, int position) {

        if(!listContainsMovie(movie,recent)) addToRoomDatabase(movie);

        Bundle arguments = new Bundle();
        arguments.putParcelable("selectedMovie", movie);
        arguments.putBoolean("inFavourite", listContainsMovie(movie, userFavorites));
        arguments.putBoolean("inWatchlist", listContainsMovie(movie,userWatchlist));

        MovieDetailsListFragment fragment = new MovieDetailsListFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_details_fragment, fragment)
                .commit();

    }

    //RoomDatabase is responsible for saving 10 movies that are recently seen. When 11th movie is seen, first movie added to the database is deleted, and 11th is then added to the end of list
    private void addToRoomDatabase(Movie movie){
        if(!recent.contains(movie)){
            if(recent.size() == 10){
                MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().delete(recent.get(0));
                recent.remove(recent.get(0));
                MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().insert(movie);
                recent.add(movie);
            }else{
                MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().insert(movie);
                recent.add(movie);
            }
        }else{
            Log.w(TAG, "Error");
            Toast.makeText(getApplicationContext() , movie + " has been recently seen so it is not added to the recent list",
                    Toast.LENGTH_SHORT).show();
        }
    }
    //If user removes movie from watchlist or list of favourites this method is called in order to refresh current activity and update details about lists
    private void refreshMovieList(){
        Intent favIntent = new Intent(this, MovieListActivity.class);
        favIntent.putParcelableArrayListExtra("userFavs",userFavorites);
        favIntent.putParcelableArrayListExtra("userWatch",userWatchlist);
        favIntent.putExtra("stringData", movieListType);
        finish();
        startActivity(favIntent);
    }

    @Override
    public boolean listContainsMovie(Movie movie, ArrayList<Movie> list) {
        for(Movie m : list){
            if(m.getId().toString().equals(movie.getId().toString()))
                return true;
        }
        return false;
    }
}
