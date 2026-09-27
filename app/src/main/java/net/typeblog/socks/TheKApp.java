package net.typeblog.socks;

import android.app.Application;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TheKApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            try {
                File dir = getExternalFilesDir(null);
                if (dir != null) {
                    String proc = getCurrentProcessName();
                    String safeProc = proc.replace(':', '_').replace('/', '_');
                    File f = new File(dir, "crash_" + safeProc + ".txt");
                    PrintWriter pw = new PrintWriter(f, "UTF-8");
                    pw.println("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
                    pw.println("Process: " + proc);
                    pw.println("Thread: " + thread.getName());
                    pw.println("---");
                    ex.printStackTrace(pw);
                    pw.flush();
                    pw.close();
                }
            } catch (Throwable ignored) {}
            if (prev != null) prev.uncaughtException(thread, ex);
        });
    }

    private String getCurrentProcessName() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                return android.app.Application.getProcessName();
            }
        } catch (Throwable ignored) {}
        return "unknown";
    }
}
