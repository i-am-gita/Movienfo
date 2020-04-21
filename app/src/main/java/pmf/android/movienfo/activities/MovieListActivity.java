package pmf.android.movienfo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.activities.movie_details.MovieDetailsActivity;
import pmf.android.movienfo.adapters.MovieAdapter;
import pmf.android.movienfo.model.Movie;

public class MovieListActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener {

    @BindView(R.id.info_text)
    TextView infoTextview;

    @BindView(R.id.movie_list_recycle)
    RecyclerView movieListRecycle;


    private MovieAdapter mAdapter;

    private String movieListType;
    private ArrayList<Movie> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_movies_list);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.logo_render_movienfo);

        ButterKnife.bind(this);

        Intent wantedData = getIntent();
        list = wantedData.getParcelableArrayListExtra("list");
        movieListType = wantedData.getStringExtra("stringData");

        switch (movieListType){
            case "favourites":
                infoTextview.setText("Personal favourites");
                break;
            case "watchlist":
                infoTextview.setText("Watchlist");
                break;
            default:
                infoTextview.setText("Results for " + movieListType);
                break;
        }

       movieListRecycle.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        movieListRecycle.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                findViewById(R.id.overviewScroll).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        mAdapter = new MovieAdapter(list, this, "item_movie_list", movieListType);
        movieListRecycle.setAdapter(mAdapter);

    }

    @Override
    public void send_details(Movie movie, int position) {
        if(movieListType.equals("favourites") || movieListType.equals("watchlist")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            if (movieListType.equals("favourites")) {
                ref.child("favorites").child(movie.getId().toString()).removeValue();

            } else {
                ref.child("watchlist").child(movie.getId().toString()).removeValue();
            }
            Movie forDel = null;
            for (Movie m : list) {
                if (m.getId().toString().equals(movie.getId().toString())) forDel = m;
            }
            if (forDel != null) list.remove(forDel);

            Intent favIntent = new Intent(this, MovieListActivity.class);
            favIntent.putParcelableArrayListExtra("list", list);
            favIntent.putExtra("stringData", movieListType);

            finish();
            startActivity(favIntent);

        }else{
            Intent movieIntent = new Intent(this, MovieDetailsActivity.class);
            movieIntent.putExtra("selectedMovie", movie);
            movieIntent.putParcelableArrayListExtra("favourites", getIntent().getParcelableArrayListExtra("userFavs"));
            movieIntent.putParcelableArrayListExtra("watchlist", getIntent().getParcelableArrayListExtra("userWatch"));
            startActivity(movieIntent);
        }
}

}
