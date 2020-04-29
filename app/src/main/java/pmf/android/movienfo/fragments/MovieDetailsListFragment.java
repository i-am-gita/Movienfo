package pmf.android.movienfo.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.activities.MovieDetailsActivity;
import pmf.android.movienfo.model.Movie;
import pmf.android.movienfo.utilities.Firebase;
import pmf.android.movienfo.utilities.MovienfoUtilities;

public class MovieDetailsListFragment extends Fragment{

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

    private Movie mMovie;
    private boolean isFav;
    private boolean inWatch;

    public MovieDetailsListFragment(){
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("selectedMovie"))
            mMovie = getArguments().getParcelable("selectedMovie");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //We are comparing fragments calling activity with MovieDetailsActivity. If they are the same, that means that BACKDROP IMAGE should be desplayed
        if(isMovieDetailsActivity()) {
            ImageView movieBackdrop = Objects.requireNonNull(getActivity()).findViewById(R.id.movie_backdrop);
            if (movieBackdrop != null) {
                Picasso.get()
                        .load(MovienfoUtilities.parseUrl(mMovie.getBackdropPath()))
                        .config(Bitmap.Config.RGB_565)
                        .into(movieBackdrop);
            }
        }
    }
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.item_movie_details, container, false);
        ButterKnife.bind(this, rootView);

        //When displaying movie details in current activity, some parameters become invisible so it blends with MovieListActivity recycle
        if (!isMovieDetailsActivity()) {
            if (mMoviePosterView != null) mMoviePosterView.setVisibility(View.GONE);
            favoriteButton.setVisibility(View.GONE);
            watchlistButton.setVisibility(View.GONE);
        }else{
            mMovieReleaseDateView.setText("Release date: " + MovienfoUtilities.formatDateString(mMovie.getReleaseDate()));
            Picasso.get()
                    .load(MovienfoUtilities.parseUrl(Objects.requireNonNull(mMovie.getPosterPath())))
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.image_placeholder)
                    .into(mMoviePosterView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mMoviePosterView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            mMovieReleaseDateView.setVisibility(View.VISIBLE);
                        }
                    });
        }
        mMovieOverviewView.setText("Overview: \n" + mMovie.getOverview());
        mMovieTitleView.setText(mMovie.getOriginalTitle());

        //Checking if current movie is in favourites/watchlist, info came from MovieListActivity
        if(getArguments() != null){
            if(getArguments().containsKey("inFavourite"))
                isFav = getArguments().getBoolean("inFavourite");
            if(getArguments().containsKey("inWatchlist")) {
                inWatch = getArguments().getBoolean("inWatchlist");
            }
        }

        //Setting button icons and their properties according to their list status
        Bitmap iconBitmap;
        if (favoriteButton != null) {
            favoriteButton.setPadding(5,5,5,5);
            favoriteButton.setText("FAVORITES");
        }
        if (watchlistButton != null) {
            watchlistButton.setPadding(5,5,5,5);
            watchlistButton.setText("WATCHLIST");
        }

        if(isFav) {
            iconBitmap = MovienfoUtilities.getBitmapFromVectorDrawable(container.getContext() ,R.drawable.icon_favourite);
            Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmap,100,100 ,true));
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
        }else {
            iconBitmap = MovienfoUtilities.getBitmapFromVectorDrawable(container.getContext(), R.drawable.icon_unfavourite);
            Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmap,100,100 ,true));
            favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
        }


        if(inWatch) {
            iconBitmap = MovienfoUtilities.getBitmapFromVectorDrawable(container.getContext(), R.drawable.icon_remove);
            Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmap,100,100 ,true));
            watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
        } else {
            iconBitmap = MovienfoUtilities.getBitmapFromVectorDrawable(container.getContext(), R.drawable.icon_add);
            Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmap,100,100 ,true));
            watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
        }
        setHasOptionsMenu(true);

        favoriteButton.setOnClickListener(v -> {
            Bitmap iconBitmapListner;
            if(isFav){
                Firebase.deleteMovieFirebase(Firebase.getInstance().getReference(), "favorites", mMovie);
                iconBitmapListner = MovienfoUtilities.getBitmapFromVectorDrawable(container.getContext(), R.drawable.icon_unfavourite);
                Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmapListner,100,100 ,true));
                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                isFav = false;
            }else{
                Firebase.addMovieFirebase(Firebase.getInstance().getReference(), "favorites",mMovie);
                iconBitmapListner = MovienfoUtilities.getBitmapFromVectorDrawable(container.getContext(),R.drawable.icon_favourite);
                Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmapListner,100,100,true));
                favoriteButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                isFav = true;
            }
        });

        watchlistButton.setOnClickListener(v -> {
            Bitmap iconBitmapListener;
            if(inWatch){
                Firebase.deleteMovieFirebase(Firebase.getInstance().getReference(), "watchlist", mMovie);
                iconBitmapListener = MovienfoUtilities.getBitmapFromVectorDrawable(container.getContext(),R.drawable.icon_add);
                Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmapListener,100,100 ,true));
                watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                inWatch = false;
            }else{
                Firebase.addMovieFirebase(Firebase.getInstance().getReference(), "watchlist", mMovie);
                iconBitmapListener =MovienfoUtilities.getBitmapFromVectorDrawable(container.getContext(),R.drawable.icon_remove);
                Drawable left = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(iconBitmapListener,100,100,true));
                watchlistButton.setCompoundDrawablesWithIntrinsicBounds(left, null , null, null);
                inWatch = true;
            }
        });

        update_rating_stars();

        return rootView;
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

    private boolean isMovieDetailsActivity(){
        return Objects.requireNonNull(getActivity()).getClass().getName().equals(MovieDetailsActivity.class.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
