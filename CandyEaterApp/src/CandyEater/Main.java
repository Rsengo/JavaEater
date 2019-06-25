package CandyEater;

import CandyEater.Candy.Candy;
import CandyEater.CandyEater.CandyEater;
import CandyEater.CandyEater.ICandyEater;
import CandyEater.Service.CandyService;

public class Main {
    public static void main(String[] args) {
        final int EATERS_COUNT = 5;
        final int CANDIES_COUNT = 20;
        final int FLAVOURS_COUNT = 7;

        var eaters = new ICandyEater[EATERS_COUNT];

        for (var i = 0; i < EATERS_COUNT; i++) {
            eaters[i] = new CandyEater();
        }

        var service = new CandyService(eaters);

        for (var i = 0; i < CANDIES_COUNT; i++) {
            var candy = new Candy(i % FLAVOURS_COUNT);
            service.addCandy(candy);
        }
    }
}
