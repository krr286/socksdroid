package net.typeblog.socks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private int mSelectedIndex = 0;

    public ServerAdapter(List<Profile> data, Listener listener) {
        this.mData = data;
        this.mListener = listener;
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= mData.size()) return;
        mSelectedIndex = index;
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
    public void onBindViewHolder(@NonNull VH h, int position) {
        Profile p = mData.get(position);
        h.name.setText(p.getName());
        String subtitle = p.getServer() + ":" + p.getPort();
        if (p.isUserPw()) subtitle += " · AUTH";
        h.subtitle.setText(subtitle);

        if (position == mSelectedIndex) {
            h.check.setVisibility(View.VISIBLE);
            h.itemView.setAlpha(1.0f);
        } else {
            h.check.setVisibility(View.INVISIBLE);
            h.itemView.setAlpha(0.7f);
        }

        h.itemView.setOnClickListener(v -> {
            mSelectedIndex = h.getAdapterPosition();
            notifyDataSetChanged();
            if (mListener != null) mListener.onSelect(p);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (mListener != null) mListener.onLongClick(p);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, subtitle;
        ImageView check;

        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.item_name);
            subtitle = v.findViewById(R.id.item_subtitle);
            check = v.findViewById(R.id.item_check);
        }
    }
}
