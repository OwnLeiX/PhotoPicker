package lx.sample;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * <p> </p><br/>
 *
 * @author Lx
 * @date 2017/3/7
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }
}
