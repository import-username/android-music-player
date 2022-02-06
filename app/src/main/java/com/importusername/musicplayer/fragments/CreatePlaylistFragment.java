package com.importusername.musicplayer.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.http.MultipartRequestEntity;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MultipartRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.views.CreateSongImageLayout;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.List;

public class CreatePlaylistFragment extends EventFragment implements IBackPressFragment {
    private ActivityResultLauncher<Intent> directoryTreeActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    final Intent dataIntent = result.getData();

                    if (dataIntent != null) {
                        final Uri fileUri = dataIntent.getData();

                        if (fileUri != null) {
                            final CreateSongImageLayout createSongImageLayout = getView().findViewById(R.id.create_playlist_menu_image_container);

                            CreatePlaylistFragment.this.getActivity().runOnUiThread(() -> {
                                CreatePlaylistFragment.this.getView().findViewById(R.id.remove_playlist_thumbnail_button).setVisibility(View.VISIBLE);
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

    public CreatePlaylistFragment() {
        super(R.layout.create_playlist_menu_layout);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_playlist_menu_layout, container, false);

        view.findViewById(R.id.submit_playlist_button).setOnClickListener(this.submitPlaylistButtonListener());

        view.findViewById(R.id.create_playlist_menu_image_container).setOnClickListener(this.createPlaylistThumbnailListener());

        view.findViewById(R.id.remove_playlist_thumbnail_button).setOnClickListener(this.removeThumbnailBtnListener());

        return view;
    }

    private View.OnClickListener submitPlaylistListener() {
        return (v) -> {

        };
    }

    private View.OnClickListener createPlaylistThumbnailListener() {
        return (v) -> {
            CreatePlaylistFragment.this.displayDirectoryTree();
        };
    }

    /**
     * Displays intent to allow user to select file.
     */
    private void displayDirectoryTree() {
        Intent fileChooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileChooserIntent.setType("image/*");

        directoryTreeActivityResult.launch(fileChooserIntent);
    }

    private View.OnClickListener removeThumbnailBtnListener() {
        return (v) -> {
            ((CreateSongImageLayout) CreatePlaylistFragment.this.getView().findViewById(R.id.create_playlist_menu_image_container)).removeCustomImage();
            v.setVisibility(View.GONE);
        };
    }

    private View.OnClickListener submitPlaylistButtonListener() {
        return (View view) -> {
            final MultipartRequestEntity multipartEntity = new MultipartRequestEntity();

            final CreateSongImageLayout playlistThumbnail = getView().findViewById(R.id.create_playlist_menu_image_container);

            final EditText playlistTitleInput = ((EditText) CreatePlaylistFragment.this.getView().findViewById(R.id.create_playlist_item_title_input));

            try {
                if (playlistTitleInput.getText().length() > 0) {
                    multipartEntity.appendData("playlistTitle", playlistTitleInput.getText().toString(), "text/plain");
                } else {
//                    TODO - add error message handling
//                    CreatePlaylistFragment.this.displayErrorMessage("Song title is a required field.");
//
                    return;
                }

                // Thumbnail part
                if (playlistThumbnail.isCustomImage()) {
                    multipartEntity.appendData(
                            "playlistThumbnail",
                            playlistThumbnail.getCustomImage(),
                            "image/jpeg",
                            "thumbnail.jpg");
                }

                final MultipartRequestThread requestThread = new MultipartRequestThread(
                        AppConfig.getProperty("url", CreatePlaylistFragment.this.getContext()) + Endpoints.CREATE_PLAYLIST,
                        true,
                        CreatePlaylistFragment.this.getContext(),
                        multipartEntity,
                        CreatePlaylistFragment.this.multipartRequestAction()
                );

                requestThread.start();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        };
    }

    private IHttpRequestAction multipartRequestAction() {
        return (status, response, headers) -> {
            final List<String> responseContentType = headers.get("content-type");

            if (status == 200) {
                CreatePlaylistFragment.this.emitFragmentEvent("refresh_dataset", null);

                CreatePlaylistFragment.this.getParentFragment().getChildFragmentManager().popBackStack();
            } else {
                // TODO - add error handling here too
//                if (responseContentType != null && CreatePlaylistFragment.this.parseHeaderContent(responseContentType.get(0)).contains("application/json")) {
//                    try {
//                        final JSONObject jsonResponse = new JSONObject(response);
//
//                        CreatePlaylistFragment.this.displayErrorMessage(jsonResponse.getString("message"));
//                    } catch (JSONException exc) {
//                        exc.printStackTrace();
//                    }
//                }
            }
        };
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
