package net.typeblog.socks;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private int mCurrentNav = -1;
    private MenuItem mPowerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawer = findViewById(R.id.drawer_layout);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        setNav(R.id.nav_home);
        setNav(R.id.nav_settings);
        setNav(R.id.nav_about);

        if (savedInstanceState == null) {
            selectItem(R.id.nav_home);
        }
    }

    public void openDrawer() {
        mDrawer.openDrawer(GravityCompat.START);
    }

    private void setNav(int id) {
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
        if (id == R.id.nav_settings) {
            f = new SettingsFragment();
        } else {
            f = new HomeFragment();
        }

        getFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit();

        mDrawer.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mPowerItem = menu.findItem(R.id.action_power);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
        int id = item.getItemId();

        if (id == R.id.action_add) {
            if (f instanceof HomeFragment) ((HomeFragment) f).addServer();
            return true;
        } else if (id == R.id.action_power) {
            if (f instanceof HomeFragment) ((HomeFragment) f).toggleConnect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setPowerColor(boolean running) {
        if (mPowerItem == null || mPowerItem.getIcon() == null) return;
        int color = running ? 0xFF7A3FF7 : 0xFF888888;
        mPowerItem.getIcon().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) mDrawer.closeDrawers();
        else super.onBackPressed();
    }
}
