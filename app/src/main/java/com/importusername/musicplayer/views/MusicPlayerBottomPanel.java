package com.importusername.musicplayer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.exoplayer2.Player;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.adapters.songsmenu.SongsMenuItem;
import com.importusername.musicplayer.fragments.SongFragment;
import com.importusername.musicplayer.services.SongItemService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MusicPlayerBottomPanel extends ConstraintLayout {
    private SongItemService service;

    private List<SongsMenuItem> songsMenuItemList;

    private OnCloseListener onCloseListener;

    public MusicPlayerBottomPanel(@NonNull @NotNull Context context) {
        super(context);
        this.initializeLayout();
    }

    public MusicPlayerBottomPanel(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initializeLayout();
    }

    public MusicPlayerBottomPanel(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initializeLayout();
    }

    public MusicPlayerBottomPanel(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initializeLayout();
    }

    /**
     * This should be called in the onCreate or onCreateView of enclosing activity/fragment/etc before user interaction.
     * @param service Service with exoplayer object.
     */
    public void setExoplayerService(SongItemService service) {
        this.service = service;
    }

    public void setSongitemsList(List<SongsMenuItem> list) {
        this.songsMenuItemList = list;

        this.findViewById(R.id.bottom_panel_skip_left).setOnClickListener((v) -> {
            MusicPlayerBottomPanel.this.service.getExoPlayer().seekToPreviousMediaItem();
        });

        this.findViewById(R.id.bottom_panel_skip_right).setOnClickListener((v) -> {
            MusicPlayerBottomPanel.this.service.getExoPlayer().seekToNextMediaItem();
        });

        ((TextView) this.findViewById(R.id.music_player_bottom_panel_title)).setText(
                this.songsMenuItemList.get(this.service.getExoPlayer().getCurrentMediaItemIndex()).getSongName()
        );

        ((TextView) this.findViewById(R.id.music_player_bottom_panel_author)).setText(
                this.songsMenuItemList.get(this.service.getExoPlayer().getCurrentMediaItemIndex()).getAuthor() != null
                        ? "by " + this.songsMenuItemList.get(this.service.getExoPlayer().getCurrentMediaItemIndex()).getAuthor()
                        : "by ..."
        );

        this.service.getExoPlayer().addListener(new Player.Listener() {
            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition,
                                                Player.PositionInfo newPosition,
                                                int reason) {
                ((TextView) MusicPlayerBottomPanel.this.findViewById(R.id.music_player_bottom_panel_title))
                        .setText(MusicPlayerBottomPanel.this.songsMenuItemList.get(newPosition.mediaItemIndex)
                                .getSongName());

                ((TextView) MusicPlayerBottomPanel.this.findViewById(R.id.music_player_bottom_panel_author))
                        .setText(
                                MusicPlayerBottomPanel.this.songsMenuItemList.get(newPosition.mediaItemIndex).getAuthor() != null
                                ? "by " + MusicPlayerBottomPanel.this.songsMenuItemList.get(newPosition.mediaItemIndex).getAuthor() : "by ..."
                        );

                MusicPlayerBottomPanel.this.service.displayNotification(
                        "Now playing",
                        MusicPlayerBottomPanel.this.songsMenuItemList.get(service.getExoPlayer().getCurrentMediaItemIndex()).getSongName()
                );
            }
        });
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.onCloseListener = listener;
    }

    private void initializeLayout() {
        MusicPlayerBottomPanel.inflate(this.getContext(), R.layout.music_player_bottom_panel, this);

        this.findViewById(R.id.close_bottom_panel_button).setOnClickListener((v) -> {
            this.setVisibility(View.GONE);

            if (MusicPlayerBottomPanel.this.onCloseListener != null) {
                MusicPlayerBottomPanel.this.onCloseListener.onPanelClosed();
            }
        });

        final ImageView pauseBtn = this.findViewById(R.id.pause_button);
        final ImageView playBtn = this.findViewById(R.id.play_button);

        this.findViewById(R.id.pause_button).setOnClickListener((v) -> {
            pauseBtn.setVisibility(View.INVISIBLE);
            playBtn.setVisibility(View.VISIBLE);

            if (this.service != null) {
                this.service.pauseAudio();
            }
        });

        this.findViewById(R.id.play_button).setOnClickListener((v) -> {
            pauseBtn.setVisibility(View.VISIBLE);
            playBtn.setVisibility(View.INVISIBLE);

            if (this.service != null) {
                this.service.resumeAudio();
            }
        });
    }

    public interface OnCloseListener {
        void onPanelClosed();
    }
}
