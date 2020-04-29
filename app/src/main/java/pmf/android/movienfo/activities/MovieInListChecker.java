package pmf.android.movienfo.activities;

import java.util.ArrayList;

import pmf.android.movienfo.model.Movie;

public interface MovieInListChecker{
    boolean listContainsMovie(Movie movie, ArrayList<Movie> list);
}
