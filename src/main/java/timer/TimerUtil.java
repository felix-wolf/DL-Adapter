package timer;

import java.util.Timer;
import java.util.TimerTask;

public class TimerUtil {

    Timer timer;
    final int delayInMillis;
    int fireCounts = 0;

    public TimerUtil(int delayInMillis) {
        this.delayInMillis = delayInMillis;
    }

    /**
     * starts a timer, sends fire events to the passed interface
     * @param timerListener the listener for the fire events
     */
    public void startTimer(TimerListener timerListener) {
        System.out.println("startTimer");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireCounts++;
                timerListener.timerFired(fireCounts);
            }
        }, 0, delayInMillis);
    }

    /**
     * stops the timer
     */
    public void stopTimer() {
        fireCounts = 0;
        timer.cancel();
    }

}