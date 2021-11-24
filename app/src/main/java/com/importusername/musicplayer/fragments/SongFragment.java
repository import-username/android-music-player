package com.importusername.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;

public class SongFragment extends Fragment implements IBackPressFragment {
    private SongsMenuItem songsMenuItem;

    public SongFragment() {
        super(R.layout.song_menu_layout);
    }

    public SongFragment(SongsMenuItem songsMenuItem) {
        super(R.layout.song_menu_layout);

        this.songsMenuItem = songsMenuItem;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.song_menu_layout, container, false);

        ((TextView) view.findViewById(R.id.song_menu_title_view)).setText(this.songsMenuItem.getSongName());

        if (this.songsMenuItem.getSongThumbnailId() != null) {
            final String url = AppConfig.getProperty("url", view.getContext())
                    + Endpoints.GET_THUMBNAIL + "/"
                    + this.songsMenuItem.getSongThumbnailId().split("/")[2];

            final GlideUrl glideUrl = new GlideUrl(
                    url,
                    new LazyHeaders.Builder().addHeader("Cookie", AppCookie.getAuthCookie(this.getActivity())).build()
            );

            final ImageView thumbnail = view.findViewById(R.id.song_menu_image_custom);
            final ImageView defaultThumbnail = view.findViewById(R.id.song_menu_image_default);

            defaultThumbnail.setVisibility(View.GONE);
            thumbnail.setVisibility(View.VISIBLE);

            Glide.with(this.getActivity())
                    .load(glideUrl)
                    .into(thumbnail);
        }

        if (this.songsMenuItem.getAuthor() != null) {
            ((TextView) view.findViewById(R.id.song_menu_author_view)).setText(this.songsMenuItem.getAuthor());
        }

        return view;
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
