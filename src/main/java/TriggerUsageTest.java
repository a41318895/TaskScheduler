import functionalInterface.InterruptedExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import service.ScheduleService;
import util.FormattedDateTimeUtil;

/**
 * <b> Task Scheduler Test Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@Slf4j
public final class TriggerUsageTest {

    public static void main(String[] args) {

        ScheduleService.schedule(() -> printMessage(1), 1000) ;

        ScheduleService.schedule(() -> printMessage(2), 2000) ;

        execute(() -> Thread.sleep(10000)) ;
        ScheduleService.getBasicTrigger().shutdown() ;
    }

    private static void printMessage(long delayTime) {

        log.info("這是 {} 秒一次的任務 - {}", delayTime, FormattedDateTimeUtil.getWithBrackets()) ;
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
        }
    }
}
