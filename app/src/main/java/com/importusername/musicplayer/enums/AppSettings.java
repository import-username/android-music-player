package com.importusername.musicplayer.enums;

/**
 * Enum for app settings. Intended use is to place all settings related sharedprefs key names
 * in one place for easier access/refactoring.
 */
public enum AppSettings {
    PLAY_IN_BACKGROUND("play_audio_in_background", Types.BOOLEAN);

    private String settingName;

    private Types prefType;

    AppSettings(String settingName, Types prefType) {
        this.settingName = settingName;
        this.prefType = prefType;
    }

    public String getSettingName() {
        return this.settingName;
    }

    public Types getPrefType() {
        return this.prefType;
    }

    /**
     * Enum for getting preference from sharedprefs with specific type.
     * i.e get methods such as getBoolean, getString, etc
     */
    public enum Types {
        BOOLEAN,
        STRING
    }
}
