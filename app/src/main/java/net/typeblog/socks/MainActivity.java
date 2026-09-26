package net.typeblog.socks;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayShowCustomEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
            View custom = getLayoutInflater().inflate(R.layout.actionbar_title, null);
            ab.setCustomView(custom);
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ProfileFragment())
                .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = getFragmentManager().findFragmentById(android.R.id.content);
        if (!(f instanceof ProfileFragment)) {
            return super.onOptionsItemSelected(item);
        }
        ProfileFragment pf = (ProfileFragment) f;

        int id = item.getItemId();
        if (id == R.id.action_add) {
            pf.addProfile();
            return true;
        } else if (id == R.id.action_power) {
            pf.toggleConnect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
