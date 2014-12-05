/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.camera.one;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.view.Surface;

import com.android.camera.burst.BurstConfiguration;
import com.android.camera.burst.ResultsAccessor;
import com.android.camera.session.CaptureSession;
import com.android.camera.util.Size;

import java.io.File;

/**
 * OneCamera is a camera API tailored around our Google Camera application
 * needs. It's not a general purpose API but instead offers an API with exactly
 * what's needed from the app's side.
 */
public interface OneCamera {

    /** Which way the camera is facing. */
    public static enum Facing {
        FRONT, BACK;
    }

    /**
     * Auto focus system status; 1:1 mapping from camera2 AF_STATE.
     * <ul>
     * <li>{@link #INACTIVE}</li>
     * <li>{@link #ACTIVE_SCAN}</li>
     * <li>{@link #ACTIVE_FOCUSED}</li>
     * <li>{@link #ACTIVE_UNFOCUSED}</li>
     * <li>{@link #PASSIVE_SCAN}</li>
     * <li>{@link #PASSIVE_FOCUSED}</li>
     * <li>{@link #PASSIVE_UNFOCUSED}</li>
     * </ul>
     */
    public static enum AutoFocusState {
        /** Indicates AF system is inactive for some reason (could be an error). */
        INACTIVE,
        /** Indicates active scan in progress. */
        ACTIVE_SCAN,
        /** Indicates active scan success (in focus). */
        ACTIVE_FOCUSED,
        /** Indicates active scan failure (not in focus). */
        ACTIVE_UNFOCUSED,
        /** Indicates passive scan in progress. */
        PASSIVE_SCAN,
        /** Indicates passive scan success (in focus). */
        PASSIVE_FOCUSED,
        /** Indicates passive scan failure (not in focus). */
        PASSIVE_UNFOCUSED
    }

    /**
     * Auto focus system mode.
     * <ul>
     * <li>{@link #CONTINUOUS_PICTURE}</li>
     * <li>{@link #AUTO}</li>
     * </ul>
     */
    public static enum AutoFocusMode {
        /** System is continuously focusing. */
        CONTINUOUS_PICTURE,
        /** System is running a triggered scan. */
        AUTO
    }

    /**
     * Classes implementing this interface will be called when the camera was
     * opened or failed to open.
     */
    public static interface OpenCallback {
        /**
         * Called when the camera was opened successfully.
         *
         * @param camera the camera instance that was successfully opened
         */
        public void onCameraOpened(OneCamera camera);

        /**
         * Called if opening the camera failed.
         */
        public void onFailure();

        /**
         * Called if the camera is closed or disconnected while attempting to
         * open.
         */
        public void onCameraClosed();
    }

    /**
     * Classes implementing this interface will be called when the camera was
     * closed.
     */
    public static interface CloseCallback {
        /** Called when the camera was fully closed. */
        public void onCameraClosed();
    }

    /**
     * Classes implementing this interface can be informed when we're ready to
     * take a picture of if setting up the capture pipeline failed.
     */
    public static interface CaptureReadyCallback {
        /** After this is called, the system is ready for capture requests. */
        public void onReadyForCapture();

        /**
         * Indicates that something went wrong during setup and the system is
         * not ready for capture requests.
         */
        public void onSetupFailed();
    }

    /**
     * Classes implementing this interface can be informed when the state of
     * capture changes.
     */
    public static interface ReadyStateChangedListener {
        /**
         * Called when the camera is either ready or not ready to take a picture
         * right now.
         */
        public void onReadyStateChanged(boolean readyForCapture);
    }

    /**
     * A class implementing this interface can be passed into the call to take a
     * picture in order to receive the resulting image or updated about the
     * progress.
     */
    public static interface PictureCallback {
        /**
         * Called near the the when an image is being exposed for cameras which
         * are exposing a single frame, so that a UI can be presented for the
         * capture.
         */
        public void onQuickExpose();

        /**
         * Called when a thumbnail image is provided before the final image is
         * finished.
         */
        public void onThumbnailResult(byte[] jpegData);

        /**
         * Called when the final picture is done taking
         *
         * @param session the capture session
         */
        public void onPictureTaken(CaptureSession session);

        /**
         * Called when the picture has been saved to disk.
         *
         * @param uri the URI of the stored data.
         */
        public void onPictureSaved(Uri uri);

        /**
         * Called when picture taking failed.
         */
        public void onPictureTakingFailed();

        /**
         * Called when capture session is reporting a processing update. This
         * should only be called by capture sessions that require the user to
         * hold still for a while.
         *
         * @param progress a value from 0...1, indicating the current processing
         *            progress.
         */
        public void onTakePictureProgress(float progress);
    }

    /**
     * Classes implementing this interface will be called when the state of the
     * focus changes. Guaranteed not to stay stuck in scanning state past some
     * reasonable timeout even if Camera API is stuck.
     */
    public static interface FocusStateListener {
        /**
         * Called when state of auto focus system changes.
         *
         * @param state Current auto focus state.
         * @param frameNumber Frame number if available.
         */
        public void onFocusStatusUpdate(AutoFocusState state, long frameNumber);
    }

    /**
     * Classes implementing this interface will be called when the focus
     * distance of the physical lens changes.
     */
    public static interface FocusDistanceListener {
        /**
         * Called when physical lens distance on the camera changes.
         *
         * @param diopter the lens diopter from the last known position.
         * @param isActive whether the lens is moving.
         */
        public void onFocusDistance(float diopter, boolean isActive);
    }

    /**
     * Single instance of the current camera AF state.
     */
    public static class FocusState {
        public final float diopter;
        public final boolean isActive;

