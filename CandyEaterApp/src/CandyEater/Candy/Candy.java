package CandyEater.Candy;

public class Candy implements ICandy {
    private int mFlavour;

    public Candy(int flavour) {
        mFlavour = flavour;
    }

    @Override
    public int getCandyFlavour() {
        return mFlavour;
    }
}
