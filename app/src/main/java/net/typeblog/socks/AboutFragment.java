package net.typeblog.socks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        TextView version = v.findViewById(R.id.about_version);
        try {
            version.setText("v" + requireContext().getPackageManager()
                .getPackageInfo(requireContext().getPackageName(), 0).versionName);
        } catch (Exception ignored) {}

        TextView support = v.findViewById(R.id.about_support);
        support.setText("🆘 @markkomov");
        support.setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://t.me/markkomov")));
            } catch (Exception ignored) {}
        });

        return v;
    }
}
