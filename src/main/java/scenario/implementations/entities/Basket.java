package scenario.implementations.entities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public class Basket {
    private UUID buyerId;

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public ArrayList<BasketItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<BasketItem> items) {
        this.items = items;
    }

    private ArrayList<BasketItem> items;

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("buyerId", buyerId);

        JSONArray itemJsonArray = new JSONArray();
        for (BasketItem item: items) {
            itemJsonArray.put(item.toJSON());
        }
        jsonObject.put("items", itemJsonArray);
        return jsonObject;
    }
}
