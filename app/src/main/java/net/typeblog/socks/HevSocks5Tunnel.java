package net.typeblog.socks;

public class HevSocks5Tunnel {
    static {
        java.lang.System.loadLibrary("hev-socks5-tunnel");
    }
    public static native void TProxyStartService(String configPath, int tunFd);
    public static native void TProxyStopService();
}
