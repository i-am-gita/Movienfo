package pmf.android.movienfo.utilities;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pmf.android.movienfo.model.Movie;

public class Firebase {

    private static FirebaseDatabase instance;

    public static  FirebaseDatabase getInstance() {
        if (instance == null) {
            instance = FirebaseDatabase.getInstance();
        }
        return instance;
    }

    public static void  addMovieFirebase(DatabaseReference ref, String collection, Movie mMovie){
        if(collection.equals("favorites")) {
            ref.child("favorites").child(mMovie.getId()+"").setValue(mMovie);
        }else{
            ref.child("watchlist").child(mMovie.getId()+"").setValue(mMovie);
        }
    }

    public static void deleteMovieFirebase(DatabaseReference ref, String collection, Movie mMovie){
        if(collection.equals("favorites")){
            ref.child("favorites").child(mMovie.getId().toString()).removeValue();
        }else{
            ref.child("watchlist").child(mMovie.getId().toString()).removeValue();
        }
    }
}
