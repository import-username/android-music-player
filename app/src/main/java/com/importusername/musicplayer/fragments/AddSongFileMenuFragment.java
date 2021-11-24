package com.importusername.musicplayer.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.http.MultipartRequestEntity;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MultipartRequestThread;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URLConnection;

public class AddSongFileMenuFragment extends EventFragment {
    private ActivityResultLauncher<Intent> directoryTreeActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    final Intent dataIntent = result.getData();

                    if (dataIntent != null) {
                        final Uri fileUri = dataIntent.getData();

                        if (fileUri != null) {
                            final CreateSongItemMenuFragment createSongItemMenuFragment = new CreateSongItemMenuFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("song_file_uri", fileUri.toString());

                            createSongItemMenuFragment.setFragmentEventListener("refresh_dataset", (data) -> {
                                AddSongFileMenuFragment.this.emitFragmentEvent("refresh_dataset", null);
                            });

                            createSongItemMenuFragment.setArguments(bundle);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.create_song_menu_fragment, createSongItemMenuFragment, null)
                                    .commit();
                        }
                    }
                }
            });

    public AddSongFileMenuFragment() {
        super(R.layout.add_song_file_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.add_song_file_menu_fragment, container, false);

        view.findViewById(R.id.add_song_file_button).setOnClickListener((View clickView) -> {
            AddSongFileMenuFragment.this.displayDirectoryTree();
        });

        return view;
    }

    private void displayDirectoryTree() {
        Intent fileChooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileChooserIntent.setType("audio/*");

        directoryTreeActivityResult.launch(fileChooserIntent);
    }
}
