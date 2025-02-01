import Component.Trigger;
import functionalInterface.InterruptedExceptionSupplier;
import service.ScheduleService;
import util.FormattedDateTimeUtil;

import java.util.logging.Logger;

/**
 * <b> Task Scheduler Test Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/01
 */
public class Test {

    private static final Logger LOGGER = Logger.getLogger(Test.class.getName()) ;

    /**
     * <b> The Testing Method </b>
     *
     * <p> Add two scheduled tasks to job queue to wait for executing within 10 seconds expiration </p>
     */
    public static void main(String[] args) {

        ScheduleService.schedule(() -> printMessage(1), 1000) ;

        ScheduleService.schedule(() -> printMessage(2), 2000) ;

        execute(() -> Thread.sleep(10000)) ;
        Trigger.shutdown() ;
    }

    /**
     * <b> Print Message Task </b>
     *
     * @param delayTime The delayTime sign to print into message payload
     */
    private static void printMessage(long delayTime) {

        System.out.println(FormattedDateTimeUtil.getWithBrackets() + " - 這是 " + delayTime + " 秒一次的任務") ;
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
        }
    }
}
