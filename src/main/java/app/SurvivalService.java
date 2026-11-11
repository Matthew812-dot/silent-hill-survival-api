package app;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;


public class SurvivalService {

    private Map<Integer, List<Integer>> inventories = new HashMap<>();
    private Map<Integer, Survivor> survivors = new HashMap<>();
    private int nextId = 1;

    private Map<Integer, Item> items = new HashMap<>();
    private int nextItemId = 1;

    public int createItem(Item item) {
        int id = nextItemId;
        items.put(id, item);
        nextItemId++;
        return id;

    }
    public boolean useInventoryItem(int survivorId, int itemId) {
        Survivor survivor = survivors.get(survivorId);
        Item item= items.get(itemId);
        if(survivor==null){
            return false;
        }
        if (!inventories.get(survivorId).contains(itemId)){
            return false;
        }
        if (item!= null){
            item.use(survivor);

        } else {
            return false;
        }
        inventories.get(survivorId).remove(Integer.valueOf(itemId));
        items.remove(itemId);
        return true;
    }
    public Item getItem(int id){return items.get(id);
    }
    public int countAliveSurvivors() {
        int count = 0;

        for (Survivor survivor : survivors.values()) {
            if (survivor.getHealth()>0){
                count++;
            }
        }
        return count;
    }
    public boolean deleteItem(int id) {
        for(List<Integer> inventory: inventories.values()){
            inventory.remove(Integer.valueOf(id));
        }
        return items.remove(id) != null;
    }
    public boolean deleteSurvivor(int id){
        if (!survivors.containsKey(id)){
            return false;
        }
        List<Integer> inventory = inventories.get(id);
        for (Integer itemId: inventory) {
            items.remove(itemId);

        }
        inventories.remove(id);
        survivors.remove(id);

        return true;

    }


    public int createSurvivor(String name ){
        Survivor survivor= new Survivor(name);
        int id=nextId;
        survivors.put(id, survivor);
        inventories.put(id,new ArrayList<>());
        nextId++;
        return id;
    }
    public boolean addItemToInventory(int survivorId, int itemId) {

        if (!survivors.containsKey(survivorId)|| !items.containsKey(itemId)){
            return false;
        }
        for (List<Integer> inventory : inventories.values()) {
            if (inventory.contains(itemId)){
                return false;
            }
        }
        inventories.get(survivorId).add(itemId);
        return true;
    }
    public Survivor getSurvivor(int id){
        return survivors.get(id);
    }
    public Collection<Survivor> getAllSurvivors(){
        return survivors.values();
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