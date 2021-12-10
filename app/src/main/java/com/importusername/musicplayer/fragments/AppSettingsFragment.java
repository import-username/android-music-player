package com.importusername.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.enums.AppSettings;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import com.importusername.musicplayer.views.AppSettingsToggleButton;
import org.jetbrains.annotations.NotNull;

public class AppSettingsFragment extends Fragment implements IBackPressFragment {
    public AppSettingsFragment() {
        super(R.layout.app_settings_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.app_settings_menu_fragment, container, false);

        ((AppSettingsToggleButton) view.findViewById(R.id.play_audio_in_app_background_toggle)).setPreferenceName(
                AppSettings.PLAY_IN_BACKGROUND.getSettingName()
        );

        return view;
    }

    @Override
    public boolean shouldAllowBackPress() {
        return true;
    }
}
