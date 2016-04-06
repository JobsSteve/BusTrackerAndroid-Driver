package com.example.android.bustracker_acg_driver.splash_screen;

/**
 * Created by giorgos on 4/4/2016.
 */


import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;


public class MutableForegroundColorSpan extends CharacterStyle
        implements UpdateAppearance {

    public static final String TAG = "MutableForegroundColorSpan";

    private int mColor;

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setColor(mColor);
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }
}

