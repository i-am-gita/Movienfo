package pmf.android.movienfo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.model.Movie;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();
    public final static int TYPE_1 = 1;
    public final static int TYPE_2 = 2;

    private List<Movie> movies;

    private int layoutId;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void sendDetails(Movie movie, int position);
    }

    public MovieAdapter(Context context, List<Movie> movies , int layoutId){
        this.context = context;
        this.movies = movies;
        this.layoutId = layoutId;
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }

    public void updateMoviesList(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public boolean shouldBeViewType1(int position) {
        return position % 5 == 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (shouldBeViewType1(position)) {
            return TYPE_1;
        } else {
            return TYPE_2;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutId, parent, false);
        if (viewType == TYPE_1) {
            return new MovieViewHolder(view);
        } else {
            return new MovieViewHolder2(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final Movie movie = movies.get(position);

        if(shouldBeViewType1(position)){
            MovieViewHolder holder = (MovieViewHolder) viewHolder;
            holder.bind(movie);

//            if(!listType.equals("favourites") && !listType.equals("watchlist")) {
//                holder.removeFromList.setVisibility(View.GONE);
//            }else{
//                holder.removeFromList.setOnClickListener(v -> onItemClickListener.sendDetails(movie, holder.getAdapterPosition()));
//            }
        }else{
            MovieViewHolder2 holder = (MovieViewHolder2) viewHolder;
            holder.bind(movie);
        }

        viewHolder.itemView.setOnClickListener(v -> onItemClickListener.sendDetails(movie, viewHolder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        //holder.cleanUp();
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.movie_thumbnail)
        ImageView mMovieThumb;

        @BindView(R.id.search_movie_poster)
        ImageView mMoviePoster;

        @BindView(R.id.movie_release_date)
        TextView mMovieRelease;

        @BindView(R.id.search_movie_title)
        TextView mMovieTitle;

        @BindView(R.id.search_movie_overview)
        TextView mMovieOverview;

        @BindView(R.id.search_movie_vote)
        TextView mMovieVote;

        @BindView(R.id.overviewScroll)
        ScrollView overviewScroll;

        @BindView(R.id.remove_from_list)
        ImageButton removeFromList;

        public MovieViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(Movie movie) {
            mMovieTitle.setText(movie.getOriginalTitle());
            mMovieVote.setText(movie.getVoteAverage());
            mMovieOverview.setText(movie.getOverview());
            overviewScroll.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });


            Picasso.get()
                    .load(movie.getPosterPath())
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.image_placeholder_movie_lists)
                    .into(mMoviePoster, new Callback() {
                        @Override
                        public void onSuccess() {
                            mMoviePoster.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            mMovieTitle.setVisibility(View.VISIBLE);
                        }
                    });
        }

        // Might be redundant
        public void cleanUp(){
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

    public static class MovieViewHolder2 extends RecyclerView.ViewHolder{
        @BindView(R.id.movie_thumbnail)
        ImageView mMovieThumb;

        @BindView(R.id.search_movie_poster)
        ImageView mMoviePoster;

        @BindView(R.id.movie_release_date)
        TextView mMovieRelease;

        @BindView(R.id.search_movie_title)
        TextView mMovieTitle;

        @BindView(R.id.search_movie_overview)
        TextView mMovieOverview;

        @BindView(R.id.search_movie_vote)
        TextView mMovieVote;

        @BindView(R.id.overviewScroll)
        ScrollView overviewScroll;

        @BindView(R.id.remove_from_list)
        ImageButton removeFromList;

        public MovieViewHolder2(View view){
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(Movie movie) {
            String releaseYear = formatDate(movie.getReleaseDate());
            mMovieRelease.setText(releaseYear);

            Picasso.get()
                    .load(movie.getPosterPath())
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.image_placeholder)
                    .into(mMovieThumb, new Callback() {
                        @Override
                        public void onSuccess() {
                            mMovieThumb.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            mMovieRelease.setVisibility(View.VISIBLE);
                        }
                    });
        }

        private String formatDate(String date){
            String[] splitedDate = date.split("-");
            return splitedDate[0];
        }

        // Might be redundant
        public void cleanUp(){
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

