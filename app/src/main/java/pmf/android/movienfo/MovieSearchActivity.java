package pmf.android.movienfo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieSearchActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener{

    @BindView(R.id.searched_movies_recycle)
    RecyclerView listRecycle;

    @BindView(R.id.results_for_query)
    TextView infoTextview;


    private MovieAdapter mAdapter;

    private String type;
    private ArrayList<Movie> list;

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

        Intent wantedData = getIntent();
        list = wantedData.getParcelableArrayListExtra("list");
        type = wantedData.getStringExtra("stringData");

        switch (type){
            case "favourites":
                infoTextview.setText("Personal favourites");
                break;
            case "watchlist":
                infoTextview.setText("Watchlist");
                break;
            default:
                infoTextview.setText("Results for " + type);
                break;
        }

        listRecycle.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        listRecycle.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                findViewById(R.id.overviewScroll).getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        mAdapter = new MovieAdapter(list, this, "search_movie_item", type);
        listRecycle.setAdapter(mAdapter);

    }

    @Override
    public void onResume() {

        super.onResume();
    }

    public void removeMovie(View view){
        ImageButton removeButton = findViewById(view.getId());
        CharSequence movieId = removeButton.getContentDescription();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        if(type.equals("favourites")){
            ref.child("favorites").child(movieId.toString()).removeValue();

        }else{
            ref.child("watchlist").child(movieId.toString()).removeValue();
        }
        Movie forDel = null;
        for(Movie m : list){
            if(m.getId().toString().equals(movieId.toString())) forDel = m;
        }
        if(forDel != null) list.remove(forDel);

        Intent favIntent = new Intent(this, MovieSearchActivity.class);
        favIntent.putParcelableArrayListExtra("list", list);
        favIntent.putExtra("stringData",type);

        finish();
        startActivity(favIntent);
    }

    @Override
    public void send_details(Movie movie, int position) {

    }
}
