package org.tfm.leshan.server.conf;

import org.aeonbits.owner.Config;

@Config.Sources({ "classpath:application.properties" })
public interface ServerConfig extends Config {
    @Key("kafka.topic")
    String kafkaTopic();
    @Key("kafka.port")
    int kafkaPort();
    @Key("kafka.server")
    String kafkaServer();
    @Key("kafka.clientId")
    String clientId();
}
