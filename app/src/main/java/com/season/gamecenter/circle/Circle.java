package com.season.gamecenter.circle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import java.text.DecimalFormat;

/**
 * Created by Administrator on 2017/12/15.
 */

public class Circle extends Ball {



    float currentDegree = 0;
    public void move() {
        currentDegree += addDegree;
    }

    float addDegree = 1;
    void setDegree(float defaultDegree, float addDegree){
        this.currentDegree = defaultDegree;
        this.addDegree = addDegree;
    }


    public static boolean isTouchPointInPath(Path path, int x, int y) {
        if (path == null) {
            return false;
        }
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        Region region = new Region();
        region.setPath(path, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));

        if (region.contains(x, y)) {
            return true;
        }
        return false;
    }

    @Override
    boolean isCrashBall(float x, float y, int ballSpeed){
        int xy = (int) ((cx - x) * (cx - x) + (cy - y) * (cy - y));
        xy = (int) Math.sqrt(xy);
        if (Math.abs(radius - xy) <= ballSpeed * 2) {
            Path path = new Path();
            float degree = currentDegree;
            path.addArc(rect,  degree, 60);
            path.addArc(innerRect,  degree, 60);
            path.addArc(outerRect,  degree, 60);

            degree += 60;
            degree += 60;
            path.addArc(rect,  degree, 60);
            path.addArc(innerRect,  degree, 60);
            path.addArc(outerRect,  degree, 60);
            degree += 60;
            degree += 60;
            path.addArc(rect,  degree, 60);
            path.addArc(innerRect,  degree, 60);
            path.addArc(outerRect,  degree, 60);

            return isTouchPointInPath(path, (int)x, (int)y);
        }
        return false;
    }

    RectF rect;
    RectF innerRect;
    RectF outerRect;
    @Override
    void init(int rad, int speed, int x, int y, int degree) {
        super.init(rad, speed, x, y, degree);
        rect = new RectF(x - rad, y - rad, x + rad, y +rad);
        int width = 1;
        innerRect = new RectF(x - rad + width, y - rad + width, x + rad - width, y +rad - width);
        outerRect = new RectF(x - rad - width, y - rad - width, x + rad + width, y +rad + width);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(16);
    }

    @Override
    boolean isCrash(Ball ballView) {
        return false;
    }

    @Override
    void draw(Canvas canvas) {
        float degree = currentDegree;
        paint.setColor(Color.RED);
        canvas.drawArc(rect,  degree, 60, false, paint);
        degree += 60;

        paint.setColor(Color.TRANSPARENT);
        canvas.drawArc(rect,  degree, 60, false, paint);
        degree += 60;

        paint.setColor(Color.GREEN);
        canvas.drawArc(rect,  degree, 60, false, paint);
        degree += 60;

        paint.setColor(Color.TRANSPARENT);
        canvas.drawArc(rect,  degree, 60, false, paint);
        degree += 60;

        paint.setColor(Color.BLUE);
        canvas.drawArc(rect,  degree, 60, false, paint);
        degree += 60;

        paint.setColor(Color.TRANSPARENT);
        canvas.drawArc(rect,  degree, 60, false, paint);

    }

}
