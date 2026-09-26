package net.typeblog.socks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

    private View mConnectButton;
    private ImageView mConnectIcon;
    private TextView mConnectLabel, mConnectStatus;
    private RecyclerView mList;
    private TextView mNoServers;
    private ServerAdapter mAdapter;
    private List<Profile> mProfiles = new ArrayList<>();

    private boolean mRunning = false;
    private boolean mStarting = false, mStopping = false;
    private IVpnService mBinder;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBinder = IVpnService.Stub.asInterface(binder);
            try { mRunning = mBinder.isRunning(); } catch (Exception e) { mRunning = false; }
            updateState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    };

    private final Runnable mStateRunnable = new Runnable() {
        @Override
        public void run() {
            updateState();
            if (mConnectButton != null) mConnectButton.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mManager = new ProfileManager(requireContext().getApplicationContext());
        mConnectButton = v.findViewById(R.id.connect_button);
        mConnectIcon = v.findViewById(R.id.connect_icon);
        mConnectLabel = v.findViewById(R.id.connect_label);
        mConnectStatus = v.findViewById(R.id.connect_status);
        mList = v.findViewById(R.id.servers_list);
        mNoServers = v.findViewById(R.id.no_servers);

        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ServerAdapter(mProfiles, this);
        mList.setAdapter(mAdapter);

        mConnectButton.setOnClickListener(view -> toggleConnect());

        ImageButton add = v.findViewById(R.id.btn_add);
        add.setOnClickListener(view -> addServer());

        ImageButton settings = v.findViewById(R.id.btn_settings);
        settings.setOnClickListener(view -> ((MainActivity) requireActivity()).navigateTo(R.id.nav_settings));

        loadProfiles();
        checkState();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mConnectButton != null) mConnectButton.removeCallbacks(mStateRunnable);
        if (mBinder != null) {
            try { requireContext().unbindService(mConnection); } catch (Exception ignored) {}
            mBinder = null;
        }
    }

    private void loadProfiles() {
        mProfiles.clear();
        String[] names = mManager.getProfiles();
        String defName = mManager.getDefault().getName();
        int selIdx = 0;
        for (int i = 0; i < names.length; i++) {
            Profile p = mManager.getProfile(names[i]);
            if (p != null) {
                mProfiles.add(p);
                if (names[i].equals(defName)) selIdx = i;
            }
        }
        mProfile = mManager.getDefault();

        if (mProfiles.isEmpty()) {
            mNoServers.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
        } else {
            mNoServers.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
        }

        mAdapter.setSelectedIndex(selIdx);
    }

    @Override
    public void onSelect(Profile p) {
        mProfile = p;
        mManager.switchDefault(p.getName());
    }

    @Override
    public void onLongClick(Profile p) {
        new AlertDialog.Builder(requireContext())
            .setItems(new String[]{"Редактировать", "Удалить"}, (d, which) -> {
                if (which == 0) editServer(p);
                else deleteServer(p);
            })
            .show();
    }

    private void addServer() {
        showServerDialog(null);
    }

    private void editServer(Profile p) {
        showServerDialog(p);
    }

    private void showServerDialog(@Nullable final Profile existing) {
        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_server, null);
        EditText name = dialogView.findViewById(R.id.dlg_name);
        EditText ip = dialogView.findViewById(R.id.dlg_ip);
        EditText port = dialogView.findViewById(R.id.dlg_port);
        EditText user = dialogView.findViewById(R.id.dlg_user);
        EditText pass = dialogView.findViewById(R.id.dlg_pass);

        if (existing != null) {
            name.setText(existing.getName());
            ip.setText(existing.getServer());
            port.setText(String.valueOf(existing.getPort()));
            user.setText(existing.getUsername());
            pass.setText(existing.getPassword());
        } else {
            port.setText("1080");
        }

        new AlertDialog.Builder(requireContext())
            .setTitle(existing == null ? "Добавить сервер" : "Редактировать сервер")
            .setView(dialogView)
            .setPositiveButton("Сохранить", (d, which) -> {
                String n = name.getText().toString().trim();
                String s = ip.getText().toString().trim();
                String p = port.getText().toString().trim();
                String u = user.getText().toString().trim();
                String pw = pass.getText().toString().trim();

                if (TextUtils.isEmpty(n) || TextUtils.isEmpty(s) || TextUtils.isEmpty(p)) {
                    Toast.makeText(requireContext(), "Заполни название, IP и порт", Toast.LENGTH_SHORT).show();
                    return;
                }

                Profile profile;
                if (existing == null) {
                    profile = mManager.addProfile(n);
                    if (profile == null) {
                        Toast.makeText(requireContext(), "Профиль с таким именем существует", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    profile = existing;
                }
                profile.setServer(s);
                profile.setPort(Integer.parseInt(p));
                profile.setIsUserpw(!TextUtils.isEmpty(u));
                profile.setUsername(u);
                profile.setPassword(pw);

                mProfile = profile;
                mManager.switchDefault(profile.getName());
                loadProfiles();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void deleteServer(Profile p) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Удалить?")
            .setMessage("Удалить \"" + p.getName() + "\"?")
            .setPositiveButton("Удалить", (d, which) -> {
                mManager.removeProfile(p.getName());
                mProfile = mManager.getDefault();
                loadProfiles();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void toggleConnect() {
        if (mRunning) stopVpn(); else startVpn();
    }

    private void checkState() {
        mRunning = false;
        if (mBinder == null) {
            requireContext().bindService(
                new Intent(requireContext(), SocksVpnService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        }
        mConnectButton.postDelayed(mStateRunnable, 500);
    }

    private void updateState() {
        if (mBinder == null) mRunning = false;
        else {
            try { mRunning = mBinder.isRunning(); } catch (Exception e) { mRunning = false; }
        }
        if (mStarting && mRunning) mStarting = false;
        if (mStopping && !mRunning) mStopping = false;

        if (mConnectLabel == null) return;

        if (mStarting) {
            mConnectLabel.setText(R.string.connecting);
            mConnectStatus.setText("Подключение…");
            mConnectStatus.setTextColor(getResources().getColor(R.color.accent_orange));
        } else if (mRunning) {
            mConnectLabel.setText(R.string.disconnect);
            mConnectStatus.setText(R.string.connected);
            mConnectStatus.setTextColor(getResources().getColor(R.color.accent_green));
        } else {
            mConnectLabel.setText(R.string.connect);
            mConnectStatus.setText(R.string.not_connected);
            mConnectStatus.setTextColor(getResources().getColor(R.color.dark_text_secondary));
        }
    }

    private void startVpn() {
        if (mProfile == null) {
            Toast.makeText(requireContext(), "Сначала добавь сервер", Toast.LENGTH_SHORT).show();
            return;
        }
        mStarting = true;
        updateState();
        Intent i = VpnService.prepare(requireContext());
        if (i != null) startActivityForResult(i, 0);
        else onActivityResult(0, Activity.RESULT_OK, null);
    }

    private void stopVpn() {
        if (mBinder == null) return;
        mStopping = true;
        updateState();
        try { mBinder.stop(); } catch (Exception ignored) {}
        mBinder = null;
        try { requireContext().unbindService(mConnection); } catch (Exception ignored) {}
        checkState();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && mProfile != null) {
            Utility.startVpn(requireContext(), mProfile);
            checkState();
        } else {
            mStarting = false;
            updateState();
        }
    }
}
