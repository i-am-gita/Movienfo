package pmf.android.movienfo.utilities;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pmf.android.movienfo.model.Movie;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class MovienfoRoomDatabase extends RoomDatabase {

    private static MovienfoRoomDatabase instance;
    public abstract MovieDao movieDao();

    public static  MovienfoRoomDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), MovienfoRoomDatabase.class, "recentMovies").allowMainThreadQueries().build();
        }
        return instance;
    }
}
