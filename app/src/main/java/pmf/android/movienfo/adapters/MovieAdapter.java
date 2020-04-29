package pmf.android.movienfo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pmf.android.movienfo.R;
import pmf.android.movienfo.model.Movie;
import pmf.android.movienfo.utilities.MovienfoUtilities;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int HOME_MOVIE_TYPE = 1;
    private final static int LIST_MOVIE_TYPE = 2;

    private List<Movie> movies;

    private int layoutId;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private OnFragmentItemClickListener onFragmentItemClickListener;
    private String listType;

    public interface OnItemClickListener {
        void sendDetails(Movie movie, int position);
    }

    public interface OnFragmentItemClickListener{
        void sendData(Movie movie, int position);
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

    public void setOnFragmentItemClickListener(OnFragmentItemClickListener mFragmentItemCLickListener){
        this.onFragmentItemClickListener = mFragmentItemCLickListener;
    }

    public void updateMoviesList(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    private boolean isHomeMovieType(int layoutId) {
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

        //Displaying movies in recycles at HomeActivity
        if(isHomeMovieType(layoutId)){
            HomeMovieViewHolder holder = (HomeMovieViewHolder) viewHolder;
            holder.bind(movie);
            holder.mMovieThumb.setOnClickListener(v -> onItemClickListener.sendDetails(movie, holder.getAdapterPosition()));

        } else{
            ListMovieViewHolder holder = (ListMovieViewHolder) viewHolder;

            //Displaying movie details in MovieListActivity
            if(showMovieOnCurrentActivity()){
                holder.mMovieOverview.setVisibility(View.GONE);
                holder.mMovieTitle.setVisibility(View.GONE);
                holder.mMovieVote.setVisibility(View.GONE);
            }
            //Displaying movie details in MovieDetailsActivity
            else{
                holder.mMovieOverview.setText(movie.getOverview());
            }
            holder.bind(movie);

            //If lists are not favorites or watchlist, button for removing shouldn't be desplayed
            if(!listType.equals("favourites") && !listType.equals("watchlist")){
                holder.removeFromList.setVisibility(View.GONE);
            }else{
                holder.removeFromList.setOnClickListener(remove -> onItemClickListener.sendDetails(movie, holder.getAdapterPosition()));
            }
            //When onFragmentItemClickListener is NOT NULL that means that movie should be displayed on the current activity
            if(onFragmentItemClickListener != null) {
                holder.mMoviePoster.setOnClickListener(show -> onFragmentItemClickListener.sendData(movie,holder.getAdapterPosition()));
                holder.mMovieOverview.setVisibility(View.GONE);
            }
            //If onFragmentItemClickListener is null, that means that movie details should be opened in another activity
            else{
                holder.mMoviePoster.setOnClickListener(go -> onItemClickListener.sendDetails(movie,-100));
            }

        }
    }

   private boolean showMovieOnCurrentActivity(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Objects.requireNonNull(wm).getDefaultDisplay().getMetrics(displayMetrics);

        return (displayMetrics.widthPixels / (context.getResources().getDisplayMetrics().densityDpi/ DisplayMetrics.DENSITY_DEFAULT)) > 600;
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    static class ListMovieViewHolder extends RecyclerView.ViewHolder{
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

        ListMovieViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }

        @SuppressLint("ClickableViewAccessibility")
        void bind(Movie movie) {
            mMovieTitle.setText(movie.getOriginalTitle());
            mMovieVote.setText(movie.getVoteAverage());
            mMovieOverview.setText(movie.getOverview());
            overviewScroll.setOnTouchListener((v, event) -> { v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
            Picasso.get()
                    .load(MovienfoUtilities.parseUrl(Objects.requireNonNull(movie.getPosterPath())))
                    .config(Bitmap.Config.RGB_565)
                    .placeholder(R.drawable.image_placeholder)
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

    static class HomeMovieViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.movie_thumbnail)
        ImageView mMovieThumb;

        @BindView(R.id.movie_release_date)
        TextView mMovieRelease;


        HomeMovieViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(Movie movie) {
            String releaseYear = formatDate(movie.getReleaseDate());
            mMovieRelease.setText(releaseYear);

            Picasso.get()
                    .load(MovienfoUtilities.parseUrl(Objects.requireNonNull(movie.getPosterPath())))
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

