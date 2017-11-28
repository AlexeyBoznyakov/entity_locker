package org.locker.entity_locker;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for creation entity lockers.
 *
 * @author Alexey Boznyakov
 */
@Log4j2
public class EntityLockerFactory {

    /**
     * Singleton instance of this factory.
     */
    private static EntityLockerFactory INSTANCE = new EntityLockerFactory();

    /**
     * Map with preserved entity lockers for each entity key class.
     */
    private final ConcurrentMap<Class, EntityLocker> entityLockersMap = new ConcurrentHashMap<>();

    /**
     * Static method for retrieving instance of entity locker factory.
     *
     * @return entity locker factory
     */
    public static EntityLockerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Private default constructor. Only one instance of this class is allowed.
     */
    private EntityLockerFactory() {
    }

    /**
     * Get entity locker by entity key class
     *
     * @param entityKeyClass entity key class
     * @param <K>            type of entity key
     * @return entity locker for a certain entity key class
     * @throws NullPointerException if entityKeyClass is null
     */
    public <K> EntityLocker<K> getEntityLocker(final @NonNull Class<K> entityKeyClass) {
        // This cast is correct, because we put to Map right EntityLocker<K>:
        // Key - Class<K>, value - EntityLocker<K>
        @SuppressWarnings("unchecked")
        EntityLocker<K> existingEntityLocker = entityLockersMap.get(entityKeyClass);
        if (existingEntityLocker != null) {
            log.debug("Entity locker for entity with entity id class {} is already created", entityKeyClass.getName());
            return existingEntityLocker;
        }
        log.debug("Entity locker for entity with entity id class {} isn't created. Create new.", entityKeyClass.getName());

        // create new entity locker for entity with id class K
        EntityLocker<K> newEntityLocker = new EntityLocker<>();

        // This cast is correct, because we put to Map right EntityLocker<K>:
        // Key - Class<K>, value - EntityLocker<K>
        @SuppressWarnings("unchecked")
        EntityLocker<K> oldEntityLocker = entityLockersMap.putIfAbsent(entityKeyClass, newEntityLocker);
        return oldEntityLocker == null ? newEntityLocker : oldEntityLocker;
    }
}
