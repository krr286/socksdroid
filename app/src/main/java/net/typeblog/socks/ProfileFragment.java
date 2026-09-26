package net.typeblog.socks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.ListPreference;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;
import net.typeblog.socks.util.Utility;

import java.util.Locale;

import static net.typeblog.socks.util.Constants.*;

public class ProfileFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener,
        CompoundButton.OnCheckedChangeListener {
    private ProfileManager mManager;
    private Profile mProfile;

    private Switch mSwitch;
    private boolean mRunning = false;
    private boolean mStarting = false, mStopping = false;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName p1, IBinder binder) {
            mBinder = IVpnService.Stub.asInterface(binder);
            try { mRunning = mBinder.isRunning(); } catch (Exception e) { e.printStackTrace(); }
            updateState();
        }
        @Override
        public void onServiceDisconnected(ComponentName p1) {
            mBinder = null;
        }
    };

    private final Runnable mStateRunnable = new Runnable() {
        @Override
        public void run() {
            updateState();
            mHandler.postDelayed(this, 1000);
        }
    };

    private IVpnService mBinder;

    private ListPreference mPrefProfile, mPrefRoutes;
    private EditTextPreference mPrefServer, mPrefPort, mPrefUsername, mPrefPassword,
            mPrefDns, mPrefDnsPort, mPrefAppList, mPrefUDPGW;
    private CheckBoxPreference mPrefUserpw, mPrefPerApp, mPrefAppBypass, mPrefIPv6, mPrefUDP, mPrefAuto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        mManager = new ProfileManager(getActivity().getApplicationContext());
        initPreferences();
        reload();
        mHandler.postDelayed(mStateRunnable, 500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mStateRunnable);
        if (mBinder != null) {
            try { getActivity().unbindService(mConnection); } catch (Exception ignored) {}
            mBinder = null;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference p) { return false; }

    @Override
    public boolean onPreferenceChange(Preference p, Object newValue) {
        if (p == mPrefProfile) {
            String name = newValue.toString();
            mProfile = mManager.getProfile(name);
            mManager.switchDefault(name);
            reload();
            return true;
        } else if (p == mPrefServer) {
            mProfile.setServer(newValue.toString());
            resetTextN(mPrefServer, newValue);
            return true;
        } else if (p == mPrefPort) {
            if (TextUtils.isEmpty(newValue.toString())) return false;
            mProfile.setPort(Integer.parseInt(newValue.toString()));
            resetTextN(mPrefPort, newValue);
            return true;
        } else if (p == mPrefUserpw) {
            mProfile.setIsUserpw(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefUsername) {
            mProfile.setUsername(newValue.toString());
            resetTextN(mPrefUsername, newValue);
            return true;
        } else if (p == mPrefPassword) {
            mProfile.setPassword(newValue.toString());
            resetTextN(mPrefPassword, newValue);
            return true;
        } else if (p == mPrefRoutes) {
            mProfile.setRoute(newValue.toString());
            resetListN(mPrefRoutes, newValue);
            return true;
        } else if (p == mPrefDns) {
            mProfile.setDns(newValue.toString());
            resetTextN(mPrefDns, newValue);
            return true;
        } else if (p == mPrefDnsPort) {
            if (TextUtils.isEmpty(newValue.toString())) return false;
            mProfile.setDnsPort(Integer.parseInt(newValue.toString()));
            resetTextN(mPrefDnsPort, newValue);
            return true;
        } else if (p == mPrefPerApp) {
            mProfile.setIsPerApp(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefAppBypass) {
            mProfile.setIsBypassApp(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefAppList) {
            mProfile.setAppList(newValue.toString());
            return true;
        } else if (p == mPrefIPv6) {
            mProfile.setHasIPv6(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefUDP) {
            mProfile.setHasUDP(Boolean.parseBoolean(newValue.toString()));
            return true;
        } else if (p == mPrefUDPGW) {
            mProfile.setUDPGW(newValue.toString());
            resetTextN(mPrefUDPGW, newValue);
            return true;
        } else if (p == mPrefAuto) {
            mProfile.setAutoConnect(Boolean.parseBoolean(newValue.toString()));
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton p1, boolean checked) {
        if (checked) startVpn(); else stopVpn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Utility.startVpn(getActivity(), mProfile);
            checkState();
        }
    }

    private void initPreferences() {
        mPrefProfile = (ListPreference) findPreference(PREF_PROFILE);
        mPrefServer = (EditTextPreference) findPreference(PREF_SERVER_IP);
        mPrefPort = (EditTextPreference) findPreference(PREF_SERVER_PORT);
        mPrefUserpw = (CheckBoxPreference) findPreference(PREF_AUTH_USERPW);
        mPrefUsername = (EditTextPreference) findPreference(PREF_AUTH_USERNAME);
        mPrefPassword = (EditTextPreference) findPreference(PREF_AUTH_PASSWORD);
        mPrefRoutes = (ListPreference) findPreference(PREF_ADV_ROUTE);
        mPrefDns = (EditTextPreference) findPreference(PREF_ADV_DNS);
        mPrefDnsPort = (EditTextPreference) findPreference(PREF_ADV_DNS_PORT);
        mPrefPerApp = (CheckBoxPreference) findPreference(PREF_ADV_PER_APP);
        mPrefAppBypass = (CheckBoxPreference) findPreference(PREF_ADV_APP_BYPASS);
        mPrefAppList = (EditTextPreference) findPreference(PREF_ADV_APP_LIST);
        mPrefIPv6 = (CheckBoxPreference) findPreference(PREF_IPV6_PROXY);
        mPrefUDP = (CheckBoxPreference) findPreference(PREF_UDP_PROXY);
        mPrefUDPGW = (EditTextPreference) findPreference(PREF_UDP_GW);
        mPrefAuto = (CheckBoxPreference) findPreference(PREF_ADV_AUTO_CONNECT);

        mPrefProfile.setOnPreferenceChangeListener(this);
        mPrefServer.setOnPreferenceChangeListener(this);
        mPrefPort.setOnPreferenceChangeListener(this);
        mPrefUserpw.setOnPreferenceChangeListener(this);
        mPrefUsername.setOnPreferenceChangeListener(this);
        mPrefPassword.setOnPreferenceChangeListener(this);
        mPrefRoutes.setOnPreferenceChangeListener(this);
        mPrefDns.setOnPreferenceChangeListener(this);
        mPrefDnsPort.setOnPreferenceChangeListener(this);
        mPrefPerApp.setOnPreferenceChangeListener(this);
        mPrefAppBypass.setOnPreferenceChangeListener(this);
        mPrefAppList.setOnPreferenceChangeListener(this);
        mPrefIPv6.setOnPreferenceChangeListener(this);
        mPrefUDP.setOnPreferenceChangeListener(this);
        mPrefUDPGW.setOnPreferenceChangeListener(this);
        mPrefAuto.setOnPreferenceChangeListener(this);
    }

    private void reload() {
        if (mProfile == null) mProfile = mManager.getDefault();

        mPrefProfile.setEntries(mManager.getProfiles());
        mPrefProfile.setEntryValues(mManager.getProfiles());
        mPrefProfile.setValue(mProfile.getName());
        mPrefRoutes.setValue(mProfile.getRoute());
        resetList(mPrefProfile, mPrefRoutes);

        mPrefUserpw.setChecked(mProfile.isUserPw());
        mPrefPerApp.setChecked(mProfile.isPerApp());
        mPrefAppBypass.setChecked(mProfile.isBypassApp());
        mPrefIPv6.setChecked(mProfile.hasIPv6());
        mPrefUDP.setChecked(mProfile.hasUDP());
        mPrefAuto.setChecked(mProfile.autoConnect());

        mPrefServer.setText(mProfile.getServer());
        mPrefPort.setText(String.valueOf(mProfile.getPort()));
        mPrefUsername.setText(mProfile.getUsername());
        mPrefPassword.setText(mProfile.getPassword());
        mPrefDns.setText(mProfile.getDns());
        mPrefDnsPort.setText(String.valueOf(mProfile.getDnsPort()));
        mPrefUDPGW.setText(mProfile.getUDPGW());
        resetText(mPrefServer, mPrefPort, mPrefUsername, mPrefPassword, mPrefDns, mPrefDnsPort, mPrefUDPGW);

        mPrefAppList.setText(mProfile.getAppList());
    }

    private void resetList(ListPreference... pref) {
        for (ListPreference p : pref) p.setSummary(p.getEntry());
    }
    private void resetListN(ListPreference pref, Object newValue) {
        pref.setSummary(newValue.toString());
    }
    private void resetText(EditTextPreference... pref) {
        for (EditTextPreference p : pref) {
            if ((p.getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) != InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                p.setSummary(p.getText());
            } else {
                if (p.getText().length() > 0)
                    p.setSummary(String.format(Locale.US, String.format(Locale.US, "%%0%dd", p.getText().length()), 0).replace("0", "*"));
                else p.setSummary("");
            }
        }
    }
    private void resetTextN(EditTextPreference pref, Object newValue) {
        if ((pref.getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD) != InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            pref.setSummary(newValue.toString());
        } else {
            String text = newValue.toString();
            if (text.length() > 0)
                pref.setSummary(String.format(Locale.US, String.format(Locale.US, "%%0%dd", text.length()), 0).replace("0", "*"));
            else pref.setSummary("");
        }
    }

    public void addProfile() {
        android.view.View view = android.view.LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_server, null);
        final EditText eName = view.findViewById(R.id.dlg_name);
        final EditText eServer = view.findViewById(R.id.dlg_server);
        final EditText ePort = view.findViewById(R.id.dlg_port);
        final EditText eUser = view.findViewById(R.id.dlg_user);
        final EditText ePass = view.findViewById(R.id.dlg_pass);
        ePort.setText("1080");

        new AlertDialog.Builder(getActivity())
            .setTitle(R.string.prof_add)
            .setView(view)
            .setPositiveButton(android.R.string.ok, (d, which) -> {
                String name = eName.getText().toString().trim();
                String srv = eServer.getText().toString().trim();
                String port = ePort.getText().toString().trim();
                String user = eUser.getText().toString().trim();
                String pass = ePass.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getActivity(), "Введи название", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(srv) || TextUtils.isEmpty(port)) {
                    Toast.makeText(getActivity(), "Введи IP и порт", Toast.LENGTH_SHORT).show();
                    return;
                }

                Profile p = mManager.addProfile(name);
                if (p == null) {
                    Toast.makeText(getActivity(), String.format(getString(R.string.err_add_prof), name), Toast.LENGTH_SHORT).show();
                    return;
                }

                p.setServer(srv);
                try { p.setPort(Integer.parseInt(port)); } catch (Exception ex) { p.setPort(1080); }
                p.setIsUserpw(!TextUtils.isEmpty(user));
                p.setUsername(user);
                p.setPassword(pass);

                mProfile = p;
                mManager.switchDefault(name);
                reload();
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create().show();
    }

    public void toggleConnect() {
        if (mRunning) stopVpn(); else startVpn();
    }

    private void checkState() {
        mRunning = false;
        if (mSwitch != null) {
            mSwitch.setEnabled(false);
            mSwitch.setOnCheckedChangeListener(null);
        }
        if (mBinder == null) {
            getActivity().bindService(new Intent(getActivity(), SocksVpnService.class), mConnection, 0);
        }
    }

    private void updateState() {
        if (mBinder == null) {
            mRunning = false;
        } else {
            try { mRunning = mBinder.isRunning(); } catch (Exception e) { mRunning = false; }
        }

        if (mSwitch != null) {
            mSwitch.setChecked(mRunning);
            if ((!mStarting && !mStopping) || (mStarting && mRunning) || (mStopping && !mRunning)) {
                mSwitch.setEnabled(true);
            }
            mSwitch.setOnCheckedChangeListener(ProfileFragment.this);
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setPowerColor(mRunning);
        }

        if (mStarting && mRunning) mStarting = false;
        if (mStopping && !mRunning) mStopping = false;
    }

    private void startVpn() {
        mStarting = true;
        Intent i = VpnService.prepare(getActivity());
        if (i != null) startActivityForResult(i, 0);
        else onActivityResult(0, Activity.RESULT_OK, null);
    }

    private void stopVpn() {
        if (mBinder == null) return;
        mStopping = true;
        try { mBinder.stop(); } catch (Exception e) { e.printStackTrace(); }
        mBinder = null;
        try { getActivity().unbindService(mConnection); } catch (Exception ignored) {}
        checkState();
    }
}
