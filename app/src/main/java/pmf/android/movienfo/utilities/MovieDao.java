package pmf.android.movienfo.utilities;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import pmf.android.movienfo.model.Movie;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie")
    List<Movie> getAll();

    @Query("SELECT * FROM movie WHERE id == (:movieId) ")
    Movie getMovie(long movieId);

    @Insert
    void insert(Movie movie);

    @Delete
    void delete(Movie movie);
}
