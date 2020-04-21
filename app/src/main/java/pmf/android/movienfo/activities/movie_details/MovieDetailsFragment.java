package pmf.android.movienfo.activities.movie_details;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.model.Movie;

public class MovieDetailsFragment extends Fragment{

    public static final String TAG = MovieDetailsFragment.class.getSimpleName();

    private Movie mMovie;

    @Nullable
    @BindView(R.id.movie_title)
    TextView mMovieTitleView;

    @Nullable
    @BindView(R.id.movie_overview)
    TextView mMovieOverviewView;

    @Nullable
    @BindView(R.id.movie_release_date)
    TextView mMovieReleaseDateView;

    @Nullable
    @BindView(R.id.movie_user_rating)
    TextView mMovieRatingView;

    @Nullable
    @BindView(R.id.movie_poster)
    ImageView mMoviePosterView;

    @Nullable
    @BindViews({R.id.rating_first_star, R.id.rating_second_star, R.id.rating_third_star, R.id.rating_fourth_star, R.id.rating_fifth_star})
    List<ImageView> ratingStarViews;

    @Nullable
    @BindView(R.id.favorite_button)
    Button favoriteButton;

    @Nullable
    @BindView(R.id.watchlist_button)
    Button watchlistButton;

    private boolean isFav;
    private boolean inWatch;



    @Nullable
    @BindView(R.id.search_movie_poster)
    ImageView mPoster;

    @Nullable
    @BindView(R.id.remove_from_list)
    ImageButton mRemoveButton;

    @Nullable
    @BindView(R.id.search_movie_title)
    TextView mTitle;

    @Nullable
    @BindView(R.id.search_movie_overview)
    TextView mOverview;

    @Nullable
    @BindView(R.id.search_movie_vote)
    TextView mVote;
    private String movieListType;

    public MovieDetailsFragment(){

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey("selectedMovie")) {
            mMovie = getArguments().getParcelable("selectedMovie");
        }

        if(getArguments().containsKey("movieListType")){
            movieListType = getArguments().getString("movieListType");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(movieListType == null) {

            Activity activity = getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

            if (appBarLayout != null && activity instanceof MovieDetailsActivity) {
                appBarLayout.setTitle(mMovie.getOriginalTitle());
            }

            ImageView movieBackdrop = ((ImageView) activity.findViewById(R.id.movie_backdrop));

            if (movieBackdrop != null) {
                Picasso.get()
                        .load(mMovie.getBackdropPath())
                        .config(Bitmap.Config.RGB_565)
                        .into(movieBackdrop);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;
        if(movieListType == null) {
            rootView = inflater.inflate(R.layout.item_movie_details, container, false);
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
                Drawable left = getResources().getDrawable(R.drawable.icon_favourite);
                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }
            else {
                Drawable left = getResources().getDrawable(R.drawable.icon_unfavourite);
                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
            }

            watchlistButton.setText("WATCHLIST");
            if(inWatch) {
                Drawable left = getResources().getDrawable(R.drawable.icon_add);
                watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }
            else {
                Drawable left = getResources().getDrawable(R.drawable.icon_remove);
                watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }

            setHasOptionsMenu(true);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isFav){
                        deleteMovieFirebase(ref, "favorites");
                        Drawable left = getResources().getDrawable(R.drawable.icon_unfavourite);
                        favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                        isFav = false;
                    }else{
                        addMovieFirebase(ref, "favorites");
                        Drawable left = getResources().getDrawable(R.drawable.icon_favourite);
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
                        Drawable left = getResources().getDrawable(R.drawable.icon_remove);
                        watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                        inWatch = false;
                    }else{
                        addMovieFirebase(ref, "watchlist");
                        Drawable left = getResources().getDrawable(R.drawable.icon_add);
                        watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                        inWatch = true;
                    }
                }
            });
        }else{
            rootView = inflater.inflate(R.layout.item_movie_list, container, false);
            ButterKnife.bind(this, rootView);
            mTitle.setText(mMovie.getOriginalTitle());
            mOverview.setText(mMovie.getOverview());
            mVote.setText(mMovie.getVoteAverage());
            Picasso.get()
                    .load(mMovie.getPosterPath())
                    .config(Bitmap.Config.RGB_565)
                    .into(mPoster);

            Log.d(TAG, "Current movie id is: " + String.valueOf(mMovie.getId()));

            mRemoveButton.setContentDescription(mMovie.getId().toString());

            //setHasOptionsMenu(true);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(movieListType.equals("favourites")){
                        ref.child("favorites").child(mMovie.getId().toString()).removeValue();

                    }else{
                        ref.child("watchlist").child(mMovie.getId().toString()).removeValue();
                    }
                }
            });
        }
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
                ratingStarViews.get(i).setImageResource(R.drawable.icon_star);
            }

            if (Math.round(user_rating) > integerPart) {
                ratingStarViews.get(integerPart).setImageResource(
                        R.drawable.icon_star_half);
            }
        } else {
            mMovieRatingView.setVisibility(View.GONE);
        }
    }
}
