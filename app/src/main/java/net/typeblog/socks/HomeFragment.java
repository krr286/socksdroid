package net.typeblog.socks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    private boolean isConnected = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ImageButton btnConnect = view.findViewById(R.id.btn_connect);
        TextView tvStatus = view.findViewById(R.id.tv_status);
        RecyclerView rvServers = view.findViewById(R.id.rv_servers);

        rvServers.setLayoutManager(new LinearLayoutManager(getContext()));

        btnConnect.setOnClickListener(v -> {
            isConnected = !isConnected;
            if (isConnected) {
                tvStatus.setText("Защищено");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                tvStatus.setText("Не защищено");
                tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        });

        return view;
    }
}
