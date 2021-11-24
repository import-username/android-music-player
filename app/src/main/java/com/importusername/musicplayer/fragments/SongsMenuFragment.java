package com.importusername.musicplayer.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuListAdapter;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SongsMenuFragment extends Fragment implements IBackPressFragment {
    private SongsMenuListAdapter songsMenuListAdapter;

    public SongsMenuFragment() {
        super(R.layout.music_player_songs_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_player_songs_menu_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.songs_menu_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        songsMenuListAdapter = new SongsMenuListAdapter(new ArrayList<>(), this.getActivity(), this.addSongClickListener(), true);

        recyclerView.setAdapter(songsMenuListAdapter);

        return view;
    }

    /**
     * Listener function for displaying fragment which allows user to add an audio file and create a song item.
     */
    private View.OnClickListener addSongClickListener() {
        return (View view) -> {
            final FragmentManager fragmentManager = getChildFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.slide_out
            );

            fragmentTransaction
                    .replace(R.id.songs_menu_fragment_container, CreateSongMenuFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack("SongsMenuFragment")
                    .commit();
        };
    }

    /**
     * Calls song list adapter's populate dataset method. Intended use for when user performs a crud based
     * query request for a song item.
     */
    public void notifySongDataChange() {
        this.songsMenuListAdapter.populateSongsDataset();
    }

    @Override
    public boolean shouldAllowBackPress() {
        if (getChildFragmentManager().findFragmentById(R.id.songs_menu_fragment_container) instanceof IBackPressFragment) {
            if (((IBackPressFragment) getChildFragmentManager().findFragmentById(R.id.songs_menu_fragment_container)).shouldAllowBackPress()) {
//                this.songsMenuListAdapter.populateSongsDataset();
            }

            return ((IBackPressFragment) getChildFragmentManager().findFragmentById(R.id.songs_menu_fragment_container)).shouldAllowBackPress();
        }

        return false;
    }
}
