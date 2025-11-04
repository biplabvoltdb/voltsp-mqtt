# voltsp-mqtt

This project demonstrates the integration between **MQTT** and **VoltSP + VoltDB**.  
It provides an example pipeline that consumes messages from an MQTT broker and inserts them into a VoltDB table using VoltSP.

---

## Overview

The main source file for this demo is:

src/main/java/org/voltsp/mqtt/demo/MQTTtoVoltPipeline.java


This file defines the pipeline logic used to:
- Consume messages from an MQTT broker.
- Process them using VoltSP.
- Insert them into VoltDB via a stored procedure call.

---

## Requirements

To run this demo successfully, ensure you have the following components set up:

1. **VoltDB** (or Volt Active Data) running as a sink target with the appropriate schema to support the VoltSP pipeline.  
2. An **MQTT Broker** (e.g. [EMQX](https://www.emqx.io/)) that can receive and forward MQTT messages.  
3. An **MQTT Producer** that publishes data to the broker topic your pipeline subscribes to.  
4. **Java** for building and running the project.

---

## Configuration

The following definitions inside the pipeline specify how the MQTT source connects and consumes data:

```java
.withTopicFilter("testtopicvolt/#")
.withGroupName("Mygroup")
.withAddressHost("broker.emqx.io")
.withAddressPort(1883)
.withQos(MqttMessageQoS.AT_LEAST_ONCE)
****
