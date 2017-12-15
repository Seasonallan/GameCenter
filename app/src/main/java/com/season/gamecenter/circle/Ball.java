package com.season.gamecenter.circle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;

import com.season.gamecenter.ball.interpolator.IInterpolator;
import com.season.gamecenter.ball.interpolator.KeepInterpolator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Disc: 玩球
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 12:08
 */
public class Ball {

    int minRadius = 40;

    public boolean big(){
        radius = radius + 2;
        radius = Math.min(width / 5, radius);
        if (radius >= width/5){
            return true;
        }else{
            textPaint.setTextSize(radius * 2 / 3);
            Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
            textX = textPaint.measureText("0.00") / 2;
            //LogConsole.log("descent=" + fontMetrics.descent + "  ascent=" + fontMetrics.ascent);
            textY = (fontMetrics.descent - fontMetrics.ascent) / 4;
            return false;
        }
    }

    public boolean small(){
        radius = radius - 2;
        radius = Math.max(minRadius * 1, radius);
        if (radius <= minRadius * 1){
            return true;
        }else{
            textPaint.setTextSize(radius * 2 / 3);
            Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
            textX = textPaint.measureText("0.00") / 2;
            //LogConsole.log("descent=" + fontMetrics.descent + "  ascent=" + fontMetrics.ascent);
            textY = (fontMetrics.descent - fontMetrics.ascent) / 4;
            return false;
        }
    }

    public List<Ball> separate() {
        List<Ball> ballList = new ArrayList<>();
        int newRadius = radius*1/2;
        if (newRadius >= minRadius){
            ballList.add(cloneBall(newRadius));
            ballList.add(cloneBall(-newRadius));
        }
        return ballList;
    }
    public Ball cloneSameBall(){
        return cloneBall(radius);
    }

    public Ball cloneBall(int r){
        Ball ball = new Ball();
        ball.id = System.currentTimeMillis() + new Random().nextLong();
        ball.cx = cx;
        ball.cy = cy;
        ball.radius = Math.abs(r);
        ball.slopDegree = slopDegree;
        ball.special = special;
        ball.color = 0xff000000 | new Random().nextInt(0x00ffffff);;
        ball.width = width;
        ball.height = height;
        ball.paint = new Paint();
        ball.paint.setColor(color);
        ball.textPaint = new TextPaint();
        ball.textPaint.setColor(Color.WHITE);
        ball.textPaint.setTextSize(ball.radius * 2 / 3);
        Paint.FontMetricsInt fontMetrics = ball.textPaint.getFontMetricsInt();
        ball.textX = ball.textPaint.measureText("0.00") / 2;
        //LogConsole.log("descent=" + fontMetrics.descent + "  ascent=" + fontMetrics.ascent);
        ball.textY = (fontMetrics.descent - fontMetrics.ascent) / 4;
        ball.ballInterpolator = new KeepInterpolator();
        ball.ballInterpolator.resetSpeed(ballInterpolator.getSpeed());
        return ball;
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(radius, radius, radius, paint);
        return bitmap;
    }

    public Rect getRect() {
        return new Rect((int)(cx - radius), (int)(cy - radius), (int)(cx + radius), (int)(cy + radius));
    }

    /**
     * 构建一个球
     */
    public static class Builder {
        Ball ball;

        Builder() {
            ball = new Ball();
        }

        Builder(Ball ball) {
            this.ball = ball;
        }

        public Builder setEdge(int width, int height) {
            ball.width = width;
            ball.height = height;
            return this;
        }

        public Builder setId(long id) {
            ball.id = id;
            return this;
        }

        public Builder setTouchEnable(boolean enableTouch){
            ball.canTouched = enableTouch;
            return this;
        }

        public Builder setSpecial(int special) {
            ball.special = special;
            return this;
        }

        public Builder setInterpolator(IInterpolator interpolator) {
            ball.ballInterpolator = interpolator;
            return this;
        }

        public Ball build() {
            return ball;
        }
    }

    int width, height;

    int radius;
    int color;
    Paint paint;
    TextPaint textPaint;

    float cx, cy;
    double slopDegree = 3;

    long id;
    int special;
    int clickSpecial;

    IInterpolator ballInterpolator;

