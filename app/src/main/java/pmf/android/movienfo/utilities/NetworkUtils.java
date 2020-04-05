package pmf.android.movienfo.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

import pmf.android.movienfo.Movie;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static ArrayList<Movie> fetchData(String url) throws IOException{
        ArrayList<Movie> movies = new ArrayList<Movie>();
        try{
            URL new_url = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) new_url.openConnection();
            conn.connect();

            InputStream inputStream = conn.getInputStream();
            String results = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
            parseJson(results,movies);
            inputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return movies;
    }

    public static void parseJson(String data, ArrayList<Movie> list){
        try{
            JSONObject mainObject = new JSONObject(data);

            JSONArray resArray = mainObject.getJSONArray("results");

            for(int i = 0; i < resArray.length(); i++){
                JSONObject jsonObject = resArray.getJSONObject(i);
                Movie movie = new Movie();
                movie.setId(jsonObject.getLong("id"));
                movie.setVoteAverage(jsonObject.getString("vote_average"));
                movie.setOriginalTitle(jsonObject.getString("original_title"));
                movie.setBackdropPath(jsonObject.getString("backdrop_path"));
                movie.setOverview(jsonObject.getString("overview"));
                if(jsonObject.has("release_date")) {
                    movie.setReleaseDate(jsonObject.getString("release_date"));
                }
                movie.setPosterPath(jsonObject.getString("poster_path"));

                list.add(movie);
            }
        }catch(JSONException e){
            e.printStackTrace();
            Log.e(TAG, "Error occurred during JSON Parsing", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Boolean networkStatus(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(manager.getActiveNetwork() != null)
            return true;
        else
            return false;
    }
}
