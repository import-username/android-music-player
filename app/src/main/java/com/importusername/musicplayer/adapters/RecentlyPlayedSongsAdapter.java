package com.importusername.musicplayer.adapters;

import android.content.Context;
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
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppCookie;
import com.importusername.musicplayer.util.AppHttp;
import com.importusername.musicplayer.util.AppToast;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class RecentlyPlayedSongsAdapter extends RecyclerView.Adapter<RecentlyPlayedSongsAdapter.ViewHolder> {
    private ArrayList<SongsMenuItem> recentlyPlayedSongs;

    private final FragmentActivity activity;

    private final Context context;

    /**
     * Initialize recentlyPlayedSongs set
     * @param recentlyPlayedSongsSet Set containing recently played songs.
     */
    public RecentlyPlayedSongsAdapter(ArrayList<SongsMenuItem> recentlyPlayedSongsSet, FragmentActivity activity, Context context) {
        this.recentlyPlayedSongs = recentlyPlayedSongsSet;
        this.activity = activity;
        this.context = context;

        this.getRecentlyPlayedSongs();
    }

    public void getRecentlyPlayedSongs() {
        final String url = AppConfig.getProperty("url", this.context)
                + Endpoints.RECENTLY_PLAYED_SONGS;

        final MusicPlayerRequestThread requestThread = new MusicPlayerRequestThread(
                url,
                RequestMethod.GET,
                this.context,
                true,
                this.getRecentlyPlayedAction()
        );

        requestThread.start();
    }

    private IHttpRequestAction getRecentlyPlayedAction() {
        return (status, response, headers) -> {
            if (AppHttp.isResponseOK(status)) {
                try {
                    final JSONObject responseObject = new JSONObject(response);
                    final JSONArray recentlyPlayedSongs = responseObject.getJSONArray("rows");

                    for (int i = 0; i < recentlyPlayedSongs.length(); i++) {
                        final SongsMenuItem songItem = new SongsMenuItem(recentlyPlayedSongs.getJSONObject(i));

                        this.recentlyPlayedSongs.add(songItem);
                    }

                    this.activity.runOnUiThread(() -> {
                        this.notifyDataSetChanged();
                    });
                } catch (Exception exc) {
                    exc.printStackTrace();

                    AppToast.showToast("Failed to get recently played songs.", this.activity);
                }
            } else {
                AppToast.showToast("Failed to get recently played songs.", this.activity);
            }
        };
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recently_played_song_item, parent, false);

        return new ViewHolder(view, this.activity);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        final int absolutePos = holder.getAbsoluteAdapterPosition();

        final SongsMenuItem correspondingSongItem = this.recentlyPlayedSongs.get(absolutePos);

        holder.setTitle(correspondingSongItem.getSongName());

        if (correspondingSongItem.getSongThumbnailId() != null) {
            holder.setThumbnail(correspondingSongItem.getSongThumbnailId().split("/")[2]);
        }
    }

    @Override
    public int getItemCount() {
        return this.recentlyPlayedSongs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout recentlyPlayedSongContainer;

        private final FragmentActivity fragmentActivity;

        public ViewHolder(@NonNull @NotNull View itemView, FragmentActivity activity) {
            super(itemView);

            recentlyPlayedSongContainer = itemView.findViewById(R.id.recently_played_song_container);
            this.fragmentActivity = activity;
        }

        public void setTitle(String title) {
            ((TextView) this.recentlyPlayedSongContainer.findViewById(R.id.recently_played_song_text))
                    .setText(title);
        }

        public void setThumbnail(String thumbnailId) {
            final String url = AppConfig.getProperty("url", this.recentlyPlayedSongContainer.getContext()) + Endpoints.GET_THUMBNAIL + "/" + thumbnailId;

            final ImageView defaultThumbnail = this.recentlyPlayedSongContainer.findViewById(R.id.recently_played_thumbnail_default);
            final ImageView customThumbnail = this.recentlyPlayedSongContainer.findViewById(R.id.recently_played_thumbnail_custom);

            defaultThumbnail.setVisibility(View.GONE);
            customThumbnail.setVisibility(View.VISIBLE);

            final GlideUrl glideUrl = new GlideUrl(
                    url,
                    new LazyHeaders.Builder().addHeader("Cookie", AppCookie.getAuthCookie(this.fragmentActivity)).build()
            );

            Glide.with(this.fragmentActivity)
                    .load(glideUrl)
                    .into(customThumbnail);
        }
    }
}
