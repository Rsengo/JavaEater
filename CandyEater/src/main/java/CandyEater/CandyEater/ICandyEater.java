package CandyEater.CandyEater;

import CandyEater.Candy.ICandy;

public interface ICandyEater {
    /**
     * Съесть конфету, может занять время
     * @param candy
     */
    void eat(ICandy candy) throws Exception;
}
