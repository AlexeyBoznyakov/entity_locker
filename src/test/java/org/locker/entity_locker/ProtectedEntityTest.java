package org.locker.entity_locker;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProtectedEntityTest {

    @Test(expected = NullPointerException.class)
    public void testConstructor_EntityKeyIsNull() {
        new ProtectedEntity<>(null, new ReentrantLock());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_LockForEntityIsNull() {
        new ProtectedEntity<>("key", null);
    }

    @Test
    public void testConstructor() {
        // then
        ProtectedEntity<String> protectedEntity = new ProtectedEntity<>("key", new ReentrantLock());
        Assert.assertNotNull(protectedEntity);
    }


    @Test
    public void testGetEntityKey() throws Exception {
        // given
        String key = "key";
        Lock lock = new ReentrantLock();

        // then
        ProtectedEntity<String> protectedEntity = new ProtectedEntity<>(key, lock);
        Assert.assertEquals(key, protectedEntity.getEntityKey());
    }

    @Test
    public void testGetLockForEntity() throws Exception {
        // given
        String key = "key";
        Lock lock = new ReentrantLock();

        // then
        ProtectedEntity<String> protectedEntity = new ProtectedEntity<>(key, lock);
        Assert.assertEquals(lock, protectedEntity.getLockForEntity());
    }
}
