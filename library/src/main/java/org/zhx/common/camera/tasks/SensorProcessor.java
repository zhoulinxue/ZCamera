package org.zhx.common.camera.tasks;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.zhx.common.camera.CameraModel;
import org.zhx.common.util.CameraUtil;

public class SensorProcessor implements SensorEventListener {
    private CameraModel.view mView;
    SensorManager sm;
    private int currentRad;

    public SensorProcessor(CameraModel.view view) {
        this.mView = view;
        sm = (SensorManager) view.getContext().getSystemService(view.getContext().SENSOR_SERVICE);
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
        int uiRot = mView.getContext().getWindowManager().getDefaultDisplay().getRotation();
        int degree = 90 * uiRot;
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
