package pmf.android.movienfo.utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.Objects;

@SuppressLint("Registered")
public class MovienfoUtilities extends AppCompatActivity {

    public static Bitmap getBitmapFromVectorDrawable(Context context, int id) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && drawable != null)  {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        Bitmap bitmap = null;
        if (drawable != null) bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = null;
        if (bitmap != null) canvas = new Canvas(bitmap);

        if (drawable != null && canvas != null) {
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }

    //Depending on the minimum screen width app chooses if setting FragmentItemClickListener is needed or not
    public static boolean showMovieOnCurrentActivity(Activity activity, float densityDpi){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return (displayMetrics.widthPixels / (densityDpi / DisplayMetrics.DENSITY_DEFAULT)) > 600;
    }

    public static String formatDateString(String date){
        String[] numbers = date.split("-");
        return numbers[2] + "." + numbers[1] + "." + numbers[0] + ".";
    }

    public static String parseUrl(String url){
        String[] posterPathHttp = Objects.requireNonNull(url.split(":"));
        return posterPathHttp[0] + "s:" + posterPathHttp[1];
    }
}
