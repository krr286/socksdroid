package net.typeblog.socks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;
import net.typeblog.socks.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ServerAdapter.Listener {

    private ProfileManager mManager;
    private Profile mProfile;
    private FrameLayout mConnectBtn;
    private ImageView mConnectIcon;
    private TextView mStatus, mCurrentServer;
    private RecyclerView mList;
    private ServerAdapter mAdapter;
    private final List<Profile> mProfiles = new ArrayList<>();
    private boolean mRunning = false, mStarting = false, mStopping = false;
    private IVpnService mBinder;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final ServiceConnection mConn = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName n, IBinder b) {
            mBinder = IVpnService.Stub.asInterface(b);
            try { mRunning = mBinder.isRunning(); } catch (Exception e) { mRunning = false; }
            updateState();
        }
        @Override public void onServiceDisconnected(ComponentName n) { mBinder = null; }
    };

    private final Runnable mStateTick = new Runnable() {
        @Override public void run() {
            updateState();
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mManager = new ProfileManager(getActivity().getApplicationContext());

        mConnectBtn = v.findViewById(R.id.connect_button);
        mConnectIcon = v.findViewById(R.id.connect_icon);
        mStatus = v.findViewById(R.id.home_status);
        mCurrentServer = v.findViewById(R.id.current_server);
        mList = v.findViewById(R.id.servers_list);

        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ServerAdapter(mProfiles, this);
        mList.setAdapter(mAdapter);

        mConnectBtn.setOnClickListener(x -> toggleConnect());

        loadProfiles();
        checkState();
        mHandler.postDelayed(mStateTick, 300);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mStateTick);
        if (mBinder != null) {
            try { getActivity().unbindService(mConn); } catch (Exception ignored) {}
            mBinder = null;
        }
    }

    private void loadProfiles() {
        mProfiles.clear();
        String[] names = mManager.getProfiles();
        String defName = mManager.getDefault().getName();
        int sel = -1;
        for (int i = 0; i < names.length; i++) {
            if ("По умолчанию".equals(names[i])) continue;
            Profile p = mManager.getProfile(names[i]);
            if (p != null) {
                mProfiles.add(p);
                if (names[i].equals(defName)) sel = mProfiles.size() - 1;
            }
        }
        mProfile = mManager.getDefault();
        mAdapter.setSelectedIndex(sel);
        mCurrentServer.setText(mProfile != null ? mProfile.getName() : "");
    }

    @Override public void onSelect(Profile p) {
        mProfile = p;
        mManager.switchDefault(p.getName());
        loadProfiles();
    }

    @Override public void onLongClick(Profile p) {
        new AlertDialog.Builder(getActivity())
            .setItems(new String[]{"Удалить"}, (d, w) -> {
                mManager.removeProfile(p.getName());
                mProfile = mManager.getDefault();
                loadProfiles();
            }).show();
    }

    public void addServer() {
        new AlertDialog.Builder(getActivity())
            .setTitle("Добавить сервер")
            .setItems(new String[]{"По ссылке (URL / подписка)", "Ввести вручную"}, (d, w) -> {
                if (w == 0) addByUrl();
                else addManually();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void addManually() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_server, null);
        EditText n = view.findViewById(R.id.dlg_name);
        EditText s = view.findViewById(R.id.dlg_server);
        EditText p = view.findViewById(R.id.dlg_port);
        EditText u = view.findViewById(R.id.dlg_user);
        EditText pw = view.findViewById(R.id.dlg_pass);
        p.setText("1080");

        new AlertDialog.Builder(getActivity())
            .setTitle("Новый сервер")
            .setView(view)
            .setPositiveButton("Сохранить", (d, w) -> {
                String name = n.getText().toString().trim();
                String srv = s.getText().toString().trim();
                String port = p.getText().toString().trim();
                String user = u.getText().toString().trim();
                String pass = pw.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(srv) || TextUtils.isEmpty(port)) {
                    Toast.makeText(getActivity(), "Заполни имя, IP и порт", Toast.LENGTH_SHORT).show();
                    return;
                }
                createProfile(name, srv, port, user, pass);
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void addByUrl() {
        final EditText e = new EditText(getActivity());
        e.setHint("socks5://user:pass@host:port или http://.../sub");

        new AlertDialog.Builder(getActivity())
            .setTitle("Ссылка")
            .setView(e)
            .setPositiveButton("Загрузить", (d, w) -> {
                String url = e.getText().toString().trim();
                if (TextUtils.isEmpty(url)) return;
                fetchUrl(url);
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void fetchUrl(final String url) {
        Toast.makeText(getActivity(), "Загрузка...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            String body = null;
            try {
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection c = (java.net.HttpURLConnection) u.openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                c.setRequestProperty("User-Agent", "TheK/1.0");
                java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("
");
                br.close();
                body = sb.toString();
            } catch (Exception ex) {
                final String err = ex.getMessage();
                mHandler.post(() -> Toast.makeText(getActivity(), "Ошибка: " + err, Toast.LENGTH_LONG).show());
                return;
            }
            final String content = body;
            mHandler.post(() -> parseAndAdd(content));
        }).start();
    }

    private void parseAndAdd(String content) {
        if (content == null) return;
        String data = content.trim();

        // Попытка base64-декодирования
        if (!data.contains("://") && data.length() > 20) {
            try {
                byte[] dec = android.util.Base64.decode(data, android.util.Base64.DEFAULT);
                String dec2 = new String(dec, "UTF-8");
                if (dec2.contains("://")) data = dec2;
            } catch (Exception ignored) {}
        }

        int added = 0;
        String[] lines = data.split("?
");
        int n = 1;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#") || line.startsWith("//")) continue;

            String name = "Сервер " + n;
            String srv = null, port = null, user = null, pass = null;

            try {
                if (line.startsWith("socks5://") || line.startsWith("socks://")) {
                    java.net.URI uri = java.net.URI.create(line.replace("socks://", "socks5://"));
                    srv = uri.getHost();
                    port = String.valueOf(uri.getPort());
                    String ui = uri.getUserInfo();
                    if (ui != null && ui.contains(":")) {
                        user = ui.split(":")[0];
                        pass = ui.split(":")[1];
                    }
                    if (uri.getFragment() != null) name = uri.getFragment();
                } else if (line.contains(":") && !line.contains(" ")) {
                    // host:port
                    String[] parts = line.split(":");
                    if (parts.length == 2) { srv = parts[0]; port = parts[1]; }
                }
            } catch (Exception ignored) { continue; }

            if (srv != null && port != null) {
                createProfile(name, srv, port, user, pass);
                added++;
                n++;
            }
        }

        if (added == 0) {
            Toast.makeText(getActivity(), "Не нашли SOCKS5 в ссылке. Нужен VLESS? Скажи — переделаем.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Добавлено: " + added, Toast.LENGTH_SHORT).show();
        }
    }

    private void createProfile(String name, String srv, String port, String user, String pass) {
        Profile pr = mManager.addProfile(name);
        if (pr == null) {
            pr = mManager.addProfile(name + "_" + System.currentTimeMillis());
            if (pr == null) return;
        }
        pr.setServer(srv);
        try { pr.setPort(Integer.parseInt(port)); } catch (Exception ex) { pr.setPort(1080); }
        pr.setIsUserpw(!TextUtils.isEmpty(user));
        pr.setUsername(user == null ? "" : user);
        pr.setPassword(pass == null ? "" : pass);
        mProfile = pr;
        mManager.switchDefault(name);
        loadProfiles();
    }

    public void toggleConnect() {
        if (mRunning) stopVpn(); else startVpn();
    }

    private void checkState() {
        if (mBinder == null) {
            getActivity().bindService(
                new Intent(getActivity(), SocksVpnService.class), mConn, 0);
        }
    }

    private void updateState() {
        if (mBinder == null) mRunning = false;
        else try { mRunning = mBinder.isRunning(); } catch (Exception e) { mRunning = false; }

        if (mStatus != null) {
            mStatus.setText(mRunning ? "ПОДКЛЮЧЕНО" : "НЕ ЗАЩИЩЕНО");
            mStatus.setTextColor(mRunning ? 0xFF7A3FF7 : 0xFF888888);
        }

        if (mConnectBtn != null) {
            if (mRunning) {
                mConnectBtn.setBackgroundResource(R.drawable.bg_connect_button_on);
            } else {
                mConnectBtn.setBackgroundResource(R.drawable.bg_connect_button);
            }
        }

        if (mStarting && mRunning) mStarting = false;
        if (mStopping && !mRunning) mStopping = false;
    }

    private void startVpn() {
        if (mProfile == null) {
            Toast.makeText(getActivity(), "Добавь сервер", Toast.LENGTH_SHORT).show();
            return;
        }
        mStarting = true;
        Intent i = VpnService.prepare(getActivity());
        if (i != null) startActivityForResult(i, 0);
        else onActivityResult(0, Activity.RESULT_OK, null);
    }

    private void stopVpn() {
        if (mBinder == null) return;
        mStopping = true;
        try { mBinder.stop(); } catch (Exception ignored) {}
        mBinder = null;
        try { getActivity().unbindService(mConn); } catch (Exception ignored) {}
        checkState();
    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res == Activity.RESULT_OK && mProfile != null) {
            Utility.startVpn(getActivity(), mProfile);
            checkState();
        } else mStarting = false;
    }
}
