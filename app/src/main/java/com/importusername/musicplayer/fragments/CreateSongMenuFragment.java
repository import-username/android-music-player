package com.importusername.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import org.jetbrains.annotations.NotNull;

public class CreateSongMenuFragment extends Fragment implements IBackPressFragment {
    public CreateSongMenuFragment() {
        super(R.layout.create_song_menu_layout);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_song_menu_layout, container, false);

        return view;
    }


    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
