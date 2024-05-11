package io.github.alfaio.afrpc.core.registry.af;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * check health for registry center
 *
 * @author LimMF
 * @since 2024/4/28
 **/
@Slf4j
public class AfHealthChecker {

    ScheduledExecutorService consumerExecutor = null;
    ScheduledExecutorService providerExecutor = null;


    public void start() {
        consumerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor = Executors.newScheduledThreadPool(1);
    }

    public void stop() {
        log.info(" ===> [AFRegistry] : stop health checker");
        gracefulShutDown(consumerExecutor);
        gracefulShutDown(providerExecutor);
    }

    @SneakyThrows
    private void gracefulShutDown(ScheduledExecutorService executor) {
        executor.shutdown();
        boolean isTerminated = executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        if (!isTerminated) {
            executor.shutdownNow();
        }
    }

    public void providerCheck(Callback callback) {
        providerExecutor.scheduleWithFixedDelay(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    public void consumerCheck(Callback callback) {
        consumerExecutor.scheduleWithFixedDelay(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }

    public interface Callback {
        void call() throws Exception;
    }

}
