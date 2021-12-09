package com.importusername.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.interfaces.IBackPressFragment;
import org.jetbrains.annotations.NotNull;

public class SettingsMenuFragment extends Fragment implements IBackPressFragment {
    public SettingsMenuFragment() {
        super(R.layout.music_player_settings_menu_fragment);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.music_player_settings_menu_fragment, container, false);

        view.findViewById(R.id.settings_menu_account_container).setOnClickListener(this.settingsButtonListener(SettingsMenuButtons.ACCOUNT));

        return view;
    }

    /**
     * Displays corresponding settings fragment.
     */
    private View.OnClickListener settingsButtonListener(SettingsMenuButtons name) {


        return (view) -> {
            switch (name) {
                case ACCOUNT:
                    SettingsMenuFragment.this.displaySettingsMenu(AccountSettingsFragment.class);

                    break;
                case APP:
                    break;
                default:
                    break;
            }
        };
    }

    private void displaySettingsMenu(Class<? extends Fragment> fragmentClass) {
        final FragmentManager fragmentManager = this.getChildFragmentManager();

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.slide_out,
                R.anim.slide_in,
                R.anim.slide_out
        );

        fragmentTransaction
                .replace(R.id.settings_menu_fragment_container, fragmentClass, null)
                .setReorderingAllowed(true)
                .addToBackStack("AccountSettingsFragment")
                .commit();
    }

    public enum SettingsMenuButtons {
        ACCOUNT,
        APP
    }

    @Override
    public boolean shouldAllowBackPress() {
        return (getChildFragmentManager().findFragmentById(R.id.settings_menu_fragment_container) instanceof IBackPressFragment
                && ((IBackPressFragment) getChildFragmentManager().findFragmentById(R.id.settings_menu_fragment_container)).shouldAllowBackPress());
    }
}
