package com.importusername.musicplayer.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.importusername.musicplayer.R;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class CreateSongImageLayout extends ConstraintLayout {
    public CreateSongImageLayout(@NonNull @NotNull Context context) {
        super(context);
    }

    public CreateSongImageLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CreateSongImageLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CreateSongImageLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Creates bitmap image from provided input stream and sets custom image view's image to the bitmap.
     * Hides default image view and displays custom image view.
     */
    public void setCustomImage(InputStream inputStream) {
        final ImageView customImage = ((ImageView) this.getViewById(R.id.create_song_menu_image_custom));

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        // TODO - add conditional for invalid files

        customImage.setImageBitmap(bitmap);
        this.toggleDefaultImage(false);
        this.toggleCustomImage(true);
    }

    /**
     * Removes custom image selected by user and sets default imageview to visible.
     */
    public void removeCustomImage() {
        final ImageView customImage = ((ImageView) this.getViewById(R.id.create_song_menu_image_custom));
        customImage.setVisibility(View.GONE);
        customImage.setImageDrawable(null);

        this.toggleDefaultImage(true);
    }

    /**
     * Toggles custom image view to provided value.
     * @param visible boolean value
     */
    public void toggleCustomImage(boolean visible) {
        final ImageView customImage = ((ImageView) this.getViewById(R.id.create_song_menu_image_custom));

        if (visible) {
            customImage.setVisibility(View.VISIBLE);
        } else {
            customImage.setVisibility(View.GONE);
        }
    }

    /**
     * Toggles default image view to provided value.
     * @param visible boolean value
     */
    public void toggleDefaultImage(boolean visible) {
        final ImageView defaultImage = ((ImageView) this.getViewById(R.id.create_song_menu_image_default));

        if (visible) {
            defaultImage.setVisibility(View.VISIBLE);
            this.setBackgroundColor(getResources().getColor(R.color.songs_menu_song_default_icon_background, null));
        } else {
            this.setBackgroundColor(Color.TRANSPARENT);
            defaultImage.setVisibility(View.GONE);
        }
    }
}
