package com.importusername.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistItem;
import com.importusername.musicplayer.adapters.playlistmenu.PlaylistMenuAdapter;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuListAdapter;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PlaylistsMenuFragment extends Fragment implements IBackPressFragment {
    private PlaylistMenuAdapter playlistMenuAdapter;

    public PlaylistsMenuFragment() {
        super(R.layout.music_player_playlists_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.music_player_playlists_menu_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.playlist_menu_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        this.playlistMenuAdapter = new PlaylistMenuAdapter(
                new ArrayList<>(),
                this.getActivity(),
                this.addPlaylistListener(),
                this.playlistItemClickListener());

        recyclerView.setAdapter(this.playlistMenuAdapter);

        return view;
    }

    private View.OnClickListener addPlaylistListener() {
        return (view) -> {

        };
    }

    private PlaylistMenuAdapter.OnPlaylistClick playlistItemClickListener() {
        return (PlaylistItem item) -> {

        };
    }

    @Override
    public boolean shouldAllowBackPress() {
        return false;
    }
}
