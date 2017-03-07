package lx.photopicker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * <b>根据高度自适应宽度的SquareFrameLayout</b>
 * Created on 2017/2/24.
 *
 * @author LeiXun
 */

public class SquareFrameLayoutH extends FrameLayout {
    public SquareFrameLayoutH(Context context) {
        super(context);
    }

    public SquareFrameLayoutH(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFrameLayoutH(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
