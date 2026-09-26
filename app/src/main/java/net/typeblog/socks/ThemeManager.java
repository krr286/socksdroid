package net.typeblog.socks;

import android.content.Context;

public class ThemeManager {
    private static final String PREFS = "thek_prefs";
    private static final String KEY = "theme";

    public static final String[] NAMES = {"Тёмная", "Светлая", "Фиолетовая", "Розовая", "Бирюзовая", "Оранжевая"};
    public static final String[] KEYS  = {"dark",   "light",  "purple",     "pink",    "turquoise",  "orange"};

    public static void apply(Context ctx) {
        ctx.setTheme(styleFor(get(ctx)));
    }

    public static int styleFor(String key) {
        switch (key) {
            case "light": return R.style.AppTheme_Light;
            case "purple": return R.style.AppTheme_Purple;
            case "pink": return R.style.AppTheme_Pink;
            case "turquoise": return R.style.AppTheme_Turquoise;
            case "orange": return R.style.AppTheme_Orange;
            default: return R.style.AppTheme_Dark;
        }
    }

    public static String get(Context ctx) {
        return ctx.getSharedPreferences(PREFS, 0).getString(KEY, "dark");
    }

    public static void set(Context ctx, String key) {
        ctx.getSharedPreferences(PREFS, 0).edit().putString(KEY, key).apply();
    }
}
