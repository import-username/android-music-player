package com.importusername.musicplayer.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.importusername.musicplayer.R;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SongsMenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<String> songsMenuArray;

    private final int VIEW_HEADER = 0;
    private final int VIEW_MUSIC_ITEM = 1;

    private View.OnClickListener addButtonListener;

    /**
     * Initialize songs menu array
     * @param songsMenuArray Array containing list of user songs.
     */
    public SongsMenuListAdapter(ArrayList<String> songsMenuArray, View.OnClickListener addButtonListener) {
        this.songsMenuArray = songsMenuArray;
        this.addButtonListener = addButtonListener;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;

        if (this.getItemViewType(viewType) == VIEW_HEADER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.songs_menu_header_container, parent, false);

            viewHolder = new SongsMenuListAdapter.ViewHolderHeader(view, this.addButtonListener);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.songs_menu_music_item, parent, false);

            viewHolder = new SongsMenuListAdapter.ViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return this.songsMenuArray.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }

        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.songs_menu_music_item_container);
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
