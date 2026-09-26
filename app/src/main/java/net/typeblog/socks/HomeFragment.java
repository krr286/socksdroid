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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_server, null);
        EditText n = view.findViewById(R.id.dlg_name);
        EditText s = view.findViewById(R.id.dlg_server);
        EditText p = view.findViewById(R.id.dlg_port);
        EditText u = view.findViewById(R.id.dlg_user);
        EditText pw = view.findViewById(R.id.dlg_pass);
        p.setText("1080");

        new AlertDialog.Builder(getActivity())
            .setTitle("Добавить сервер")
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
                Profile pr = mManager.addProfile(name);
                if (pr == null) {
                    Toast.makeText(getActivity(), "Такое имя уже есть", Toast.LENGTH_SHORT).show();
                    return;
                }
                pr.setServer(srv);
                try { pr.setPort(Integer.parseInt(port)); } catch (Exception ex) { pr.setPort(1080); }
                pr.setIsUserpw(!TextUtils.isEmpty(user));
                pr.setUsername(user);
                pr.setPassword(pass);

                mProfile = pr;
                mManager.switchDefault(name);
                loadProfiles();
            })
            .setNegativeButton("Отмена", null)
            .show();
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
