package com.importusername.musicplayer.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistItem;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistMenuAdapter;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryEntity;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryUri;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.ArrayList;

public class AddToPlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<PlaylistItem> playlistItems = new ArrayList<>();

    private final SongsMenuItem clickedSong;

    private final FragmentActivity activity;

    private OnPlaylistClickListener onPlaylistClickListener;

    private int totalPlaylists = 0;

    private Uri playlistQueryUri;

    private final SongsQueryEntity playlistQueryEntity = new SongsQueryEntity();

    public AddToPlaylistAdapter(SongsMenuItem clickedSong, FragmentActivity activity) {
        this.clickedSong = clickedSong;
        this.activity = activity;
        this.playlistQueryUri = Uri.parse(AppConfig.getProperty("url", this.activity.getApplicationContext())
                + Endpoints.GET_PLAYLISTS);

        this.populatePlaylistDataset();
    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener listener) {
        this.onPlaylistClickListener = listener;
    }

    public void addPlaylistItem(PlaylistItem playlistItem) {
        if (playlistItem != null) {
            this.playlistItems.add(playlistItem);
        }
    }

    public void populatePlaylistDataset() {
        final SongsQueryUri playlistsQueryUri = new SongsQueryUri();
        playlistsQueryUri.setSongQueryHost(this.playlistQueryUri);
        playlistsQueryUri.addQueryParam("includeTotal", "true");

        this.playlistQueryEntity.queryNextSong(
                this.activity.getApplicationContext(),
                playlistsQueryUri,
                (jsonArray, total) -> {
                    this.totalPlaylists = total;

                    if (jsonArray != null) {
                        this.activity.runOnUiThread(() -> {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    this.addPlaylistItem(new PlaylistItem(jsonArray.getJSONObject(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            this.notifyDataSetChanged();
                        });
                    }
                });
    }

    public int getPlaylistItemCount() {
        return this.playlistItems.size();
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songs_menu_music_item, parent, false);;
        final RecyclerView.ViewHolder viewHolder = new AddToPlaylistAdapter.ViewHolder(view, this.activity, this.onPlaylistClickListener);;

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        final PlaylistItem playlistItem = this.playlistItems.get(position);
        final ViewHolder playlistViewHolder = (ViewHolder) holder;

        playlistViewHolder.setText(playlistItem.getPlaylistName());

        playlistViewHolder.setClickListener(playlistItem, this.clickedSong);

        if (playlistItem.getPlaylistThumbnailId() != null) {
            playlistViewHolder.setItemThumbnail(playlistItem.getPlaylistThumbnailId().split("/")[2]);
        } else {
            playlistViewHolder.clearThumbnail();
        }

        if (this.getPlaylistItemCount() < this.totalPlaylists) {
            if (position > 0 && position % 10 == 0) {
                this.populatePlaylistDataset();
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.playlistItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentActivity activity;

        private final ConstraintLayout constraintLayout;

        private final OnPlaylistClickListener clickListener;

        public ViewHolder(@NonNull @NotNull View itemView, FragmentActivity activity, OnPlaylistClickListener clickListener) {
            super(itemView);

            this.activity = activity;
            this.clickListener = clickListener;
            this.constraintLayout = itemView.findViewById(R.id.songs_menu_music_item_container);

            this.constraintLayout.findViewById(R.id.music_item_options_button).setVisibility(View.GONE);
        }

        public void setText(String text) {
            ((TextView) this.constraintLayout.findViewById(R.id.songs_menu_music_item_title)).setText(text);
        }

        public void setItemThumbnail(String id) {
            final String url = AppConfig.getProperty("url", this.constraintLayout.getContext()) + Endpoints.GET_PLAYLIST_THUMBNAIL + "/" + id;

            final ImageView thumbnailDefaultIcon = this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo);
            final ImageView thumbnailImageView = this.constraintLayout.findViewById(R.id.songs_menu_music_item_thumbnail);

            this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo_container).setBackground(null);

            thumbnailDefaultIcon.setVisibility(View.GONE);
            thumbnailImageView.setVisibility(View.VISIBLE);

            final GlideUrl glideUrl = new GlideUrl(
                    url,
                    new LazyHeaders.Builder().addHeader("Cookie", AppCookie.getAuthCookie(this.activity)).build()
            );

            Glide.with(this.activity)
                    .load(glideUrl)
                    .into(thumbnailImageView);
        }

        public void clearThumbnail() {
            this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo_container).setBackgroundResource(R.color.songs_menu_song_default_icon_background);

            final ImageView thumbnailDefaultIcon = this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo);
            final ImageView thumbnailImageView = this.constraintLayout.findViewById(R.id.songs_menu_music_item_thumbnail);

            thumbnailImageView.setImageResource(android.R.color.transparent);

            Glide.with(this.activity)
                    .clear(thumbnailImageView);

            thumbnailDefaultIcon.setVisibility(View.VISIBLE);
            thumbnailImageView.setVisibility(View.GONE);
        }

        public void setClickListener(PlaylistItem playlistItem, SongsMenuItem songsMenuItem) {
            this.constraintLayout.setOnClickListener((view) -> {
                clickListener.click(playlistItem, songsMenuItem);
            });
        }
    }

    public interface OnPlaylistClickListener {
        void click(PlaylistItem playlistItem, SongsMenuItem songsMenuItem);
    }
}
