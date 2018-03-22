/**
 * Copyright (C) 2011-2017 ARM Limited. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mbed.coap.example;

import com.mbed.coap.CoapConstants;
import com.mbed.coap.packet.BlockSize;
import com.mbed.coap.utils.CoapResource;
import com.mbed.coap.packet.Code;
import com.mbed.coap.server.CoapExchange;
import com.mbed.coap.server.MessageIdSupplierImpl;
import com.mbed.coap.server.SimpleObservationIDGenerator;


import com.mbed.coap.exception.CoapException;
import com.mbed.coap.observe.SimpleObservableResource;
import com.mbed.coap.server.CoapServer;
import com.mbed.coap.server.CoapServerBuilder;
import com.mbed.coap.transport.CoapTransport;
import com.mbed.coap.transport.javassl.SSLSocketClientTransport;
import com.mbed.coap.transport.udp.DatagramSocketTransport;
import com.mbed.coap.utils.ReadOnlyCoapResource;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by szymon
 */
public class DeviceEmulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceEmulator.class);
    private final CoapServer emulatorServer;
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws IOException {
        final DeviceEmulator deviceEmulator = new DeviceEmulator(5683);

        Runtime.getRuntime().addShutdownHook(new Thread(deviceEmulator::stop));
    }


    public DeviceEmulator(int port) throws IOException {
        //CoapTransport coapTransport = new SocketServerTransport(port, true);
        DatagramSocketTransport transport = new DatagramSocketTransport(5683);

        String text5k = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc commodo ullamcorper blandit. Nullam eget erat sollicitudin velit rhoncus dictum eget vel magna. Donec turpis ante, egestas vel eros ut, facilisis dapibus tortor. Aenean a dignissim massa, id cursus est. Curabitur lectus magna, elementum id sagittis non, euismod vulputate est. Donec tincidunt et eros quis luctus. Quisque viverra rutrum tortor, convallis finibus ex pretium eu. Praesent elementum turpis id velit tempus venenatis.\nPraesent ullamcorper, urna in bibendum mollis, nisi ipsum mollis ipsum, non gravida ipsum lacus at erat. Donec non ipsum sit amet nibh tempus malesuada. Donec diam nunc, consectetur a felis eu, vehicula tempor risus. Curabitur felis ex, rhoncus a augue eget, scelerisque lobortis sem. Pellentesque tincidunt rutrum nisi, a eleifend augue pulvinar et. Sed nisl orci, malesuada in auctor non, tristique et leo. Mauris iaculis id augue a cursus. Aliquam sollicitudin blandit mauris, a ultrices erat tincidunt sed. Ut mattis tortor et maximus euismod.\nEtiam tincidunt urna diam, ac hendrerit lacus viverra eu. Ut sit amet ante eu ligula tincidunt imperdiet. Donec dapibus velit vel sapien tristique, a pulvinar sem aliquet. Nam vulputate est lorem, vitae venenatis odio gravida id. Interdum et malesuada fames ac ante ipsum primis in faucibus. Mauris hendrerit neque a blandit molestie. Pellentesque hendrerit, neque ut venenatis interdum, leo nulla suscipit massa, a porttitor nunc urna vel quam. Proin erat diam, consequat quis lorem quis, tempor sollicitudin ante. Phasellus vel maximus felis, eu scelerisque lorem. Phasellus vehicula porta convallis. Nulla sagittis, dolor ac commodo vestibulum, est ipsum tristique libero, sed posuere orci ex sit amet dui. Nulla euismod est sem, non venenatis risus lacinia vitae. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Proin tristique luctus aliquet. Nunc blandit sapien sit amet efficitur tempor.\nEtiam posuere posuere pellentesque. Nam semper tellus quis lectus hendrerit sodales sed vel ex. Curabitur eu elit eu eros dignissim tincidunt et in justo. Sed vel enim accumsan, fringilla turpis at, gravida mauris. Nullam volutpat a ligula et tempus. Proin suscipit malesuada mollis. Vestibulum a ligula magna. Suspendisse bibendum est eget massa varius luctus. Integer lacinia arcu quis nunc scelerisque, eu finibus eros molestie. Fusce sagittis feugiat nibh a dapibus.\nAliquam eu varius sapien, eget iaculis est. Nullam sed ante et justo maximus lobortis. Praesent faucibus ultricies rutrum. Fusce mauris neque, malesuada eleifend dapibus egestas, varius sed nulla. In eget lacus eget eros maximus rutrum. Fusce id hendrerit felis. Nulla scelerisque lectus a magna tincidunt ultricies. In ultrices lorem ligula, at faucibus augue aliquet a. Nam scelerisque sem a erat interdum, et varius diam blandit. Nullam non vulputate diam. Mauris iaculis sapien sit amet bibendum venenatis.\nCras est libero, eleifend vitae quam at, suscipit semper mi. Pellentesque ullamcorper sed metus in sollicitudin. In eu lectus libero. Proin quam dolor, malesuada sit amet blandit et, blandit porttitor quam. Vivamus gravida condimentum magna at porta. Vivamus finibus id risus sollicitudin finibus. Pellentesque tempor lectus felis, eget feugiat nibh pharetra ac. Fusce semper fringilla bibendum. Nulla fringilla mi eu tortor ullamcorper suscipit. Nam tincidunt nulla cursus risus ultrices, placerat ullamcorper odio luctus. Nulla sed nunc pretium, viverra nulla vel, condimentum lorem. Sed commodo urna et mollis condimentum. Integer in metus velit. Vestibulum facilisis laoreet felis, in imperdiet nisi. Donec rhoncus enim et tincidunt mollis.\nPhasellus metus turpis, viverra quis velit in, mattis volutpat libero. Cras non augue massa. Sed viverra vestibulum lacus sit amet hendrerit. Curabitur tristique blandit augue ac faucibus. Duis tincidunt ipsum quis cursus gravida. Praesent viverra aliquet turpis, id pulvinar tellus pellentesque eget. Suspendisse tempus felis et ante tristique, in lobortis urna lobortis. Pellentesque rhoncus quis velit vitae dignissim. Fusce quis blandit ante, eget rutrum est. In ullamcorper porttitor egestas. Praesent id faucibus lectus, placerat mollis magna.\nDonec erat felis, fermentum vitae ex nec, varius pharetra neque. Sed nec euismod turpis. Quisque suscipit arcu id aliquam iaculis. Cras mattis egestas feugiat. In sit amet ultricies lectus. Sed sollicitudin velit non dictum finibus. Nullam vitae vestibulum lorem.\nQuisque vitae enim euismod, imperdiet metus vitae, mollis purus. Mauris at eros ligula. In porta et risus sed tincidunt. Suspendisse faucibus feugiat nibh in malesuada. Duis eu dui risus. Duis lobortis pellentesque nunc at lobortis. Curabitur id dui tellus. Morbi lacus quam, feugiat at faucibus quis, dignissim nec est. In eget dapibus mauris, ac tincidunt enim. Pellentesque ornare, tellus ut pharetra ultricies, velit velit elementum enim, sit amet fermentum ligula odio ac ante cras amet.";

        emulatorServer = CoapServerBuilder.newBuilder()
//                .transport(port, Executors.newCachedThreadPool())
//                .transport(0)
                .transport(new DatagramSocketTransport(5683))
                .midSupplier(new MessageIdSupplierImpl(0))
                .observerIdGenerator(new SimpleObservationIDGenerator(0))
                .blockSize(BlockSize.S_128)
                .build()
                .start();


        //read only resources
        emulatorServer.addRequestHandler("/small", new ReadOnlyCoapResource(text5k.substring(0, 10)));
        emulatorServer.addRequestHandler("/large", new ReadOnlyCoapResource(text5k));

        //observable resource
        SimpleObservableResource timeResource = new SimpleObservableResource("", emulatorServer);
        emulatorServer.addRequestHandler("/time", timeResource);
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                timeResource.setBody(new Date().toString());
            } catch (CoapException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }, 30, 30, TimeUnit.SECONDS);

        emulatorServer.addRequestHandler("/write", new CoapResource() {
            @Override
            public void get(CoapExchange exchange) {
                exchange.setResponseCode(Code.C405_METHOD_NOT_ALLOWED);
                exchange.sendResponse();
            }

            @Override
            public void put(CoapExchange exchange) {
                exchange.setResponseCode(Code.C204_CHANGED);
                exchange.setResponseBody(Integer.valueOf(exchange.getRequestBody().length).toString());
                exchange.sendResponse();
            }
        });

        emulatorServer.addRequestHandler(CoapConstants.WELL_KNOWN_CORE, emulatorServer.getResourceLinkResource());
    }

    void stop() {
        emulatorServer.stop();
    }

}
