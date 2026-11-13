package app;

public class EnergyDrink extends Item {

    public EnergyDrink() {
        super("EnergyDrink");
    }

    @Override
    public void use(Survivor survivor) {
        survivor.restoreStamina(30);
    }

}