package Component;

import entity.TaskExecutionStatistics;
import functionalInterface.InterruptedExceptionSupplier;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.Map ;
import java.util.concurrent.TimeUnit;

/**
 * <b> Task Resource Manager Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@Slf4j
public class TaskResourceManager {

    private final Semaphore taskSemaphore ;
    private final Map<String, TaskExecutionStatistics> taskExecutionStatisticsMap = new ConcurrentHashMap<>() ;

    public TaskResourceManager(int maxConcurrentTaskCounter) {

        this.taskSemaphore = new Semaphore(maxConcurrentTaskCounter) ;
    }

    public boolean acquireTaskResource(long timeout, TimeUnit timeUnit) {

        return execute(() -> taskSemaphore.tryAcquire(timeout, timeUnit)) ;
    }

    public void releaseTaskResource() {

        taskSemaphore.release() ;
    }

    public void recordTaskExecution(String taskId, boolean success, long executionTime, int retryCounter) {

        taskExecutionStatisticsMap.computeIfAbsent(taskId, key -> new TaskExecutionStatistics())
                .recordTaskExecution(success, executionTime, retryCounter) ;
    }

    /**
     * <b> Exception Supplier </b>
     *
     * @param supplier functional interface to execute code block with throwing InterruptedException
     */
    private static <T> T execute(InterruptedExceptionSupplier<T> supplier) {

        try {

            return supplier.get() ;
        } catch (InterruptedException exception) {

            log.error(exception.getMessage()) ;

            throw new RuntimeException(exception) ;
        }
    }
}
