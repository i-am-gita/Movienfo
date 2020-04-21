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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.model.Movie;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();
    public final static int HOME_MOVIE_TYPE = 1;
    public final static int LIST_MOVIE_TYPE = 2;

    private List<Movie> movies;

    private int layoutId;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private String listType;

    public interface OnItemClickListener {
        void sendDetails(Movie movie, int position);
    }

    public MovieAdapter(Context context, List<Movie> movies , int layoutId){
        this.context = context;
        this.movies = movies;
        this.layoutId = layoutId;
    }
    public MovieAdapter(Context context, List<Movie> movies , int layoutId, String listType){
        this.context = context;
        this.movies = movies;
        this.layoutId = layoutId;
        this.listType = listType;
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }

    public void updateMoviesList(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    public boolean isHomeMovieType(int layoutId) {
        return layoutId == R.layout.item_movie_home;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHomeMovieType(layoutId)) {
                return HOME_MOVIE_TYPE;
            } else {
                return LIST_MOVIE_TYPE;
            }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutId, parent, false);
        if (viewType == HOME_MOVIE_TYPE) {
            return new HomeMovieViewHolder(view);
        } else {
            return new ListMovieViewHolder(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final Movie movie = movies.get(position);
        if(isHomeMovieType(layoutId)){
            HomeMovieViewHolder holder = (HomeMovieViewHolder) viewHolder;
            holder.bind(movie);
            holder.mMovieThumb.setOnClickListener(v -> onItemClickListener.sendDetails(movie, holder.getAdapterPosition()));
        }else{
            ListMovieViewHolder holder = (ListMovieViewHolder) viewHolder;
            holder.bind(movie);
            if(!listType.equals("favourites") && !listType.equals("watchlist")){
                holder.removeFromList.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(iv -> onItemClickListener.sendDetails(movie, holder.getAdapterPosition()));
            }else{
                holder.removeFromList.setOnClickListener(rfl -> onItemClickListener.sendDetails(movie, holder.getAdapterPosition()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public static class ListMovieViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.search_movie_poster)
        ImageView mMoviePoster;

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

        public ListMovieViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void bind(Movie movie) {
            mMovieTitle.setText(movie.getOriginalTitle());
            mMovieVote.setText(movie.getVoteAverage());
            mMovieOverview.setText(movie.getOverview());
            overviewScroll.setOnTouchListener((v, event) -> { v.getParent().requestDisallowInterceptTouchEvent(true);
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
    }

    public static class HomeMovieViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.movie_thumbnail)
        ImageView mMovieThumb;

        @BindView(R.id.movie_release_date)
        TextView mMovieRelease;


        public HomeMovieViewHolder(View view){
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
    }
}

