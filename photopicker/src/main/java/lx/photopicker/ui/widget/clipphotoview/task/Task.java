package lx.photopicker.ui.widget.clipphotoview.task;

import android.os.Handler;
import android.os.Looper;

/**
 * <b></b>
 * Created on 2017/3/10.
 *
 * @author LeiXun
 */

public abstract class Task implements Runnable {
    private boolean isCompleted = false;
    protected Handler mHandler;

    @Override
    public void run() {
        isCompleted = false;
        work();
        isCompleted = true;
    }

    protected abstract void work();

    public boolean isCompleted()
    {
        return isCompleted;
    }

    protected Handler getHandler()
    {
        if (mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());
        return mHandler;
    }

}
