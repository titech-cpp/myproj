import java.lang.Runnable;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;/**
 * executortest
 */
public class executortest {
    public static void main(final String[] args) {
        executor.submit(commands);
        return;
    }

    static Runnable commands = new Runnable() {

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {

                System.out.println(LocalTime.now());
            }
        }
    };
    static ExecutorService executor = Executors.newFixedThreadPool(30);
}