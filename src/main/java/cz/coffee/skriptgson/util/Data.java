package cz.coffee.skriptgson.util;

import com.google.gson.JsonElement;

import java.util.Date;

@SuppressWarnings("unused")
public class Data {
    public Data(JsonElement data, Date date) {
        super();
        this.data = data;
        this.date = date;
    }
    private JsonElement data;
    final private Date date;

    public Date getDate() {
        return date;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }
}
