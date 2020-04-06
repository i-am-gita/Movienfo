package pmf.android.movienfo;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.utilities.NetworkUtils;

public class HomeActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    String apiKey = BuildConfig.API_KEY;
    String mShow;

    @BindView(R.id.upcoming_movies_recyclerView)
    RecyclerView upcomingRecycle;

    @BindView(R.id.trending_movies_recyclerView)
    RecyclerView trendingRecycle;

    String upcomingMoviesURL;
    String trendingMoviesURL;

    ArrayList<Movie> mUpcomingList;
    ArrayList<Movie> mTrending;

    private MovieAdapter mAdapter;

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
        mAdapter = new MovieAdapter(new ArrayList<Movie>(), this);
        trendingRecycle.setAdapter(mAdapter);
        upcomingRecycle.setAdapter(mAdapter);

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
       // Intent intent = new Intent(this, MovieDetailActivity.class);
       // intent.putExtra(MovieDetailFragment.ARG_MOVIE, movie);
      //  startActivity(intent);
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

            mAdapter = new MovieAdapter(mTrending,HomeActivity.this);
            trendingRecycle.setAdapter(mAdapter);

            mAdapter = new MovieAdapter(mUpcomingList, HomeActivity.this);
            upcomingRecycle.setAdapter(mAdapter);

        }
    }
}

