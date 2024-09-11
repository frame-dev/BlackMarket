package ch.framedev.blackMarket;



/*
 * ch.framedev.blackMarket
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 24.08.2024 23:06
 */

public class DataMaterial {
    int id;
    String material;
    double value;
    double sellValue;

    public DataMaterial(int id, String material, double value, double sellValue) {
        this.id = id;
        this.material = material;
        this.value = value;
        this.sellValue = sellValue;
    }

    public int getId() {
        return id;
    }

    public String getMaterial() {
        return material;
    }

    public double getValue() {
        return value;
    }

    public double getSellValue() {
        return sellValue;
    }
}
