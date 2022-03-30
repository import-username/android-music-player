package com.importusername.musicplayer.adapters.songsmenu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistMenuAdapter;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.fragments.SongsMenuFragment;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.interfaces.ISongItemListener;
import com.importusername.musicplayer.interfaces.IThumbnailRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.threads.ThumbnailRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
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
import java.util.List;

public class SongsMenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<SongsMenuItem> songsMenuArray = new ArrayList<>();

    private final FragmentActivity activity;

    private final int VIEW_HEADER = 0;
    private final int VIEW_MUSIC_ITEM = 1;

    private final View.OnClickListener addButtonListener;

    private final ISongItemListener songItemClickListener;

    private OnAddPlaylistClick onAddPlaylistClick;

    private final boolean automaticallyGetSongs;

    private int totalRows = 0;

    private Uri queryUri;

    private final SongsQueryEntity songsQueryEntity = new SongsQueryEntity();

    private boolean populatingDataset = false;

    private int populatedPosition = 0;

    /**
     * Initialize songs menu array
     * @param songsMenuArray Array containing list of user songs.
     */
    public SongsMenuListAdapter(ArrayList<SongsMenuItem> songsMenuArray,FragmentActivity activity,
                                View.OnClickListener addButtonListener, ISongItemListener songItemClickListener,
                                boolean automaticallyGetSongs) {
        this.songsMenuArray.add(null);
        this.songsMenuArray.addAll(songsMenuArray);
        this.activity = activity;
        this.addButtonListener = addButtonListener;
        this.songItemClickListener = songItemClickListener;
        this.automaticallyGetSongs = automaticallyGetSongs;
        this.queryUri = Uri.parse(AppConfig.getProperty("url", this.activity.getApplicationContext())
                + Endpoints.GET_SONGS);

        this.populateSongsDataset();
    }

    /**
     * Adds music item to songs menu array and notifies adapter of data change.
     * @param songsMenuItem Songs menu item object representing song data received from server.
     * @throws JSONException
     */
    public void addItem(SongsMenuItem songsMenuItem) throws JSONException {
        if (songsMenuItem != null) {
            this.songsMenuArray.add(songsMenuItem);
        }
    }

    /**
     * Sends a request to get user songs and populates songsMenuArray with song item objects.
     */
    public void populateSongsDataset() {
        if (this.automaticallyGetSongs && !this.populatingDataset) {
            this.populatingDataset = true;

            final SongsQueryUri songsQueryUri = new SongsQueryUri();
            songsQueryUri.setSongQueryHost(this.queryUri);
            songsQueryUri.addQueryParam("includeTotal", "true");

            this.songsQueryEntity.queryNextSong(
                    this.activity.getApplicationContext(),
                    songsQueryUri,
                    (jsonArray, total) -> {
                        SongsMenuListAdapter.this.totalRows = total;

                        SongsMenuListAdapter.this.activity.runOnUiThread(() -> {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    SongsMenuListAdapter.this.addItem(new SongsMenuItem(jsonArray.getJSONObject(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            SongsMenuListAdapter.this.notifyDataSetChanged();

                            this.populatingDataset = false;

                            final RecyclerView recyclerView = SongsMenuListAdapter.this.activity.findViewById(R.id.songs_menu_recyclerview);
                            final ConstraintLayout constraintLayout = SongsMenuListAdapter.this.activity.findViewById(R.id.songs_menu_loading_view);

                            if (recyclerView != null && recyclerView.getVisibility() == View.GONE) {
                                constraintLayout.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                    });
        }
    }

    public void refreshDataset() {
        this.songsMenuArray.clear();
        this.songsMenuArray.add(null);

        this.notifyDataSetChanged();

        this.songsQueryEntity.reset();

        this.populateSongsDataset();
    }

    public void changeQueryUrl(Uri uri) {
        // This uri will be used for all queries until changed again
        this.queryUri = uri;

        // Clears array of all song items and adds first null item to display menu header.
        this.songsMenuArray.clear();
        this.songsMenuArray.add(null);

        this.notifyDataSetChanged();

        // Reset query entity to reset skip field to 0.
        this.songsQueryEntity.reset();

        // Repopulate dataset with new uri
        this.populateSongsDataset();
    }

    public void setOnAddPlaylistClickListener(OnAddPlaylistClick onAddPlaylistClick) {
        this.onAddPlaylistClick = onAddPlaylistClick;
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

                view.findViewById(R.id.songs_menu_search_bar_input)
                        .setOnKeyListener((v, keyCode, event) -> {
                            final String text = ((EditText) v).getText().toString();

                            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                                SongsMenuListAdapter.this.changeQueryUrl(Uri.parse(
                                        AppConfig.getProperty("url", SongsMenuListAdapter.this.activity.getApplicationContext())
                                        + Endpoints.GET_SONGS
                                        + (text.length() > 0 ? "?titleIncludes=" + ((EditText) v).getText() : "")
                                ));
                            }

                            return true;
                        });

                viewHolder = new ViewHolderHeader(view, this.addButtonListener);
                break;
            case VIEW_MUSIC_ITEM:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.songs_menu_music_item, parent, false);

                viewHolder = new ViewHolder(view, this.activity, this.songItemClickListener);
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
                    SongsMenuListAdapter.this.changeQueryUrl(Uri.parse(
                            AppConfig.getProperty("url", SongsMenuListAdapter.this.activity.getApplicationContext())
                                    + Endpoints.GET_SONGS
                                    + (text.length() > 0 ? "?titleIncludes=" + text : "")
                    ));
                });

                break;
            case VIEW_MUSIC_ITEM:
                final SongsMenuItem songItem = this.songsMenuArray.get(position);
                final SongsMenuListAdapter.ViewHolder songViewHolder = (SongsMenuListAdapter.ViewHolder) holder;
                songViewHolder.setText(songItem.getSongName());
                songViewHolder.setClickListener(songItem);
                songViewHolder.setOnOptionsListener(songItem);

                songViewHolder.setOnDeleteListener((deletedSongItem) -> {
                    final int deletedItemPos = holder.getAbsoluteAdapterPosition();

                    SongsMenuListAdapter.this.songsMenuArray.remove(deletedItemPos);

                    SongsMenuListAdapter.this.activity.runOnUiThread(() -> {
                        SongsMenuListAdapter.this.notifyItemRemoved(deletedItemPos);
                    });
                });

                songViewHolder.setOnAddPlaylistListener(() -> {
                     if (SongsMenuListAdapter.this.onAddPlaylistClick != null) {
                         SongsMenuListAdapter.this.onAddPlaylistClick.click(songItem);
                     }
                });

                if (songItem.getSongThumbnailId() != null && songItem.getSongThumbnailId().split("/").length >= 2) {
                    ((ViewHolder) holder).setItemThumbnail(songItem.getSongThumbnailId().split("/")[2]);
                } else {
                    ((ViewHolder) holder).clearThumbnail();
                }

                break;
        }

        // If there are more songs to get from the server, automatically load them when the 10th item has been binded to view.
        if (this.getSongItemCount() < this.totalRows) {
            if (position > 0 && position % 40 == 0 && position > this.populatedPosition) {
                this.populatedPosition = position;

                this.populateSongsDataset();
            }
        }
    }

     public int getSongItemIndex(SongsMenuItem item) {
        for (int i = 0; i < this.songsMenuArray.size(); i++) {
            if (this.songsMenuArray.get(i) != null && this.songsMenuArray.get(i).getSongId().equals(item.getSongId()))
                return i;
        }

        return -1;
     }

    @Override
    public int getItemCount() {
        return this.songsMenuArray.size();
    }

    public int getSongItemCount() {
        return this.songsMenuArray.size() - 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }

        return 1;
    }

    public List<SongsMenuItem> getSongItems() {
        return this.songsMenuArray;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentActivity fragmentActivity;

        private final ConstraintLayout constraintLayout;

        private final ISongItemListener clickListener;

        private OnAddPlaylistClickListener addPlaylistClickListener;

        private OnDeleteListener onDeleteListener;

        public ViewHolder(@NonNull @NotNull View itemView, FragmentActivity activity, ISongItemListener clickListener) {
            super(itemView);

            this.fragmentActivity = activity;
            this.constraintLayout = itemView.findViewById(R.id.songs_menu_music_item_container);
            this.clickListener = clickListener;
        }

        public void setText(String text) {
            ((TextView) this.constraintLayout.findViewById(R.id.songs_menu_music_item_title)).setText(text);
        }

        public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
            this.onDeleteListener = onDeleteListener;
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

        public void setClickListener(SongsMenuItem item) {
            this.constraintLayout.setOnClickListener((View view) -> {
                ViewHolder.this.clickListener.songItemClickListener(item);
            });
        }

        public void setOnOptionsListener(SongsMenuItem songsMenuItem) {
            this.constraintLayout.findViewById(R.id.music_item_options_button).setOnClickListener((v) -> {
                final Context contextWrapper = new ContextThemeWrapper(this.fragmentActivity, R.style.PopupMenu);

                final PopupMenu popupMenu = new PopupMenu(contextWrapper, v);

                popupMenu.setOnMenuItemClickListener((menuItem) -> {
                    switch (menuItem.getTitle().toString()) {
                        case "Delete":
                            final String url = AppConfig.getProperty("url", this.fragmentActivity)
                                    + Endpoints.DELETE_SONG
                                    + "/"
                                    + songsMenuItem.getSongId();

                            final MusicPlayerRequestThread thread = new MusicPlayerRequestThread(
                                    url,
                                    RequestMethod.DELETE,
                                    this.fragmentActivity,
                                    true,
                                    (status, response, headers) -> {
                                        if (status == 200 && ViewHolder.this.onDeleteListener != null) {
                                            ViewHolder.this.onDeleteListener.delete(songsMenuItem);
                                        }
                                    }
                            );

                            thread.start();

                            break;
                        case "Add to playlist":
                            if (this.addPlaylistClickListener != null) {
                                this.addPlaylistClickListener.click();
                            }

                            break;
                    }

                    return true;
                });
                popupMenu.inflate(R.menu.song_item_popup);
                popupMenu.show();
            });
        }

        public void setOnAddPlaylistListener(OnAddPlaylistClickListener listener) {
            this.addPlaylistClickListener = listener;
        }

        public interface OnAddPlaylistClickListener {
            void click();
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        private final ConstraintLayout constraintLayoutHeader;

        private final View.OnClickListener addButtonListener;

        private OnSearchEnter onSearchEnter;

        public ViewHolderHeader(@NonNull @NotNull View itemView, View.OnClickListener addButtonListener) {
            super(itemView);

            this.addButtonListener = addButtonListener;

            constraintLayoutHeader = itemView.findViewById(R.id.songs_menu_header_container);
            itemView.findViewById(R.id.songs_menu_add_song_button).setOnClickListener(this.addButtonListener);

            constraintLayoutHeader.findViewById(R.id.songs_menu_search_bar_input).setOnFocusChangeListener((view, focused) -> {
                if (!focused) {
                    InputMethodManager inputMethodManager =(InputMethodManager) this.constraintLayoutHeader.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            });

            constraintLayoutHeader.findViewById(R.id.songs_menu_search_bar_input)
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

        public void setOnSearchEnter(OnSearchEnter onSearchEnter) {
            this.onSearchEnter = onSearchEnter;
        }

        public interface OnSearchEnter {
            void enter(String searchText);
        }
    }

    public interface OnAddPlaylistClick {
        void click(SongsMenuItem clickedSong);
    }

    public interface OnDeleteListener {
        void delete(SongsMenuItem songsMenuItem);
    }
}
