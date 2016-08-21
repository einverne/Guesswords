package info.einverne.guesswords.detector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by einverne on 8/17/16.
 */
public class ScreenFaceDetector implements SensorEventListener {

    private static final int DETAULT_ACCELERATION_THRESHOLD = 7;

    private int screenOrientationUpThreshold = 60;
    private int screenOrientationDownThreshold = 180 - screenOrientationUpThreshold;

    /**
     * Listener for screen face
     */
    public interface Listener {
        void FaceUp();

        void FaceDown();
    }

    private final Listener listener;
    private final SampleQueue queue = new SampleQueue();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    public ScreenFaceDetector(Listener listener) {
        this.listener = listener;
    }

    public boolean start(SensorManager sensorManager) {
        if (accelerometer != null) {
            return true;
        }

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (accelerometer != null && magnetometer != null) {
            this.sensorManager = sensorManager;
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
        return accelerometer != null && magnetometer != null;
    }

    public void stop() {
        if (accelerometer != null && magnetometer != null) {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnetometer);
            sensorManager = null;
            accelerometer = null;
            magnetometer = null;
        }
    }

    float[] inclineGravity = new float[3];
    float[] gravityValue;
    float[] geomagneticValue;
    float orientation[] = new float[3];
    float azimuth;
    float pitch;
    float roll;
    private ScreenFaceOrientation screenFaceOrientation = ScreenFaceOrientation.FaceVertical;
    private ScreenFaceOrientation lastScreenFaceOrientation = ScreenFaceOrientation.FaceVertical;

    public enum ScreenFaceOrientation {
        FaceUp,
        FaceDown,
        FaceVertical
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean faceChanged = isScreenFaceChange(event);
        long timestamp = event.timestamp;
        queue.add(timestamp, faceChanged);
        if (queue.isFaceChanging()) {
            changeScreenStatus(lastScreenFaceOrientation);
            queue.clear();
        }
    }

