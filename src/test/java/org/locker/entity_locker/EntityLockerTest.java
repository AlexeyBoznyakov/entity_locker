package org.locker.entity_locker;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class EntityLockerTest {

    private EntityLocker<String> testedClass = EntityLockerFactory.getInstance().getEntityLocker(String.class);

    @Test(expected = IllegalStateException.class)
    public void testLockEntityByKey_LockeOutsideProtectedCode() throws Exception {
        testedClass.lockEntityByKey("key");
    }

    @Test(expected = NullPointerException.class)
    public void testLockEntityByKey_EntityKeyIsNull() throws Exception {
        testedClass.lockEntityByKey(null);
    }

    @Test
    public void runProtectedCode() throws Exception {
        testedClass.runProtectedCode(() -> {
            testedClass.lockEntityByKey("1");
        });
    }

    @Test(expected = NullPointerException.class)
    public void runProtectedCode_ProtectedCodeIsNull() throws Exception {
        testedClass.runProtectedCode(null);
    }

    @Test(expected = IllegalStateException.class)
    public void runProtectedCode_NestedProtectedCode() throws Exception {
        testedClass.runProtectedCode(() -> testedClass.runProtectedCode(() -> {
            testedClass.lockEntityByKey("1");
        }));
    }

    @Test
    public void runProtectedCode_2Threads() throws Exception {
        Thread thread1 = new Thread(() -> {
            try {
                testedClass.runProtectedCode(() -> {
                    testedClass.lockEntityByKey("1");
                    TimeUnit.SECONDS.sleep(5);
                    testedClass.lockEntityByKey("2");
                });
            } catch (InterruptedException e) {
                throw new IllegalStateException("Interrupted");
            }
        });
        thread1.start();

        Thread thread2 = new Thread(() -> {
            try {
                testedClass.runProtectedCode(() -> {
                    testedClass.lockEntityByKey("2");
                    TimeUnit.SECONDS.sleep(10);
                    testedClass.lockEntityByKey("3");
                });
            } catch (InterruptedException e) {
                throw new IllegalStateException("Interrupted");
            }
        });
        thread2.start();

        thread1.join();
        thread2.join();
    }
}