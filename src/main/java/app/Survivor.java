package app;

public class Survivor {
    private String name;
    private int health;
    private int stamina;

    public Survivor(String name){
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be blank");
        this.name = name.trim();
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
        health= Math.min(100, health + Math.min(amount, 100));
    }
    public void restoreStamina(int amount){
        if (amount<=0){
            return;
        }
        stamina=Math.min(100, stamina + Math.min(amount, 100));
    }
    public void takeDamage(int amount){
        if (amount>0){
            health=Math.max(0, health-amount);
        }

    }
    public void update(String name, int health, int stamina) {
        if (name == null || name.isBlank() || health < 0 || health > 100 || stamina < 0 || stamina > 100)
            throw new IllegalArgumentException("Invalid survivor state");
        this.name = name.trim(); this.health = health; this.stamina = stamina;
    }
}

