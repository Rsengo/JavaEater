package CandyEater.CandyEater;

import CandyEater.Candy.ICandy;

public interface ICandyEater {
    /**
     * Имеется ли пожираемая конфета.
     * @return Наличие пожираемой конфеты.
     */
    boolean hasCandy();

    /**
     * Получение пожираемой конфеты.
     * @return Пожираемая конфета.
     */
    ICandy getCandy();

    /**
     * Съесть конфету, может занять время
     * @param candy
     */
    void eat(ICandy candy) throws Exception;
}