    /**
     * DOWN事件是否落在球体区域内
     *
     * @param x
     * @param y
     * @return
     */
    boolean isTouched(float x, float y) {
        if (canTouched){
            RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            if (rectF.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    boolean canTouched = true;
    boolean isTouched = false;

    void onTouch() {
        isTouched = true;
        recordSpeed = ballInterpolator.getSpeed();
        ballInterpolator.resetSpeed(0);
    }
    void resume(){
        isTouched = false;
        recordSpeed = (float) Math.max(recordSpeed, 0.1);
        ballInterpolator.resetSpeed(recordSpeed);
    }

    float recordSpeed;
    float touchSpeed;
    float x, y;

    void onMove(float x, float y) {
        isTouched = true;
        this.x = x;
        this.y = y;
        touchSpeed = (x - cx) * (x - cx) + (y - cy) * (y - cy);
        touchSpeed = (float) Math.sqrt(touchSpeed);
        touchSpeed = touchSpeed / 50;
        touchSpeed = Math.min(touchSpeed, 50);
    }

    void stop(){
        isTouched = false;
        ballInterpolator.resetSpeed(0);
    }

    double getDegree(float x, float y) {
        double degree = Math.atan2((y - cy), (x - cx));
        return 180 * degree / Math.PI;
    }
    float getSpeed(float x, float y){
        float speed = (x - cx) * (x - cx) + (y - cy) * (y - cy);
        speed = (float) Math.sqrt(speed);
        speed = speed / 50;
        speed = Math.min(speed, 50);
        return speed;
    }

    void onRelease(float x, float y) {
        float speed = (x - cx) * (x - cx) + (y - cy) * (y - cy);
        speed = (float) Math.sqrt(speed);
        speed = speed / 50;
        double degree = Math.atan2((y - cy), (x - cx));
        degree = 180 * degree / Math.PI;
        isTouched = false;
        slopDegree = degree;
        speed = Math.min(speed, 50);
        ballInterpolator.resetSpeed(speed);
    }

    float textX, textY;


    void randomSetUp() {
        int radius = new Random().nextInt(width / 5);
        radius = Math.max(minRadius, radius);
        int speed = new Random().nextInt(8) + 8;
        init(radius, speed,
                new Random().nextInt(width - radius), new Random().nextInt(height - radius), new Random().nextInt(360));
        fixXY();
    }



    void init(int rad, int speed, int x, int y, int degree) {
        radius = rad;
        if (color <= 0) {
            color = 0xff000000 | new Random().nextInt(0x00ffffff);
        }
        if (paint == null) {
            paint = new Paint();
            paint.setColor(color);
        }
        if (textPaint == null) {
            textPaint = new TextPaint();
            textPaint.setTextSize(radius * 2 / 3);
            textPaint.setColor(Color.WHITE);
            Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
            textX = textPaint.measureText("0.00") / 2;
            //LogConsole.log("descent=" + fontMetrics.descent + "  ascent=" + fontMetrics.ascent);
            textY = (fontMetrics.descent - fontMetrics.ascent) / 4;
        }
        ballInterpolator.resetSpeed(speed);
        slopDegree = degree;
        cx = x;
        cy = y;
    }

    void setSpeed(float speed){
        ballInterpolator.resetSpeed(speed);
    }

    void fixXY() {
        if (cx <= radius || cx >= width - radius) {
            slopDegree = 180 - slopDegree;
        }
        if (cy >= height - radius || cy <= radius) {
            slopDegree = -slopDegree;
        }
        while (slopDegree < 0) {
            slopDegree += 360;
        }
        while (slopDegree > 360) {
            slopDegree -= 360;
        }

        cx = Math.max(radius, cx);
        cy = Math.max(radius, cy);
        cx = Math.min(cx, width - radius);
        cy = Math.min(cy, height - radius);
    }

    boolean isCrash(Ball ballView) {
        if (isTouched) {
            return false;
        }
        if (ballView instanceof Circle){
            return ballView.isCrashBall(cx, cy, (int) ballInterpolator.getSpeed());
        }
        double xy = (cx - ballView.cx) * (cx - ballView.cx) + (cy - ballView.cy) * (cy - ballView.cy);
        xy = Math.sqrt(xy);
        if (xy <= radius + ballView.radius) {
            return true;
        }
        return false;
    }

    boolean isCrashBall(float cx, float cy, int radius){
        return false;
    }

    /**
     * 碰撞速度角度处理
     *
     * @param crashModel
     */
    void crashChanged(Ball crashModel) {
        double degree = Math.atan2((cy - crashModel.cy), (cx - crashModel.cx));
        degree = 180 * degree / Math.PI;
        float speed = ballInterpolator.getSpeed();
        if (speed > 0) {

            if (sameArea(degree, slopDegree)) {
                slopDegree = degree;
            } else {
                slopDegree += 180;
                degree += 180;
                slopDegree = degree - slopDegree + degree;
            }
        } else {

            slopDegree = crashModel.slopDegree;
            slopDegree += 180;
            degree += 180;
            slopDegree = degree - slopDegree + degree;
            slopDegree += 180;
        }

        if (special > 0){
            //ballInterpolator.speedChange(0, crashModel.ballInterpolator);
        }else{
            //int speedCost = getSpeedCost(degree, slopDegree);
            //ballInterpolator.speedChange(speedCost, crashModel.ballInterpolator);
        }
    }

    int getSpeedCost(double from, double to) {
        int mul = (int) (from - to);
        return mul % 360;
    }

    /**
     * 用于重合方向纠正
     *
     * @param from
     * @param to
     * @return
     */
    boolean sameArea(double from, double to) {
        int mul = (int) (from - to);
        return mul % 360 < 180;
    }

    public void move() {
        ballInterpolator.speedCost();
        if (ballInterpolator.getSpeed() > 0) {
            cx += ballInterpolator.getSpeed() * Math.cos(slopDegree * Math.PI / 180);
            cy += ballInterpolator.getSpeed() * Math.sin(slopDegree * Math.PI / 180);
            fixXY();
        }
    }

    public boolean hasSpeed() {
        return ballInterpolator.getSpeed() > 0;
    }

    void draw(Canvas canvas) {
        canvas.drawCircle(cx, cy, radius, paint);

        if (true){
            return;
        }
        DecimalFormat df = new DecimalFormat("###.00");
        String speedStr;
        if (isTouched) {
            speedStr = df.format(touchSpeed);
        } else {
            speedStr = df.format(ballInterpolator.getSpeed());
        }
        if (speedStr.length() > 4) {
            speedStr = speedStr.substring(0, 4);
        }
        canvas.drawText(speedStr, cx - textX, cy + textY, textPaint);

    }


}
