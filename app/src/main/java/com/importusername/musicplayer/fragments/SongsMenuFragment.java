package com.importusername.musicplayer.fragments;

import android.content.Intent;
import android.database.Cursor;
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
import com.importusername.musicplayer.http.MultipartRequestEntity;
import com.importusername.musicplayer.http.MusicPlayerRequest;
import com.importusername.musicplayer.threads.MultipartRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SongsMenuFragment extends Fragment {
    private ActivityResultLauncher<Intent> directoryTreeActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    final Intent dataIntent = result.getData();

                    if (dataIntent != null) {
                        final Uri fileUri = dataIntent.getData();

                        if (fileUri != null) {
                            try {
                                SongsMenuFragment.this.uploadFile(fileUri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
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

    private void uploadFile(Uri fileUri) throws FileNotFoundException {
        InputStream inputStream = SongsMenuFragment.this.getContext().getContentResolver().openInputStream(fileUri);

        final String filename = DocumentFile.fromSingleUri(SongsMenuFragment.this.getContext(), fileUri).getName();

        MultipartRequestThread multipartRequestThread = new MultipartRequestThread(
                AppConfig.getProperty("url", getContext()) + "/song/upload-song",
                true,
                getContext(),
                inputStream,
                "songFile",
                filename);
        Log.d("SongsMenuFragment", filename);
        multipartRequestThread.start();
    }
}
