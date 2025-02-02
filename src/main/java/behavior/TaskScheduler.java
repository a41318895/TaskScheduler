package behavior;

import entity.Job;

/**
 * <b> Task Scheduler Trigger Common Behavior Interface </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
public interface TaskScheduler {

    void setToJobQueue(Job job) ;

    void shutdown() ;
}
