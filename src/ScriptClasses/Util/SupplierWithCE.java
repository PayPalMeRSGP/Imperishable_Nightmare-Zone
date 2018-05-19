package ScriptClasses.Util;
/*
This is needed to allow for a Supplier that can throw exceptions
such as InterruptedException.
 */
@FunctionalInterface
public interface SupplierWithCE<T, X extends Exception> {
    T get() throws X;
}