package lx.photopicker.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * <b>BaseActivity</b>
 *
 * Created on 2017/2/23.
 * @author Leixun
 */

public abstract class BaseActivity extends AppCompatActivity
{
	private Toast mToast;
	protected static final int CODE_COMPLETE = 0;
	protected static final int CODE_CANCEL = 1;

	@Override
	final protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		onInitFeature();
		super.onCreate(savedInstanceState);
		setContentView(onInitContentView());
	}

	protected void onInitFeature()
	{
	}

	@Override
	final public void onContentChanged()
	{
		onInitView();
		onInitListener();
		onInitData();
	}


	protected void onInitData()
	{
	}

	protected void onInitListener()
	{
	}

	abstract protected View onInitContentView();

	protected void onInitView()
	{
	}

	final protected void showShortToast(String msg)
	{
		if (mToast == null)
		{
			mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		} else
		{
			mToast.setText(msg);
		}
		mToast.show();
	}

	final protected void finishByCancel(){
		setResult(CODE_CANCEL);
		finish();
	}

	final protected void finishByComplete(){
		setResult(CODE_COMPLETE);
		finish();
	}
}
