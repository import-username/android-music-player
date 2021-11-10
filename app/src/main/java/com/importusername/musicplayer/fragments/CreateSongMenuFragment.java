package com.importusername.musicplayer.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.http.MultipartRequestEntity;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.threads.MultipartRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URLConnection;

// TODO - add toolbar
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

        getChildFragmentManager().beginTransaction()
                .replace(R.id.create_song_menu_fragment, AddSongFileMenuFragment.class, null)
                .commit();

        return view;
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }


}
