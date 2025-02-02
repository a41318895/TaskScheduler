package functionalInterface;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * <b> Future Class's get method handler Functional Interface </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@FunctionalInterface
public interface FutureClassGetMethodHandler {

    void apply() throws TimeoutException, InterruptedException, ExecutionException ;
}
