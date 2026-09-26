package net.typeblog.socks;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;

import java.util.ArrayList;
import java.util.List;

public class ServersFragment extends Fragment implements ServerAdapter.Listener {

    private ProfileManager mManager;
    private ServerAdapter mAdapter;
    private final List<Profile> mData = new ArrayList<>();
    private RecyclerView mRv;
    private TextView mEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle s) {
        View v = inflater.inflate(R.layout.fragment_servers, c, false);
        mManager = new ProfileManager(getActivity().getApplicationContext());
        mRv = v.findViewById(R.id.servers_rv);
        mEmpty = v.findViewById(R.id.servers_empty);

        mRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ServerAdapter(mData, this);
        mRv.setAdapter(mAdapter);

        load();
        return v;
    }

    private void load() {
        mData.clear();
        String[] names = mManager.getProfiles();
        String defName = mManager.getDefault().getName();
        int sel = -1;
        for (String n : names) {
            if ("По умолчанию".equals(n)) continue;
            Profile p = mManager.getProfile(n);
            if (p != null) {
                mData.add(p);
                if (n.equals(defName)) sel = mData.size() - 1;
            }
        }
        mAdapter.setSelectedIndex(sel);
        mEmpty.setVisibility(mData.isEmpty() ? View.VISIBLE : View.GONE);
        mRv.setVisibility(mData.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSelect(Profile p) {
        mManager.switchDefault(p.getName());
        load();
        Toast.makeText(getActivity(), "Выбран: " + p.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongClick(Profile p) {
        new AlertDialog.Builder(getActivity())
            .setItems(new String[]{"Редактировать", "Удалить"}, (d, w) -> {
                if (w == 0) editDialog(p);
                else deleteDialog(p);
            })
            .show();
    }

    private void editDialog(Profile p) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_server, null);
        EditText n = view.findViewById(R.id.dlg_name);
        EditText s = view.findViewById(R.id.dlg_server);
        EditText pt = view.findViewById(R.id.dlg_port);
        EditText u = view.findViewById(R.id.dlg_user);
        EditText pw = view.findViewById(R.id.dlg_pass);

        n.setText(p.getName());
        s.setText(p.getServer());
        pt.setText(String.valueOf(p.getPort()));
        u.setText(p.getUsername());
        pw.setText(p.getPassword());

        new AlertDialog.Builder(getActivity())
            .setTitle("Редактировать сервер")
            .setView(view)
            .setPositiveButton("Сохранить", (d, w) -> {
                String srv = s.getText().toString().trim();
                String port = pt.getText().toString().trim();
                if (TextUtils.isEmpty(srv) || TextUtils.isEmpty(port)) {
                    Toast.makeText(getActivity(), "Заполни IP и порт", Toast.LENGTH_SHORT).show();
                    return;
                }
                p.setServer(srv);
                try { p.setPort(Integer.parseInt(port)); } catch (Exception ex) { p.setPort(1080); }
                String user = u.getText().toString().trim();
                String pass = pw.getText().toString().trim();
                p.setIsUserpw(!TextUtils.isEmpty(user));
                p.setUsername(user);
                p.setPassword(pass);
                load();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void deleteDialog(Profile p) {
        new AlertDialog.Builder(getActivity())
            .setTitle("Удалить сервер?")
            .setMessage("Удалить \"" + p.getName() + "\"?")
            .setPositiveButton("Удалить", (d, w) -> {
                mManager.removeProfile(p.getName());
                load();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
}
