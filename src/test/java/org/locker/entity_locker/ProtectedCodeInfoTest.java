package org.locker.entity_locker;

import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;

public class ProtectedCodeInfoTest {

    private ProtectedCodeInfo<String> testedClass = new ProtectedCodeInfo<>();

    @Test(expected = NullPointerException.class)
    public void testAddProtectedEntity_KeyIsNull(){
        testedClass.addProtectedEntity(null, new ReentrantLock());
    }

    @Test(expected = NullPointerException.class)
    public void testAddProtectedEntity_LockIsNull(){
        testedClass.addProtectedEntity("key", null);
    }


    @Test
    public void testAddProtectedEntity(){
        // then
        testedClass.addProtectedEntity("key", new ReentrantLock());
        assertEquals(1, testedClass.getProtectedEntities().size());
    }

    @Test
    public void testGetProtectedEntities() throws Exception {
        // given
        String key = "key";
        Lock lock = new ReentrantLock();

        // when
        testedClass.addProtectedEntity(key, lock);

        // then
        assertEquals(1, testedClass.getProtectedEntities().size());
        ProtectedEntity<String> protectedEntity = testedClass.getProtectedEntities().get(0);
        assertEquals(key, protectedEntity.getEntityKey());
        assertEquals(lock, protectedEntity.getLockForEntity());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetProtectedEntities_ModifyCollectionOutsideClass() throws Exception {
        testedClass.getProtectedEntities().add(new ProtectedEntity<>("key", new ReentrantLock()));
    }


}