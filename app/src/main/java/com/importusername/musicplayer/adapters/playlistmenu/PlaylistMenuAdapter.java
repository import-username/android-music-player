package com.importusername.musicplayer.adapters.playlistmenu;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuListAdapter;
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

    private OnRefreshComplete onRefreshComplete;

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

                            if (this.onRefreshComplete != null) {
                                this.onRefreshComplete.refresh();
                            }

                            PlaylistMenuAdapter.this.notifyDataSetChanged();

                            final RecyclerView recyclerView = PlaylistMenuAdapter.this.activity.findViewById(R.id.playlist_menu_recyclerview);
                            final ConstraintLayout constraintLayout = PlaylistMenuAdapter.this.activity.findViewById(R.id.playlist_menu_loading_view);

                            if (recyclerView.getVisibility() == View.GONE) {
                                constraintLayout.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
    }

    public void refreshDataset() {
        this.playlistMenuArray.clear();
        this.playlistMenuArray.add(null);

        this.notifyDataSetChanged();

        this.playlistQueryEntity.reset();

        this.populatePlaylistDataset();
    }

    public void setOnRefreshComplete(OnRefreshComplete onRefreshComplete) {
        this.onRefreshComplete = onRefreshComplete;
    }

    public void changeQueryUrl(Uri uri) {
        this.playlistQueryUri = uri;

        this.playlistMenuArray.clear();
        this.playlistMenuArray.add(null);

        this.notifyDataSetChanged();

        this.playlistQueryEntity.reset();

        this.populatePlaylistDataset();
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
                            final String text = ((EditText) v).getText().toString();

                            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                                PlaylistMenuAdapter.this.changeQueryUrl(Uri.parse(
                                        AppConfig.getProperty("url", PlaylistMenuAdapter.this.activity.getApplicationContext())
                                        + Endpoints.GET_PLAYLISTS
                                        + (text.length() > 0 ? "?titleIncludes=" + ((EditText) v).getText() : "")
                                ));
                            }

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
                final ViewHolderHeader header = (ViewHolderHeader) holder;

                header.setOnSearchEnter((text) -> {
                    this.changeQueryUrl(Uri.parse(
                            AppConfig.getProperty("url", this.activity.getApplicationContext())
                                    + Endpoints.GET_PLAYLISTS
                                    + (text.length() > 0 ? "?titleIncludes=" + text : "")
                    ));
                });

                break;
            case VIEW_PLAYLIST_ITEM:
                final PlaylistItem playlistItem = this.playlistMenuArray.get(position);
                final PlaylistMenuAdapter.ViewHolder playlistViewHolder = (PlaylistMenuAdapter.ViewHolder) holder;

                playlistViewHolder.setText(playlistItem.getPlaylistName());
                playlistViewHolder.setOptionsClickListener(playlistItem);
                playlistViewHolder.setClickListener(playlistItem);
                playlistViewHolder.setOnDeleteListener((item) -> {
                    final int deletedItemPos = holder.getAbsoluteAdapterPosition();

                    PlaylistMenuAdapter.this.playlistMenuArray.remove(deletedItemPos);

                    PlaylistMenuAdapter.this.activity.runOnUiThread(() -> {
                        PlaylistMenuAdapter.this.notifyItemRemoved(deletedItemPos);
                    });
                });

                if (playlistItem.getPlaylistThumbnailId() != null) {
                    playlistViewHolder.setItemThumbnail(playlistItem.getPlaylistThumbnailId().split("/")[2]);
                } else {
                    playlistViewHolder.clearThumbnail();
                }

                break;
        }
        // TODO
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

        private OnDeleteListener deleteListener;

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

        public void setOptionsClickListener(PlaylistItem item) {
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
                                        if (status == 200 && ViewHolder.this.deleteListener != null) {
                                            ViewHolder.this.deleteListener.delete(item);
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
        
        public void setOnDeleteListener(OnDeleteListener deleteListener) {
            this.deleteListener = deleteListener;
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        private final ConstraintLayout constraintLayoutHeader;

        private final View.OnClickListener addPlaylistClickListener;

        private final Handler handler = new Handler(Looper.getMainLooper());

        private OnSearchEnter onSearchEnter;

        public ViewHolderHeader(@NonNull @NotNull View itemView, View.OnClickListener addPlaylistClickListener) {
            super(itemView);

            this.addPlaylistClickListener = addPlaylistClickListener;

            constraintLayoutHeader = itemView.findViewById(R.id.playlist_menu_header_container);
            itemView.findViewById(R.id.playlist_menu_create_button).setOnClickListener(this.addPlaylistClickListener);

            ((EditText) constraintLayoutHeader.findViewById(R.id.playlist_menu_search_bar_input))
                    .addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            sendSearchAfterTime(s.toString(), 1200);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {}
                    });

            constraintLayoutHeader.findViewById(R.id.playlist_menu_search_bar_input).setOnFocusChangeListener((view, focused) -> {
                if (!focused) {
                    InputMethodManager inputMethodManager =(InputMethodManager) this.constraintLayoutHeader.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            });

            constraintLayoutHeader.findViewById(R.id.playlist_menu_search_bar_input)
                    .setOnKeyListener((v, keyCode, event) -> {
                        final String text = ((EditText) v).getText().toString();

                        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                            if (this.onSearchEnter != null) {
                                this.onSearchEnter.enter(text);
                            }
                        }

                        if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                            return false;
                        }

                        return false;
                    });
        }

        private void sendSearchAfterTime(String search, int milliseconds) {
            handler.postDelayed(() -> {
                final String currentString = ((EditText) constraintLayoutHeader.findViewById(R.id.playlist_menu_search_bar_input)).getText().toString();

                if (currentString.equals(search)) {
                    PlaylistMenuAdapter.ViewHolderHeader.this.onSearchEnter.enter(search);
                }
            }, milliseconds);
        }

        public void setOnSearchEnter(OnSearchEnter onSearchEnter) {
            this.onSearchEnter = onSearchEnter;
        }

        public interface OnSearchEnter {
            void enter(String searchText);
        }
    }

    public interface OnPlaylistClick {
        void click(PlaylistItem playlistItem);
    }

    public interface OnDeleteListener {
        void delete(PlaylistItem playlistItem);
    }
}
