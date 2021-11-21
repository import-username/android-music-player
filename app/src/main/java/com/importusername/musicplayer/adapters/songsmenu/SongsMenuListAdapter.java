package com.importusername.musicplayer.adapters.songsmenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.interfaces.IThumbnailRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.threads.ThumbnailRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class SongsMenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<SongsMenuItem> songsMenuArray = new ArrayList<>();

    private final FragmentActivity activity;

    private final int VIEW_HEADER = 0;
    private final int VIEW_MUSIC_ITEM = 1;

    private View.OnClickListener addButtonListener;

    /**
     * Initialize songs menu array
     * @param songsMenuArray Array containing list of user songs.
     */
    public SongsMenuListAdapter(ArrayList<SongsMenuItem> songsMenuArray, FragmentActivity activity, View.OnClickListener addButtonListener) {
        this.songsMenuArray.add(null);
        this.songsMenuArray.addAll(songsMenuArray);
        this.activity = activity;
        this.addButtonListener = addButtonListener;
    }

    /**
     * Adds music item to songs menu array and notifies adapter of data change.
     * @param songsMenuItem Songs menu item object representing song data received from server.
     * @param fetchThumbnail Boolean value to determine if get request should be sent to receive the song item's thumbnail image.
     * @throws JSONException
     */
    public void addItem(SongsMenuItem songsMenuItem, boolean fetchThumbnail) throws JSONException {
        if (songsMenuItem != null) {
            this.songsMenuArray.add(songsMenuItem);

            this.notifyDataSetChanged();
        }
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;

        switch (this.getItemViewType(viewType)) {
            case VIEW_HEADER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.songs_menu_header_container, parent, false);

                viewHolder = new ViewHolderHeader(view, this.addButtonListener);
                break;
            case VIEW_MUSIC_ITEM:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.songs_menu_music_item, parent, false);

                viewHolder = new ViewHolder(view, this.activity);
                break;
            default:
                throw new IllegalStateException("Unexpected view type " + this.getItemViewType(viewType));
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        switch (this.getItemViewType(position)) {
            case VIEW_HEADER:
                break;
            case VIEW_MUSIC_ITEM:
                final SongsMenuItem songItem = this.songsMenuArray.get(position);
                ((ViewHolder) holder).setText(songItem.getSongName());

                if (songItem.getSongThumbnailId() != null) {
                    ((ViewHolder) holder).setItemThumbnail(songItem.getSongThumbnailId().split("/")[2]);
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        return this.songsMenuArray.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }

        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentActivity fragmentActivity;

        private final ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull @NotNull View itemView, FragmentActivity activity) {
            super(itemView);

            this.fragmentActivity = activity;
            constraintLayout = itemView.findViewById(R.id.songs_menu_music_item_container);
        }

        public void setText(String text) {
            ((TextView) this.constraintLayout.findViewById(R.id.songs_menu_music_item_title)).setText(text);
        }

        public void setItemThumbnail(String id) {
            final String url = AppConfig.getProperty("url", this.constraintLayout.getContext()) + Endpoints.GET_THUMBNAIL + "/" + id;

            final ImageView thumbnailDefaultIcon = this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo);
            final ImageView thumbnailImageView = this.constraintLayout.findViewById(R.id.songs_menu_music_item_thumbnail);

            this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo_container).setBackground(null);

            thumbnailDefaultIcon.setVisibility(View.GONE);
            thumbnailImageView.setVisibility(View.VISIBLE);

            final ThumbnailRequestThread requestThread = new ThumbnailRequestThread(
                    url,
                    constraintLayout.getContext(),
                    this.requestAction(thumbnailDefaultIcon, thumbnailImageView)
            );

            requestThread.start();
        }

        private IThumbnailRequestAction requestAction(ImageView defaultImg, ImageView thumbnailImg) {
            return (stream) -> {
                if (stream != null) {
                    ViewHolder.this.fragmentActivity.runOnUiThread(() -> {
                        defaultImg.setVisibility(View.GONE);
                        thumbnailImg.setVisibility(View.VISIBLE);

                        this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo_container).setBackground(null);

                        Bitmap bitmap = BitmapFactory.decodeStream(stream);

                        thumbnailImg.setImageBitmap(bitmap);
                    });
                }
            };
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        private final ConstraintLayout constraintLayoutHeader;

        private final View.OnClickListener addButtonListener;

        public ViewHolderHeader(@NonNull @NotNull View itemView, View.OnClickListener addButtonListener) {
            super(itemView);

            this.addButtonListener = addButtonListener;

            constraintLayoutHeader = itemView.findViewById(R.id.songs_menu_header_container);
            itemView.findViewById(R.id.songs_menu_add_song_button).setOnClickListener(this.addButtonListener);
        }
    }
}
