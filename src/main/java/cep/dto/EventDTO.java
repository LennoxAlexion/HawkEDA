package cep.dto;

import org.json.JSONObject;

public class EventDTO {
    private final String eventName;
    private final String actualEventTimestamp;
    private final JSONObject messageBody;

    public EventDTO(String eventName, String actualEventTimestamp, JSONObject messageBody){
        this.eventName = eventName;
        this.actualEventTimestamp = actualEventTimestamp;
        this.messageBody = messageBody;
    }

    public String getEventName() {
        return eventName;
    }

    public String getTimestamp() {
        return actualEventTimestamp;
    }

    public JSONObject getMessageBody(){
        return messageBody;
    }

    @Override
    public String toString() {
        return toJSONObject().toString();
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("eventName", getEventName());
        jsonObject.put("actualEventTimestamp", getTimestamp());
//        jsonObject.put("messageBody", getMessageBody());
        return jsonObject;
    }
}
