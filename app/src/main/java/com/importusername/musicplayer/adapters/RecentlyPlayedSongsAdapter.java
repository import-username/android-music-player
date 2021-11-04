package com.importusername.musicplayer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.importusername.musicplayer.R;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;

public class RecentlyPlayedSongsAdapter extends RecyclerView.Adapter<RecentlyPlayedSongsAdapter.ViewHolder> {
    private Set<String> recentlyPlayedSongs;

    /**
     * Initialize recentlyPlayedSongs set
     * @param recentlyPlayedSongsSet Set containing recently played songs.
     */
    public RecentlyPlayedSongsAdapter(Set<String> recentlyPlayedSongsSet) {
        this.recentlyPlayedSongs = recentlyPlayedSongsSet;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recently_played_song_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        final ConstraintLayout recentlyPlayedSongsContainer = holder.getRecentlyPlayedSongContainer();
        final int childrenCount = holder.getRecentlyPlayedSongContainer().getChildCount();

        for (int i = 0; i < childrenCount; i++) {
            final View child = recentlyPlayedSongsContainer.getChildAt(i);
            if (child instanceof TextView) {
                final TextView childTextView = ((TextView) child);
                final String text = childTextView.getText() + "|" + new ArrayList<>(this.recentlyPlayedSongs).get(i);
                childTextView.setText(text);
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.recentlyPlayedSongs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout recentlyPlayedSongContainer;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            recentlyPlayedSongContainer = itemView.findViewById(R.id.recently_played_song_container);
        }

        public ConstraintLayout getRecentlyPlayedSongContainer() {
            return this.recentlyPlayedSongContainer;
        }
    }
}
