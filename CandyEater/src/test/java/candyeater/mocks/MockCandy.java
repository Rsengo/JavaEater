package candyeater.mocks;

import CandyEater.Candy.ICandy;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.HashSet;

public class MockCandy implements ICandy {
    private final int flavour;

    private volatile boolean isEaten = false;

    private volatile boolean isEatenMoreThanOnce = false;

    public static Collection<MockCandy> spawn(int num, int flavour) {
        final HashSet<MockCandy> candies = new HashSet<>();

        for (int i = 0; i < num; i++) {
            candies.add(new MockCandy(flavour));
        }

        return candies;
    }

    public MockCandy(int flavour) {
        this.flavour = flavour;
    }

    @Override
    public int getCandyFlavour() {
        return flavour;
    }

    public boolean isEaten() {
        return isEaten;
    }

    void setEaten() {
        if (isEaten) {
            isEatenMoreThanOnce = true;
        }

        isEaten = true;
    }

    public void validate() {
        Assertions.assertTrue(isEaten, "Eaten at all");
        Assertions.assertFalse(isEatenMoreThanOnce, "Eaten more than once");
    }
}
