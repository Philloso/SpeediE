package pcher.test.philloso.speedometer.Utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import java.util.List;

import pcher.test.philloso.speedometer.Activity.MainActivity;
import pcher.test.philloso.speedometer.R;

/**
 * Created by pcher on 2/24/2016.
 */
public class GPSManager implements android.location.GpsStatus.Listener {

    private static final int gpsMinTime = 500;
    private static final int gpsMinDistance = 0;

    private static LocationManager locationManager = null;
    private static LocationListener locationListener = null;
    Context mContext;

    private Location mLastLocation;
    private double mDistance;

    public GPSManager(Context context) {

        mContext = context;
        mDistance = 0;

        GPSManager.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                double distance = 0;
                if (mLastLocation != null) {
                    distance = mLastLocation.distanceTo(location);
                }
                if (location.getAccuracy() < distance) {
                    addDistance(distance);
                }
                mLastLocation = location; // position
                ((MainActivity) mContext).onMyLocationChanged(location); //Callback MainActivity
            }

            @Override
            public void onProviderDisabled(final String provider) {
            }

            @Override
            public void onProviderEnabled(final String provider) {
            }

            @Override
            public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            }
        };
    }

    public void displayAlert() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(mContext.getString(R.string.gps_activation));
        alertDialog.setMessage(mContext.getString(R.string.ask_gps_activation));
        alertDialog.setPositiveButton(mContext.getString(R.string.gps_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void startListening() {


        if (GPSManager.locationManager == null) {
            GPSManager.locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }

        GPSManager.locationManager.addGpsStatusListener(this);

        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        final String bestProvider = GPSManager.locationManager.getBestProvider(criteria, true);
        if (bestProvider != null && bestProvider.length() > 0) {
            GPSManager.locationManager.requestLocationUpdates(bestProvider, GPSManager.gpsMinTime, GPSManager.gpsMinDistance, GPSManager.locationListener);
        } else {
            final List<String> providers = GPSManager.locationManager.getProviders(true);
            for (final String provider : providers) {
                GPSManager.locationManager.requestLocationUpdates(provider, GPSManager.gpsMinTime,
                        GPSManager.gpsMinDistance, GPSManager.locationListener);
            }
        }
    }


    public void stopListening() {
        try {
            if (GPSManager.locationManager != null && GPSManager.locationListener != null) {
                GPSManager.locationManager.removeUpdates(GPSManager.locationListener);
            }
            GPSManager.locationManager = null;
        } catch (final Exception ex) {
        }
    }


    public void onGpsStatusChanged(int event) {
        int Satellites = 0;
        int SatellitesInFix = 0;
        int timetofix = locationManager.getGpsStatus(null).getTimeToFirstFix();
        for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
            if (sat.usedInFix()) {
                SatellitesInFix++;
            }
            Satellites++;
        }
    }


    public interface ILocation {
        public void onMyLocationChanged(Location location);
    }


    private void addDistance(double distance) {
        mDistance += distance;
    }

    public double getmDistance() {
        return mDistance;
    }

    public void setmDistance(double mDistance) {
        this.mDistance = mDistance;
    }


}
