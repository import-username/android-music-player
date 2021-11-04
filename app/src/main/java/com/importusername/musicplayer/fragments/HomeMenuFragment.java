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
import com.importusername.musicplayer.adapters.RecentlyPlayedSongsAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class HomeMenuFragment extends Fragment {
    public HomeMenuFragment() {
        super(R.layout.music_player_home_menu_fragment);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.music_player_home_menu_fragment, container, false);

        final HashSet<String> recentlyPlayedSongs = new HashSet<>();
        // TODO - add event listener that displays recently played songs recyclerview when song is added to recently played list

        final RecyclerView recyclerView = rootView.findViewById(R.id.recently_played_songs_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        RecentlyPlayedSongsAdapter recentlyPlayedSongsAdapter = new RecentlyPlayedSongsAdapter(recentlyPlayedSongs);
        recyclerView.setAdapter(recentlyPlayedSongsAdapter);

        return rootView;
    }
}
