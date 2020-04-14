package pmf.android.movienfo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieSearchActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener{

    @BindView(R.id.searched_movies_recycle)
    RecyclerView searchedResults;


    @BindView(R.id.results_for_query)
    TextView resultsFor;


    private MovieAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.search_movie_activity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.home_icon);

        ButterKnife.bind(this);

        Intent searched = getIntent();
        ArrayList<Movie> searchMovies = searched.getParcelableArrayListExtra("searchResults");
        String query = searched.getStringExtra("query");
        resultsFor.setText(query);

        searchedResults.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        searchedResults.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                findViewById(R.id.overviewScroll).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        mAdapter = new MovieAdapter(searchMovies, this, "search_movie_item");
        searchedResults.setAdapter(mAdapter);

    }

    @Override
    public void send_details(Movie movie, int position) {

    }
}
