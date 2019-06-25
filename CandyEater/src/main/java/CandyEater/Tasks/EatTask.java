package CandyEater.Tasks;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;

import java.util.function.Consumer;

public class EatTask implements Runnable {
    /**
     * Колбэк на успешное выполнение.
     */
    private Action successCallback;

    /**
     * Колбэк на исключение.
     */
    private Consumer<Exception> errorCallback;

    /**
     * Пожиратель.
     */
    private ICandyEater eater;

    /**
     * Конфета.
     */
    private ICandy candy;

    public EatTask(
            ICandyEater eater,
            ICandy candy,
            Action successCallback,
            Consumer<Exception> errorCallback) {
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;
        this.eater = eater;
        this.candy = candy;
    }

    @Override
    public void run() {
        try {
            eater.eat(candy);
            successCallback.execute();
        } catch (Exception ex) {
            errorCallback.accept(ex);
        }
    }
}
