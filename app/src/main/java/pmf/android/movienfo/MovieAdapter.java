package pmf.android.movienfo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();
    public static final float POSTER_ASPECT_RATIO = 1.5f;

    private final ArrayList<Movie> mMovies;


    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void send_details(Movie movie, int position);
    }

    public MovieAdapter(ArrayList<Movie> movies, OnItemClickListener mItemClickListener){
        this.mMovies = movies;
        this.mOnItemClickListener = mItemClickListener;
    }



    @NonNull
    @Override
    public MovieAdapter.MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parentContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(R.layout.movie_item_home, parent, shouldAttachToParentImmediately);

        final Context context = view.getContext();
        MovieViewHolder viewHolder = new MovieViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.MovieViewHolder holder, int position) {
        final Movie movie = mMovies.get(position);
        final Context context = holder.mView.getContext();

        holder.mMovie = movie;
        String releaseYear = formatDate(movie.getReleaseDate());
        holder.mMovieRelease.setText(releaseYear);

        String posterUrl = movie.getPosterPath();

        //App crashes if url is null
        if(posterUrl == null){
            holder.mMovieRelease.setVisibility(View.VISIBLE);
        }

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

        holder.mView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mOnItemClickListener.send_details(movie, holder.getAdapterPosition());
            }
        });
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

    public void add(List<Movie> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }


    public ArrayList<Movie> getMovies() {
        return mMovies;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        public Movie mMovie;

        @BindView(R.id.movie_thumbnail)
        ImageView mMovieThumb;

        @BindView(R.id.movie_release_date)
        TextView mMovieRelease;

        public MovieViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

        public void cleanUp(){
            final Context context = mView.getContext();
            Picasso.get().cancelRequest(mMovieThumb);

            mMovieThumb.setImageBitmap(null);
            mMovieThumb.setVisibility(View.INVISIBLE);
            mMovieRelease.setVisibility(View.GONE);
        }
    }
}

