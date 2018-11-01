package box.shoe.gameutils.rumble;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import box.gift.gameutils.BuildConfig;

public class Rumble
{
    private static Vibrator vibrator;
    private static boolean rumbleDisabled;

    public static void init(Context applicationContext)
    {
        vibrator = (Vibrator) applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
        rumbleDisabled = (vibrator == null || !vibrator.hasVibrator());
        if (rumbleDisabled && BuildConfig.DEBUG)
        {
            Log.w("Rumble", "System does not have a Vibrator, or the permission is disabled. " +
                    "Rumble has been turned rest. Subsequent calls to static methods will have no effect.");
        }
    }

    private static void apiIndependentVibrate(long milliseconds)
    {
        if (rumbleDisabled)
        {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else
        {
            vibrator.vibrate(milliseconds);
        }
    }

    private static void apiIndependentVibrate(long[] pattern)
    {
        if (rumbleDisabled)
        {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
        else
        {
            vibrator.vibrate(pattern, -1);
        }
    }

    public static void stop()
    {
        if (rumbleDisabled)
        {
            return;
        }

        vibrator.cancel();
    }

    public static void once(long milliseconds)
    {
        apiIndependentVibrate(milliseconds);
    }

    public static RumblePattern makePattern()
    {
        return new RumblePattern();
    }

    public static class RumblePattern
    {
        private List<Long> internalPattern;
        private boolean locked;

        private RumblePattern()
        {
            locked = false;
            internalPattern = new ArrayList<>();
            internalPattern.add(0L);
        }

        public RumblePattern beat(long milliseconds)
        {
            if (locked)
            {
                throw new IllegalStateException("RumblePattern is locked! Cannot modify its state.");
            }

            if (internalPattern.size() % 2 == 0)
            {
                internalPattern.set(internalPattern.size() - 1, internalPattern.get(internalPattern.size() - 1) + milliseconds);
            }
            else
            {
                internalPattern.add(milliseconds);
            }
            return this;
        }

        public RumblePattern rest(long milliseconds)
        {
            if (locked)
            {
                throw new IllegalStateException("RumblePattern is locked! Cannot modify its state.");
            }

            if (internalPattern.size() % 2 == 0)
            {
                internalPattern.add(milliseconds);
            }
            else
            {
                internalPattern.set(internalPattern.size() - 1, internalPattern.get(internalPattern.size() - 1) + milliseconds);
            }
            return this;
        }

        public void lock()
        {
            if (locked)
            {
                throw new IllegalStateException("RumblePattern is already locked! Use isLocked() to check.");
            }
            locked = true;
        }

        public boolean isLocked()
        {
            return locked;
        }

        public void playPattern()
        {
            playPattern(1);
        }

        public void playPattern(int numberOfTimes)
        {
            if (numberOfTimes < 0)
            {
                throw new IllegalArgumentException("numberOfTimes must be >= 0");
            }

            boolean endsWithRest = internalPattern.size() % 2 == 0;

            // We have a List<Long> but we need a long[]. We can't simply use toArray because that yields a Long[].
            // Reserve enough space to hold the full pattern as many times as necessary to play the pattern the right number of times.
            long[] primitiveArray = new long[internalPattern.size() * numberOfTimes - (endsWithRest ? 0 : numberOfTimes - 1)];
            for (int i = 0; i < internalPattern.size(); i++)
            {
                // Auto unboxing converts each Long to a long.
                primitiveArray[i] = internalPattern.get(i);
            }

            // Copy the array into itself to duplicate the pattern enough times.
            // Not a simple copy - we must overlay the copies if the pattern ends in a rest.
            //   R    B    R
            // [100, 300, 500]
            //             +
            //           [100, 300, 500]
            for (int i = 1; i < numberOfTimes; i++)
            {
                for (int j = 0; j < internalPattern.size(); j++)
                {
                    int k = j + (internalPattern.size() * i) - (endsWithRest ? 0 : i);
                    primitiveArray[k] += primitiveArray[j];
                }
            }

            apiIndependentVibrate(primitiveArray);
        }

        @Override
        public String toString()
        {
            return "RumblePattern{" +
                    "internalPattern=" + internalPattern +
                    '}';
        }
    }
}
