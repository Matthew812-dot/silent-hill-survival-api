package app;

public class Survivor {
    private String name;
    private int health;
    private int stamina;

    public Survivor(String name){
        this.name= name;
        this.health=100;
        this.stamina=100;

    }
    public String getName(){
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getStamina() {
        return stamina;
    }

    public void healHealth(int amount) {
        if (amount<=0) {
            return;
        }
        health= Math.min(100, health+amount);
    }
    public void restoreStamina(int amount){
        if (amount<=0){
            return;
        }
        stamina=Math.min(100, stamina+amount);
    }
    public void takeDamage(int amount){
        if (amount>0){
            health=Math.max(0, health-amount);
        }

    }
}

