package CandyEater.Service;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;

public abstract class CandyServiceBase {
    /**
     * Пул пожирателей.
     */
    protected ICandyEater[] mEatersPool;

    /**
     * Сервис получает при инициализации массив доступных пожирателей конфет.
     * @param candyEaters Список пожирателей.
     */
    public CandyServiceBase(ICandyEater[] candyEaters) {
        mEatersPool = candyEaters;
    }

    /**
     * Добавить конфету на съедение.
     * @param candy Конфета.
     */
    public abstract void addCandy(ICandy candy);
}
