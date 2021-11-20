package com.importusername.musicplayer.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.importusername.musicplayer.R;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SongsMenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<String> songsMenuArray = new ArrayList<>();

    private final int VIEW_HEADER = 0;
    private final int VIEW_MUSIC_ITEM = 1;

    private View.OnClickListener addButtonListener;

    /**
     * Initialize songs menu array
     * @param songsMenuArray Array containing list of user songs.
     */
    public SongsMenuListAdapter(ArrayList<String> songsMenuArray, View.OnClickListener addButtonListener) {
        this.songsMenuArray.add(null);
        this.songsMenuArray.addAll(songsMenuArray);
        this.addButtonListener = addButtonListener;
    }

    /**
     * Adds music item to songs menu array and notifies adapter of data change.
     * @param itemObject Json object representing song data received from server.
     * @param fetchThumbnail Boolean value to determine if get request should be sent to receive the song item's thumbnail image.
     * @throws JSONException
     */
    public void addItem(JSONObject itemObject, boolean fetchThumbnail) throws JSONException {
        if (itemObject != null) {
            this.songsMenuArray.add(itemObject.getString("song_title"));

            this.notifyDataSetChanged();
        }
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

                viewHolder = new ViewHolder(view);
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
                ((ViewHolder) holder).setText(this.songsMenuArray.get(position));

                break;
        }
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

        public void setText(String text) {
            ((TextView) this.constraintLayout.findViewById(R.id.songs_menu_music_item_title)).setText(text);
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
