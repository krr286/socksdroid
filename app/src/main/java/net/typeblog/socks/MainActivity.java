package net.typeblog.socks;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import net.typeblog.socks.util.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private int mCurrentNav = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawer = findViewById(R.id.drawer_layout);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            getSupportActionBar().setTitle("THEK");
        }

        setNavClickListener(R.id.nav_home);
        setNavClickListener(R.id.nav_servers);
        setNavClickListener(R.id.nav_settings);
        setNavClickListener(R.id.nav_about);

        if (savedInstanceState == null) {
            selectItem(R.id.nav_home);
        }
    }

    private void setNavClickListener(int id) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(view -> selectItem(id));
    }

    private void selectItem(int id) {
        if (mCurrentNav == id) {
            mDrawer.closeDrawers();
            return;
        }
        mCurrentNav = id;

        Fragment f;
        if (id == R.id.nav_servers) {
            f = new ServersFragment();
        } else if (id == R.id.nav_settings) {
            f = new SettingsFragment();
        } else if (id == R.id.nav_about) {
            f = new AboutFragment();
        } else {
            f = new HomeFragment();
        }

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit();

        mDrawer.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    public void navigateTo(int id) {
        mCurrentNav = -1;
        selectItem(id);
    }
}
