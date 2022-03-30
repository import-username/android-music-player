package com.importusername.musicplayer.adapters.playlistmenu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.RepeatModeUtil;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryEntity;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryUri;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.OnRefreshComplete;
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

    private OnRefreshComplete onRefreshComplete;

    private ExoPlayer player;

    private OnBackPressed onBackPressed;

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

    public void setOnBackPressed(OnBackPressed onBackPressed) {
        this.onBackPressed = onBackPressed;
    }

    public void setExoplayer(ExoPlayer player) {
        this.player = player;
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

                        if (this.onRefreshComplete != null) {
                            this.onRefreshComplete.refresh();
                        }

                        PlaylistAdapter.this.notifyDataSetChanged();
                    });
                }
        );
    }

    public void refreshDataset() {
        this.playlistSongsList.clear();
        this.playlistSongsList.add(null);

        this.notifyDataSetChanged();

        this.songsQueryEntity.reset();

        this.populatePlaylistDataset();
    }

    public void setOnMenuRefresh(OnRefreshComplete onRefreshComplete) {
        this.onRefreshComplete = onRefreshComplete;
    }

    public ArrayList<SongsMenuItem> getPlaylistSongsList() {
        return new ArrayList<>(this.playlistSongsList);
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
                        .inflate(R.layout.playlist_music_item, parent, false);

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

                if (this.onBackPressed != null) {
                    ((PlaylistHeader) holder).setOnBackPressed(this.onBackPressed);
                }

                if (this.playlistItem.getPlaylistThumbnailId() != null) {
                    ((PlaylistHeader) holder).setThumbnail(this.playlistItem.getPlaylistThumbnailId().split("/")[2]);
                }

                if (this.player != null) {
                    Log.i("PLAYLIST ADAPTER", "Attempting to set exoplayer.");

                    ((PlaylistHeader) holder).setExoplayer(this.player);
                }

                break;
            case PLAYLIST_SONG_ITEM:
                final SongsMenuItem songItem = this.playlistSongsList.get(position);
                final PlaylistAdapter.PlaylistSongItem playlistSongItem = (PlaylistAdapter.PlaylistSongItem) holder;

                playlistSongItem.setText(songItem.getSongName());

                playlistSongItem.setOnOptionsClickListener(songItem, this.playlistItem);

                playlistSongItem.setOnDeleteListener((deletedSongItem, playlistItem) -> {
                    final int deletedItemPos = holder.getAbsoluteAdapterPosition();

                    this.playlistSongsList.remove(deletedItemPos);

                    this.activity.runOnUiThread(() -> {
                        this.notifyItemRemoved(deletedItemPos);
                    });
                });

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

        private OnBackPressed onBackPressed;

        public PlaylistHeader(@NonNull @NotNull View itemView) {
            super(itemView);

            this.headerLayout = itemView.findViewById(R.id.playlist_header_container);
            this.headerLayout.findViewById(R.id.playlist_menu_song_name).setSelected(true);

            this.headerLayout.findViewById(R.id.playlist_menu_back_button).setOnClickListener((v) -> {
                if (this.onBackPressed != null) {
                    this.onBackPressed.backPress();
                }
            });

            this.headerLayout.findViewById(R.id.playlist_menu_media_overlay).setOnClickListener((v) -> {
                this.headerLayout.findViewById(R.id.playlist_menu_media_overlay).setVisibility(View.GONE);
            });

            this.headerLayout.findViewById(R.id.playlist_menu_default_thumbnail).setOnClickListener((v) -> {
                this.headerLayout.findViewById(R.id.playlist_menu_media_overlay).setVisibility(View.VISIBLE);
            });

            this.headerLayout.findViewById(R.id.playlist_menu_custom_thumbnail).setOnClickListener((v) -> {
                this.headerLayout.findViewById(R.id.playlist_menu_media_overlay).setVisibility(View.VISIBLE);
            });
        }

        public void setOnBackPressed(OnBackPressed onBackPressed) {
            this.onBackPressed = onBackPressed;
        }

        public void setTitle(String title) {
            ((TextView) this.headerLayout.findViewById(R.id.playlist_menu_name)).setText(title);
        }

        public void setPlayingTitle(String title) {
            ((TextView) this.headerLayout.findViewById(R.id.playlist_menu_song_name)).setText(title);
        }

        public void setExoplayer(ExoPlayer player) {
            final PlayerControlView playerControlView = this.headerLayout.findViewById(R.id.player_view);
            playerControlView.setShowNextButton(true);
            playerControlView.setShowPreviousButton(true);
            playerControlView.setShowShuffleButton(true);

            playerControlView.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL);

            playerControlView.setShowTimeoutMs(0);

            playerControlView.setPlayer(player);
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

        public Drawable getThumbnail() {
            final ImageView thumbnailView = this.headerLayout.findViewById(R.id.playlist_menu_custom_thumbnail);

            return thumbnailView.getDrawable();
        }
    }

    public static class PlaylistSongItem extends RecyclerView.ViewHolder {
        private final ConstraintLayout constraintLayout;

        private final FragmentActivity activity;

        private OnDeleteListener onDeleteListener;

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

        public Drawable getThumbnail() {
            final ImageView thumbnailView = this.constraintLayout.findViewById(R.id.songs_menu_music_item_thumbnail);

            return thumbnailView.getDrawable();
        }

        public void setOnOptionsClickListener(SongsMenuItem songsMenuItem, PlaylistItem playlistItem) {
            this.constraintLayout.findViewById(R.id.music_item_options_button).setOnClickListener((v) -> {
                final Context contextWrapper = new ContextThemeWrapper(this.activity, R.style.PopupMenu);

                final PopupMenu popupMenu = new PopupMenu(contextWrapper, v);

                popupMenu.setOnMenuItemClickListener((menuItem) -> {
                    switch (menuItem.getTitle().toString()) {
                        case "Remove from playlist":
                            final String url = AppConfig.getProperty("url", this.activity)
                                    + Endpoints.REMOVE_SONG_FROM_PLAYLIST
                                    + "/" + songsMenuItem.getSongId()
                                    + "/" + playlistItem.getPlaylistId();

                            final MusicPlayerRequestThread thread = new MusicPlayerRequestThread(
                                    url,
                                    RequestMethod.PATCH,
                                    this.activity,
                                    true,
                                    (status, response, headers) -> {
                                        if (status == 200 && PlaylistSongItem.this.onDeleteListener != null) {
                                            PlaylistSongItem.this.onDeleteListener.delete(songsMenuItem, playlistItem);
                                        }
                                    }
                            );

                            thread.start();
                            break;
                    }

                    return true;
                });

                popupMenu.inflate(R.menu.playlist_song_item_popup);
                popupMenu.show();
            });
        }

        public void setClickListener(PlaylistSongItem.OnClickListener onClickListener, SongsMenuItem songsMenuItem, int index) {
            this.constraintLayout.setOnClickListener((v) -> {
                onClickListener.click(songsMenuItem, index);
            });
        }

        public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
            this.onDeleteListener = onDeleteListener;
        }

        public interface OnClickListener {
            void click(SongsMenuItem songItem, int index);
        }

        public interface OnDeleteListener {
            void delete(SongsMenuItem songsMenuItem, PlaylistItem playlistItem);
        }
    }

    public interface OnBackPressed {
        void backPress();
    }
}
