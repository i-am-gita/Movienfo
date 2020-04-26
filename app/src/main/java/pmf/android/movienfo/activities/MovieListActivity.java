package pmf.android.movienfo.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.activities.movie_details.MovieDetailsActivity;
import pmf.android.movienfo.activities.movie_details.MovieDetailsFragment;
import pmf.android.movienfo.adapters.MovieAdapter;
import pmf.android.movienfo.model.Movie;
import pmf.android.movienfo.utilities.MovienfoRoomDatabase;

public class MovieListActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener, MovieAdapter.OnFragmentItemClickListener {

    private static final String TAG = MovieListActivity.class.getSimpleName();

    //Custom action bar elements
    Toolbar toolbar;
    EditText searchField;
    ImageButton searchIcon;
    ImageView homeIcon;

    @BindView(R.id.info_text)
    TextView infoTextview;

    @BindView(R.id.movie_list_recycle)
    RecyclerView movieListRecycle;


    private MovieAdapter mAdapter;

    private String movieListType;
    private ArrayList<Movie> list;
    private ArrayList<Movie> userFavorites;
    private ArrayList<Movie> userWatchlist;

    //Landscape changes
    private boolean orientationLandscape;
    private MovieDetailsFragment fragment;

    //Roomdatabase data
    private ArrayList<Movie> recent;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_movies_list);

        setActionBarElements(getSupportActionBar());
        ButterKnife.bind(this);

        Intent wantedData = getIntent();
        list = wantedData.getParcelableArrayListExtra("list");
        movieListType = wantedData.getStringExtra("stringData");

        userFavorites = wantedData.getParcelableArrayListExtra("userFavs");
        userWatchlist = wantedData.getParcelableArrayListExtra("userWatch");

        recent = (ArrayList<Movie>) MovienfoRoomDatabase.getInstance(getApplicationContext()).movieDao().getAll();

        switch (movieListType){
            case "favourites":
                infoTextview.setText("Personal favourites");
                break;
            case "watchlist":
                infoTextview.setText("Watchlist");
                break;
            case "recent":
                infoTextview.setText("Recently viewed movies");
            default:
                infoTextview.setText("Results for " + movieListType);
                break;
        }
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientationLandscape = true;
        }else{
            orientationLandscape = false;
        }

       movieListRecycle.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

       movieListRecycle.setOnTouchListener((v, event) -> {
            findViewById(R.id.overviewScroll).getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });
        mAdapter = new MovieAdapter(this, list, R.layout.item_movie_list, movieListType);
        mAdapter.setOnItemClickListener(this);

        if(orientationLandscape) mAdapter.setOnFragmentItemClickListener(this);
        movieListRecycle.setAdapter(mAdapter);

    }

    private void setActionBarElements(ActionBar customActionBar) {
        customActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        customActionBar.setDisplayShowCustomEnabled(true);
        customActionBar.setCustomView(R.layout.action_bar_custom);
        customActionBar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        View view = getSupportActionBar().getCustomView();
        toolbar = view.findViewById(R.id.home_toolbar);
        searchIcon = view.findViewById(R.id.search_bar_hint_icon);
        homeIcon = view.findViewById(R.id.icon_home);
        searchField = view.findViewById(R.id.search_bar_edit_text);
        searchField.setVisibility(View.GONE);

        homeIcon.setOnClickListener(v -> {
            Intent homeIntent = new Intent(v.getContext(), HomeActivity.class);
            startActivity(homeIntent);
        });

        searchIcon.setVisibility(View.GONE);
        searchField.setVisibility(View.GONE);
    }

    @Override
    public void sendDetails(Movie movie, int position) {

        switch (movieListType){
            case "favourites":
                FirebaseDatabase.getInstance().getReference().child("favorites").child(movie.getId().toString()).removeValue();
                list.remove(movie);
                Toast.makeText(getApplicationContext() , movie.getOriginalTitle() + " removed from favourites!", Toast.LENGTH_SHORT).show();
                refreshMovieList();
                break;
            case "watchlist":
                FirebaseDatabase.getInstance().getReference().child("watchlist").child(movie.getId().toString()).removeValue();
                list.remove(movie);
                Toast.makeText(getApplicationContext() , movie.getOriginalTitle() + " removed from watchlist!", Toast.LENGTH_SHORT).show();
                refreshMovieList();
                break;
            default:
                Intent movieIntent = new Intent(this, MovieDetailsActivity.class);
                movieIntent.putExtra("selectedMovie", movie);
                movieIntent.putParcelableArrayListExtra("searchResultMovies", new ArrayList(list));
                movieIntent.putParcelableArrayListExtra("favourites", userFavorites);
                movieIntent.putParcelableArrayListExtra("watchlist", userWatchlist);
                startActivity(movieIntent);
                break;
        }
}

    @Override
    public void sendData(Movie movie, int position) {

            addToRoomDatabase(movie);

            Bundle arguments = new Bundle();
            arguments.putParcelable("selectedMovie", movie);
            arguments.putBoolean("inFavourite", isFavorite(movie));
            arguments.putBoolean("inWatchlist", inWatchlist(movie));
            arguments.putParcelableArrayList("watchlist",userWatchlist);
            arguments.putParcelableArrayList("favourites",userFavorites);

            fragment = new MovieDetailsFragment();
            fragment.setArguments(arguments);
            if(fragment == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.movie_details_fragment, fragment)
                        .commit();
            }else{
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_details_fragment, fragment)
                        .commit();
            }


    }

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

    private void refreshMovieList(){
        Intent favIntent = new Intent(this, MovieListActivity.class);
        favIntent.putParcelableArrayListExtra("list", list);
        favIntent.putExtra("stringData", movieListType);
        finish();
        startActivity(favIntent);
    }
    private boolean isFavorite(Movie movie){
        for(Movie m : userFavorites){
            if(m.getId().toString().equals(movie.getId().toString()))return true;
        }
        return false;
    }

    private boolean inWatchlist(Movie movie){
        for(Movie m : userWatchlist){
            if(m.getId().toString().equals(movie.getId().toString()))return true;
        }
        return false;
    }


}
