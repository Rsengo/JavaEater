package CandyEater.CandyEater;

import CandyEater.Candy.ICandy;

public class CandyEater implements ICandyEater {
    @Override
    public void eat(ICandy candy) throws Exception {
        System.out.println("start: " + candy.getCandyFlavour());
        Thread.sleep(1000);
        System.out.println("end: " + candy.getCandyFlavour());
    }
}
