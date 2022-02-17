package com.importusername.musicplayer.interfaces;

import androidx.annotation.Nullable;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;

import java.util.List;

/**
 * Used for allowing fragments to hide/display bottom panel during lifecycle methods such as onresume, onstop, etc
 */
public interface BottomPanelInterface {
    void setOnFragmentLifecycleChange(OnFragmentLifecycleChange listener);

    interface OnFragmentLifecycleChange {
        void displayBottomPanel(boolean display, @Nullable List<SongsMenuItem> songsMenuItemList);
    }
}
