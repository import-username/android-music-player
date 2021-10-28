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
import androidx.fragment.app.Fragment;
import com.importusername.musicplayer.R;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class SongsMenuFragment extends Fragment {
    private ActivityResultLauncher<Intent> directoryTreeActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    final Intent dataIntent = result.getData();

                    final Uri fileUri = dataIntent.getData();

                    if (fileUri != null) {
                        try {
                            SongsMenuFragment.this.uploadFile(fileUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    public SongsMenuFragment() {
        super(R.layout.music_player_songs_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.music_player_songs_menu_fragment, container, false);

        view.findViewById(R.id.songs_menu_add_song_button).setOnClickListener(this.addSongClickListener());

        return view;
    }

    private View.OnClickListener addSongClickListener() {
        return (View view) -> {
            SongsMenuFragment.this.displayDirectoryTree();
        };
    }

    private void displayDirectoryTree() {
        Intent fileChooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileChooserIntent.setType("*/*");

        directoryTreeActivityResult.launch(fileChooserIntent);
    }

    private void uploadFile(Uri fileUri) throws IOException {
        InputStream inputStream = SongsMenuFragment.this.getContext().getContentResolver().openInputStream(fileUri);


    }
}
