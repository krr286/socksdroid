package net.typeblog.socks;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
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

    private LinearLayout mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle s) {
        ScrollView sv = new ScrollView(getActivity());
        mContainer = new LinearLayout(getActivity());
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.setPadding(50, 80, 50, 50);

        TextView header = new TextView(getActivity());
        header.setText("Для Telegram");
        header.setTextSize(26);
        header.setTextColor(0xFFFFFFFF);
        header.setPadding(0, 0, 0, 16);
        mContainer.addView(header);

        TextView sub = new TextView(getActivity());
        sub.setText("Нажми на прокси — Telegram сам предложит подключить. Работает без VPN.");
        sub.setTextSize(14);
        sub.setTextColor(0x99FFFFFF);
        sub.setPadding(0, 0, 0, 48);
        mContainer.addView(sub);

        TextView loading = new TextView(getActivity());
        loading.setText("Загрузка...");
        loading.setTextColor(0x66FFFFFF);
        loading.setTextSize(15);
        loading.setGravity(Gravity.CENTER);
        loading.setPadding(0, 100, 0, 0);
        loading.setId(999999);
        mContainer.addView(loading);

        sv.addView(mContainer);

        // Загружаем в фоне — на главном потоке network запрещён
        new Thread(() -> {
            List<JSONObject> proxies = null;
            String error = null;
            try {
                proxies = RemoteAssets.getMtProxies(getActivity());
            } catch (Exception e) {
                error = e.getMessage();
                android.util.Log.e("THEK_TG", "load error: " + e.getMessage(), e);
            }

            final List<JSONObject> finalProxies = proxies;
            final String finalError = error;

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                View loadingView = mContainer.findViewById(999999);
                if (loadingView != null) mContainer.removeView(loadingView);

                if (finalError != null) {
                    TextView err = new TextView(getActivity());
                    err.setText("Ошибка загрузки: " + finalError);
                    err.setTextColor(0xFFFF3F3F);
                    err.setTextSize(14);
                    err.setGravity(Gravity.CENTER);
                    err.setPadding(0, 60, 0, 0);
                    mContainer.addView(err);
                    return;
                }

                if (finalProxies == null || finalProxies.isEmpty()) {
                    TextView empty = new TextView(getActivity());
                    empty.setText("Прокси пока не добавлены.\nПопроси админа добавить их в боте.");
                    empty.setTextColor(0x66FFFFFF);
                    empty.setTextSize(15);
                    empty.setGravity(Gravity.CENTER);
                    empty.setPadding(0, 100, 0, 0);
                    mContainer.addView(empty);
                } else {
                    for (JSONObject p : finalProxies) {
                        mContainer.addView(buildProxyCard(p));
                    }
                }
            });
        }).start();

        return sv;
    }

    private View buildProxyCard(JSONObject p) {
        LinearLayout card = new LinearLayout(getActivity());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(40, 40, 40, 40);
        card.setBackgroundColor(0xFF1A1F2E);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 28);
        card.setLayoutParams(lp);

        String flag = p.optString("flag", "");
        String name = p.optString("name", "Proxy");
        String server = p.optString("server", "");
        int port = p.optInt("port", 443);

        JSONObject load = p.optJSONObject("load");
        int current = load != null ? load.optInt("current", 0) : 0;
        int max = load != null ? load.optInt("max", 500) : 500;

        LinearLayout topRow = new LinearLayout(getActivity());
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = new TextView(getActivity());
        title.setText(flag + " " + name);
        title.setTextSize(18);
        title.setTextColor(0xFFFFFFFF);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topRow.addView(title);

        TextView loadView = new TextView(getActivity());
        String indicator = current < max * 0.5 ? "🟢" : current < max * 0.8 ? "🟡" : "🔴";
        loadView.setText(indicator + " " + current + "/" + max);
        loadView.setTextSize(13);
        loadView.setTextColor(0xFFFFFFFF);
        topRow.addView(loadView);
        card.addView(topRow);

        LinearLayout addrRow = new LinearLayout(getActivity());
        addrRow.setOrientation(LinearLayout.HORIZONTAL);
        addrRow.setPadding(0, 8, 0, 20);

        TextView addr = new TextView(getActivity());
        addr.setText(server + ":" + port);
        addr.setTextSize(13);
        addr.setTextColor(0xFF29B6F6);
        addr.setLayoutParams(new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        addrRow.addView(addr);

        TextView pingView = new TextView(getActivity());
        pingView.setText("пинг...");
        pingView.setTextSize(13);
        pingView.setTextColor(0x99FFFFFF);
        addrRow.addView(pingView);
        card.addView(addrRow);

        TextView btn = new TextView(getActivity());
        btn.setText("Подключить в Telegram");
        btn.setTextSize(15);
        btn.setTextColor(0xFFFFFFFF);
        btn.setGravity(Gravity.CENTER);
        btn.setBackgroundColor(0xFF0288D1);
        btn.setPadding(30, 24, 30, 24);
        btn.setOnClickListener(v -> connectTelegram(p));
        card.addView(btn);

        new Thread(() -> {
            long ms = pingHost(server, port);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (ms < 0) {
                    pingView.setText("нет связи");
                    pingView.setTextColor(0xFFFF3F3F);
                } else {
                    pingView.setText(ms + " мс");
                    if (ms < 150) pingView.setTextColor(0xFF3FFF7A);
                    else if (ms < 400) pingView.setTextColor(0xFFFFCC33);
                    else pingView.setTextColor(0xFFFF3F3F);
                }
            });
        }).start();

        return card;
    }

    private long pingHost(String host, int port) {
        try {
            long start = java.lang.System.currentTimeMillis();
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress(host, port), 3000);
            long ms = java.lang.System.currentTimeMillis() - start;
            s.close();
            return ms;
        } catch (Exception e) {
            return -1;
        }
    }

    private void connectTelegram(JSONObject p) {
        String link = p.optString("tg_link", "");
        if (link.isEmpty()) {
            Toast.makeText(getActivity(), "Ссылка пустая", Toast.LENGTH_SHORT).show();
            return;
        }
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
