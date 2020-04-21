package pmf.android.movienfo.activities.movie_details;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.ButterKnife;
import pmf.android.movienfo.model.Movie;
import pmf.android.movienfo.R;

public class MovieDetailsActivity extends AppCompatActivity {

    public static final String TAG = MovieDetailsActivity.class.getSimpleName();

    public static ArrayList<Movie> userFavorites;
    public static ArrayList<Movie> userWatchlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        checkBuildVersion();

        ActionBar actionB = getSupportActionBar();
        if(actionB != null){
            actionB.setDisplayHomeAsUpEnabled(true);
        }

        if(savedInstanceState == null){
            Bundle arguments = new Bundle();
            Movie selected = getIntent().getParcelableExtra("selectedMovie");
            arguments.putParcelable("selectedMovie", selected);
            userWatchlist = getIntent().getParcelableArrayListExtra("watchlist");
            userFavorites = getIntent().getParcelableArrayListExtra("favourites");


            manageDatabase(FirebaseDatabase.getInstance().getReference().child("watchlist"), FirebaseDatabase.getInstance().getReference().child("favourites"));

            arguments.putBoolean("favorite", isFavorite(selected));
            arguments.putBoolean("watchlist", inWatchlist(selected));

            MovieDetailsFragment fragment = new MovieDetailsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
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

                Movie wMovie = dataSnapshot.getValue(Movie.class);
                for(Movie wathlistM : userWatchlist){
                    if(wathlistM.getId().equals(wMovie.getId())) wathlistM = wMovie;
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Deleted movie from watchlist: " + dataSnapshot.getKey());

                Movie wMovie = dataSnapshot.getValue(Movie.class);
                if(userWatchlist.contains(wMovie)) userWatchlist.remove(wMovie);
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

        watchRef.addChildEventListener(watchlistEventListener);

        ChildEventListener favoritesEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "Added new favorite movie: " + dataSnapshot.getKey());

                Movie fMovie = dataSnapshot.getValue(Movie.class);
                userFavorites.add(fMovie);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "Changed favorite movie: " + dataSnapshot.getKey());

                Movie fMovie = dataSnapshot.getValue(Movie.class);
                for(Movie fav : userFavorites){
                    if(fav.getId().equals(fMovie.getId())) fav = fMovie;
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Deleted favorite movie: " + dataSnapshot.getKey());

                Movie fMovie = dataSnapshot.getValue(Movie.class);
                if(userFavorites.contains(fMovie)) userFavorites.remove(fMovie);
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

        favRef.addChildEventListener(favoritesEventListener);
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

    protected void checkBuildVersion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


