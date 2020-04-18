package com.example.audiokeyboard.Utils;

import android.graphics.Color;
import android.graphics.Paint;

public class Painters {
    public static Paint backgroundPaint,textPaint,boundPaint;
    public static Paint movePaint,originPaint,currentPaint, rankingPaint;
    public static Paint.FontMetrics fontMetrics;
    public static float fonttop, fontbottom;

    float screen_height_ratio = 1F;
    void initTextPaint() {
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        this.textPaint.setColor(Color.BLACK);
        this.textPaint.setStrokeJoin(Paint.Join.ROUND);
        this.textPaint.setStrokeCap(Paint.Cap.ROUND);
        this.textPaint.setStrokeWidth(3);
        this.textPaint.setTextSize(Math.round(70*screen_height_ratio));
        this.textPaint.setTextAlign(Paint.Align.CENTER);
        this.textPaint.setStrokeJoin(Paint.Join.ROUND);
        this.textPaint.setStrokeCap(Paint.Cap.ROUND);
        this.textPaint.setTextSize(60);
    }

    void initBoundPaint() {
        this.boundPaint=new Paint();
        this.boundPaint.setColor(Color.rgb(201,184,201));
        this.boundPaint.setStrokeJoin(Paint.Join.ROUND);
        this.boundPaint.setStrokeCap(Paint.Cap.ROUND);
        this.boundPaint.setStrokeWidth(3);
    }

    void initMovePaint() {
        this.movePaint=new Paint();
        this.movePaint.setColor(Color.rgb(255,201,14));
        this.movePaint.setStrokeJoin(Paint.Join.ROUND);
        this.movePaint.setStrokeCap(Paint.Cap.ROUND);
        this.movePaint.setStrokeWidth(3);
    }

    void initRankingPaint() {
        this.rankingPaint=new Paint();
        this.rankingPaint.setColor(Color.BLUE);
        this.rankingPaint.setStrokeJoin(Paint.Join.ROUND);
        this.rankingPaint.setStrokeCap(Paint.Cap.ROUND);
        this.rankingPaint.setStrokeWidth(3);
        this.rankingPaint.setTextSize(Math.round(30*screen_height_ratio));
        this.rankingPaint.setTextAlign(Paint.Align.CENTER);
        this.rankingPaint.setStrokeJoin(Paint.Join.ROUND);
        this.rankingPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    void initBackgroundPaint() {
        this.backgroundPaint=new Paint();
        this.backgroundPaint.setColor(Color.rgb(239,239,239));
        this.backgroundPaint.setStrokeJoin(Paint.Join.ROUND);
        this.backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        this.backgroundPaint.setStrokeWidth(3);
    }

    void initOriginPaint() {
        this.originPaint=new Paint();
        //this.originPaint.setColor(Color.rgb(231,88,113));
        this.originPaint.setColor(Color.rgb(255, 0, 0));
        //this.originPaint.setColor(Color.rgb(239,239,239));
        this.originPaint.setStrokeJoin(Paint.Join.ROUND);
        this.originPaint.setStrokeCap(Paint.Cap.ROUND);
        this.originPaint.setStrokeWidth(3);
        this.originPaint.setStrokeWidth(3);
    }

    void initCurrentPaint() {
        this.currentPaint=new Paint();
        this.currentPaint.setColor(Color.BLUE);
        this.currentPaint.setStrokeJoin(Paint.Join.ROUND);
        this.currentPaint.setStrokeCap(Paint.Cap.ROUND);
        this.currentPaint.setStrokeWidth(3);
        this.currentPaint.setStrokeWidth(3);
    }

    void initMetrics() {
        fontMetrics = textPaint.getFontMetrics();
        fonttop = fontMetrics.top;
        fontbottom = fontMetrics.bottom;
    }

    void initAllPainters() {
        initBoundPaint();
        initTextPaint();
        initMovePaint();
        initRankingPaint();
        initBackgroundPaint();
        initOriginPaint();
        initCurrentPaint();
        initMetrics();
    }

    public Painters() {
        initAllPainters();
    }

    public Painters(float screen_height_ratio) {
        this.screen_height_ratio = screen_height_ratio;
        initAllPainters();
    }
}
