package net.typeblog.socks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import net.typeblog.socks.util.Profile;
import net.typeblog.socks.util.ProfileManager;

import java.io.File;
import java.io.FileOutputStream;

public class SocksVpnService extends VpnService {
    private static final String TAG = "SocksVpnService";
    private static final String CHANNEL_ID = "TheK_VPN";
    private static final int NOTIFICATION_ID = 1;

    private ParcelFileDescriptor mTunFd;
    private Thread mTunnelThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopVpn();
            return START_NOT_STICKY;
        }
        ProfileManager manager = new ProfileManager(getApplicationContext());
        Profile profile = manager.getDefault();
        if (profile == null) { stopSelf(); return START_NOT_STICKY; }
        startVpn(profile);
        return START_STICKY;
    }

    private void startVpn(Profile profile) {
        try {
            String configPath = writeHevConfig(profile);

            Builder builder = new Builder()
                    .setSession("THEK")
                    .setMtu(1500)
                    .addAddress("198.18.0.1", 30)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("1.1.1.1")
                    .addDnsServer("8.8.8.8");

            if (Build.VERSION.SDK_INT >= 21) {
                try { builder.addDisallowedApplication(getPackageName()); } catch (Exception ignored) {}
            }

            mTunFd = builder.establish();
            if (mTunFd == null) { stopSelf(); return; }

            final String cfg = configPath;
            final int fd = mTunFd.getFd();

            mTunnelThread = new Thread(() -> {
                try {
                    HevSocks5Tunnel.TProxyStartService(cfg, fd);
                } catch (Throwable t) {
                    Log.e(TAG, "Tunnel error", t);
                }
            }, "HevTunnel");
            mTunnelThread.start();

            startForegroundNotification();
        } catch (Exception e) {
            Log.e(TAG, "startVpn", e);
            stopVpn();
        }
    }

    private void stopVpn() {
        try {
            HevSocks5Tunnel.TProxyStopService();
            if (mTunnelThread != null) { mTunnelThread.join(2000); mTunnelThread = null; }
            if (mTunFd != null) { mTunFd.close(); mTunFd = null; }
        } catch (Exception ignored) {}
        stopForeground(true);
        stopSelf();
    }

    private String writeHevConfig(Profile profile) throws Exception {
        StringBuilder yaml = new StringBuilder();
        yaml.append("tunnel:\n");
        yaml.append("  mtu: 1500\n");
        yaml.append("  ipv4: 198.18.0.1\n");
        yaml.append("  ipv6: 'fc00::1'\n");
        yaml.append("  icmp: 'reply'\n");
        yaml.append("socks5:\n");
        yaml.append("  port: ").append(profile.getPort()).append("\n");
        yaml.append("  address: '").append(profile.getServer()).append("'\n");
        yaml.append("  udp: 'udp'\n");
        if (profile.isUserPw()) {
            yaml.append("  username: '").append(profile.getUsername()).append("'\n");
            yaml.append("  password: '").append(profile.getPassword()).append("'\n");
        }

        File f = new File(getFilesDir(), "hev-socks5-tunnel.yaml");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(yaml.toString().getBytes());
        }
        return f.getAbsolutePath();
    }

    private void startForegroundNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "THEK VPN", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(ch);
        }
        Intent stopIntent = new Intent(this, SocksVpnService.class);
        stopIntent.setAction("STOP");
        PendingIntent pi = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("THEK VPN активен")
                .setContentText("Защищённое соединение установлено")
                .setSmallIcon(R.drawable.ic_power_circle_big)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, n);
    }

    @Override
    public void onDestroy() {
        stopVpn();
        super.onDestroy();
    }
}
