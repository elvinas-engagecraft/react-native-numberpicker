package io.fixd.reactnativenumberpicker;

import javax.annotation.Nullable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.NumberPicker;
import android.widget.EditText;
import android.graphics.Color;

import com.facebook.react.bridge.ReactContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RNNumberPicker extends NumberPicker {

    private @Nullable OnChangeListener mOnChangeListener;
    private boolean mSuppressNextEvent;
    private @Nullable Integer mStagedSelection;
    private @Nullable Integer mTextSize;
    private @Nullable Integer mTextColor;
    private @Nullable String mFontFamily;
    private @Nullable Integer mFontWeight = Typeface.NORMAL;

    private ReactContext reactContext;

    /**
     * Listener interface for events.
     */
    public interface OnChangeListener {
        void onValueChange(int value, int viewId, ReactContext reactContext);
    }

    public RNNumberPicker(ReactContext context) {
        super(context);
        this.reactContext = context;
    }

    public RNNumberPicker(ReactContext context, AttributeSet attrs) {
        super(context, attrs);
        this.reactContext = context;
    }

    public RNNumberPicker(ReactContext context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.reactContext = context;
    }

    public void setKeyboardInputEnabled(boolean enabled) {
        if (!enabled) {
            this.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        } else {
            this.setDescendantFocusability(NumberPicker.FOCUS_AFTER_DESCENDANTS);
        }
    }

    public void disableDivider() {
        Class<?> numberPickerClass = null;
        try {
            numberPickerClass = Class.forName("android.widget.NumberPicker");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Field selectionDivider = null;
        try {
            selectionDivider = numberPickerClass.getDeclaredField("mSelectionDivider");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            selectionDivider.setAccessible(true);
            selectionDivider.set(this, null);
        } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public void setOnChangeListener(@Nullable OnChangeListener onValueChangeListener) {
        setOnValueChangedListener(
                new OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        if (!mSuppressNextEvent && mOnChangeListener != null) {
                            mOnChangeListener.onValueChange(newVal, getId(), reactContext);
                        }
                        mSuppressNextEvent = false;
                    }
                }
        );
        mOnChangeListener = onValueChangeListener;
    }

    @Nullable
    public OnChangeListener getOnChangeListener() {
        return mOnChangeListener;
    }

    /**
     * Will cache "selection" value locally and set it only once {@link #updateStagedSelection} is
     * called
     */
    public void setStagedSelection(int selection) {
        mStagedSelection = selection;
    }

    public void updateStagedSelection() {
        if (mStagedSelection != null) {
            setValueWithSuppressEvent(mStagedSelection);
            mStagedSelection = null;
        }
    }

    /**
     * Set the selection while suppressing the follow-up event.
     * This is used so we don't get an event when changing the selection ourselves.
     *
     * @param value
     */
    private void setValueWithSuppressEvent(int value) {
        if (value != getValue()) {
            mSuppressNextEvent = true;
            setValue(value);
        }
    }

    public void setmTextColor(@Nullable Integer mTextColor) {
        this.mTextColor = mTextColor;
        updateInnerText();
    }

    public void setmTextSize(@Nullable Integer mTextSize) {
        this.mTextSize = mTextSize;
        updateInnerText();
    }

    public void setmFontFamily(@Nullable String mFontFamily) {
        this.mFontFamily = mFontFamily;
        updateInnerText();
    }

    private static int parseNumericFontWeight(String fontWeightString) {
        // This should be much faster than using regex to verify input and Integer.parseInt
        return fontWeightString.length() == 3 && fontWeightString.endsWith("00")
                && fontWeightString.charAt(0) <= '9' && fontWeightString.charAt(0) >= '1' ?
                100 * (fontWeightString.charAt(0) - '0') : -1;
    }

    public void setmFontWeight(@Nullable  String fontWeightString) {
        int fontWeightNumeric = fontWeightString != null ?
                parseNumericFontWeight(fontWeightString) : -1;
        int fontWeight = -1;
        if (fontWeightNumeric >= 500 || "bold".equals(fontWeightString)) {
            fontWeight = Typeface.BOLD;
        } else if ("normal".equals(fontWeightString) ||
                (fontWeightNumeric != -1 && fontWeightNumeric < 500)) {
            fontWeight = Typeface.NORMAL;
        }
        if (fontWeight != mFontWeight) {
            mFontWeight = fontWeight;
            updateInnerText();
        }
    }

    private void updateInnerText() {
        final int count = getChildCount();
        for(int i = 0; i < count; i++){
            View child = getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);

                    if(mTextColor != null) {
                        ((Paint) selectorWheelPaintField.get(this)).setColor(mTextColor);
                        ((EditText) child).setTextColor(mTextColor);
                    }
                    if(mTextSize != null) {
                        ((Paint) selectorWheelPaintField.get(this))
                                .setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                        mTextSize, getResources().getDisplayMetrics()));
                        ((EditText) child).setTextSize(mTextSize);
                    }
                    if(mFontFamily != null) {
                        Typeface typeface = Typeface.create(mFontFamily, mFontWeight);
                        ((Paint) selectorWheelPaintField.get(this)).setTypeface(typeface);
                        ((EditText) child).setTypeface(typeface);
                    }
                    invalidate();
                }
                catch(Exception e){
                    Log.w("setNumberPickerProps", e);
                }
            }
        }
    }

}
