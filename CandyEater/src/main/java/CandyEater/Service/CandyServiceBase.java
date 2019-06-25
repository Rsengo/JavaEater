package CandyEater.Service;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;

public abstract class CandyServiceBase {
    /**
     * Сервис получает при инициализации массив доступных пожирателей конфет.
     * @param candyEaters Список пожирателей.
     */
    public CandyServiceBase(ICandyEater[] candyEaters) {

    }

    /**
     * Добавить конфету на съедение.
     * @param candy Конфета.
     */
    public abstract void addCandy(ICandy candy);
}
