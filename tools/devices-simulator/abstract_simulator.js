// Copyright (c) Microsoft. All rights reserved.

"use strict";

var Client = require("azure-iot-device").Client;
var Message = require("azure-iot-device").Message;
var debug = false;

/**
 @abstract
 @constructor
 */
function AbstractSimulator(hubName, deviceId, accessKey, protocol, frequency) {
    if (this.constructor === AbstractSimulator) {
        throw new Error("AbstractSimulator is an abstract class, cannot create instance");
    }

    this.name = deviceId;
    this.connectionString = "HostName=" + hubName + ".azure-devices.net;DeviceId=" + deviceId + ";SharedAccessKey=" + accessKey;
    this.protocol = protocol;
    this.frequency = frequency;
    this.connectionStatus = "disconnected";
    this.clock = null;
    this.messageSchema = "";
}

/**
 @abstract
 */
AbstractSimulator.prototype.generateData = function () {
    throw new Error("abstract method 'generateMessage' not implemented. Must return a <JSON string> with the data (e.g. using JSON.stringify).");
};

AbstractSimulator.prototype.getConnectCallback = function () {
    var self = this;
    return function (err) {
        if (err) {
            self.connectionStatus = "failed";
            console.error("[" + self.name + "] Could not connect: " + err.message);
        } else {
            if (self.connectionStatus !== "connecting") {
                return;
            }
            self.connectionStatus = "connected";

            console.log("[" + self.name + "] Client connected");

            self.client.on("message", function (msg) {
                console.log("[" + self.name + "] Id: " + msg.messageId + " Body: " + msg.data);
                self.client.complete(msg, self.printResultFor("completed"));
            });

            self.client.on("error", function (err) {
                console.error("[" + self.name + "] " + err.message);
            });

            self.client.on("disconnect", function () {
                clearInterval(self.clock);
                self.connectionStatus = "disconnected";
                console.log("[" + self.name + "] Disconnected.");
                self.client.removeAllListeners();
                self.client.open(this.getConnectCallback());
            });
        }
    };
};

AbstractSimulator.prototype.connect = function () {
    this.connectionStatus = "connecting";
    this.client = Client.fromConnectionString(this.connectionString, this.protocol);
    this.client.open(this.getConnectCallback());
};

AbstractSimulator.prototype.startSending = function () {
    var self = this;
    this.clock = setInterval(function () {
        // Verify if the client is connected yet
        if (self.connectionStatus === "connecting") {
            console.error("[" + self.name + "] The client is not connected yet... [" + self.connectionStatus + "]");
            return;
        }

        if (self.connectionStatus !== "connected") {
            console.error("[" + self.name + "] The client could not connect [" + self.connectionStatus + "]");
            return;
        }

        var message = new Message(self.generateData());
        message.properties.add("$$contentType", "json");
        if (self.messageSchema !== "") {
            message.properties.add("$$MessageSchema", self.messageSchema);
            console.log("[" + self.name + "] Sending " + self.messageSchema + ": " + message.getData());
        } else {
            console.log("[" + self.name + "] Sending message: " + message.getData());
        }

        self.client.sendEvent(message, self.printResultFor("send", self.name));
    }, self.frequency);
};

AbstractSimulator.prototype.printResultFor = function (op, name) {
    return function printResult(err, res) {
        if (err) {
            console.log("[" + name + "] " + op + " error: " + err.toString());
        }
        if (res) {
            if (debug) console.log("[" + name + "] " + op + " status: " + res.constructor.name);
        }
    };
};

module.exports = AbstractSimulator;
