package net.typeblog.socks;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

public class TelegramFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle s) {
        ScrollView sv = new ScrollView(getActivity());
        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(40, 60, 40, 40);

        TextView header = new TextView(getActivity());
        header.setText("Telegram-прокси");
        header.setTextSize(24);
        header.setTextColor(0xFFFFFFFF);
        header.setPadding(0, 0, 0, 20);
        ll.addView(header);

        TextView sub = new TextView(getActivity());
        sub.setText("Нажми на прокси, чтобы подключить его в Telegram. Работает без VPN.");
        sub.setTextSize(14);
        sub.setTextColor(0xFF99FFFFFF);
        sub.setPadding(0, 0, 0, 40);
        ll.addView(sub);

        List<JSONObject> proxies = RemoteAssets.getMtProxies(getActivity());
        if (proxies.isEmpty()) {
            TextView empty = new TextView(getActivity());
            empty.setText("Прокси пока не добавлены админом.");
            empty.setTextColor(0xFF66FFFFFF);
            empty.setPadding(0, 40, 0, 0);
            ll.addView(empty);
        } else {
            for (JSONObject p : proxies) {
                ll.addView(buildProxyCard(p));
            }
        }

        sv.addView(ll);
        return sv;
    }

    private View buildProxyCard(JSONObject p) {
        LinearLayout card = new LinearLayout(getActivity());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(30, 30, 30, 30);
        card.setBackgroundColor(0xFF1A1F2E);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 24);
        card.setLayoutParams(lp);

        TextView title = new TextView(getActivity());
        title.setText(p.optString("flag", "") + " " + p.optString("name", "Proxy"));
        title.setTextSize(18);
        title.setTextColor(0xFFFFFFFF);
        title.setPadding(0, 0, 0, 8);
        card.addView(title);

        TextView addr = new TextView(getActivity());
        addr.setText(p.optString("server") + ":" + p.optInt("port", 443));
        addr.setTextSize(13);
        addr.setTextColor(0xFF29B6F6);
        addr.setPadding(0, 0, 0, 16);
        card.addView(addr);

        TextView btn = new TextView(getActivity());
        btn.setText("  Подключить в Telegram  ");
        btn.setTextSize(15);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0xFF0288D1);
        btn.setPadding(30, 20, 30, 20);
        btn.setOnClickListener(v -> connectTelegram(p));
        card.addView(btn);

        return card;
    }

    private void connectTelegram(JSONObject p) {
        String link = p.optString("tg_link", "");
        if (link.isEmpty()) return;
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            i.setPackage("org.telegram.messenger");
            startActivity(i);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            } catch (Exception e2) {
                Toast.makeText(getActivity(), "Telegram не установлен", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
