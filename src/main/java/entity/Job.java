package entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <b> Job Entity Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/01
 */
@Data
@NoArgsConstructor(force = true)
public class Job implements Comparable<Job> {

    private final Runnable task ;
    private final long startTime ;
    private final long delay ;

    public Job(Runnable task, long delay) {

        this.task = task ;
        this.startTime = System.currentTimeMillis() + delay ;
        this.delay = delay ;
    }

    @Override
    public int compareTo(Job job) {

        return Long.compare(this.startTime, job.getStartTime()) ;
    }
}
