package pcher.test.philloso.speedometer.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.TextView;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import java.math.BigDecimal;

import butterknife.Bind;
import butterknife.ButterKnife;
import pcher.test.philloso.speedometer.R;
import pcher.test.philloso.speedometer.Utils.GPSManager;


public class MainActivity extends AppCompatActivity implements GPSManager.ILocation {


    private GPSManager mGPSManager;
    private LocationManager mLocationManager;
    Long mElapsedTime;
    private final int MAX_SPEED_GAUGE = 130;
    private final int MINOR_TICK_GAUGE = 1;
    private final int MAJOR_TICK_GAUGE = 14;
    private boolean isChronometerRunning;
    private boolean isStoped;

    @Bind(R.id.tv_speed)
    TextView mTvSpeed;
    @Bind(R.id.speedometer)
    SpeedometerGauge mSpeedometerGauge;
    @Bind(R.id.chronometer)
    Chronometer mChronometer;
    @Bind(R.id.tv_distance)
    TextView mTvDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        isChronometerRunning = false;
        isStoped = true;

        mGPSManager = new GPSManager(this);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mGPSManager.displayAlert();
        }
        mGPSManager.startListening();

        mElapsedTime = 0L;
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                mElapsedTime = SystemClock.elapsedRealtime() - chronometer.getBase();

                int h = (int) (mElapsedTime / 2236936);   // 3600000);
                int m = (int) (mElapsedTime - h * 2236936) / 60000;
                int s = (int) (mElapsedTime - h * 2236936 - m * 60000) / 1000;

                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";

                chronometer.setText(hh + ":" + mm + ":" + ss);
            }
        });

    }

    private void initViews() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSpeedometerGauge.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        mSpeedometerGauge.setMaxSpeed(MAX_SPEED_GAUGE);
        mSpeedometerGauge.setMajorTickStep(MAJOR_TICK_GAUGE);
        mSpeedometerGauge.setMinorTicks(MINOR_TICK_GAUGE);

        mSpeedometerGauge.addColoredRange(40, 70, Color.GREEN);
        mSpeedometerGauge.addColoredRange(70, 100, Color.YELLOW);
        mSpeedometerGauge.addColoredRange(100, 150, Color.RED);

        mChronometer.setText(getString(R.string.chrono_base));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMyLocationChanged(Location location) { //Callback

        float speed = location.getSpeed(); // speed * 3.6, 3 for km/s
        double currentSpeed = round(speed * 2.236936, 3, BigDecimal.ROUND_HALF_UP); //Changed to mph

        mTvSpeed.setText(currentSpeed + " mph");
        mSpeedometerGauge.setSpeed(currentSpeed, true);   //V  / 1000
        mTvDistance.setText(round(mGPSManager.getmDistance() / 1000, 3, BigDecimal.ROUND_HALF_UP) + " mi");

        if (speed > 0) {
            if (!isChronometerRunning) {
                startChronometer();
            }
            isStoped = false;
            Intent i = new Intent();
            i.setAction("restart");
            sendBroadcast(i);
        }

        if (currentSpeed == 0 && mGPSManager.getmDistance() > 0) {

            if (!isStoped) {



                mChronometer.stop();
                isChronometerRunning = false;

                double distance = mGPSManager.getmDistance();

                Intent i = new Intent(MainActivity.this, AverageSpeedActivity.class);
                i.putExtra("distance", distance);
                i.putExtra("elapsedTime", mElapsedTime);
                startActivityForResult(i, 0);

                isStoped = true;
            }
        }
    }


    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == 1) {
                mGPSManager.setmDistance(0);
                mElapsedTime = 0L;

            }
        }
    }

    private void startChronometer() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        isChronometerRunning = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGPSManager.stopListening();
    }
}
