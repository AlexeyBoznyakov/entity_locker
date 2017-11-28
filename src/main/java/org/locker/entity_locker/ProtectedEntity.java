package org.locker.entity_locker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.concurrent.locks.Lock;

/**
 * Class is intended to represent protected entity.
 *
 * @param <T> type of entity key
 * @author Alexey Boznyakov
 */
@Getter
@AllArgsConstructor
public class ProtectedEntity<T> {
    /**
     * Entity key.
     */
    @NonNull
    private T entityKey;

    /**
     * Lock object, which is used for protection entity.
     */
    @NonNull
    private Lock lockForEntity;
}