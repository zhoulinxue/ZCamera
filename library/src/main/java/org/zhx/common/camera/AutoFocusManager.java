/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zhx.common.camera;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

public final class AutoFocusManager implements Camera.AutoFocusCallback {

    private static final String TAG = AutoFocusManager.class.getSimpleName();

    private static final long AUTO_FOCUS_INTERVAL_MS = 5000L;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private boolean focusing;
    private final Camera camera;
    private AsyncTask<?, ?, ?> outstandingTask;
    private Camera.AutoFocusCallback mCallback;

    public AutoFocusManager(Camera camera, Camera.AutoFocusCallback callback) {
        this.camera = camera;
        this.mCallback = callback;
//        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        focusing = false;
        if (mCallback != null) {
            mCallback.onAutoFocus(success, theCamera);
        }
    }

    public synchronized void start() {
        if (outstandingTask == null) {
            AutoFocusTask newTask = new AutoFocusTask();
            try {
                newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                outstandingTask = newTask;
            } catch (RejectedExecutionException ree) {
                Log.w(TAG, "Could not request auto focus", ree);
            }
        }
    }

    public boolean isFocusing() {
        return focusing;
    }

    public synchronized void stop() {
        try {
            if (outstandingTask != null) {
                if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
                    outstandingTask.cancel(true);
                }
                outstandingTask = null;
            }
            if (focusing) {
                camera.cancelAutoFocus();
            }
        } catch (Exception e) {

        }
    }

    private final class AutoFocusTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... voids) {
            outstandingTask = null;
            if (!focusing) {
                try {
                    camera.autoFocus(AutoFocusManager.this);
                    focusing = true;
                } catch (RuntimeException re) {
                    Log.w(TAG, "Unexpected exception while focusing", re);
                }
            }
            return null;
        }
    }

}
