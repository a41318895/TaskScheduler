package Component;

import behavior.TaskScheduler;
import entity.EnhancedJob;
import entity.Job;
import enumeration.TaskStatusEnum;
import functionalInterface.InterruptedExceptionHandler;
import functionalInterface.FutureClassGetMethodHandler;
import lombok.extern.slf4j.Slf4j;
import util.FormattedDateTimeUtil;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * <b> Task Execution Enhanced Trigger Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@Slf4j
public class EnhancedTrigger implements TaskScheduler {

    private final ThreadPoolExecutor executorService ;
    private final PriorityBlockingQueue<EnhancedJob> enhancedJobQueue ;
    private final AtomicBoolean isRunning ;
    private final Thread processingThread ;
    private final TaskResourceManager taskResourceManager ;
    private EnhancedJob lastExecutedJob ;

    private static Future<?> future = new CompletableFuture<>() ;
    private static final long TASK_TIMEOUT = 60000 ;

    public EnhancedTrigger() {

        this.executorService = createExecutorService() ;
        this.enhancedJobQueue = new PriorityBlockingQueue<>() ;
        this.isRunning = new AtomicBoolean(true) ;
        this.taskResourceManager = new TaskResourceManager(Runtime.getRuntime().availableProcessors()) ;
        this.processingThread = createProcessingThread() ;
        this.processingThread.start() ;
        log.info("The Enhanced Trigger was executed at: {}", FormattedDateTimeUtil.getWithBrackets()) ;
    }

    private ThreadPoolExecutor createExecutorService() {

        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(), // Core Thread Number
                Runtime.getRuntime().availableProcessors() * 2, // Max Thread Number
                60L, // Free Thread Lifetime
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), // Used Queue Type
                new ThreadFactory() {

                    private final AtomicInteger threadNumber = new AtomicInteger(1) ;

                    @Override
                    public Thread newThread(Runnable runnable) {

                        Thread thread = new Thread(runnable) ;
                        thread.setName("EnhancedTask - " + threadNumber.getAndIncrement()) ;
                        thread.setDaemon(false) ;

                        return thread ;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        ) ;
    }

    private Thread createProcessingThread() {

        return new Thread(() -> {

            while (isRunning.get()) {

                handleInterrupt(() -> {

                    EnhancedJob enhancedJob = enhancedJobQueue.take() ;

                    long currentTime = System.currentTimeMillis() ;

                    if (enhancedJob.getStartTime() <= currentTime) {

                        executeWithRetry(enhancedJob) ;
                    } else {

                        enhancedJobQueue.offer(enhancedJob) ;
                        LockSupport.parkUntil(enhancedJob.getStartTime()) ;
                    }
                }) ;
            }
        }) ;
    }

    public void executeWithRetry(EnhancedJob enhancedJob) {

        if (taskResourceManager.acquireTaskResource(5, TimeUnit.SECONDS)) {

            try {

                long startTime = System.currentTimeMillis() ;
                future = executorService.submit(() -> {

                    Thread.currentThread().setName("Task - " + enhancedJob.getPriorityLevel() + " - " + startTime) ;

                    enhancedJob.getTask().run() ;
                }) ;

                execute(() -> {

                   future.get(TASK_TIMEOUT, TimeUnit.MILLISECONDS) ;

                   enhancedJob.setTaskStatus(TaskStatusEnum.COMPLETED) ;
                   enhancedJob.setExecutionTime(System.currentTimeMillis() - startTime) ;

                    EnhancedJob nextJob = createNextJobInternal(enhancedJob) ;

                    enhancedJobQueue.offer(nextJob) ;

                }, enhancedJob) ;


            } finally {
                taskResourceManager.releaseTaskResource() ;
            }
        }
    }
    private EnhancedJob createNextJobInternal(EnhancedJob currentJob) {

        if (enhancedJobQueue.isEmpty()) {

            return new EnhancedJob(
                    currentJob.getTask(),
                    currentJob.getDelay(),
                    currentJob.getPriorityLevel(),
                    currentJob.getMaxRetryCounter()
            ) ;
        }

        EnhancedJob highestPriorityJob = enhancedJobQueue.peek() ;

        if (currentJob.getPriorityLevel() < highestPriorityJob.getPriorityLevel()) {

            return new EnhancedJob(
                    highestPriorityJob.getTask(),
                    highestPriorityJob.getDelay(),
                    highestPriorityJob.getPriorityLevel(),
                    highestPriorityJob.getMaxRetryCounter()
            ) ;
        }

        return new EnhancedJob(
                currentJob.getTask(),
                currentJob.getDelay(),
                currentJob.getPriorityLevel(),
                currentJob.getMaxRetryCounter()
        ) ;
    }

    @Override
    public void setToJobQueue(Job enhancedJob) {

        enhancedJobQueue.offer((EnhancedJob) enhancedJob) ;
    }

    @Override
    public void shutdown() {

        isRunning.set(false) ;

        processingThread.interrupt() ;

        executorService.shutdown() ;

        handleInterrupt(() -> {

            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {

                executorService.shutdownNow() ;
            }
        }) ;
    }

    private void handleTaskFail(EnhancedJob enhancedJob, String errorMessage) {

        enhancedJob.setErrorMessage(errorMessage) ;

        if (enhancedJob.getRetryCounter() < enhancedJob.getMaxRetryCounter()) {

            enhancedJob.setRetryCounter(enhancedJob.getRetryCounter() + 1) ;
            enhancedJob.setTaskStatus(TaskStatusEnum.PENDING) ;

            long retryDelay = enhancedJob.getDelay() * (long) Math.pow(2, enhancedJob.getRetryCounter()) ;

            enhancedJobQueue.offer(new EnhancedJob(
                    enhancedJob.getTask(),
                    retryDelay,
                    enhancedJob.getPriorityLevel(),
                    enhancedJob.getMaxRetryCounter()
            )) ;
        } else {

            enhancedJob.setTaskStatus(TaskStatusEnum.FAILED) ;
        }
    }

    /**
     * <b> Exception Handler </b>
     *
     * @param handler functional interface to execute code block with throwing Exceptions
     * @param enhancedJob entity param offers to handleTaskFail() method
     */
    private void execute(FutureClassGetMethodHandler handler, EnhancedJob enhancedJob) {

        try {

            handler.apply() ;
        } catch (InterruptedException | ExecutionException e) {

            log.error(e.getMessage()) ;

            handleTaskFail(enhancedJob, e.getMessage()) ;
        } catch (TimeoutException te) {

            log.error(te.getMessage()) ;

            future.cancel(true) ;

            handleTaskFail(enhancedJob, "Task Timeout") ;
        }
    }

    /**
     * <b> Exception Handler </b>
     *
     * @param handler functional interface to execute code block with throwing Interrupted Exceptions
     */
    private void handleInterrupt(InterruptedExceptionHandler handler) {

        try {

            handler.apply() ;
        } catch (InterruptedException e) {

            log.error(e.getMessage()) ;

            executorService.shutdownNow() ;

            Thread.currentThread().interrupt() ;
        }
    }
}
