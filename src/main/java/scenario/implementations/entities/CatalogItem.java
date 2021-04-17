package scenario.implementations.entities;

import org.json.JSONObject;

import java.util.UUID;

public class CatalogItem {
    public int id;
    public String name;
    public String description;
    public float price;
    public String pictureFileName;
    public String pictureUri;
    public int catalogTypeId;
    public String catalogType;
    public int catalogBrandId; //max length("123");
    public String catalogBrand;
    public int availableStock;
    public int restockThreshold;
    public int maxStockThreshold;
    public Boolean onReorder;


    public CatalogItem(int itemId, String productName, int quantity, float price){
        id = itemId;
        availableStock = quantity;
        this.price = price;
        name = productName;

        restockThreshold = 0;
        maxStockThreshold = 0;
        onReorder = false;
        pictureFileName = "8.png";
        pictureUri = "http://host.docker.internal:5202/c/api/v1/catalog/items/8/pic/";
        catalogTypeId = 2;
        catalogType = null;
        catalogBrandId = 5;
        catalogBrand = null;
        description = "temp desc";
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("description", description);
        jsonObject.put("price", price);
        jsonObject.put("pictureFileName", pictureFileName);
        jsonObject.put("pictureUri", pictureUri);
        jsonObject.put("catalogTypeId", catalogTypeId);
        jsonObject.put("catalogType", catalogType);
        jsonObject.put("catalogBrandId", catalogBrandId);
        jsonObject.put("catalogBrand", catalogBrand);
        jsonObject.put("availableStock", availableStock);
        jsonObject.put("restockThreshold", restockThreshold);
        jsonObject.put("maxStockThreshold", maxStockThreshold);
        jsonObject.put("onReorder", onReorder);

        return jsonObject;
    }
}
