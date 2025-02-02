package functionalInterface;

/**
 * <b> InterruptedException supplier Functional Interface </b>
 *
 * @author Aki Chou
 * @date 2025/02/02
 */
@FunctionalInterface
public interface InterruptedExceptionSupplier<T> {

    T get() throws InterruptedException ;
}
