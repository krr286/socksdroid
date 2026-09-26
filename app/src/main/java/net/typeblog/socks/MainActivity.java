package net.typeblog.socks;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

    private MenuItem mPowerItem;

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
        mPowerItem = menu.findItem(R.id.action_power);
        updatePowerIcon(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment f = getFragmentManager().findFragmentById(android.R.id.content);
        if (!(f instanceof ProfileFragment)) return super.onOptionsItemSelected(item);
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

    public void updatePowerIcon(boolean running) {
        if (mPowerItem == null) return;
        int color = running ? 0xFF7A3FF7 : 0xFF888888;
        if (mPowerItem.getIcon() != null) {
            mPowerItem.getIcon().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }
}
