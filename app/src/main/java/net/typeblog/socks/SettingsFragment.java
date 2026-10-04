package net.typeblog.socks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment {

    private static final String KEY_THEME = "pref_theme";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        Preference themePref = findPreference(KEY_THEME);
        if (themePref == null) {
            // Если в settings.xml нет — создаём программно
            Preference p = new Preference(getActivity());
            p.setKey(KEY_THEME);
            p.setTitle("Тема");
            p.setSummary("Сменить тему приложения");
            p.setOnPreferenceClickListener(pref -> {
                showThemeDialog();
                return true;
            });
            getPreferenceScreen().addPreference(p);
        } else {
            themePref.setOnPreferenceClickListener(pref -> {
                showThemeDialog();
                return true;
            });
        }

        // Обновляем summary
        updateThemeSummary();
    }

    private void updateThemeSummary() {
        Preference p = findPreference(KEY_THEME);
        if (p != null) {
            String cur = ThemeManager.get(getActivity());
            int idx = 0;
            for (int i = 0; i < ThemeManager.KEYS.length; i++) {
                if (ThemeManager.KEYS[i].equals(cur)) idx = i;
            }
            p.setSummary(ThemeManager.NAMES[idx]);
        }
    }

    private void showThemeDialog() {
        String cur = ThemeManager.get(getActivity());
        int idx = 0;
        for (int i = 0; i < ThemeManager.KEYS.length; i++) {
            if (ThemeManager.KEYS[i].equals(cur)) idx = i;
        }
        new AlertDialog.Builder(getActivity())
            .setTitle("Тема")
            .setSingleChoiceItems(ThemeManager.NAMES, idx, (d, w) -> {
                ThemeManager.set(getActivity(), ThemeManager.KEYS[w]);
                d.dismiss();
                getActivity().recreate();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
}
