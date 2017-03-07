package lx.photopicker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * <b>根据宽度自适应高度的SquareFrameLayout</b>
 * Created on 2017/2/24.
 *
 * @author LeiXun
 */

public class SquareFrameLayoutW extends FrameLayout {
    public SquareFrameLayoutW(Context context) {
        super(context);
    }

    public SquareFrameLayoutW(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFrameLayoutW(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
