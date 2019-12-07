package org.tfm.leshan.server;

import org.aeonbits.owner.ConfigFactory;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.StaticModelProvider;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.tfm.leshan.server.conf.ServerConfig;

import java.util.Collection;
import java.util.List;

public class Main {
    static  Registration currentRegistration = null;
    static KafkaPublisher kafka;

    public static void main(String[] args) throws InterruptedException {

        ServerConfig cfg = ConfigFactory.create(ServerConfig.class);

        // Cargamos los models por defecto(Security, Server, Device ...)
        List<ObjectModel> models = ObjectLoader.loadDefault();
        // Cargamos los models del sensor
        String[] modelPaths = new String[] { "3303.xml", "3304.xml"};
        models.addAll(ObjectLoader.loadDdfResources("/models/", modelPaths));

        // Añadimos los models al server
        LeshanServerBuilder builder = new LeshanServerBuilder();
        LwM2mModelProvider modelProvider = new StaticModelProvider(models);
        builder.setObjectModelProvider(modelProvider);

        LeshanServer server = builder.build();
        kafka = new KafkaPublisher(cfg.kafkaTopic(), cfg.kafkaServer(), cfg.kafkaPort(), cfg.clientId(), true);
        server.start();

        server.getRegistrationService().addListener(new RegistrationListener() {

            public void registered(Registration registration, Registration previousReg,
                                   Collection<Observation> previousObsersations) {
                currentRegistration = registration;
                System.out.println("new device: " + registration.getEndpoint());
                /*try {
                    ReadResponse response = server.send(registration, new ReadRequest(3303,0,5700));
                    if (response.isSuccess()) {
                        System.out.println("Device temp:" + ((LwM2mResource)response.getContent()).getValue());
                    }else {
                        System.out.println("Failed to read:" + response.getCode() + " " + response.getErrorMessage());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }

            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
                currentRegistration = updatedReg;
                System.out.println("device is still here: " + updatedReg.getEndpoint());
                try {
                    ReadResponse response = server.send(updatedReg, new ReadRequest(3303,0,5701));
                    if (response.isSuccess()) {
                        System.out.println("Device time:" + ((LwM2mResource)response.getContent()).getValue());
                    }else {
                        System.out.println("Failed to read:" + response.getCode() + " " + response.getErrorMessage());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public void unregistered(Registration registration, Collection<Observation> observations, boolean expired,
                                     Registration newReg) {
                System.out.println("device left: " + registration.getEndpoint());
            }
        });

        while (true) {
            if (currentRegistration != null){
                try {
                    Object temp = null;
                    Object hum = null;
                    ReadResponse responseTemp = server.send(currentRegistration, new ReadRequest(3303,0,5700));
                    if (responseTemp.isSuccess()) {
                        temp =  ((LwM2mResource)responseTemp.getContent()).getValue();
                        System.out.println("Device temp:" + temp);
                    }else {
                        System.out.println("Failed to read:" + responseTemp.getCode() + " " + responseTemp.getErrorMessage());
                    }

                    ReadResponse responseHum = server.send(currentRegistration, new ReadRequest(3304,0,5700));
                    if (responseHum.isSuccess()) {
                        hum =  ((LwM2mResource)responseHum.getContent()).getValue();

                        System.out.println("Device Humidity:" + hum);
                    }else {
                        System.out.println("Failed to read:" + responseTemp.getCode() + " " + responseTemp.getErrorMessage());
                    }

                    if (responseTemp.isSuccess() && responseHum.isSuccess())
                        kafka.sendMessage(temp, hum);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(2000);
        }


    }



}
