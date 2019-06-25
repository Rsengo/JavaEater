package candyeater;

import CandyEater.Service.CandyService;
import candyeater.mocks.MockCandy;
import candyeater.mocks.MockCandyEater;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class CandyEaterTest {

    @Test
    void shouldEatAllCandies() throws InterruptedException {
        final Collection<MockCandy> candies = buildTestCandySet();

        final MockCandyEater[] eaters = MockCandyEater.spawn(5);

        final CandyService service = new CandyService(eaters);


        for (MockCandy candy :
                candies) {
            service.addCandy(candy);
        }

        service.getDispatchLoopHandle().join(10000);

        assertEquals(candies.size(), candies.stream().filter(MockCandy::isEaten).count());

        candies.forEach(MockCandy::validate);

        Arrays.stream(eaters).forEach(MockCandyEater::validate);
    }

    @Test
    void shouldNotAcceptCandiesWithNoEaters() {
        final CandyService service = new CandyService(new MockCandyEater[0]);

        assertFalse(service.getDispatchLoopHandle().isAlive());

        assertThrows(IllegalStateException.class, () -> service.addCandy(new MockCandy(0)));
    }

    @Test
    void shouldNotAcceptNullCandy() {
        final CandyService service = new CandyService(MockCandyEater.spawn(1));

        assertThrows(IllegalArgumentException.class, () -> service.addCandy(null));
    }

    @Test
    void shouldNotFallWithTooManyEaters() throws InterruptedException {
        final Collection<MockCandy> candies = buildTestCandySet();

        final CandyService service = new CandyService(MockCandyEater.spawn(1000000));

        for (MockCandy candy :
                candies) {
            service.addCandy(candy);
        }

        service.getDispatchLoopHandle().join(10000);

        assertEquals(candies.size(), candies.stream().filter(MockCandy::isEaten).count());

        candies.forEach(MockCandy::validate);
    }

    private Collection<MockCandy> buildTestCandySet() {
        final LinkedList<MockCandy> candies = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            candies.addAll(MockCandy.spawn(5, i));
        }

        Collections.shuffle(candies);

        return candies;
    }
}
