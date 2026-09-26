package net.typeblog.socks;

import android.app.Activity;
import android.os.Bundle;

import net.typeblog.socks.util.ThemeManager;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Тема должна примениться ДО super.onCreate
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        this.getFragmentManager().beginTransaction()
            .replace(android.R.id.content, new ProfileFragment())
            .commit();
    }

    // Метод для перезапуска после смены темы
    public void restartForTheme() {
        recreate();
    }
}