    private boolean isScreenFaceChange(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValue = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticValue = event.values;
        }
        if (gravityValue != null && geomagneticValue != null) {
            float[] R = new float[9];
            float[] I = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, gravityValue, geomagneticValue);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);

                roll = (float) Math.toDegrees(orientation[2]);
                final double magnitudeSquared = roll * roll;
                if (magnitudeSquared < screenOrientationUpThreshold * screenOrientationUpThreshold) {
                    lastScreenFaceOrientation = ScreenFaceOrientation.FaceUp;
                } else if (magnitudeSquared > screenOrientationDownThreshold * screenOrientationDownThreshold) {
                    lastScreenFaceOrientation = ScreenFaceOrientation.FaceDown;
                } else if (magnitudeSquared > 80 * 80 && magnitudeSquared < 100 * 100) {
                    screenFaceOrientation = ScreenFaceOrientation.FaceVertical;
                }
                return magnitudeSquared > screenOrientationDownThreshold * screenOrientationDownThreshold || magnitudeSquared < screenOrientationUpThreshold * screenOrientationUpThreshold;
            }
        }
        return false;
    }

    /**
     * only this function change screenFaceOrientation
     * If new Orientation is different from screenFaceOrientation, then change it
     * otherwise, do nothing
     * @param newOrientation new Orientation need to assign to screenFaceOrientation
     */
    private void changeScreenStatus(ScreenFaceOrientation newOrientation) {
        if (screenFaceOrientation != newOrientation) {
            screenFaceOrientation = newOrientation;
            switch (screenFaceOrientation) {
                case FaceUp:
                    listener.FaceUp();
                    break;
                case FaceDown:
                    listener.FaceDown();
                    break;
            }
        }
    }

    public boolean isTiltUpward() {
        if (gravityValue != null && geomagneticValue != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, gravityValue, geomagneticValue);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                /*
                * If the roll is positive, you're in reverse landscape (landscape right), and if the roll is negative you're in landscape (landscape left)
                *
                * Similarly, you can use the pitch to differentiate between portrait and reverse portrait.
                * If the pitch is positive, you're in reverse portrait, and if the pitch is negative you're in portrait.
                *
                * orientation -> azimut, pitch and roll
                */

                /*
                 * values[0]  表示Z轴的角度：方向角，我们平时判断的东西南北就是看这个数据的，
                 * 使用第一种方式获得方向（磁场+加速度）得到的数据范围是（-180～180）,也就是说，
                 * 0表示正北，90表示正东，180/-180表示正南，-90表示正西。
                 * values[1] pitch 倾斜角  即由静止状态开始，前后翻转，
                 * 手机顶部往上抬起（0~-90），手机尾部往上抬起（0~90）
                 * values[2] roll 旋转角 即由静止状态开始，左右翻转，
                 * 手机左侧抬起（0~90）,手机右侧抬起（0~-90）
                 */

                azimuth = orientation[0];               // 弧度
                Timber.d("azimuth" + Math.toDegrees(azimuth));
                pitch = orientation[1];
                Timber.d("pitch " + Math.toDegrees(pitch));
                roll = orientation[2];
                Timber.d("roll " + Math.toDegrees(roll));

                inclineGravity = gravityValue.clone();

                double norm_Of_g = Math.sqrt(inclineGravity[0] * inclineGravity[0] + inclineGravity[1] * inclineGravity[1] + inclineGravity[2] * inclineGravity[2]);

                // Normalize the accelerometer vector
                inclineGravity[0] = (float) (inclineGravity[0] / norm_Of_g);
                inclineGravity[1] = (float) (inclineGravity[1] / norm_Of_g);
                inclineGravity[2] = (float) (inclineGravity[2] / norm_Of_g);

                //Checks if device is flat on ground or not
                int inclination = (int) Math.round(Math.toDegrees(Math.acos(inclineGravity[2])));

                /*
                * Float obj1 = new Float("10.2");
                * Float obj2 = new Float("10.20");
                * int retval = obj1.compareTo(obj2);
                *
                * if(retval > 0) {
                * System.out.println("obj1 is greater than obj2");
                * }
                * else if(retval < 0) {
                * System.out.println("obj1 is less than obj2");
                * }
                * else {
                * System.out.println("obj1 is equal to obj2");
                * }
                */
                Float objPitch = new Float(pitch);
                Float objZero = new Float(0.0);
                Float objZeroPointTwo = new Float(0.2);
                Float objZeroPointTwoNegative = new Float(-0.2);

                int objPitchZeroResult = objPitch.compareTo(objZero);
                int objPitchZeroPointTwoResult = objZeroPointTwo.compareTo(objPitch);
                int objPitchZeroPointTwoNegativeResult = objPitch.compareTo(objZeroPointTwoNegative);

                if (roll < 0 && ((objPitchZeroResult > 0 && objPitchZeroPointTwoResult > 0) || (objPitchZeroResult < 0 && objPitchZeroPointTwoNegativeResult > 0)) && (inclination > 30 && inclination < 40)) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    public boolean isTiltDownward() {
        if (gravityValue != null && geomagneticValue != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, gravityValue, geomagneticValue);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                pitch = orientation[1];
                roll = orientation[2];

                inclineGravity = gravityValue.clone();

                double norm_Of_g = Math.sqrt(inclineGravity[0] * inclineGravity[0] + inclineGravity[1] * inclineGravity[1] + inclineGravity[2] * inclineGravity[2]);

                // Normalize the accelerometer vector
                inclineGravity[0] = (float) (inclineGravity[0] / norm_Of_g);
                inclineGravity[1] = (float) (inclineGravity[1] / norm_Of_g);
                inclineGravity[2] = (float) (inclineGravity[2] / norm_Of_g);

                //Checks if device is flat on groud or not
                int inclination = (int) Math.round(Math.toDegrees(Math.acos(inclineGravity[2])));

                Float objPitch = new Float(pitch);
                Float objZero = new Float(0.0);
                Float objZeroPointTwo = new Float(0.2);
                Float objZeroPointTwoNegative = new Float(-0.2);

                int objPitchZeroResult = objPitch.compareTo(objZero);
                int objPitchZeroPointTwoResult = objZeroPointTwo.compareTo(objPitch);
                int objPitchZeroPointTwoNegativeResult = objPitch.compareTo(objZeroPointTwoNegative);

                if (roll < 0 && ((objPitchZeroResult > 0 && objPitchZeroPointTwoResult > 0) || (objPitchZeroResult < 0 && objPitchZeroPointTwoNegativeResult > 0)) && (inclination > 140 && inclination < 170)) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Queue of samples. Keeps a running average.
     */
    static class SampleQueue {

        /**
         * Window size in ns. Used to compute the average.
         */
        private static final long MAX_WINDOW_SIZE = 500000000; // 0.5s
        private static final long MIN_WINDOW_SIZE = MAX_WINDOW_SIZE >> 1; // 0.25s

        /**
         * Ensure the queue size never falls below this size, even if the device
         * fails to deliver this many events during the time window. The LG Ally
         * is one such device.
         */
        private static final int MIN_QUEUE_SIZE = 4;

        private final SamplePool pool = new SamplePool();

        private Sample oldest;
        private Sample newest;
        private int sampleCount;
        private int acceleratingCount;

        /**
         * Adds a sample.
         *
         * @param timestamp    in nanoseconds of sample
         * @param accelerating true if up or down
         */
        void add(long timestamp, boolean accelerating) {
            // Purge samples that proceed window.
            purge(timestamp - MAX_WINDOW_SIZE);

            // Add the sample to the queue.
            Sample added = pool.acquire();
            added.timestamp = timestamp;
            added.accelerating = accelerating;
            added.next = null;
            if (newest != null) {
                newest.next = added;
            }
            newest = added;
            if (oldest == null) {
                oldest = added;
            }

            // Update running average.
            sampleCount++;
            if (accelerating) {
                acceleratingCount++;
            }
        }

        /**
         * Removes all samples from this queue.
         */
        void clear() {
            while (oldest != null) {
                Sample removed = oldest;
                oldest = removed.next;
                pool.release(removed);
            }
            newest = null;
            sampleCount = 0;
            acceleratingCount = 0;
        }

        /**
         * Purges samples with timestamps older than cutoff.
         */
        void purge(long cutoff) {
            while (sampleCount >= MIN_QUEUE_SIZE
                    && oldest != null && cutoff - oldest.timestamp > 0) {
                // Remove sample.
                Sample removed = oldest;
                if (removed.accelerating) {
                    acceleratingCount--;
                }
                sampleCount--;

                oldest = removed.next;
                if (oldest == null) {
                    newest = null;
                }
                pool.release(removed);
            }
        }

        /**
         * Copies the samples into a list, with the oldest entry at index 0.
         */
        List<Sample> asList() {
            List<Sample> list = new ArrayList<Sample>();
            Sample s = oldest;
            while (s != null) {
                list.add(s);
                s = s.next;
            }
            return list;
        }

        /**
         * Returns true if we have enough samples and all of those samples
         * are accelerating.
         */
        boolean isFaceChanging() {
            return newest != null
                    && oldest != null
                    && newest.timestamp - oldest.timestamp >= MIN_WINDOW_SIZE
                    && acceleratingCount >= (sampleCount >> 1) + (sampleCount >> 2);
        }
    }

    /**
     * An accelerometer sample.
     */
    static class Sample {
        /**
         * Time sample was taken.
         */
        long timestamp;

        /**
         * If up or down
         */
        boolean accelerating;

        /**
         * Next sample in the queue or pool.
         */
        Sample next;
    }

    /**
     * Pools samples. Avoids garbage collection.
     */
    static class SamplePool {
        private Sample head;

        /**
         * Acquires a sample from the pool.
         */
        Sample acquire() {
            Sample acquired = head;
            if (acquired == null) {
                acquired = new Sample();
            } else {
                // Remove instance from pool.
                head = acquired.next;
            }
            return acquired;
        }

        /**
         * Returns a sample to the pool.
         */
        void release(Sample sample) {
            sample.next = head;
            head = sample;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
