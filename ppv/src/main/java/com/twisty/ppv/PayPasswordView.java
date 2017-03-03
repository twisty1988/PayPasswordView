package com.twisty.ppv;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

/**
 * Project : PayPassword<br>
 * Created by twisty on 2017/3/2.<br>
 */

public class PayPasswordView extends View {
    private InputMethodManager input;
    private int size;
    private int borderColor;
    private int dotColor;
    private int length;
    private int borderGap;
    private int borderWidth = 2;
    private boolean isAutoClear;

    private Paint borderPaint;
    private Paint dotPaint;

    private ArrayList<Integer> result;

    Rect paintRect;
    OnInputDoneListener onInputDoneListener;

    public void setOnInputDoneListener(OnInputDoneListener onInputDoneListener) {
        this.onInputDoneListener = onInputDoneListener;
    }

    public PayPasswordView(Context context) {
        this(context, null);
    }

    public PayPasswordView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayPasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        input = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PayPasswordView);
        borderColor = typedArray.getColor(R.styleable.PayPasswordView_ppv_borderColor, Color.GRAY);
        dotColor = typedArray.getColor(R.styleable.PayPasswordView_ppv_dotColor, Color.GRAY);
        isAutoClear = typedArray.getBoolean(R.styleable.PayPasswordView_ppv_autoClear, false);
        length = typedArray.getInt(R.styleable.PayPasswordView_ppv_length, 6);
        typedArray.recycle();

        size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, getResources().getDisplayMetrics());
        borderGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());


        result = new ArrayList<>();
        borderPaint = new Paint();
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setStyle(Paint.Style.STROKE);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(dotColor);
        dotPaint.setStyle(Paint.Style.FILL);

        paintRect = new Rect();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        //只根据宽度来控制控件的宽和高
        if (wMode == MeasureSpec.AT_MOST) {
            wSize = (size + borderGap) * length;
        } else {
            size = wSize / length - borderGap;
        }
        setMeasuredDimension(wSize, size + borderWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画边框
        for (int i = 0; i < length; i++) {
            paintRect.set(i * size + i * borderGap + borderWidth, borderWidth, (i + 1) * size + i * borderGap, getHeight() - borderWidth);
            canvas.drawRect(paintRect, borderPaint);
        }
        //根据已输入的字符画圆点
        for (int i = 0; i < result.size(); i++) {
            paintRect.set(i * size + i * borderGap + borderWidth, borderWidth, (i + 1) * size + i * borderGap, getHeight() - borderWidth);
            canvas.drawCircle(paintRect.centerX(), paintRect.centerY(), paintRect.width() / 5, dotPaint);
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //点击控件时获取焦点弹出软键盘输入
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            input.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            input.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        } else {
            input.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            input.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;//输入类型为数字
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new BaseInputConnection(this, false);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.isShiftPressed()) return false;
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            if (result.size() < length) {
                result.add(keyCode - 7);
                invalidate();
            }
            if (result.size() >= length) {
                if (!isAutoClear) {
                    finishInput();
                } else {
                    //如果没有这个延迟,并且设置了AutoClear,因为执行太快了,界面上看起来像是不会显示最后一位密码的圆点而直接clear
                    //很像没有输入最后一位就结束了...看起来总觉得不得劲..
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finishInput();
                            clear();
                        }
                    }, 160);
                }
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (!result.isEmpty()) {
                result.remove(result.size() - 1);
                invalidate();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void finishInput() {
        StringBuilder sb = new StringBuilder();
        for (Integer word : result) {
            sb.append(word);
        }
        if (onInputDoneListener != null)
            onInputDoneListener.onInputDone(sb.toString());
    }


    public void clear() {
        if (!result.isEmpty()) result.clear();
        invalidate();
    }


    public interface OnInputDoneListener {
        void onInputDone(String result);
    }
}
