package pmf.android.movienfo.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.fragments.MovieDetailsListFragment;
import pmf.android.movienfo.model.Movie;

public class MovieDetailsActivity extends AppCompatActivity implements MovieInListChecker {

    public static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private ArrayList<Movie> userFavorites;
    private ArrayList<Movie> userWatchlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_movie_details);
        Objects.requireNonNull(getSupportActionBar()).hide();

        ButterKnife.bind(this);

        checkBuildVersion();

        movieFragmentInitialization(savedInstanceState);
    }

    private void movieFragmentInitialization(Bundle savedInstanceState){
        if(savedInstanceState == null){
            Bundle arguments = new Bundle();
            Movie selected = getIntent().getParcelableExtra("selectedMovie");
            userWatchlist = getIntent().getParcelableArrayListExtra("watchlist");
            userFavorites = getIntent().getParcelableArrayListExtra("favourites");

            manageDatabase(FirebaseDatabase.getInstance().getReference().child("watchlist"), FirebaseDatabase.getInstance().getReference().child("favourites"));

            arguments.putParcelable("selectedMovie", selected);
            arguments.putBoolean("inFavourite", listContainsMovie(selected, userFavorites));
            arguments.putBoolean("inWatchlist", listContainsMovie(selected, userWatchlist));

            MovieDetailsListFragment fragment = new MovieDetailsListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
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

    protected void checkBuildVersion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void manageDatabase(DatabaseReference watchRef, DatabaseReference favRef){

        //Listener for watchlist
        ChildEventListener watchlistEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "Added new movie to watchlist: " + dataSnapshot.getKey());
                Movie wMovie = dataSnapshot.getValue(Movie.class);
                userWatchlist.add(wMovie);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "Changed movie from the watchlist: " + dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Deleted movie from watchlist: " + dataSnapshot.getKey());
                userWatchlist.remove(dataSnapshot.getValue(Movie.class));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Error", databaseError.toException());
                Toast.makeText(getApplicationContext() , "Failed to load watchlist.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        ChildEventListener favoritesEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "Added new favorite movie: " + dataSnapshot.getKey());
                userFavorites.add(dataSnapshot.getValue(Movie.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "Changed favorite movie: " + dataSnapshot.getKey());

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Deleted favorite movie: " + dataSnapshot.getKey());

                Movie fMovie = dataSnapshot.getValue(Movie.class);
                userFavorites.remove(fMovie);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Error", databaseError.toException());
                Toast.makeText(getApplicationContext() , "Failed to load list of favorite movies.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        watchRef.addChildEventListener(watchlistEventListener);
        favRef.addChildEventListener(favoritesEventListener);
    }
}


