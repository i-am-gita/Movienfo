package pmf.android.movienfo.activities.movie_details;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.activities.HomeActivity;
import pmf.android.movienfo.model.Movie;

public class MovieDetailsFragment extends Fragment{

    private static final String TAG = MovieDetailsFragment.class.getSimpleName();

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
        if (getArguments() != null && getArguments().containsKey("selectedMovie")) {
            mMovie = getArguments().getParcelable("selectedMovie");
        }

        if (getArguments() != null && getArguments().containsKey("movieListType")) {
            movieListType = getArguments().getString("movieListType");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        if(movieListType == null) {
            ImageView movieBackdrop = null;
            if (activity != null) {
                movieBackdrop = activity.findViewById(R.id.movie_backdrop);
            }
            if (movieBackdrop != null) {
                Picasso.get()
                        .load(mMovie.getBackdropPath())
                        .config(Bitmap.Config.RGB_565)
                        .into(movieBackdrop);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        //Movie selected on home activity or in landscape movie list activity
        if(movieListType == null) {
            rootView = inflater.inflate(R.layout.item_movie_details, container, false);
            ButterKnife.bind(this, rootView);

            if (width>600 && getActivity().getClass() != MovieDetailsActivity.class) {
                if (mMoviePosterView != null) {
                    mMoviePosterView.setVisibility(View.GONE);
                }
                if (mMovieTitleView != null) {
                    mMovieTitleView.setVisibility(View.GONE);
                }
                if (mMovieRatingView != null) {
                    mMovieRatingView.setVisibility(View.GONE);
                }
                if (mMovieReleaseDateView != null) {
                    mMovieReleaseDateView.setVisibility(View.GONE);
                }

            }else{
                Picasso.get()
                        .load(mMovie.getPosterPath())
                        .config(Bitmap.Config.RGB_565)
                        .into(mMoviePosterView);
                if (mMovieTitleView != null) {
                    mMovieTitleView.setText(mMovie.getOriginalTitle());
                }
                String releaseDate = formatDateString(mMovie.getReleaseDate());
                if (mMovieReleaseDateView != null) {
                    mMovieReleaseDateView.setText("Release date: " + releaseDate);
                }
            }

            if(mMovieOverviewView != null) mMovieOverviewView.setText(mMovie.getOverview());
            update_rating_stars();
            Log.d(TAG, "Current selected movie id is: " + mMovie.getId());

            if (getArguments() != null && getArguments().containsKey("inFavourite")) {
                isFav = getArguments().getBoolean("inFavourite");
            }

            if (getArguments() != null && getArguments().containsKey("inWatchlist")) {
                inWatch = getArguments().getBoolean("inWatchlist");
            }

            Bitmap iconBitmap;
            if (favoriteButton != null) {
                favoriteButton.setPadding(5,5,5,5);
                favoriteButton.setText("FAVORITES");
            }

            if(isFav) {
                iconBitmap = HomeActivity.getBitmapFromVectorDrawable(getActivity().getApplicationContext(),R.drawable.icon_favourite);
                Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmap,100,100 ,true));
                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }
            else {
                iconBitmap = HomeActivity.getBitmapFromVectorDrawable(getActivity().getApplicationContext(),R.drawable.icon_unfavourite);
                Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmap,100,100 ,true));
                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
            }
            if (watchlistButton != null) {
                watchlistButton.setPadding(5,5,5,5);
                watchlistButton.setText("WATCHLIST");
            }
            if(inWatch) {
                iconBitmap = HomeActivity.getBitmapFromVectorDrawable(getActivity().getApplicationContext(),R.drawable.icon_remove);
                Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmap,100,100 ,true));
                watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }
            else {
                iconBitmap = HomeActivity.getBitmapFromVectorDrawable(getActivity().getApplicationContext(),R.drawable.icon_add);
                Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmap,100,100 ,true));
                watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
            }

            setHasOptionsMenu(true);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            favoriteButton.setOnClickListener(v -> {
                Bitmap iconBitmapListner;
                if(isFav){
                    deleteMovieFirebase(ref, "favorites");
                    iconBitmapListner = HomeActivity.getBitmapFromVectorDrawable(getActivity().getApplicationContext(),R.drawable.icon_unfavourite);
                    Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmapListner,100,100 ,true));
                    favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                    isFav = false;
                }else{
                    addMovieFirebase(ref, "favorites");
                    iconBitmapListner = HomeActivity.getBitmapFromVectorDrawable(getActivity().getApplicationContext(),R.drawable.icon_favourite);
                    Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmapListner,100,100,true));
                    favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                    isFav = true;
                }
            });

            watchlistButton.setOnClickListener(v -> {
                Bitmap iconBitmapListener;
                if(inWatch){
                    deleteMovieFirebase(ref, "watchlist");
                    iconBitmapListener = HomeActivity.getBitmapFromVectorDrawable(getActivity().getApplicationContext(),R.drawable.icon_add);
                    Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmapListener,100,100 ,true));
                    watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                    inWatch = false;
                }else{
                    addMovieFirebase(ref, "watchlist");
                    iconBitmapListener = HomeActivity.getBitmapFromVectorDrawable(getActivity().getApplicationContext(),R.drawable.icon_remove);
                    Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmapListener,100,100,true));
                    watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                    inWatch = true;
                }
            });

            //watchlist/favourites or searched movie
        }else{
            rootView = inflater.inflate(R.layout.item_movie_list, container, false);
            ButterKnife.bind(this, rootView);

            if (width>600 && mOverview != null) {
                mOverview.setVisibility(View.GONE);
            }else if(mOverview != null){
                mOverview.setText(mMovie.getOverview());
            }

            if(mTitle != null) mTitle.setText(mMovie.getOriginalTitle());
            if(mVote != null)mVote.setText(mMovie.getVoteAverage());
            Picasso.get()
                    .load(mMovie.getPosterPath())
                    .config(Bitmap.Config.RGB_565)
                    .into(mPoster);

            Log.d(TAG, "Current movie id is: " + mMovie.getId());

            if(mRemoveButton != null)mRemoveButton.setContentDescription(mMovie.getId().toString());

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            mRemoveButton.setOnClickListener(v -> {
                if(movieListType.equals("favourites")){
                    ref.child("favorites").child(mMovie.getId().toString()).removeValue();

                }else{
                    ref.child("watchlist").child(mMovie.getId().toString()).removeValue();
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
            if(mMovieRatingView != null) mMovieRatingView.setText(user_rating_star);
            float user_rating = Float.parseFloat(mMovie.getVoteAverage()) / 2;
            int integerPart = (int) user_rating;

            for (int i = 0; i < integerPart; i++) {
                if (ratingStarViews != null) {
                    ratingStarViews.get(i).setImageResource(R.drawable.icon_star);
                }
            }

            if (Math.round(user_rating) > integerPart) {
                if (ratingStarViews != null) {
                    ratingStarViews.get(integerPart).setImageResource(
                            R.drawable.icon_star_half);
                }
            }
        } else {
            if (mMovieRatingView != null) {
                mMovieRatingView.setVisibility(View.GONE);
            }
        }
    }
}
