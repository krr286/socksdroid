package net.typeblog.socks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.typeblog.socks.util.Profile;

import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.VH> {

    public interface Listener {
        void onSelect(Profile p);
        void onLongClick(Profile p);
    }

    private final List<Profile> mData;
    private final Listener mListener;
    private int mSelected = -1;

    public ServerAdapter(List<Profile> data, Listener l) {
        mData = data;
        mListener = l;
    }

    public void setSelectedIndex(int i) {
        mSelected = i;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_server, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Profile p = mData.get(pos);
        h.name.setText(p.getName());
        h.addr.setText(p.getServer() + ":" + p.getPort());
        h.check.setVisibility(pos == mSelected ? View.VISIBLE : View.INVISIBLE);

        h.itemView.setOnClickListener(v -> mListener.onSelect(p));
        h.itemView.setOnLongClickListener(v -> {
            mListener.onLongClick(p);
            return true;
        });
    }

    @Override
    public int getItemCount() { return mData.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, addr, check, flag;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.srv_name);
            addr = v.findViewById(R.id.srv_addr);
            check = v.findViewById(R.id.srv_check);
            flag = v.findViewById(R.id.srv_flag);
        }
    }
}
