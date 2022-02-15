package com.importusername.musicplayer.activity;

import android.net.Uri;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuListAdapter;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryEntity;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryUri;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<SongsMenuItem> playlistSongsList = new ArrayList<>();

    private final PlaylistItem playlistItem;

    private final FragmentActivity activity;

    private final int PLAYLIST_HEADER = 0;
    private final int PLAYLIST_SONG_ITEM = 1;

    private final Uri playlistSongsUri;

    private final SongsQueryEntity songsQueryEntity = new SongsQueryEntity();

    private PlaylistSongItem.OnClickListener onItemClickListener;

    public PlaylistAdapter(PlaylistItem playlistItem, FragmentActivity fragmentActivity) {
        this.playlistItem = playlistItem;
        this.activity = fragmentActivity;

        this.playlistSongsList.add(null);

        playlistSongsUri = Uri.parse(
                AppConfig.getProperty("url", this.activity)
                + Endpoints.GET_PLAYLIST_SONGS
                + "/" + this.playlistItem.getPlaylistId()
        );

        this.populatePlaylistDataset();
    }

    public void setOnItemClickListener(PlaylistSongItem.OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void populatePlaylistDataset() {
        final SongsQueryUri songsQueryUri = new SongsQueryUri();
        songsQueryUri.setSongQueryHost(this.playlistSongsUri);

        this.songsQueryEntity.queryNextSong(
                this.activity,
                songsQueryUri,
                (jsonArray, total) -> {
                    PlaylistAdapter.this.activity.runOnUiThread(() -> {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                PlaylistAdapter.this.playlistSongsList.add(new SongsMenuItem(jsonArray.getJSONObject(i)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        PlaylistAdapter.this.notifyDataSetChanged();
                    });
                }
        );
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;

        switch (this.getItemViewType(viewType)) {
            case PLAYLIST_HEADER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.playlist_menu_header, parent, false);

                viewHolder = new PlaylistHeader(view);

                break;
            case PLAYLIST_SONG_ITEM:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.songs_menu_music_item, parent, false);

                viewHolder = new PlaylistSongItem(view, this.activity);
                break;
            default:
                throw new IllegalStateException("Unexpected view type " + this.getItemViewType(viewType));
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        switch (this.getItemViewType(position)) {
            case PLAYLIST_HEADER:
                ((PlaylistHeader) holder).setTitle(this.playlistItem.getPlaylistName());

                if (this.playlistItem.getPlaylistThumbnailId() != null) {
                    ((PlaylistHeader) holder).setThumbnail(this.playlistItem.getPlaylistThumbnailId().split("/")[2]);
                }

                break;
            case PLAYLIST_SONG_ITEM:
                final SongsMenuItem songItem = this.playlistSongsList.get(position);
                final PlaylistAdapter.PlaylistSongItem playlistSongItem = (PlaylistAdapter.PlaylistSongItem) holder;

                playlistSongItem.setText(songItem.getSongName());

                if (this.onItemClickListener != null) {
                    playlistSongItem.setClickListener(this.onItemClickListener, songItem, holder.getAbsoluteAdapterPosition() - 1);
                }

                if (songItem.getSongThumbnailId() != null) {
                    ((PlaylistAdapter.PlaylistSongItem) holder).setItemThumbnail(songItem.getSongThumbnailId().split("/")[2]);
                } else {
                    ((PlaylistAdapter.PlaylistSongItem) holder).clearThumbnail();
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        return this.playlistSongsList.size();
    }

    public int getSongItemCount() {
        return this.playlistSongsList.size() - 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }

        return 1;
    }

    public static class PlaylistHeader extends RecyclerView.ViewHolder {
        private final ConstraintLayout headerLayout;

        public PlaylistHeader(@NonNull @NotNull View itemView) {
            super(itemView);

            this.headerLayout = itemView.findViewById(R.id.playlist_header_container);
        }

        public void setTitle(String title) {
            ((TextView) this.headerLayout.findViewById(R.id.playlist_menu_name)).setText(title);
        }

        public void setThumbnail(String thumbnailId) {
            final String url = AppConfig.getProperty("url", this.headerLayout.getContext()) + Endpoints.GET_PLAYLIST_THUMBNAIL + "/" + thumbnailId;

            this.headerLayout.findViewById(R.id.playlist_menu_default_thumbnail).setVisibility(View.GONE);
            final ImageView thumbnailView = this.headerLayout.findViewById(R.id.playlist_menu_custom_thumbnail);
            thumbnailView.setVisibility(View.VISIBLE);

            final GlideUrl glideUrl = new GlideUrl(
                    url,
                    new LazyHeaders.Builder().addHeader("Cookie", AppCookie.getAuthCookie(this.headerLayout.getContext())).build()
            );

            Glide.with(this.headerLayout.getContext())
                    .load(glideUrl)
                    .into(thumbnailView);
        }
    }

    public static class PlaylistSongItem extends RecyclerView.ViewHolder {
        private final ConstraintLayout constraintLayout;

        private final FragmentActivity activity;

        public PlaylistSongItem(@NonNull @NotNull View itemView, FragmentActivity activity) {
            super(itemView);

            this.activity = activity;
            this.constraintLayout = itemView.findViewById(R.id.songs_menu_music_item_container);
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

        public void setClickListener(PlaylistSongItem.OnClickListener onClickListener, SongsMenuItem songsMenuItem, int index) {
            this.constraintLayout.setOnClickListener((v) -> {
                onClickListener.click(songsMenuItem, index);
            });
        }

        public interface OnClickListener {
            void click(SongsMenuItem songItem, int index);
        }
    }
}
