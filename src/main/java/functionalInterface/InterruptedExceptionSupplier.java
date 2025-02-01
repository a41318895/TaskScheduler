package functionalInterface;

/**
 * <b> InterruptedException handler Functional Interface </b>
 *
 * @author Aki Chou
 * @date 2025/02/01
 */
@FunctionalInterface
public interface InterruptedExceptionSupplier {

    void apply() throws InterruptedException ;
}
