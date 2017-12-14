package com.season.gamecenter.ball;

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


/**
 * Disc: 单个View多个球体
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-06-08 11:59
 */
public class GameView extends View {

    Paint paint;
    List<Ball> ballList;

    public GameView(Context context) {
        super(context);
        ballList = new ArrayList<>();
        paint = new Paint();
        paint.setStrokeWidth(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
           // this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }


    private boolean running = true;
    int time = 10;

    public void stop() {
        running = false;
        handler.removeMessages(1);
    }

    public void start() {
        running = false;
        if (touchBall != null && touchBall.isTouched) {
            running = true;
        }
        for (Ball ballModel : ballList) {
            if (ballModel.hasSpeed()){
                ballModel.move();
                running = true;
            }
        }
        crashCheck();
        invalidate();
        handler.sendEmptyMessageDelayed(1, time);
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
                    radiu = touchBall.radius/2;
                    Ball copyBall = touchBall.cloneBall(radiu);
                    copyBall.slopDegree = touchBall.getDegree(x, y);
                    copyBall.canTouched = false;
                    //copyBall.setSpeed(touchBall.getSpeed(x, y) * 2);
                    copyBall.setSpeed(35);
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


    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (running) {
                start();
            }
        }

        ;
    };


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

    public void removeBall(Ball ball){
        ballList.remove(ball);
    }

    /**
     * 添加一个球
     *
     */
    public void addOneBall() {
        String interpolatorFlag = BallInterpolatorFactory.LINEAR;
        int special = -1;
        if (ballList.size() == 0) {
            interpolatorFlag = BallInterpolatorFactory.KEEP;
            special = 10;
        }
        if (ballList.size() == 1) {
            interpolatorFlag = BallInterpolatorFactory.KEEP;
            special = 20;
        }
        IInterpolator interpolator = BallInterpolatorFactory.getInterpolator(interpolatorFlag);
        Ball ballModel = new Ball.Builder()
                .setId(System.currentTimeMillis())
                .setEdge(getWidth(), getHeight())
                .setInterpolator(interpolator)
                .setSpecial(special)
                .build();
        ballModel.randomSetUp();
        ballList.add(ballModel);
        if (!running){
            start();
        }
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
        int perHeight = (int) (150 + 150 * difficulty);
        int count = totalHeight/perHeight;
        int height = 150;
        int[] ys = new int[count];
        for (int i = 0; i < ys.length; i++){
            ys[i] = height;
            height += perHeight;
        }
        int perWidth = getWidth()/count;
        int width = 0;
        int[] xs = new int[count];
        for (int i = 0; i < xs.length; i++){
            xs[i] = width;
            width += perWidth;
        }
        int speed = 50;
        for (int i = 0; i< count; i++){
            Ball ballModelRun = new Ball.Builder()
                    .setId(System.currentTimeMillis())
                    .setEdge(getWidth(), getHeight())
                    .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                    .setTouchEnable(false)
                    .setSpecial(100)
                    .build();
            ballModelRun.init(20, speed, xs[i], ys[i], 0);
            speed -= 3;
            ballList.add(ballModelRun);
        }

        Ball ballModel = new Ball.Builder()
                .setId(System.currentTimeMillis() + 1)
                .setEdge(getWidth(), getHeight())
                .setInterpolator(BallInterpolatorFactory.getInterpolator(BallInterpolatorFactory.KEEP))
                .setSpecial(0)
                .build();
        ballModel.init(100, 0,getWidth()/2, getHeight(), 0);

        ballList.add(ballModel);
        if (!running){
            start();
        }
    }

    public GoalListener listener;

    public interface GoalListener{
        void onSuccess(Ball ball);
        void onFail(Ball ball);
    }

}
