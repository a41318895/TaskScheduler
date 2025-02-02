package service;

import Component.EnhancedTrigger;
import Component.Trigger;
import entity.EnhancedJob;
import entity.Job;

/**
 * <b> Schedule Service Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
public class ScheduleService {

    private static Trigger basicTrigger ;
    private static EnhancedTrigger enhancedTrigger ;

    private static final int PRIORITY_LEVEL_INIT = 1 ;
    private static final int MAX_RETRY_COUNTER_INIT = 0 ;

    // Singleton Static Getters :
    public static synchronized Trigger getBasicTrigger() {

        if (basicTrigger == null) basicTrigger = new Trigger() ;

        return basicTrigger ;
    }
    public static synchronized EnhancedTrigger getEnhancedTrigger() {

        if (enhancedTrigger == null) enhancedTrigger = new EnhancedTrigger() ;

        return enhancedTrigger ;
    }

    public static void schedule(Runnable task, long delay) {

        Job job = new Job(task, delay) ;

        getBasicTrigger().setToJobQueue(job) ;
    }

    public static void enhancedSchedule(Runnable task, long delay) {

        enhancedSchedule(task, delay, PRIORITY_LEVEL_INIT) ;
    }

    public static void enhancedSchedule(Runnable task, long delay, int priorityLevel) {

        enhancedSchedule(task, delay, priorityLevel, MAX_RETRY_COUNTER_INIT) ;
    }

    public static void enhancedSchedule(Runnable task, long delay, int priorityLevel, int maxRetryCounter) {

        EnhancedJob enhancedJob = new EnhancedJob(task, delay, priorityLevel, maxRetryCounter) ;

        getEnhancedTrigger().setToJobQueue(enhancedJob) ;
    }
}
