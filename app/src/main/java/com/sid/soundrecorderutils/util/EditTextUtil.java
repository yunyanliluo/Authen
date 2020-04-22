package com.sid.soundrecorderutils.util;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.sid.soundrecorderutils.R;

public class EditTextUtil extends android.support.v7.widget.AppCompatEditText {
    private final static String TAG = "EditTextUtil";
    private Drawable imgInable;
    private Context mContext;

    public EditTextUtil(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public EditTextUtil(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public EditTextUtil(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        imgInable = mContext.getResources().getDrawable(R.drawable.ic_clear_black_48dp);
        imgInable.setBounds(0,0,50,50);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setDrawable();
            }
        });
        setDrawable();
    }

    // 设置删除图片
    private void setDrawable() {
        if (length() < 1)
            setCompoundDrawables(null, null, null, null);
        else {
            setCompoundDrawables(null, null, imgInable, null);
        }
    }

    // 处理删除事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imgInable != null && event.getAction() == MotionEvent.ACTION_UP) {
            int eventX = (int) event.getRawX();
            int eventY = (int) event.getRawY();
            Log.e(TAG, "eventX = " + eventX + "; eventY = " + eventY);
            Rect rect = new Rect();
            getGlobalVisibleRect(rect);
            Log.d(TAG, "onTouchEvent: rect: " + rect);
            rect.left = rect.right - 50;
            rect.top += 500;
            rect.bottom += 500;
            Log.d(TAG, "onTouchEvent: cross: " + rect);
            if (rect.contains(eventX, eventY))
                setText("");
        }
        return super.onTouchEvent(event);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
