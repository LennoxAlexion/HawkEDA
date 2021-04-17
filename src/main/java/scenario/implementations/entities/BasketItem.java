package scenario.implementations.entities;

import org.json.JSONObject;

import java.util.UUID;

public class BasketItem{
    public UUID id; //basketId
    public int productId;
    public String productName;
    public float unitPrice;
    public float oldUnitPrice;
    public int quantity;
    public String pictureUrl;

    public BasketItem(UUID basketId, int productId, String productName, float unitPrice, int quantity){
        this.id = basketId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.oldUnitPrice = 0;
        this.quantity = quantity;
        pictureUrl = "http://host.docker.internal:5202/c/api/v1/catalog/items/8/pic/";
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("productId", productId);
        jsonObject.put("productName", productName);
        jsonObject.put("unitPrice", unitPrice);
        jsonObject.put("oldUnitPrice", oldUnitPrice);
        jsonObject.put("quantity", quantity);
        jsonObject.put("pictureUrl", pictureUrl);
        return jsonObject;
    }
}
