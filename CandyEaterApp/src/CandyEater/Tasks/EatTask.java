package CandyEater.Tasks;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;

import java.util.function.Consumer;

public class EatTask implements Runnable {
    /**
     * Колбэк на успешное выполнение.
     */
    private Action mSuccessCallback;

    /**
     * Колбэк на исключение.
     */
    private Consumer<Exception> mErrorCallback;

    /**
     * Пожиратель.
     */
    private ICandyEater mEater;

    /**
     * Конфета.
     */
    private ICandy mCandy;

    public EatTask(
            ICandyEater eater,
            ICandy candy,
            Action successCallback,
            Consumer<Exception> errorCallback) {
        mSuccessCallback = successCallback;
        mErrorCallback = errorCallback;
        mEater = eater;
        mCandy = candy;
    }

    @Override
    public void run() {
        try {
            mEater.eat(mCandy);
            mSuccessCallback.execute();
        } catch (Exception ex) {
            mErrorCallback.accept(ex);
        }
    }
}
