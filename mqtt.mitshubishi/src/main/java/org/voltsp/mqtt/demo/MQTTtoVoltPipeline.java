/*
 * Copyright 2024-2025 Volt Active Data Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.voltsp.mqtt.demo;


import com.google.gson.Gson;
import org.voltdb.stream.api.pipeline.VoltPipeline;
import org.voltdb.stream.api.pipeline.VoltStreamBuilder;
import org.voltdb.stream.plugin.mqtt.api.MqttMessageQoS;
import org.voltdb.stream.plugin.mqtt.api.MqttSourceConfigBuilder;
import org.voltdb.stream.plugin.volt.api.ProcedureVoltSinkConfigBuilder;
import org.voltdb.stream.plugin.volt.api.VoltDBResourceConfigBuilder;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Definition of a pipeline that reads strings from mqtt and inserts them into a Volt table via
 * procedure call.
 */
public class MQTTtoVoltPipeline implements VoltPipeline {

    @Override
    public void define(VoltStreamBuilder stream) {
        // Get the configuration value from the environment.
        // if resource is configured in yaml
        stream.configureResource("primary-cluster", VoltDBResourceConfigBuilder.class, config -> {
            config.addToServers("localhost", 21211);
        });

        stream
                // Optionally name the stream//consume from MQTT source
                .withName("MQTT to Volt Stream").consumeFromSource(MqttSourceConfigBuilder.builder()
                        .withTopicFilter("testtopicvolt/#")
                        .withGroupName("Mygroup")
                        .withAddressHost("broker.emqx.io")
                        .withAddressPort(1883)
                        .withQos(MqttMessageQoS.AT_LEAST_ONCE))
                // Process the consuming data from MQTT (in this case
                .processWith(message -> {
                    ByteBuffer payload = message.payload();
                    byte[] data = new byte[payload.remaining()];
                    message.payload().get(data);
                    String stringData = new String(data);
                    Gson gson = new Gson();
                    System.out.println(stringData);
                    Map<String, String> load = gson.fromJson(stringData, Map.class);

                    return new Object[] {
                            load.get("msg"),
                            load.get("ip"),
                            load.get("name")
                    };
                })
                // Send data to VoltDB
                .terminateWithSink(
                        ProcedureVoltSinkConfigBuilder.builder()
                                .withVoltClientResourceName("primary-cluster")
                                .withProcedureName("GREETINGS.insert")
                );
    }
}
