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
    private ArrayList<SongsMenuItem> songsMenuArray = new ArrayList<>();

    private final FragmentActivity activity;

    private final int VIEW_HEADER = 0;
    private final int VIEW_MUSIC_ITEM = 1;

    private View.OnClickListener addButtonListener;

    private ISongItemListener songItemClickListener;

    private final boolean automaticallyGetSongs;

    private final int songsRequestLimit = 12;

    private int songsRequestSkip = 0;

    private int totalRows = 0;

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

            this.notifyDataSetChanged();
        }
    }

    /**
     * Sends a request to get user songs and populates songsMenuArray with song item objects.
     */
    public void populateSongsDataset() {
        if (this.automaticallyGetSongs) {
            final String url = AppConfig.getProperty("url", this.activity.getApplicationContext()) +
                    Endpoints.GET_SONGS +
                    String.format("?limit=%s", this.songsRequestLimit) +
                    String.format("&skip=%s", this.songsRequestSkip) +
                    "&includeTotal=true";

            final MusicPlayerRequestThread requestThread = new MusicPlayerRequestThread(
                    url,
                    RequestMethod.GET,
                    this.activity.getApplicationContext(),
                    true,
                    this.getSongsRequestAction()
            );

            requestThread.start();
        }
    }

    /**
     * Adds each song item to the recyclerview adapter's array.
     */
    private IHttpRequestAction getSongsRequestAction() {
        return (status, response, headers) -> {
            // TODO - add a conditional for non 2xx status codes
            try {
                final JSONObject responseObject = new JSONObject(response);
                final JSONArray rowsArray = responseObject.getJSONArray("rows");
                final String totalRows = responseObject.getString("total");

                SongsMenuListAdapter.this.totalRows = Integer.parseInt(totalRows);
                SongsMenuListAdapter.this.songsRequestSkip += rowsArray.length();

                SongsMenuListAdapter.this.activity.runOnUiThread(() -> {
                    for (int i = 0; i < rowsArray.length(); i++) {
                        try {
                            SongsMenuListAdapter.this.addItem(new SongsMenuItem(rowsArray.getJSONObject(i)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    final RecyclerView recyclerView = SongsMenuListAdapter.this.activity.findViewById(R.id.songs_menu_recyclerview);
                    final ConstraintLayout constraintLayout = SongsMenuListAdapter.this.activity.findViewById(R.id.songs_menu_loading_view);

                    if (recyclerView.getVisibility() == View.GONE) {
                        constraintLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            } catch (JSONException exc) {
                exc.printStackTrace();
            }
        };
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
