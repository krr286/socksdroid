package net.typeblog.socks;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle s) {
        ScrollView sv = new ScrollView(getActivity());
        TextView tv = new TextView(getActivity());
        String fallback = "THEK VPN\n\nVPN & Proxy\n\nВерсия 1.0";
        String txt = RemoteAssets.getText(getActivity(), "text_about", fallback);
        tv.setText(txt);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(16);
        tv.setPadding(48, 80, 48, 48);
        tv.setLineSpacing(0, 1.4f);
        sv.addView(tv);
        return sv;
    }
}
