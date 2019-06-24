package CandyEater.Tasks;

import CandyEater.Candy.ICandy;
import CandyEater.CandyEater.ICandyEater;

public class EatTask implements Runnable {
    private Action mSuccessCallback;

    private Action mErrorCallback;

    private ICandyEater mEater;

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
