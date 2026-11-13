package app;

public class Medkit extends Item {

    public Medkit() {
        super("Medkit");
    }

    @Override
    public void use(Survivor survivor) {
        survivor.healHealth(40);
    }
}