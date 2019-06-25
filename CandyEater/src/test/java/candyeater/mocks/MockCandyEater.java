package candyeater.mocks;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;
import org.junit.jupiter.api.Assertions;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MockCandyEater implements ICandyEater {
    private static final Set<Integer> flavours = ConcurrentHashMap.newKeySet();

    private final AtomicBoolean isWorkingNow = new AtomicBoolean(false);

    private volatile boolean attemptedToProcessDuplicateFlavour = false;

    private volatile boolean attemptedToProcessDoubleCandies = false;

    public static MockCandyEater[] spawn(int num) {
        MockCandyEater[] eaters = new MockCandyEater[num];

        for (int i = 0; i < num; i++) {
            eaters[i] = new MockCandyEater();
        }

        return eaters;
    }

    @Override
    public void eat(ICandy candy) throws Exception {
        synchronized (isWorkingNow) {
            if (isWorkingNow.get()) {
                attemptedToProcessDoubleCandies = true;

                return;
            } else {
                isWorkingNow.set(true);
            }
        }

        final int flavour = candy.getCandyFlavour();

        synchronized (flavours) {
            if (flavours.contains(flavour)) {
                attemptedToProcessDuplicateFlavour = true;

                return;
            } else {
                flavours.add(flavour);
            }
        }

        Thread.sleep(200);

        ((MockCandy) candy).setEaten();

        synchronized (flavours) {
            flavours.remove(flavour);
        }

        synchronized (isWorkingNow) {
            isWorkingNow.set(false);
        }
    }

    public void validate() {
        Assertions.assertFalse(attemptedToProcessDoubleCandies, "Tried to process multiple candies");

        Assertions.assertFalse(attemptedToProcessDuplicateFlavour, "Tried to process duplicate flavours");
    }
}
