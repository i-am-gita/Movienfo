package pmf.android.movienfo.movie_details;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import pmf.android.movienfo.Movie;
import pmf.android.movienfo.R;

public class MovieDetailsFragment extends Fragment{

    public static final String TAG = MovieDetailsFragment.class.getSimpleName();

    private Movie mMovie;

    @BindView(R.id.movie_title)
    TextView mMovieTitleView;

    @BindView(R.id.movie_overview)
    TextView mMovieOverviewView;

    @BindView(R.id.movie_release_date)
    TextView mMovieReleaseDateView;

    @BindView(R.id.movie_user_rating)
    TextView mMovieRatingView;

    @BindView(R.id.movie_poster)
    ImageView mMoviePosterView;

    @BindViews({R.id.rating_first_star, R.id.rating_second_star, R.id.rating_third_star, R.id.rating_fourth_star, R.id.rating_fifth_star})
    List<ImageView> ratingStarViews;

    @BindView(R.id.favorite_button)
    Button favoriteButton;

    @BindView(R.id.watchlist_button)
    Button watchlistButton;

    private boolean isFav;
    private boolean inWatch;

    public MovieDetailsFragment(){

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey("selectedMovie")) {
            mMovie = getArguments().getParcelable("selectedMovie");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

        if(appBarLayout != null && activity instanceof MovieDetailsActivity){
            appBarLayout.setTitle(mMovie.getOriginalTitle());
        }

        ImageView movieBackdrop = ((ImageView) activity.findViewById(R.id.movie_backdrop));

        if(movieBackdrop != null){
            Picasso.get()
                    .load(mMovie.getBackdropPath())
                    .config(Bitmap.Config.RGB_565)
                    .into(movieBackdrop);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_details, container, false);
        ButterKnife.bind(this, rootView);

        mMovieTitleView.setText(mMovie.getOriginalTitle());
        mMovieOverviewView.setText(mMovie.getOverview());
        String releaseDate = formatDateString(mMovie.getReleaseDate());
        mMovieReleaseDateView.setText("Release date: " + releaseDate);
        Picasso.get()
                .load(mMovie.getPosterPath())
                .config(Bitmap.Config.RGB_565)
                .into(mMoviePosterView);

        update_rating_stars();
        Log.d(TAG, "Current selected movie id is: " + String.valueOf(mMovie.getId()));

        if(getArguments().containsKey("favorite")){
            isFav = getArguments().getBoolean("favorite");
        }

        if(getArguments().containsKey("watchlist")){
            inWatch = getArguments().getBoolean("watchlist");
        }

        favoriteButton.setText("FAVORITES");
            if(isFav) {
                Drawable left = getResources().getDrawable(R.drawable.favourite_icon_20px);
                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }
            else {
                Drawable left = getResources().getDrawable(R.drawable.unfavourite_icon_20px);
                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
            }

        watchlistButton.setText("WATCHLIST");
            if(inWatch) {
                Drawable left = getResources().getDrawable(R.drawable.add_icon_20px);
                watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }
            else {
                Drawable left = getResources().getDrawable(R.drawable.remove_icon_20px);
                watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }

        setHasOptionsMenu(true);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFav){
                    deleteMovieFirebase(ref, "favorites");
                    Drawable left = getResources().getDrawable(R.drawable.unfavourite_icon_20px);
                    favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                    isFav = false;
                }else{
                    addMovieFirebase(ref, "favorites");
                    Drawable left = getResources().getDrawable(R.drawable.favourite_icon_20px);
                    favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                    isFav = true;
                }
            }
        });

        watchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inWatch){
                    deleteMovieFirebase(ref, "watchlist");
                    Drawable left = getResources().getDrawable(R.drawable.remove_icon_20px);
                    watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                    inWatch = false;
                }else{
                    addMovieFirebase(ref, "watchlist");
                    Drawable left = getResources().getDrawable(R.drawable.add_icon_20px);
                    watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                    inWatch = true;
                }
            }
        });

        return rootView;
    }

    private void  addMovieFirebase(DatabaseReference ref, String collection){
        if(collection.equals("favorites")) {
            ref.child("favorites").child(mMovie.getId()+"").setValue(mMovie);
        }else{
            ref.child("watchlist").child(mMovie.getId()+"").setValue(mMovie);
        }
    }

    private void deleteMovieFirebase(DatabaseReference ref, String collection){
        Query movieQuery;
        if(collection.equals("favorites")){
            ref.child("favorites").child(mMovie.getId().toString()).removeValue();
        }else{
            ref.child("watchlist").child(mMovie.getId().toString()).removeValue();
        }
    }

    private String formatDateString(String date){
        String[] numbers = date.split("-");
        return numbers[2] + "." + numbers[1] + "." + numbers[0] + ".";
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void update_rating_stars() {
        if (mMovie.getVoteAverage() != null && !mMovie.getVoteAverage().isEmpty()) {
            String user_rating_star = getResources().getString(R.string.movie_user_rating, mMovie.getVoteAverage());
            mMovieRatingView.setText(user_rating_star);
            float user_rating = Float.valueOf(mMovie.getVoteAverage()) / 2;
            int integerPart = (int) user_rating;

            for (int i = 0; i < integerPart; i++) {
                ratingStarViews.get(i).setImageResource(R.drawable.ic_star_red_24dp);
            }

            if (Math.round(user_rating) > integerPart) {
                ratingStarViews.get(integerPart).setImageResource(
                        R.drawable.ic_star_half_red_24dp);
            }
        } else {
            mMovieRatingView.setVisibility(View.GONE);
        }
    }
}
