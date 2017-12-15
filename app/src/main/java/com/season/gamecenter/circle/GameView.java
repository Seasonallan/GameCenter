package com.season.gamecenter.circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.season.gamecenter.ball.interpolator.BallInterpolatorFactory;
import com.season.gamecenter.ball.interpolator.IInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Disc: 单个View多个球体
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 11:59
 */
public class GameView extends View {

    Paint paint;
    CopyOnWriteArrayList<Ball> ballList;

    public GameView(Context context) {
        super(context);
        ballList = new CopyOnWriteArrayList<>();
        paint = new Paint();
        paint.setStrokeWidth(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
           // this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }


    public void stop() {
        if (runningThread != null){
            runningThread.interrupt();
        }
    }

    public void start() {
        if (runningThread != null){
            runningThread.interrupt();
        }
        runningThread = new RunningThread();
        runningThread.start();
    }

    RunningThread runningThread;
    class RunningThread extends Thread{
        @Override
        public void run() {
            while (true){
                for (Ball ballModel : ballList) {
                    if (ballModel.hasSpeed()){
                        ballModel.move();
                    }
                }
                crashCheck();
                postInvalidate();
                try {
                   sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void crashCheck() {
        for (Ball currentBall : ballList) {
            for (Ball checkBall : ballList) {
                if (currentBall.id != checkBall.id) {
                    if (currentBall.isCrash(checkBall)) {
                        if (checkBall.special > 0){
                            currentBall.clickSpecial = checkBall.special;
                        }else{
                            currentBall.clickSpecial = 5;
                        }
                        //currentBall.crashChanged(checkBall);
                    }
                }
            }
        }
        for (int i = ballList.size() - 1; i>= 0; i--){
            Ball ball = ballList.get(i);
            if (ball.clickSpecial == 100){
                ballList.remove(ball);
                if (listener != null){
                    listener.onFail(ball);
                }
                canTouch = true;
                return;
            }else if (ball.clickSpecial == 200){
                ballList.remove(ball);
                Ball ball1 = ballList.remove(0);
                if (listener != null){
                    listener.onSuccess(ball1);
                }
                return;
            }
        }
    }

    Ball touchBall;
    //VelocityTracker mVelocityTracker;
    float x, y;

    boolean canTouch = true;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canTouch){
            return false;
        }
        x = event.getX();
        y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchBall = null;
                touchBall = getTouchBall(x, y);
                if (touchBall != null) {
                    touchBall.onTouch();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchBall != null) {
//                    mVelocityTracker.addMovement(event);
                    touchBall.onMove(x, y);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (touchBall != null) {
                    int radiu = touchBall.radius - new Random().nextInt(touchBall.radius);
                    radiu = Math.max(20, radiu);
                    radiu = touchBall.radius/3;
                    Ball copyBall = touchBall.cloneBall(radiu);
                    copyBall.slopDegree = touchBall.getDegree(x, y);
                    copyBall.canTouched = false;
                    copyBall.setSpeed(touchBall.getSpeed(x, y) * 1.35f);
                    //copyBall.setSpeed(35);
                    ballList.add(copyBall);
                    canTouch = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            canTouch = true;
                        }
                    }, 1000);
                    touchBall = null;
                }
                break;

        }

        return super.onTouchEvent(event);
    }

    Ball getTouchBall(float x, float y) {
        for (Ball ballModel : ballList) {
            if (ballModel.isTouched(x, y)) {
                return ballModel;
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Ball ballModel : ballList)
            ballModel.draw(canvas);

        if (touchBall != null && touchBall.isTouched) {
            paint.setColor(touchBall.color);
         //   paint.setPathEffect(new DashPathEffect(new float[] {25, ( touchBall.cy - y) * 2/200}, 0));
            canvas.drawLine(touchBall.cx, touchBall.cy, x, y, paint);

           // ballTouchExpand.onDraw(canvas, x, y);
        }

    }


    public void clear() {
        ballList.clear();
    }


    public void restartGame(float difficulty) {
        clear();
        startGame(difficulty);
    }

    public void startGame(float difficulty) {
        if (ballList.size() > 0){
            return;
        }

        Ball ballModelGoal = new Ball.Builder()
                .setId(System.currentTimeMillis())
                .setEdge(getWidth(), getHeight())
                .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                .setTouchEnable(false)
                .setSpecial(200)
                .build();
        ballModelGoal.init(100, 0, getWidth()/2, 0, 0);
        ballList.add(ballModelGoal);
        int totalHeight = getHeight() - 150 - 150;
        int totalWidth = getWidth() - 50;
        int radius = Math.min(totalHeight, totalWidth)/2;

        int offRadius = (int) (100 + 100 * difficulty);
        int count = radius/offRadius;

        for (int i = 0; i< count; i++){
            Circle ballModelRun = (Circle) new Ball.Builder(new Circle())
                    .setId(System.currentTimeMillis())
                    .setEdge(getWidth(), getHeight())
                    .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                    .setTouchEnable(false)
                    .setSpecial(100)
                    .build();
            ballModelRun.init(radius, 1, getWidth()/2, getHeight()/2, 0);
            ballModelRun.setDegree(new Random().nextInt(360), (new Random().nextInt(200) - 100)* 1.0f/50);
            ballList.add(ballModelRun);
            radius -= offRadius;
        }

        Ball ballModel = new Ball.Builder()
                .setId(System.currentTimeMillis() + 1)
                .setEdge(getWidth(), getHeight())
                .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                .setSpecial(0)
                .build();
        ballModel.init(100, 0,getWidth()/2, getHeight(), 0);

        ballList.add(ballModel);
    }

    public GoalListener listener;

    public interface GoalListener{
        void onSuccess(Ball ball);
        void onFail(Ball ball);
    }

}
