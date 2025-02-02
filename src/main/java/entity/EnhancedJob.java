package entity;

import enumeration.TaskStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <b> Enhanced Job Entity Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EnhancedJob extends Job {

    private final int priorityLevel ;
    private TaskStatusEnum taskStatus ;
    private int retryCounter ;
    private final int maxRetryCounter ;

    private long executionTime ;
    private String errorMessage ;

    public EnhancedJob(Runnable task, long delay, int priorityLevel, int maxRetryCounter) {

        super(task, delay) ;
        this.priorityLevel = priorityLevel ;
        this.taskStatus = TaskStatusEnum.PENDING ;
        this.maxRetryCounter = maxRetryCounter ;
        this.retryCounter = 0 ;
    }

    @Override
    public int compareTo(Job o) {

        if (o instanceof EnhancedJob enhancedJob) {

            int priorityLevelCompare = Integer.compare(this.priorityLevel, enhancedJob.getPriorityLevel()) ;

            return priorityLevelCompare != 0 ? priorityLevelCompare : super.compareTo(o) ;
        }

        return super.compareTo(o) ;
    }
}
