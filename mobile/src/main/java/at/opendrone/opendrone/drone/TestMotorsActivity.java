package at.opendrone.opendrone.drone;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import at.opendrone.opendrone.R;
import at.opendrone.opendrone.network.ConnectDisconnectTasks;
import at.opendrone.opendrone.network.OpenDroneFrame;
import at.opendrone.opendrone.utils.OpenDroneUtils;

public class TestMotorsActivity extends AppCompatActivity {
    private static final String TAG = "TestMotory";

    private ImageView motor1;
    private ImageView motor2;
    private ImageView motor3;
    private ImageView motor4;
    private ImageView imageForward;
    private TextView infoTxt;

    private ConnectDisconnectTasks tasks = ConnectDisconnectTasks.getInstance();
    private Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_motors);

        findViews();
        initActionbar();
    }

    private void findViews() {
        motor1 = findViewById(R.id.motor1Img);
        motor2 = findViewById(R.id.motor2Img);
        motor3 = findViewById(R.id.motor3Img);
        motor4 = findViewById(R.id.motor4Img);
        imageForward = findViewById(R.id.imageForward);
        infoTxt = findViewById(R.id.infoTxt);

        motor1.setOnClickListener(v -> sendMotorSpinCommand(motor1, 1));
        motor2.setOnClickListener(v -> sendMotorSpinCommand(motor2, 2));
        motor3.setOnClickListener(v -> sendMotorSpinCommand(motor3, 3));
        motor4.setOnClickListener(v -> sendMotorSpinCommand(motor4, 4));

        imageForward.setOnClickListener(v -> flyForward(imageForward));

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void sendMotorSpinCommand(ImageView motor, int motorNr) {
        try {
            OpenDroneFrame frame = new OpenDroneFrame((byte) 1, new String[]{motorNr + ""}, new int[]{OpenDroneUtils.CODE_SPIN_MOTOR});
            Log.i(TAG, frame.toString());
            tasks.sendMessage(frame.toString());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        doFeedbackStuff(motor);
    }

    private void flyForward(ImageView motor) {
        doFeedbackStuff(motor);
        doFeedbackStuff(motor);
        new TextAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void doFeedbackStuff(ImageView motor) {
        animateBackground(motor);
        vibrate(50);
    }

    private void animateBackground(ImageView motor) {
        int colorFrom = getResources().getColor(R.color.primaryTextColor);
        int colorTo = getResources().getColor(R.color.primaryColor);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo, colorFrom);
        colorAnimation.setDuration(1000); // milliseconds
        colorAnimation.addUpdateListener(animator -> motor.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private void vibrate(int millis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(millis);
        }
    }

    private void initActionbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_TestMotors);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private class TextAsyncTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            changeTxt(getString(R.string.fly_forward));
            sleep(1000);
            changeTxt("3");
            sleep(700);
            changeTxt("2");
            sleep(700);
            changeTxt("1");
            sleep(2000);
            changeTxt("pranked.");
            sleep(500);
            changeTxt(getString(R.string.txt_test_motor_instructions));
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "test1");
        }

        private void changeTxt(String txt) {
            runOnUiThread(() -> infoTxt.setText(txt));
        }

        private void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
