package pcher.test.philloso.speedometer.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.math.BigDecimal;

import butterknife.Bind;
import butterknife.ButterKnife;
import pcher.test.philloso.speedometer.R;

public class AverageSpeedActivity extends AppCompatActivity {

    @Bind(R.id.tv_average_speed)
    TextView mTvAverageSpeed;

    private double mDistance;
    private long mElapsedTime;
    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initBroadcastReceiver();


        mDistance = getIntent().getDoubleExtra("distance", 0);
        mElapsedTime = getIntent().getLongExtra("elapsedTime", 0);


        double averageSpeed = (mDistance / (mElapsedTime / 1000)) * 2.23693629;

        try {
            double average = round(averageSpeed, 3, BigDecimal.ROUND_HALF_UP);
            mTvAverageSpeed.setText(average + " mph");
        } catch (Exception e) {
            mTvAverageSpeed.setText("...");
        }
    }

    private void initViews() {
        setContentView(R.layout.activity_average_speed);
        ButterKnife.bind(this);

    }

    private void initBroadcastReceiver() {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("restart");
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if ("restart".equals(action)) {
                        AverageSpeedActivity.this.setResult(1);
                        finish();
                    }
                }
            }
        };
        this.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_average_speed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
