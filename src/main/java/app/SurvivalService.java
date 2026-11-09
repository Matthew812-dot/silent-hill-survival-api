package app;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class SurvivalService {

    private Map<Integer, Survivor> survivors = new HashMap<>();
    private int nextId = 1;

    public int createSurvivor(String name ){
        Survivor survivor= new Survivor(name);
        int id=nextId;
        survivors.put(id, survivor);
        nextId++;
        return id;
    }
    public Survivor getSurvivor(int id){
        return survivors.get(id);
    }
    public Collection<Survivor> getAllSurvivors(){
        return survivors.values();
    }
    public boolean deleteSurvivor(int id){
        return survivors.remove(id) != null;
    }
    public boolean damageSurvivor(int id,int amount){
        Survivor survivor = survivors.get(id);

        if (survivor==null){
            return false;}

        survivor.takeDamage(amount);
        return true;



    }
    public boolean useItem(int survivorId, Item item) {
        Survivor survivor = survivors.get(survivorId);

        if (survivor == null || item == null) {
            return false;
        }

        item.use(survivor);
        return true;
    }
}