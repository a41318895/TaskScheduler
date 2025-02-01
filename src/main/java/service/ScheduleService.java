package service;

import Component.Trigger;
import entity.Job;

/**
 * <b> Schedule Service Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/01
 */
public class ScheduleService {

    /**
     * <b> Schedule Task Method </b>
     *
     * @param task The task is going to add to job queue
     * @param delay The delay millisecond of executing task
     */
    public static void schedule(Runnable task, long delay) {

        // Creating a job to handle task
        Job job = new Job(
                task,
                delay
        ) ;

        Trigger.setToJobQueue(job) ;
    }
}
