package entity;

import lombok.Data;

/**
 * <b> Task Execution Statistics Entity Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@Data
public class TaskExecutionStatistics {

    private long totalExecutionTime ;
    private int successCounter ;
    private int failCounter ;
    private int retryCounter ;

    /**
     * <p> To record the whole execution status </p>
     *
     * @param success The flag to represent was the task successfully executed
     * @param executionTime The execution time of the single task
     * @param retryCounter The retry times of the single task
     */
    public void recordTaskExecution(boolean success, long executionTime, int retryCounter) {

        this.totalExecutionTime += executionTime ;

        if (success) successCounter ++ ;
        else failCounter ++ ;

        this.retryCounter += retryCounter ;
    }
}
