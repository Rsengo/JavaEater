package CandyEater.CandyEater;

import CandyEater.Candy.ICandy;

import java.util.Random;

public class CandyEater implements ICandyEater {
    @Override
    public void eat(ICandy candy) throws Exception {
        var r = new Random();
        var delay = r.nextInt((5000-1000)+1) + 1000;

        System.out.println("start: " + candy.getCandyFlavour());
        Thread.sleep(delay);
        System.out.println("end: " + candy.getCandyFlavour());
    }
}
