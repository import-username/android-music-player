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
import com.importusername.musicplayer.views.CreateSongImageLayout;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public class CreateSongItemMenuFragment extends Fragment {
    private ActivityResultLauncher<Intent> directoryTreeActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    final Intent dataIntent = result.getData();

                    if (dataIntent != null) {
                        final Uri fileUri = dataIntent.getData();

                        if (fileUri != null) {

                            final CreateSongImageLayout createSongImageLayout = getView().findViewById(R.id.create_song_menu_image_container);

                            CreateSongItemMenuFragment.this.getActivity().runOnUiThread(() -> {
                                CreateSongItemMenuFragment.this.getView().findViewById(R.id.remove_song_item_thumbnail_button).setVisibility(View.VISIBLE);
                            });

                            try {
                                createSongImageLayout.setCustomImage(getContext().getContentResolver().openInputStream(fileUri));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

    public CreateSongItemMenuFragment() {
        super(R.layout.create_song_item_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.create_song_item_menu_fragment, container, false);

        view.findViewById(R.id.create_song_menu_image_container).setOnClickListener(this.createSongImageListener());

        view.findViewById(R.id.remove_song_item_thumbnail_button).setOnClickListener(this.removeThumbnailBtnListener());

        return view;
    }

    private View.OnClickListener createSongImageListener() {
        return (View view) -> {
            CreateSongItemMenuFragment.this.displayDirectoryTree();
        };
    }

    private void displayDirectoryTree() {
        Intent fileChooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileChooserIntent.setType("image/*");

        directoryTreeActivityResult.launch(fileChooserIntent);
    }

    private View.OnClickListener removeThumbnailBtnListener() {
        return (View view) -> {
            ((CreateSongImageLayout) CreateSongItemMenuFragment.this.getView().findViewById(R.id.create_song_menu_image_container)).removeCustomImage();
            view.setVisibility(View.GONE);
        };
    }
}
