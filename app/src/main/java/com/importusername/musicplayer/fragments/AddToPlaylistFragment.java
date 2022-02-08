package com.importusername.musicplayer.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.AddToPlaylistAdapter;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;

public class AddToPlaylistFragment extends Fragment implements IBackPressFragment {
    private final SongsMenuItem clickedSong;

    private AddToPlaylistAdapter addToPlaylistAdapter;

    public AddToPlaylistFragment(SongsMenuItem clickedSong) {
        super(R.layout.add_to_playlist_layout);

        this.clickedSong = clickedSong;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.add_to_playlist_layout, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.add_to_playlist_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        this.addToPlaylistAdapter = new AddToPlaylistAdapter(
                this.clickedSong,
                this.getActivity()
        );

        this.addToPlaylistAdapter.setOnPlaylistClickListener((playlistItem, clickedSong) -> {
            final MusicPlayerRequestThread thread = new MusicPlayerRequestThread(
                    AppConfig.getProperty("url", AddToPlaylistFragment.this.getContext())
                    + Endpoints.ADD_SONG_TO_PLAYLIST
                    + String.format("/%s/%s", clickedSong.getSongId(), playlistItem.getPlaylistId()),
                    RequestMethod.PATCH,
                    AddToPlaylistFragment.this.getContext(),
                    true,
                    (status, response, headers) -> {
                        if (status == 200) {
                            final String toastSongName = clickedSong.getSongName().length() > 30
                                    ? clickedSong.getSongName().substring(0, 30) + "..."
                                    : clickedSong.getSongName();


                            AddToPlaylistFragment.this.showToast(String.format("Added %s", toastSongName));
                        } else {
                            AddToPlaylistFragment.this.showToast("Failed to add to playlist.");
                        }

                        this.getParentFragmentManager().popBackStack();
                    }
            );

            thread.start();
        });

        recyclerView.setAdapter(this.addToPlaylistAdapter);

        return view;
    }

    private void showToast(String text) {
        final Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            Toast.makeText(this.getActivity(), text, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