        public FocusState(float diopter, boolean isActive) {
            this.diopter = diopter;
            this.isActive = isActive;
        }
    }

    /**
     * Parameters to be given to capture requests.
     */
    public static abstract class CaptureParameters {
        /** The title/filename (without suffix) for this capture. */
        public final String title;

        /** The device orientation so we can compute the right JPEG rotation. */
        public final int orientation;

        /** The location of this capture. */
        public final Location location;

        /** Set this to provide a debug folder for this capture. */
        public final File debugDataFolder;

        public CaptureParameters(String title, int orientation, Location location, File
                debugDataFolder) {
            this.title = title;
            this.orientation = orientation;
            this.location = location;
            this.debugDataFolder = debugDataFolder;
        }
    }

    /**
     * Parameters to be given to photo capture requests.
     */
    public static class PhotoCaptureParameters extends CaptureParameters {
        /**
         * Flash modes.
         * <p>
         * Has to be in sync with R.arrays.pref_camera_flashmode_entryvalues.
         */
        public static enum Flash {
            AUTO, OFF, ON
        }

        /** Called when the capture is completed or failed. */
        public final PictureCallback callback;
        /** The heading of the device at time of capture. In degrees. */
        public final int heading;
        /** Flash mode for this capture. */
        public final Flash flashMode;
        /** Zoom value. */
        public final float zoom;
        /** Timer duration in seconds or 0 for no timer. */
        public final float timerSeconds;

        public PhotoCaptureParameters(String title, int orientation, Location location, File
                debugDataFolder, PictureCallback callback, int heading,
                Flash flashMode, float zoom, float timerSeconds) {
            super(title, orientation, location, debugDataFolder);
            this.callback = callback;
            this.heading = heading;
            this.flashMode = flashMode;
            this.zoom = zoom;
            this.timerSeconds = timerSeconds;
        }
    }

    /**
     * The callback to be invoked when results are available.
     */
    public interface BurstResultsCallback {
        void onBurstComplete(ResultsAccessor resultAccessor);
    }

    /**
     * Parameters to be given to burst requests.
     */
    public static class BurstParameters extends CaptureParameters {
        /** The title/filename (without suffix) for this capture. */
        public final BurstConfiguration burstConfiguration;
        public final BurstResultsCallback callback;

        public BurstParameters(String title, int orientation, Location location,
                File debugDataFolder, BurstConfiguration burstConfiguration,
                BurstResultsCallback callback) {
            super(title, orientation, location, debugDataFolder);
            this.burstConfiguration = burstConfiguration;
            this.callback = callback;
        }
    }

    /**
     * Meters and triggers auto focus scan with ROI around tap point.
     * <p/>
     * Normalized coordinates are referenced to portrait preview window with
     * (0, 0) top left and (1, 1) bottom right. Rotation has no effect.
     *
     * @param nx normalized x coordinate.
     * @param ny normalized y coordinate.
     */
    public void triggerFocusAndMeterAtPoint(float nx, float ny);

    /**
     * Call this to take a picture.
     *
     * @param params parameters for taking pictures.
     * @param session the capture session for this picture.
     */
    public void takePicture(PhotoCaptureParameters params, CaptureSession session);

    /**
     * Call this to take a burst.
     *
     * @param params parameters for taking burst.
     * @param session the capture session for this burst.
     */

    public void startBurst(BurstParameters params, CaptureSession session);

    /**
     * Call this to stop taking burst.
     *
     */
    public void stopBurst();

    /**
     * Sets or replaces a listener that is called whenever the focus state of
     * the camera changes.
     */
    public void setFocusStateListener(FocusStateListener listener);

    /**
     * Sets or replaces a listener that is called whenever the focus state of
     * the camera changes.
     */
    public void setFocusDistanceListener(FocusDistanceListener listener);

    /**
     * Sets or replaces a listener that is called whenever the state of the
     * camera changes to be either ready or not ready to take another picture.
     */
    public void setReadyStateChangedListener(ReadyStateChangedListener listener);

    /**
     * Starts a preview stream and renders it to the given surface.
     * @param Surface the surface on which to render preview frames
     *                @param listener
     */
    public void startPreview(Surface surface, CaptureReadyCallback listener);

    /**
     * Closes the camera.
     *
     * @param closeCallback Optional. Called as soon as the camera is fully
     *            closed.
     */
    public void close(CloseCallback closeCallback);

    /**
     * @return A list of all supported preview resolutions.
     */
    public Size[] getSupportedPreviewSizes();

    /**
     * @return The aspect ratio of the full size capture (usually the native
     *         resolution of the camera).
     */
    public float getFullSizeAspectRatio();

    /**
     * @return The direction of the camera.
     */
    public Facing getDirection();

    /**
     * Get the maximum zoom value.
     *
     * @return A float number to represent the maximum zoom value(>= 1.0).
     */
    public float getMaxZoom();

    /**
     * This function sets the current zoom ratio value.
     * <p>
     * The zoom range must be [1.0, maxZoom]. The maxZoom can be queried by
     * {@link #getMaxZoom}.
     *
     * @param zoom Zoom ratio value passed to scaler.
     */
    public void setZoom(float zoom);

    /**
     * Based on the selected picture size, this returns the best preview size.
     *
     * @param pictureSize the picture size as selected by the user. A camera
     *            might choose not to obey these and therefore the returned
     *            preview size might not match the aspect ratio of the given
     *            size.
     * @param context the android application context
     * @return The preview size that best matches the picture aspect ratio that
     *         will be taken.
     */
    public Size pickPreviewSize(Size pictureSize, Context context);
}
