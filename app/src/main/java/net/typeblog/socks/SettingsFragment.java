package net.typeblog.socks;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;
import net.typeblog.socks.util.ThemeManager;

public class SettingsFragment extends Fragment {

    private ProfileManager mManager;
    private Profile mProfile;

    private RadioGroup mThemeGroup;
    private EditText mDns, mDnsPort;
    private Spinner mRoute;
    private Switch mIpv6, mUdp, mAuto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        mManager = new ProfileManager(requireContext().getApplicationContext());
        mProfile = mManager.getDefault();

        mThemeGroup = v.findViewById(R.id.theme_group);
        mDns = v.findViewById(R.id.settings_dns);
        mDnsPort = v.findViewById(R.id.settings_dns_port);
        mRoute = v.findViewById(R.id.settings_route);
        mIpv6 = v.findViewById(R.id.settings_ipv6);
        mUdp = v.findViewById(R.id.settings_udp);
        mAuto = v.findViewById(R.id.settings_auto);

        switch (ThemeManager.getTheme(requireContext())) {
            case ThemeManager.THEME_LIGHT: mThemeGroup.check(R.id.theme_light); break;
            case ThemeManager.THEME_PURPLE: mThemeGroup.check(R.id.theme_purple); break;
            default: mThemeGroup.check(R.id.theme_dark);
        }
        mThemeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int theme = ThemeManager.THEME_DARK;
            if (checkedId == R.id.theme_light) theme = ThemeManager.THEME_LIGHT;
            else if (checkedId == R.id.theme_purple) theme = ThemeManager.THEME_PURPLE;
            if (theme != ThemeManager.getTheme(requireContext())) {
                ThemeManager.setTheme(requireContext(), theme);
                requireActivity().recreate();
            }
        });

        mDns.setText(mProfile.getDns());
        mDns.addTextChangedListener(simpleWatcher(s -> mProfile.setDns(s)));

        mDnsPort.setText(String.valueOf(mProfile.getDnsPort()));
        mDnsPort.addTextChangedListener(simpleWatcher(s -> {
            try { mProfile.setDnsPort(Integer.parseInt(s)); } catch (Exception ignored) {}
        }));

        ArrayAdapter<String> routeAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item,
            new String[]{"Все (по умолчанию)", "Non-Chinese IPs"});
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRoute.setAdapter(routeAdapter);
        mRoute.setSelection(mProfile.getRoute().equals("non_chn") ? 1 : 0);
        mRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View view, int pos, long id) {
                mProfile.setRoute(pos == 1 ? "non_chn" : "all");
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        mIpv6.setChecked(mProfile.hasIPv6());
        mIpv6.setOnCheckedChangeListener((b, c) -> mProfile.setHasIPv6(c));

        mUdp.setChecked(mProfile.hasUDP());
        mUdp.setOnCheckedChangeListener((b, c) -> mProfile.setHasUDP(c));

        mAuto.setChecked(mProfile.autoConnect());
        mAuto.setOnCheckedChangeListener((b, c) -> mProfile.setAutoConnect(c));

        return v;
    }

    private TextWatcher simpleWatcher(final OnText onText) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { onText.on(s.toString()); }
        };
    }

    interface OnText { void on(String s); }
}
