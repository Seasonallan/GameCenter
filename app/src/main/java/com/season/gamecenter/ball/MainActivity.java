package com.season.gamecenter.ball;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.season.util.explosion.ExplosionField;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mContainerView;
    private GameView mBallView;
    private ExplosionField mExplosionField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        mContainerView = new RelativeLayout(this);

        textView = new TextView(this);
        textView.setText("拖动底部小球击中顶部");
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.addRule(RelativeLayout.CENTER_IN_PARENT);
        mContainerView.addView(textView, param);

        mBallView = new GameView(this);
        mContainerView.addView(mBallView);

        setContentView(mContainerView);

        mBallView.listener = new GameView.GoalListener() {
            @Override
            public void onSuccess(Ball ball) {
                mExplosionField.explode(ball.getBitmap(), ball.getRect(), 10, 1500);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onLongClick(null);
                    }
                }, 1500);
            }

            @Override
            public void onFail(Ball ball) {
                mExplosionField.explode(ball.getBitmap(), ball.getRect(), 10, 1500);
            }
        };
        mExplosionField = ExplosionField.attach2Window(this);

        mBallView.post(new Runnable() {
            @Override
            public void run() {
                onLongClick(null);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 1500);
    }
    TextView textView;

    public boolean onLongClick(View v) {
        mExplosionField.clear();
        mBallView.restartGame(new Random().nextInt(100) * 1.0f/100);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBallView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBallView.start();
    }

}
