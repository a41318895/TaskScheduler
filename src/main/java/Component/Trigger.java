package Component;

import entity.Job;
import functionalInterface.InterruptedExceptionSupplier;
import util.FormattedDateTimeUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

/**
 * <b> Task Execution Trigger Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/01
 */
public class Trigger {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(6) ;
    private static final PriorityBlockingQueue<Job> jobQueue = new PriorityBlockingQueue<>() ;
    private static final Logger LOGGER = Logger.getLogger(Trigger.class.getName()) ;
    private static final AtomicBoolean isRunning = new AtomicBoolean(true) ;

    private static final Thread thread = new Thread(() -> {

        while(isRunning.get()) {

            execute(() -> {

                // If there's any job here, get it. Or, block the current thread until it has job.
                Job job = jobQueue.take() ;

                long currentTime = System.currentTimeMillis() ;

                if (job.getStartTime() <= currentTime) {

                    executorService.execute(job.getTask()) ;

                    Job nextJob = new Job(
                            job.getTask(),
                            job.getDelay()
                    ) ;
                    jobQueue.offer(nextJob) ;
                } else {

                    jobQueue.offer(job) ;
                    LockSupport.parkUntil(job.getStartTime()) ;
                }
            }) ;
        }
    }) ;

    static {
        thread.start() ;
        System.out.println("The Trigger was executed at: " + FormattedDateTimeUtil.getWithBrackets()) ;
    }

    /**
     * <b> Add job to the job queue </b>
     *
     * @param job The job to add to job queue to wait for executing
     */
    public static void setToJobQueue(Job job) {

        jobQueue.offer(job) ;
    }

    /**
     * <b> Gracefully shutdown the thread and thread pool </b>
     */
    public static void shutdown() {

        isRunning.set(false) ;

        thread.interrupt() ;

        executorService.shutdown() ;

        execute(() -> {

            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {

                executorService.shutdownNow() ;
            }
        }) ;
    }

    /**
     * <b> Exception Handler </b>
     *
     * @param supplier functional interface to execute code block with throwing InterruptedException
     */
    private static void execute(InterruptedExceptionSupplier supplier) {

        try {

            supplier.apply() ;
        } catch (InterruptedException exception) {

            LOGGER.warning(exception.getMessage()) ;

            executorService.shutdownNow() ;

            Thread.currentThread().interrupt() ;
        }
    }
}
