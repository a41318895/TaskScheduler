package Component;

import behavior.TaskScheduler;
import entity.Job;
import functionalInterface.InterruptedExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import util.FormattedDateTimeUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * <b> Task Execution Trigger Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@Slf4j
public class Trigger implements TaskScheduler {

    private final ExecutorService executorService ;
    private final PriorityBlockingQueue<Job> jobQueue ;
    private final AtomicBoolean isRunning ;

    public Trigger() {

        this.executorService = Executors.newFixedThreadPool(6) ;
        this.jobQueue = new PriorityBlockingQueue<>() ;
        this.isRunning = new AtomicBoolean(true) ;
        Thread processingThread = createProcessingThread() ;
        processingThread.start() ;
        log.info("The Trigger was executed at: {}", FormattedDateTimeUtil.getWithBrackets()) ;
    }

    private Thread createProcessingThread() {

        return new Thread(() -> {

            while(isRunning.get()) {

                execute(() -> {

                    Job job = jobQueue.take() ;

                    long currentTime = System.currentTimeMillis() ;

                    if (job.getStartTime() <= currentTime) {

                        executorService.execute(job.getTask()) ;

                        Job nextJob = new Job(job.getTask(), job.getDelay()) ;
                        jobQueue.offer(nextJob) ;
                    } else {

                        jobQueue.offer(job) ;
                        LockSupport.parkUntil(job.getStartTime()) ;
                    }
                }) ;
            }
        }) ;
    }

    @Override
    public void setToJobQueue(Job job) {

        jobQueue.offer(job) ;
    }

    public void shutdown() {

        isRunning.set(false) ;

        Thread.currentThread().interrupt() ;

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
     * @param handler functional interface to execute code block with throwing InterruptedException
     */
    private void execute(InterruptedExceptionHandler handler) {

        try {

            handler.apply() ;
        } catch (InterruptedException exception) {

            log.error(exception.getMessage()); ;

            executorService.shutdownNow() ;

            Thread.currentThread().interrupt() ;
        }
    }
}
