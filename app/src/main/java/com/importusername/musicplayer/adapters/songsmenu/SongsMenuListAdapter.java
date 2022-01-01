package com.importusername.musicplayer.adapters.songsmenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
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

    private final boolean automaticallyGetSongs;

    private int totalRows = 0;

    private Uri queryUri;

    private final SongsQueryEntity songsQueryEntity = new SongsQueryEntity();

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
        if (this.automaticallyGetSongs) {
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

                            final RecyclerView recyclerView = SongsMenuListAdapter.this.activity.findViewById(R.id.songs_menu_recyclerview);
                            final ConstraintLayout constraintLayout = SongsMenuListAdapter.this.activity.findViewById(R.id.songs_menu_loading_view);

                            if (recyclerView.getVisibility() == View.GONE) {
                                constraintLayout.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                    });
        }
    }

    public void changeQueryUrl(Uri uri) {
        // This uri will be used for all queries until changed again
        this.queryUri = uri;

        // Clears array of all song items and adds first null item to display menu header.
        this.songsMenuArray.clear();
        this.songsMenuArray.add(null);

        // Reset query entity to reset skip field to 0.
        this.songsQueryEntity.reset();

        // Repopulate dataset with new uri
        this.populateSongsDataset();
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

                view.findViewById(R.id.songs_menu_search_bar_icon)
                        .setOnClickListener((v) -> {
                            final String searchText = ((EditText) view.findViewById(R.id.songs_menu_search_bar_input))
                                    .getText().toString();

                            SongsMenuListAdapter.this.changeQueryUrl(Uri.parse(
                                    AppConfig.getProperty("url", SongsMenuListAdapter.this.activity.getApplicationContext())
                                            + Endpoints.GET_SONGS
                                            + (searchText.length() > 0 ? "?titleIncludes=" + searchText : "")
                            ));
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
                break;
            case VIEW_MUSIC_ITEM:
                final SongsMenuItem songItem = this.songsMenuArray.get(position);
                ((ViewHolder) holder).setText(songItem.getSongName());
                ((ViewHolder) holder).setClickListener(songItem);

                if (songItem.getSongThumbnailId() != null) {
                    ((ViewHolder) holder).setItemThumbnail(songItem.getSongThumbnailId().split("/")[2]);
                } else {
                    ((ViewHolder) holder).clearThumbnail();
                }

                break;
        }

        // If there are more songs to get from the server, automatically load them when the 10th item has been binded to view.
        if (this.getSongItemCount() < this.totalRows) {
            if (position > 0 && position % 10 == 0) {
                this.populateSongsDataset();
            }
        }
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

        public ViewHolder(@NonNull @NotNull View itemView, FragmentActivity activity, ISongItemListener clickListener) {
            super(itemView);

            this.fragmentActivity = activity;
            this.constraintLayout = itemView.findViewById(R.id.songs_menu_music_item_container);
            this.clickListener = clickListener;
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
