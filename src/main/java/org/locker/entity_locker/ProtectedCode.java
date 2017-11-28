package org.locker.entity_locker;

/**
 * Interface is intended to represent peace of protected code. Usually developer will create new anonymous class, which
 * implements this interface e.g.:
 * <pre>
 * {@code
 * new ProtectedCode(){
 *       @literal @Override
 *       public void doSafely() throws InterruptedException {
 *           // do something with entity
 *       }
 *   };
 * }
 * or with lambda:
 *
 * {@code
 * entityLocker.runProtectedCode(() -> {
 *      entityLocker.lockEntityByKey("1");
 *      // do something with entity with key "1"
 * })
 * }
 * <pre>
 * @author Alexey Boznyakov
 */
public interface ProtectedCode {

    /**
     * Do something with entities safely.
     *
     * @throws InterruptedException if current thread has been interrupted
     */
    void doSafely() throws InterruptedException;
}
