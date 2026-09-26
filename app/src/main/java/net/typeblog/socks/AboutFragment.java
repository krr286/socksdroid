package net.typeblog.socks;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle s) {
        TextView tv = new TextView(getActivity());
        tv.setText("THEK VPN\n\nVPN & Proxy\n\nВерсия 1.0");
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(16);
        tv.setPadding(48, 80, 48, 48);
        return tv;
    }
}
