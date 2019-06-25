package CandyEater.Candy;

public class Candy implements ICandy {
    private int flavour;

    public Candy(int flavour) {
        this.flavour = flavour;
    }

    @Override
    public int getCandyFlavour() {
        return flavour;
    }
}
