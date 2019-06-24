package CandyEater.Tasks;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;

public class EatTask implements Runnable {
    /**
     * Колбэк на успешное выполнение.
     */
    private Action mSuccessCallback;

    /**
     * Колбэк на исключение.
     */
    private Action mErrorCallback;

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
            Action errorCallback) {
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
            mErrorCallback.execute();
        }
    }
}
