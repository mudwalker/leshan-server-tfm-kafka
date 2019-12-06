package model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class Dht22 {
    @Getter @Setter private double temperature;
    @Getter @Setter private double humidity;
    @Getter @Setter private long timestamp;
    @Getter @Setter private String deviceId;
}
