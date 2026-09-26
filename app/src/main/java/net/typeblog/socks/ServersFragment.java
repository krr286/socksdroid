package net.typeblog.socks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;

import java.util.ArrayList;
import java.util.List;

public class ServersFragment extends Fragment implements ServerAdapter.Listener {

    private ProfileManager mManager;
    private final List<Profile> mData = new ArrayList<>();
    private ServerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_servers, container, false);
        mManager = new ProfileManager(requireContext().getApplicationContext());

        RecyclerView list = v.findViewById(R.id.servers_full_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ServerAdapter(mData, this);
        list.setAdapter(mAdapter);

        load();
        return v;
    }

    private void load() {
        mData.clear();
        String[] names = mManager.getProfiles();
        String defName = mManager.getDefault().getName();
        int selIdx = 0;
        for (int i = 0; i < names.length; i++) {
            Profile p = mManager.getProfile(names[i]);
            if (p != null) {
                mData.add(p);
                if (names[i].equals(defName)) selIdx = i;
            }
        }
        mAdapter.setSelectedIndex(selIdx);
    }

    @Override
    public void onSelect(Profile p) {
        mManager.switchDefault(p.getName());
        Toast.makeText(requireContext(), "Активный: " + p.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongClick(Profile p) {
        mManager.removeProfile(p.getName());
        load();
    }
}
