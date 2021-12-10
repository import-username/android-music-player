package com.importusername.musicplayer.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.importusername.musicplayer.R;
import com.importusername.musicplayer.enums.AppSettings;
import org.jetbrains.annotations.NotNull;

public class AppSettingsToggleButton extends ConstraintLayout {
    private String buttonTitle = "...";

    private String buttonDescription = "...";

    private String preferenceName;

    public AppSettingsToggleButton(@NonNull @NotNull Context context) {
        super(context);
        this.initializeLayout();
    }

    public AppSettingsToggleButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AppSettingsToggleButton,
                0,
                0
        );

        try {
            this.buttonTitle = typedArray.getString(R.styleable.AppSettingsToggleButton_buttonTitle);
            this.buttonDescription = typedArray.getString(R.styleable.AppSettingsToggleButton_buttonDescription);
            this.preferenceName = typedArray.getString(R.styleable.AppSettingsToggleButton_preferenceName);
        } catch (RuntimeException exception) {
            exception.printStackTrace();
        } finally {
            typedArray.recycle();
        }

        this.initializeLayout();
    }

    public AppSettingsToggleButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initializeLayout();
    }

    public AppSettingsToggleButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initializeLayout();
    }

    private void initializeLayout() {
        AppSettingsToggleButton.inflate(this.getContext(), R.layout.settings_menu_toggle_button, this);

        this.setTitle(this.buttonTitle);
        this.setDescription(this.buttonDescription);
        this.initializeToggleState();
    }

    public void setTitle(String title) {
        ((TextView) this.findViewById(R.id.settings_menu_toggle_button_title)).setText(title);
    }

    public void setDescription(String description) {
        ((TextView) this.findViewById(R.id.settings_menu_toggle_button_description)).setText(description);
    }

    public void setPreferenceName(String preferenceName) {
        this.preferenceName = preferenceName;
        this.initializeToggleState();
    }

    private void initializeToggleState() {
        if (this.preferenceName != null) {
            final SharedPreferences prefs = this.getContext().getSharedPreferences("app", 0);
            final SharedPreferences.Editor prefsEditor = prefs.edit();

            final Switch toggleButtonSwitch = this.findViewById(R.id.settings_menu_toggle_button_switch);

            toggleButtonSwitch.setChecked(
                    prefs.getBoolean(this.preferenceName, false)
            );

            toggleButtonSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                prefsEditor.putBoolean(
                        this.preferenceName,
                        isChecked
                );

                prefsEditor.apply();
            });
        }
    }
}
