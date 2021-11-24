package com.importusername.musicplayer.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
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
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MultipartRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.views.CreateSongImageLayout;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        view.findViewById(R.id.submit_song_button).setOnClickListener(this.submitSongButtonListener());

        return view;
    }

    /**
     * Creates multipart entity object with all the available user input fields and sends request to server.
     * @return View.OnClickListener function
     */
    private View.OnClickListener submitSongButtonListener() {
        return (View view) -> {
            final MultipartRequestEntity multipartEntity = new MultipartRequestEntity();

            final Uri fileUri = Uri.parse(getArguments().getString("song_file_uri"));

            final CreateSongImageLayout songThumbnail = getView().findViewById(R.id.create_song_menu_image_container);

            final EditText songTitleInput = ((EditText) CreateSongItemMenuFragment.this.getView().findViewById(R.id.create_song_item_title_input));

            final EditText songAuthorInput = ((EditText) CreateSongItemMenuFragment.this.getView().findViewById(R.id.create_song_item_author_input));

            try {
                // Song title part
                if (songTitleInput.getText().length() > 0) {
                    multipartEntity.appendData("songTitle", songTitleInput.getText().toString(), "text/plain");
                } else {
                    CreateSongItemMenuFragment.this.displayErrorMessage("Song title is a required field.");

                    return;
                }

                // Song file part
                InputStream inputStream = CreateSongItemMenuFragment.this.getContext().getContentResolver().openInputStream(fileUri);

                final String filename = DocumentFile.fromSingleUri(CreateSongItemMenuFragment.this.getContext(), fileUri).getName();

                multipartEntity.appendData("songFile", inputStream, URLConnection.guessContentTypeFromName(filename), filename);

                // Thumbnail part
                if (songThumbnail.isCustomImage()) {
                    multipartEntity.appendData(
                        "songThumbnail",
                        songThumbnail.getCustomImage(),
                        "image/jpeg",
                        "song_thumbnail.jpg");
                }

                // Author part
                if (songAuthorInput.getText().length() > 0) {
                    multipartEntity.appendData(
                            "songAuthor",
                            songAuthorInput.getText().toString(),
                            "text/plain"
                    );
                }

                final MultipartRequestThread requestThread = new MultipartRequestThread(
                        AppConfig.getProperty("url", CreateSongItemMenuFragment.this.getContext()) + "/song/upload-song",
                        true,
                        CreateSongItemMenuFragment.this.getContext(),
                        multipartEntity,
                        CreateSongItemMenuFragment.this.multipartRequestAction()
                );

                requestThread.start();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        };
    }

//    private IHttpRequestAction createSongRequestAction() {
//        return (status, response, headers) -> {
//            Log.d("CreateSongItemMenuFrag", status + "");
//        };
//    }

    /**
     * Click listener for create-song-image-layout view.
     * @return View.OnClickListener function
     */
    private View.OnClickListener createSongImageListener() {
        return (View view) -> {
            CreateSongItemMenuFragment.this.displayDirectoryTree();
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

    /**
     * Removes custom image set by user and resets to default image.
     * @return View.OnClickListener function
     */
    private View.OnClickListener removeThumbnailBtnListener() {
        return (View view) -> {
            ((CreateSongImageLayout) CreateSongItemMenuFragment.this.getView().findViewById(R.id.create_song_menu_image_container)).removeCustomImage();
            view.setVisibility(View.GONE);
        };
    }

    /**
     * Action to perform when multipart request is completed.
     * @return IHttpRequestAction function
     */
    private IHttpRequestAction multipartRequestAction() {
        return (status, response, headers) -> {
            final List<String> responseContentType = headers.get("content-type");

            if (status == 200) {
                final SongsMenuFragment songsMenuFragment = ((SongsMenuFragment) getParentFragment().getParentFragment());

                songsMenuFragment.notifySongDataChange();

                // TODO - redirect to corresponding song menu or something
            } else {
                if (responseContentType != null && CreateSongItemMenuFragment.this.parseHeaderContent(responseContentType.get(0)).contains("application/json")) {
                    try {
                        final JSONObject jsonResponse = new JSONObject(response);

                        CreateSongItemMenuFragment.this.displayErrorMessage(jsonResponse.getString("message"));
                    } catch (JSONException exc) {
                        exc.printStackTrace();
                    }
                }
            }
        };
    }

    /**
     * Isolates all header content fields separated by a semicolon into a list object.
     * @param content Header content field (ex. 'application/json; charset=utf-8')
     * @return List object with generic type string.
     */
    private List<String> parseHeaderContent(String content) {
        String removeWhiteSpace = content.replace(" ", "");
        List<String> parsedStringList = Arrays.asList(removeWhiteSpace.split(";"));

        return parsedStringList;
    }

    /**
     * Displays an error message on the fragment's ui.
     * @param message Error message to display.
     */
    private void displayErrorMessage(String message) {
        CreateSongItemMenuFragment.this.getActivity().runOnUiThread(() -> {
            final TextView errorText = CreateSongItemMenuFragment.this.getView().findViewById(R.id.create_song_item_error_text);
            errorText.setText(message);

            errorText.setVisibility(View.VISIBLE);
        });
    }

    private void uploadSongFile() {}
}
