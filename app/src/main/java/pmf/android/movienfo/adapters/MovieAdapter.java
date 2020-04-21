package pmf.android.movienfo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.model.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();

    private final ArrayList<Movie> mMovies;

    private String layoutName;

    private String listType;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void send_details(Movie movie, int position);
    }

    public MovieAdapter(ArrayList<Movie> movies, OnItemClickListener mItemClickListener, String layoutName, String listType){
        this.mMovies = movies;
        this.mOnItemClickListener = mItemClickListener;
        this.layoutName = layoutName;
        this.listType = listType;
    }

    public MovieAdapter(ArrayList<Movie> movies, OnItemClickListener mItemClickListener, String layoutName){
        this.mMovies = movies;
        this.mOnItemClickListener = mItemClickListener;
        this.layoutName = layoutName;
    }




    @NonNull
    @Override
    public MovieAdapter.MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;
        View view;
        if(layoutName.equals("item_movie_list")){
            view = inflater.inflate(R.layout.item_movie_list, parent, shouldAttachToParentImmediately);
        }else{
            view = inflater.inflate(R.layout.item_movie_home, parent, shouldAttachToParentImmediately);
        }


        final Context context = view.getContext();
        MovieViewHolder viewHolder = new MovieViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.MovieViewHolder holder, int position) {
        final Movie movie = mMovies.get(position);
        final Context context = holder.mView.getContext();

        holder.mMovie = movie;
        String posterUrl = movie.getPosterPath();

        if(layoutName.equals("item_movie_list")){
            holder.mMovieTitle.setText(movie.getOriginalTitle());
            holder.mMovieVote.setText(movie.getVoteAverage());
            holder.mMovieOverview.setText(movie.getOverview());
            holder.overviewScroll.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

            if(!listType.equals("favourites") && !listType.equals("watchlist")) {
                holder.removeFromList.setVisibility(View.GONE);
            }else{
                holder.removeFromList.setOnClickListener(v -> mOnItemClickListener.send_details(movie, holder.getAdapterPosition()));
            }



            Picasso.get()
                    .load(posterUrl)
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.image_placeholder_movie_lists)
                    .into(holder.mMoviePoster, new Callback() {
                        @Override
                        public void onSuccess() {
                            if(holder.mMovie.getId() != movie.getId()){
                                holder.cleanUp();
                            }else{
                                holder.mMoviePoster.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            holder.mMovieTitle.setVisibility(View.VISIBLE);
                        }
                    });
        }else{
            String releaseYear = formatDate(movie.getReleaseDate());
            holder.mMovieRelease.setText(releaseYear);

            Picasso.get()
                    .load(posterUrl)
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.mMovieThumb, new Callback() {
                        @Override
                        public void onSuccess() {
                            if(holder.mMovie.getId() != movie.getId()){
                                holder.cleanUp();
                            }else{
                                holder.mMovieThumb.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            holder.mMovieRelease.setVisibility(View.VISIBLE);
                        }
                    });
        }

        holder.mView.setOnClickListener(v -> mOnItemClickListener.send_details(movie, holder.getAdapterPosition()));
    }

    private String formatDate(String date){
        String[] splitedDate = date.split("-");
        return splitedDate[0];
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    @Override
    public void onViewRecycled(MovieViewHolder holder) {
        super.onViewRecycled(holder);
        //holder.cleanUp();
    }

    public ArrayList<Movie> getMovies() {
        return mMovies;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        public Movie mMovie;

        @Nullable
        @BindView(R.id.movie_thumbnail)
        ImageView mMovieThumb;

        @Nullable
        @BindView(R.id.search_movie_poster)
        ImageView mMoviePoster;

        @Nullable
        @BindView(R.id.movie_release_date)
        TextView mMovieRelease;

        @Nullable
        @BindView(R.id.search_movie_title)
        TextView mMovieTitle;

        @Nullable
        @BindView(R.id.search_movie_overview)
        TextView mMovieOverview;

        @Nullable
        @BindView(R.id.search_movie_vote)
        TextView mMovieVote;

        @Nullable
        @BindView(R.id.overviewScroll)
        ScrollView overviewScroll;

        @Nullable
        @BindView(R.id.remove_from_list)
        ImageButton removeFromList;

        public MovieViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

        public void cleanUp(){
            final Context context = mView.getContext();
            Picasso.get().cancelRequest(mMovieThumb);

            mMovieThumb.setImageBitmap(null);
            mMoviePoster.setImageBitmap(null);
            mMovieThumb.setVisibility(View.INVISIBLE);
            mMoviePoster.setVisibility(View.INVISIBLE);
            mMovieRelease.setVisibility(View.GONE);
            mMovieTitle.setVisibility(View.GONE);
            mMovieOverview.setVisibility(View.GONE);
            mMovieVote.setVisibility(View.GONE);
        }
    }
}

