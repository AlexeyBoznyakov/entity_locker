package org.locker.entity_locker;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class is intended to lock entities by its keys in protected code.
 * The class is supposed to be used by the components that are responsible for managing storage and caching
 * of different type of entities in the application. EntityLocker itself does not deal with the entities, only with
 * the IDs (primary keys) of the entities.
 * <p>
 * Rules:
 * <p>
 * 1. EntityLocker support different types of entity IDs.
 * Only one instance for each type of key should be allowed. Therefore {@link EntityLockerFactory} class should be used.
 * <p>
 * 2. EntityLocker’s interface allow the caller to specify which entity does it want to work with (using entity ID),
 * and designate the boundaries of the code that should have exclusive access to the entity (called “protected code”).
 * The main common pattern of using this class:
 * <pre>
 * {@code
 * EntityLocker<String> entityLocker = EntityLockerFactory.getInstance().getEntityLocker(String.class);
 *      entityLocker.runProtectedCode(() -> {
 *      entityLocker.lockEntityByKey("1");
 *      // do something with entity with key "1"
 * })}
 * </pre>
 * 3. For any given entity, EntityLocker guarantee that at most one thread executes protected code on that entity.
 * If there’s a concurrent request to lock the same entity, the other thread should wait until the entity becomes available.
 * <p>
 * 4. EntityLocker allow concurrent execution of protected code on different entities.
 * <p>
 * 5. Nested protected code is forbidden
 * <p>
 * 6. Attempt to lock entity outside protected code is forbidden
 *
 * @param <T> type of entity key
 * @author Alexey Boznyakov
 */
@Log4j2
public class EntityLocker<T> {

    /**
     * Map for saving lock objects for each entity id.
     */
    private final ConcurrentMap<T, Lock> lockTable = new ConcurrentHashMap<>();

    /**
     * Thread local variable for saving information about current protected code.
     */
    private final ThreadLocal<ProtectedCodeInfo<T>> currentProtectedCodeInfo = new ThreadLocal<>();

    /**
     * Lock entity by key. The method should be called only from protected code. Otherwise {@link IllegalStateException}
     * will be thrown.
     *
     * @param entityKey entity key
     * @throws InterruptedException if current thread has been interrupted
     * @throws NullPointerException if entity key is null
     */
    public void lockEntityByKey(final @NonNull T entityKey) throws InterruptedException {
        log.debug("Thread {} is trying to lock entity with key {}", Thread.currentThread().getName(), entityKey);
        if (currentProtectedCodeInfo.get() == null) {
            throw new IllegalStateException("Attempt to lock entity outside protected code");
        }
        Lock lockForEntity = createNewLockForKey(entityKey);
        currentProtectedCodeInfo.get().addProtectedEntity(entityKey, lockForEntity);
    }

    /**
     * Create new lock for entity and persist it to {@link EntityLocker#lockTable}.
     * <p>
     * Implementation notes:
     * If  the {@link EntityLocker#lockTable} already contains a lock object, it means that there is another thread,
     * which locks entity with a certain key. In this case current thread should wait until other thread unlocks lock
     * object. Otherwise current thread creates new lock object and tries put it to {@link EntityLocker#lockTable}. If
     * {@link ConcurrentMap#putIfAbsent(Object, Object)} return not null object it means that other thread insert own
     * lock object faster than current. And current thread repeats locking process again until method
     * {@link ConcurrentMap#putIfAbsent(Object, Object)} doesn't return null.
     *
     * @param entityKey entity key
     * @return new created lock on entity
     * @throws InterruptedException if current thread has been interrupted
     * @throws NullPointerException if entity key is null
     */
    private Lock createNewLockForKey(final @NonNull T entityKey) throws InterruptedException {
        boolean repeatLocking = false;
        Lock currentLock = null;
        do {
            try {
                repeatLocking = false;
                currentLock = lockTable.get(entityKey);
                if (currentLock != null) {
                    log.debug("Lock for this entity with id {} already is acquired by another thread. Wait...",
                            entityKey);
                    currentLock.lockInterruptibly();
                    repeatLocking = true;
                } else {
                    log.debug("Create new lock object for entity with key {}", entityKey);
                    currentLock = new ReentrantLock();
                    currentLock.lock();
                    // not null object means that other thread insert own lock object faster than current
                    repeatLocking = lockTable.putIfAbsent(entityKey, currentLock) != null;
                }
            } finally {
                if (repeatLocking) {
                    log.debug("Repeat lock entity with id {}", entityKey);
                    currentLock.unlock();
                }
            }
        } while (repeatLocking);
        return currentLock;
    }

    /**
     * Run protected code safely. In protected code developer can locks some items by keys.
     *
     * @param protectedCode protected code, which should be executed
     * @throws InterruptedException if current thread has been interrupted
     * @throws IllegalStateException if there is attempt to run protected code inside another protected code
     */
    public void runProtectedCode(final @NonNull ProtectedCode protectedCode) throws InterruptedException {
        if (currentProtectedCodeInfo.get() != null) {
            throw new IllegalStateException("Nested protected code is forbidden");
        }

        currentProtectedCodeInfo.set(new ProtectedCodeInfo<>());
        try {
            log.debug("==================== Start protected code ====================");
            LocalDateTime startTime = LocalDateTime.now();

            // execute protected code
            protectedCode.doSafely();

            LocalDateTime endTime = LocalDateTime.now();
            log.debug("==================== End protected code. Execution time: {} seconds ====================",
                    ChronoUnit.SECONDS.between(startTime, endTime));
        } catch (Throwable ex) {
            log.error("Error during protected code execution", ex);
            throw ex;
        } finally {
            log.debug("Clear resources after protected code execution");
            clearResourcesAfterProtectedCodeExecution();
        }
    }

    /**
     * Clear resources after protected code execution e.g.
     * 1. remove locks from {@link EntityLocker#lockTable}
     * 2. unlock lock object.
     */
    private void clearResourcesAfterProtectedCodeExecution() {
        currentProtectedCodeInfo.get().getProtectedEntities().forEach(l -> {
            log.debug("Unlock lock object for entity with key {} and remove it from table", l.getEntityKey());
            lockTable.remove(l.getEntityKey());
            l.getLockForEntity().unlock();
        });
        currentProtectedCodeInfo.remove();
    }
}
