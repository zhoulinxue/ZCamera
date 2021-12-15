package org.zhx.common.camera.tasks;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.zhx.common.camera.CameraModel;
import org.zhx.common.util.CameraUtil;

public class SensorProcessor implements SensorEventListener {
    private Context mContext;
    private CameraModel.view mView;
    SensorManager sm;
    private int currentRad;

    public SensorProcessor(Context context, CameraModel.view mView) {
        this.mContext = context;
        this.mView = mView;
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
            return;
        }
        float[] values = event.values;
        float ax = values[0];
        float ay = values[1];
        double g = Math.sqrt(ax * ax + ay * ay);
        double cos = ay / g;
        if (cos > 1) {
            cos = 1;
        } else if (cos < -1) {
            cos = -1;
        }
        double rad = Math.acos(cos);
        if (ax < 0) {
            rad = 2 * Math.PI - rad;
        }
        int uiRot = mView.getRotation();
        double uiRad = Math.PI / 2 * uiRot;
        rad -= uiRad;
        currentRad = (int) (180 * rad / Math.PI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int getDegree(boolean isFrontCamera) {
        return CameraUtil.getPortraitDegree(isFrontCamera, currentRad);
    }

    public void destory() {
        sm.unregisterListener(this);
    }
}
