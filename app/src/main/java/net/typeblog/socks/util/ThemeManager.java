package net.typeblog.socks.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import net.typeblog.socks.R;

public class ThemeManager {
    private static final String PREF_KEY = "app_theme";

    public static final int THEME_DARK = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_PURPLE = 2;

    public static void applyTheme(Activity activity) {
        switch (getTheme(activity)) {
            case THEME_LIGHT:
                activity.setTheme(R.style.AppTheme_Light);
                break;
            case THEME_PURPLE:
                activity.setTheme(R.style.AppTheme_Purple);
                break;
            default:
                activity.setTheme(R.style.AppTheme_Dark);
                break;
        }
    }

    public static void setTheme(Context context, int theme) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_KEY, theme).apply();
    }

    public static int getTheme(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_KEY, THEME_DARK);
    }

    public static String getThemeName(Context context) {
        switch (getTheme(context)) {
            case THEME_LIGHT: return "Светлая";
            case THEME_PURPLE: return "Фиолетовая";
            default: return "Тёмная";
        }
    }
}
