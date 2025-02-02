import functionalInterface.InterruptedExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import service.ScheduleService;
import util.FormattedDateTimeUtil;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b> Enhanced Task Scheduler Test Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@Slf4j
public final class EnhancedTriggerUsageTest {

    public static void main(String[] args) {

        String text = """
                
                1 -> Test Basic Scheduling
                2 -> Test Priority Level Scheduling
                3 -> Test Task Retry Mechanism
                4 -> Test Task Resource Limits
                5 -> Test Task Timeout
                """ ;
        log.info(text) ;

        Scanner scanner = new Scanner(System.in) ;
        System.out.println("Choose the test method to test : ") ;
        if (scanner.hasNextInt()) {

            int nextIntValue = scanner.nextInt() ;

            switch (nextIntValue) {

                case 1 -> testBasicScheduling() ;
                case 2 -> testPriorityLevelScheduling() ;
                case 3 -> testTaskRetryMechanism() ;
                case 4 -> testTaskResourceLimits() ;
                case 5 -> testTaskTimeout() ;
                default -> throw new IllegalStateException("Unexpected value: " + nextIntValue) ;
            }
        }
    }

    /**
     * <b> Test basic task scheduling </b>
     */
    private static void testBasicScheduling() {

        log.info("Starting basic task scheduling test - {}", FormattedDateTimeUtil.getWithBrackets()) ;

        // Schedule the simple task with different delay times
        ScheduleService.enhancedSchedule(() -> logExecutionTimeInternal(1), 1000, 1, 0) ;
        ScheduleService.enhancedSchedule(() -> logExecutionTimeInternal(2), 2000, 1, 0) ;

        shutdownScheduledTasksWithinIndicatedTimeSecond(10) ;
    }
    private static void logExecutionTimeInternal(int delayTimeSecond) {

        log.info("This simple task ({} sec a time) executed at: {}", delayTimeSecond, FormattedDateTimeUtil.getWithBrackets()) ;
    }

    /**
     * <b> Test task priority-based scheduling </b>
     */
    private static void testPriorityLevelScheduling() {

        log.info("Starting task priority scheduling test - {}", FormattedDateTimeUtil.getWithBrackets()) ;

        // Create tasks with different priorities
        Runnable highPriorityTask = () -> {

            log.info("High priority task executed at: {}", FormattedDateTimeUtil.getWithBrackets()) ;
        } ;

        Runnable lowPriorityTask = () -> {

            log.info("Low priority task executed at: {}", FormattedDateTimeUtil.getWithBrackets()) ;
        } ;

        // Schedule tasks with same delay but different priorities
        ScheduleService.enhancedSchedule(highPriorityTask, 1000, 1, 0) ;
        ScheduleService.enhancedSchedule(lowPriorityTask, 1000, 5, 0) ;

        shutdownScheduledTasksWithinIndicatedTimeSecond(10) ;
    }

    /**
     * <b> Test task retry mechanism </b>
     */
    private static void testTaskRetryMechanism() {

        log.info("Starting task retry mechanism test - {}",FormattedDateTimeUtil.getWithBrackets()) ;

        // Create a task that fails on first attempts
        AtomicInteger attempts = new AtomicInteger(0) ;

        Runnable failingTask = () -> {

            int currentAttempt = attempts.incrementAndGet() ;

            if (currentAttempt <= 2) { // Fail first two attempts

                log.info("Task failing attempt {} at: {}", currentAttempt, FormattedDateTimeUtil.getWithBrackets()) ;
                throw new RuntimeException("Simulated failure") ;
            }
            log.info("Task succeeded on attempt {} at: {}", currentAttempt, FormattedDateTimeUtil.getWithBrackets()) ;
        } ;

        // Schedule task with retry mechanism
        ScheduleService.enhancedSchedule(failingTask, 1000, 1, 2) ;

        shutdownScheduledTasksWithinIndicatedTimeSecond(10) ;
    }

    /**
     * <b> Test task resource limits </b>
     */
    private static void testTaskResourceLimits() {

        log.info("Starting task resource limits test - {}", FormattedDateTimeUtil.getWithBrackets()) ;

        // Create multiple long-running tasks
        Runnable longRunningTask = () -> execute(() -> {

            log.info("Long running task started at: {}", FormattedDateTimeUtil.getWithBrackets()) ;
            Thread.sleep(3000) ;
            log.info("Long running task completed at: {}", FormattedDateTimeUtil.getWithBrackets()) ;
        });

        // Schedule multiple tasks simultaneously
        for (int i = 0 ; i < 10 ; i ++) {
            ScheduleService.enhancedSchedule(longRunningTask, 0, 1, 0) ;
        }

        shutdownScheduledTasksWithinIndicatedTimeSecond(35) ;
    }

    /**
     * <b> Test task timeout mechanism </b>
     */
    private static void testTaskTimeout() {

        log.info("Starting task timeout test - {}", FormattedDateTimeUtil.getWithBrackets()) ;

        // Create a task that runs longer than the timeout
        Runnable timeoutTask = () -> execute(() -> {

            log.info("Timeout task started at: {}", FormattedDateTimeUtil.getWithBrackets()) ;
            Thread.sleep(70000) ;

            log.info("This message should not appear...") ;
        });

        ScheduleService.enhancedSchedule(timeoutTask, 1000, 1, 0) ;

        shutdownScheduledTasksWithinIndicatedTimeSecond(75) ;
    }

    private static void shutdownScheduledTasksWithinIndicatedTimeSecond(long durationTimeSecond) {

        execute(() -> Thread.sleep(durationTimeSecond * 1000)) ;

        ScheduleService.getEnhancedTrigger().shutdown() ;
    }

    /**
     * <b> Exception Handler </b>
     *
     * @param handler functional interface to execute code block with throwing InterruptedException
     */
    private static void execute(InterruptedExceptionHandler handler) {

        try {

            handler.apply() ;
        } catch (InterruptedException exception) {

            log.error(exception.getMessage()) ;

            Thread.currentThread().interrupt() ;
        }
    }
}
