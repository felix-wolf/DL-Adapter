package timer;

import java.util.Timer;
import java.util.TimerTask;

public class TimerUtil {

    Timer timer;
    int delayInMillis = 5000;
    int fireCounts = 0;

    public TimerUtil() {}

    public TimerUtil(int delayInMillis) {
        this.delayInMillis = delayInMillis;
    }

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

    public void stopTimer() {
        fireCounts = 0;
        timer.cancel();
    }

}
