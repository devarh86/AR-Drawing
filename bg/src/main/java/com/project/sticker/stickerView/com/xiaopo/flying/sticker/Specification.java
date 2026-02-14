package com.project.sticker.stickerView.com.xiaopo.flying.sticker;

import android.graphics.Typeface;

public class Specification {
    public String stickerTitle;
    public int stickerTextColor;
    public int stickerTextOpacity = 255;
    public int stickerTextOpacityPercent = 100;
    public int stickerBackgroundColor;
    public int stickerBackgroundOpacity = 255;
    public int stickerBackgroundOpacityPercent = 100;
    public Typeface stickerTypeFace;
    public float[] stickerShadow = new float[4];
    public boolean isShodowed = false;


    public void setStickerTitle(String stickerTitle) {
        this.stickerTitle = stickerTitle;
    }

    public void setStickerTextColor(int stickerTextColor) {
        this.stickerTextColor = stickerTextColor;
    }

    public void setStickerTextOpacity(int stickerTextOpacity) {
        this.stickerTextOpacity = stickerTextOpacity;
    }

    public void setStickerBackgroundColor(int stickerBackgroundColor) {
        this.stickerBackgroundColor = stickerBackgroundColor;
    }

    public void setStickerBackgroundOpacity(int stickerBackgroundOpacity) {
        this.stickerBackgroundOpacity = stickerBackgroundOpacity;
    }

    public void setStickerTypeFace(Typeface stickerTypeFace) {
        this.stickerTypeFace = stickerTypeFace;
    }

    public void setStickerShadow(float[] stickerShadow) {
        this.stickerShadow = stickerShadow;
    }

    public void setShodowed(boolean shodowed) {
        isShodowed = shodowed;
    }


    public void setStickerBackgroundOpacityPercent(int stickerBackgroundOpacityPercent) {
        this.stickerBackgroundOpacityPercent = stickerBackgroundOpacityPercent;
    }
    public void setStickerTextOpacityPercent(int stickerTextOpacityPercent) {
        this.stickerTextOpacityPercent = stickerTextOpacityPercent;
    }

    public int getStickerTextOpacityPercent() {
        return stickerTextOpacityPercent;
    }

    public int getStickerBackgroundOpacityPercent() {
        return stickerBackgroundOpacityPercent;
    }


    public String getStickerTitle() {
        return stickerTitle;
    }

    public int getStickerTextColor() {
        return stickerTextColor;
    }

    public int getStickerBackgroundColor() {
        return stickerBackgroundColor;
    }

    public Typeface getStickerTypeFace() {
        return stickerTypeFace;
    }

    public float[] getStickerShadow() {
        return stickerShadow;
    }

    public boolean isShodowed() {
        return isShodowed;
    }

    public int getStickerTextOpacity() {
        return stickerTextOpacity;
    }

    public int getStickerBackgroundOpacity() {
        return stickerBackgroundOpacity;
    }


}
