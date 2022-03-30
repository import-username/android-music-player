package com.importusername.musicplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.constants.Endpoints;
import com.importusername.musicplayer.enums.RequestMethod;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.interfaces.IHttpRequestAction;
import com.importusername.musicplayer.threads.MusicPlayerRequestThread;
import com.importusername.musicplayer.util.AppConfig;
import com.importusername.musicplayer.util.AppHttp;
import com.importusername.musicplayer.util.AppToast;
import org.jetbrains.annotations.NotNull;

public class CreateSongFromLinkFragment extends Fragment implements IBackPressFragment {
    private OnRequestCompleteListener onRequestCompleteListener;

    public CreateSongFromLinkFragment() {
        super(R.layout.create_song_from_yt_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(
                R.layout.create_song_from_yt_fragment,
                container,
                false
        );

        view.findViewById(R.id.submit_youtube_link_button).setOnClickListener(this.onSubmitLinkClicked());

        return view;
    }

    private View.OnClickListener onSubmitLinkClicked() {
        return (v) -> {
            try {
                final Uri videoUrl = Uri.parse(((EditText) this.getView().findViewById(R.id.create_song_from_yt_input)).getText().toString());

                if ((videoUrl.getHost().equals("www.youtube.com") || videoUrl.getHost().equals("youtube.com"))) {
                    final String videoId = AppHttp.getQueryParams(videoUrl).get("v");

                    if (videoId != null) {
                        final String url = AppConfig.getProperty("url", this.getContext())
                                + Endpoints.CREATE_FROM_YT
                                + "?ytVideoId=" + videoId;

                        this.sendUploadLinkRequest(url);
                    }
                } else if (videoUrl.getHost().equals("youtu.be")) {
                    final String urlParam = videoUrl.getPath();

                    if (urlParam != null) {
                        final String url = AppConfig.getProperty("url", this.getContext())
                                + Endpoints.CREATE_FROM_YT
                                + "?ytVideoId=" + urlParam.replace("/", "");

                        this.sendUploadLinkRequest(url);
                    }
                } else {
                    AppToast.showToast("Invalid video url.", this.getActivity());
                }

            } catch (NullPointerException nullExc) {
                nullExc.printStackTrace();
            }
        };
    }

    private void sendUploadLinkRequest(String url) {
        final MusicPlayerRequestThread requestThread = new MusicPlayerRequestThread(
                url,
                RequestMethod.POST,
                this.getContext(),
                true,
                this.uploadLinkRequestAction()
        );

        this.getActivity().runOnUiThread(() -> {
            this.getView().findViewById(R.id.upload_link_progress_bar).setVisibility(View.VISIBLE);
            this.getView().findViewById(R.id.upload_link_progress_text).setVisibility(View.VISIBLE);
        });

        requestThread.start();
    }

    private IHttpRequestAction uploadLinkRequestAction() {
        return (status, response, headers) -> {
            this.getActivity().runOnUiThread(() -> {
                this.getView().findViewById(R.id.upload_link_progress_bar).setVisibility(View.GONE);
                this.getView().findViewById(R.id.upload_link_progress_text).setVisibility(View.GONE);
            });

            if (status == 200) {
                this.getParentFragmentManager().popBackStack();

                if (this.onRequestCompleteListener != null) {
                    this.onRequestCompleteListener.onComplete();
                }
            } else {
                AppToast.showToast("Failed to create song.", this.getActivity());
            }
        };
    }

    public void setOnRequestCompleteListener(OnRequestCompleteListener listener) {
        this.onRequestCompleteListener = listener;
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }

    public interface OnRequestCompleteListener {
        void onComplete();
    }
}
