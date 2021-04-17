package scenario.implementations.entities;

import com.github.javafaker.Faker;
import org.json.JSONObject;

import java.util.UUID;

public class UserDetail {
    private String city;
    private String street;
    private String state;
    private String country;
    private String zipCode;
    private String cardNumber;
    private String cardHolderName;
    private String cardExpiration; //"2022-03-02T00:00:00\"
    private String cardSecurityNumber; //max length("123");
    private String cardTypeId;
    private String buyer;

    public UserDetail(UUID userId){
        buyer = userId.toString();
        cardTypeId = "1";
        cardExpiration = "2022-03-02T00:00:00";
        cardSecurityNumber = "123";

        //Generate using faker.
        Faker faker = new Faker();
        city = faker.address().cityName();
        street = faker.address().streetName();
        state = faker.address().state();
        country = faker.address().country();
        zipCode = faker.address().zipCode();
        cardNumber = "1234567890123456";
        cardHolderName = faker.name().fullName();
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("city", city);
        jsonObject.put("street", street);
        jsonObject.put("state", state);
        jsonObject.put("country", country);
        jsonObject.put("zipCode", zipCode);
        jsonObject.put("cardNumber", cardNumber);
        jsonObject.put("cardHolderName", cardHolderName);
        jsonObject.put("cardExpiration", cardExpiration);
        jsonObject.put("cardSecurityNumber", cardSecurityNumber);
        jsonObject.put("cardTypeId", cardTypeId);
        jsonObject.put("buyer", buyer);
        return jsonObject;
    }
}
