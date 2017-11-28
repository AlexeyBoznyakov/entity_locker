package org.locker.entity_locker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EntityLockerFactoryTest {

    @Test
    public void testGetInstance() throws Exception {
        assertNotNull(EntityLockerFactory.getInstance());
    }

    @Test(expected = NullPointerException.class)
    public void testGetEntityLocker_EntityKeyClassIsNull() throws Exception {
        assertNotNull(EntityLockerFactory.getInstance().getEntityLocker(null));
    }

    @Test
    public void testGetEntityLocker() throws Exception {
        assertNotNull(EntityLockerFactory.getInstance().getEntityLocker(String.class));
    }

    @Test
    public void testGetEntityLocker_EntityLockerAlreadyCreated() throws Exception {
        EntityLocker<String> firstEntityLocker = EntityLockerFactory.getInstance().getEntityLocker(String.class);
        EntityLocker<String> secondEntityLocker = EntityLockerFactory.getInstance().getEntityLocker(String.class);

        assertEquals(firstEntityLocker, secondEntityLocker);
    }

}