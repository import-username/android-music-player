package com.importusername.musicplayer.adapters.playlistmenu;

import android.content.Context;
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
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryEntity;
import com.importusername.musicplayer.adapters.songsmenu.SongsQueryUri;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaylistMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<PlaylistItem> playlistMenuArray = new ArrayList<>();

    private final FragmentActivity activity;

    private final int VIEW_HEADER = 0;
    private final int VIEW_PLAYLIST_ITEM = 1;

    private final View.OnClickListener addPlaylistListener;

    private final OnPlaylistClick playlistClickListener;

    private int totalPlaylists = 0;

    private Uri playlistQueryUri;

    private final SongsQueryEntity playlistQueryEntity = new SongsQueryEntity();

    public PlaylistMenuAdapter(ArrayList<PlaylistItem> playlistMenuArray, FragmentActivity activity,
                               View.OnClickListener addPlaylistListener, OnPlaylistClick playlistClickListener) {
        this.playlistMenuArray.add(null);
        this.playlistMenuArray.addAll(playlistMenuArray);
        this.activity = activity;
        this.addPlaylistListener = addPlaylistListener;
        this.playlistClickListener = playlistClickListener;
        this.playlistQueryUri = Uri.parse(AppConfig.getProperty("url", this.activity.getApplicationContext())
                + Endpoints.GET_PLAYLISTS);

        this.populatePlaylistDataset();
    }

    public void addPlaylistItem(PlaylistItem playlistItem) {
        if (playlistItem != null) {
            this.playlistMenuArray.add(playlistItem);
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
                    PlaylistMenuAdapter.this.totalPlaylists = total;

                    if (jsonArray != null) {
                        PlaylistMenuAdapter.this.activity.runOnUiThread(() -> {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    PlaylistMenuAdapter.this.addPlaylistItem(new PlaylistItem(jsonArray.getJSONObject(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            PlaylistMenuAdapter.this.notifyDataSetChanged();

                            final RecyclerView recyclerView = PlaylistMenuAdapter.this.activity.findViewById(R.id.playlist_menu_recyclerview);
                            // TODO - remove load screen here
    //                        final ConstraintLayout constraintLayout = PlaylistMenuAdapter.this.activity.findViewById(R.id.songs_menu_loading_view);

    //                        if (recyclerView.getVisibility() == View.GONE) {
    //                            constraintLayout.setVisibility(View.GONE);
    //                            recyclerView.setVisibility(View.VISIBLE);
    //                        }
                        });
                    }
                });
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
                        .inflate(R.layout.playlist_menu_header_container, parent, false);

                view.findViewById(R.id.playlist_menu_search_bar_input)
                        .setOnKeyListener((v, keyCode, event) -> {
//                            final String text = ((EditText) v).getText().toString();
//
//                            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
//                                PlaylistMenuAdapter.this.changeQueryUrl(Uri.parse(
//                                        AppConfig.getProperty("url", PlaylistMenuAdapter.this.activity.getApplicationContext())
//                                                + Endpoints.GET_SONGS
//                                                + (text.length() > 0 ? "?titleIncludes=" + ((EditText) v).getText() : "")
//                                ));
//                            }
//
                            return true;
                        });

                viewHolder = new ViewHolderHeader(view, this.addPlaylistListener);

                break;
            case VIEW_PLAYLIST_ITEM:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.songs_menu_music_item, parent, false);

                viewHolder = new ViewHolder(view, this.activity, this.playlistClickListener);

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
            case VIEW_PLAYLIST_ITEM:
                final PlaylistItem playlistItem = this.playlistMenuArray.get(position);
                final PlaylistMenuAdapter.ViewHolder playlistViewHolder = (PlaylistMenuAdapter.ViewHolder) holder;

                playlistViewHolder.setText(playlistItem.getPlaylistName());
                playlistViewHolder.setOptionsClickListener(playlistItem, (item) -> {
                    final int deletedItemPos = holder.getAbsoluteAdapterPosition();

                    PlaylistMenuAdapter.this.playlistMenuArray.remove(deletedItemPos);

                    PlaylistMenuAdapter.this.activity.runOnUiThread(() -> {
                        PlaylistMenuAdapter.this.notifyItemRemoved(deletedItemPos);
                    });
                });
                playlistViewHolder.setClickListener(playlistItem);

                if (playlistItem.getPlaylistThumbnailId() != null) {
                    playlistViewHolder.setItemThumbnail(playlistItem.getPlaylistThumbnailId().split("/")[2]);
                } else {
                    playlistViewHolder.clearThumbnail();
                }

                break;
        }

        if (this.getPlaylistItemCount() < this.totalPlaylists) {
            if (position > 0 && position % 10 == 0) {
                this.populatePlaylistDataset();
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.playlistMenuArray.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }

        return 1;
    }

    public int getPlaylistItemCount() {
        return this.playlistMenuArray.size() - 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentActivity fragmentActivity;

        private final ConstraintLayout constraintLayout;

        private final OnPlaylistClick clickListener;

        public ViewHolder(@NonNull @NotNull View itemView, FragmentActivity activity, OnPlaylistClick clickListener) {
            super(itemView);

            this.fragmentActivity = activity;
            this.constraintLayout = itemView.findViewById(R.id.songs_menu_music_item_container);
            this.clickListener = clickListener;
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
                    new LazyHeaders.Builder().addHeader("Cookie", AppCookie.getAuthCookie(this.fragmentActivity)).build()
            );

            Glide.with(this.fragmentActivity)
                    .load(glideUrl)
                    .into(thumbnailImageView);
        }

        public void clearThumbnail() {
            this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo_container).setBackgroundResource(R.color.songs_menu_song_default_icon_background);

            final ImageView thumbnailDefaultIcon = this.constraintLayout.findViewById(R.id.songs_menu_music_item_logo);
            final ImageView thumbnailImageView = this.constraintLayout.findViewById(R.id.songs_menu_music_item_thumbnail);

            thumbnailImageView.setImageResource(android.R.color.transparent);

            Glide.with(this.fragmentActivity)
                    .clear(thumbnailImageView);

            thumbnailDefaultIcon.setVisibility(View.VISIBLE);
            thumbnailImageView.setVisibility(View.GONE);
        }

        public void setClickListener(PlaylistItem item) {
            this.constraintLayout.setOnClickListener((View view) -> {
                PlaylistMenuAdapter.ViewHolder.this.clickListener.click(item);
            });
        }

        public void setOptionsClickListener(PlaylistItem item, OnDeleteListener deleteListener) {
            this.constraintLayout.findViewById(R.id.music_item_options_button).setOnClickListener((v) -> {
                final Context contextWrapper = new ContextThemeWrapper(this.fragmentActivity, R.style.PopupMenu);

                final PopupMenu popupMenu = new PopupMenu(contextWrapper, v);

                popupMenu.setOnMenuItemClickListener((menuItem) -> {
                    switch (menuItem.getTitle().toString()) {
                        case "Delete":
                            final String url = AppConfig.getProperty("url", this.fragmentActivity)
                                    + Endpoints.DELETE_PLAYLIST
                                    + "/"
                                    + item.getPlaylistId();

                            final MusicPlayerRequestThread thread = new MusicPlayerRequestThread(
                                    url,
                                    RequestMethod.DELETE,
                                    this.fragmentActivity,
                                    true,
                                    (status, response, headers) -> {
                                        if (status == 200) {
                                            deleteListener.delete(item);
                                        }
                                    }
                            );

                            thread.start();
                            break;
                    }

                    return true;
                });
                popupMenu.inflate(R.menu.playlist_item_popup);
                popupMenu.show();
            });
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        private final ConstraintLayout constraintLayoutHeader;

        private final View.OnClickListener addPlaylistClickListener;

        public ViewHolderHeader(@NonNull @NotNull View itemView, View.OnClickListener addPlaylistClickListener) {
            super(itemView);

            this.addPlaylistClickListener = addPlaylistClickListener;

            constraintLayoutHeader = itemView.findViewById(R.id.playlist_menu_header_container);
            itemView.findViewById(R.id.playlist_menu_create_button).setOnClickListener(this.addPlaylistClickListener);
        }
    }

    public interface OnPlaylistClick {
        void click(PlaylistItem playlistItem);
    }

    public interface OnDeleteListener {
        void delete(PlaylistItem playlistItem);
    }
}
