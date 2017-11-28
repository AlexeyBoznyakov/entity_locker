package org.locker.entity_locker;


import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Class is intended to represent information about protected code e.g. locked items and etc.
 * Case of usage:
 * It's needed for clearing resources after protected code execution.
 *
 * @author Alexey Boznyakov
 */
public class ProtectedCodeInfo<T> {

    /**
     * Protected entities.
     */
    @Getter
    private final List<ProtectedEntity<T>> protectedEntities = new ArrayList<>();

    /**
     * @param entityKey entity key
     * @param lock      lock object, which is used for protection entity
     * @throws NullPointerException if entityKey or lock is null
     */
    public void addProtectedEntity(final @NonNull T entityKey, final @NonNull Lock lock) {
        protectedEntities.add(new ProtectedEntity<>(entityKey, lock));
    }
}
