package com.effacy.jui.core.client.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.effacy.jui.platform.util.client.TimerSupport.ITimer;

public class DelayedValueHandlerTest {

    @Test
    public void configuration() throws Exception {
        LocalDelayedValueHandler<String> handler = new LocalDelayedValueHandler<>(v -> {});

        // TEST: The default threshold of 300ms and max count of 6.
        Assertions.assertEquals(6, handler.maxCount);
        Assertions.assertEquals(300, handler.threshold);

        // TEST: Assign a valid max count.
        handler.maxCount(4);
        Assertions.assertEquals(4, handler.maxCount);

        // TEST: Assign a max count beyond the maximum of 10.
        handler.maxCount(12);
        Assertions.assertEquals(10, handler.maxCount);

        // TEST: Assign a max count beyond the minimum of 1.
        handler.maxCount(0);
        Assertions.assertEquals(1, handler.maxCount);

        // TEST: Assign a valid threshod.
        handler.threshold(1000);
        Assertions.assertEquals(1000, handler.threshold);

        // TEST: Assign a threshod below the minimum of 10.
        handler.threshold(0);
        Assertions.assertEquals(10, handler.threshold);
    }

    @Test
    public void count() throws Exception {
        List<String> received = new ArrayList<>();
        LocalDelayedValueHandler<String> handler = new LocalDelayedValueHandler<>(v -> received.add(v));

        // Test threshold.
        handler.maxCount (4);
        handler.threshold(1000);
        handler.reset();

        handler.modified("a");
        Assertions.assertTrue(received.isEmpty());
        Thread.sleep(2);
        
        handler.modified("aa");
        Assertions.assertTrue(received.isEmpty());
        Thread.sleep(2);

        handler.modified("aaa");
        Assertions.assertTrue(received.isEmpty());
        Thread.sleep(2);

        handler.modified("aaaa");
        Assertions.assertTrue(received.isEmpty());
        Thread.sleep(2);

        handler.modified("aaaaa");
        Assertions.assertEquals(1, received.size());
        Assertions.assertEquals("aaaaa", received.get(0));
    }



    @Test
    public void threshold() throws Exception {
        List<String> received = new ArrayList<>();
        LocalDelayedValueHandler<String> handler = new LocalDelayedValueHandler<>(v -> received.add(v));

        // Test threshold.
        handler.maxCount (4);
        handler.threshold(10);
        handler.reset();
        
        handler.modified("aa");
        Assertions.assertTrue(received.isEmpty());

        // Sleep until after the threshold time.
        Thread.sleep(20);

        Assertions.assertEquals(1, received.size());
        Assertions.assertEquals("aa", received.get(0));
    }

    /**
     * Local version of the value handler that uses a Java timer.
     */
    public class LocalDelayedValueHandler<V> extends DelayedValueHandler<V> {

        public LocalDelayedValueHandler(Consumer<V> receiver) {
            super(receiver);
        
            timer =  new ITimer () {

                private Timer timer;

                @Override
                public void schedule(int millis) {
                    if (timer != null)
                        timer.cancel();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            fire();
                        }
                    
                    }, millis);
                }

                @Override
                public void repeat(int millis) {
                    // Nothing.
                }

                @Override
                public void cancel() {
                    if (timer != null)
                        timer.cancel();
                }

                @Override
                public void run() {
                    // Nothing.
                }
                
            };
        }
    }
}
