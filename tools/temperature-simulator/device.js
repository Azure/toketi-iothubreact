// Copyright (c) Microsoft. All rights reserved.

'use strict';

var Client = require('azure-iot-device').Client;
var Message = require('azure-iot-device').Message;

function TemperatureSimulator (name, connectionString, protocol, frequency) {
  this.name             = name;
  this.connectionString = connectionString;
  this.protocol         = protocol;
  this.frequency        = frequency;
  this.connectionStatus = 'disconnected';
  this.clock            = null;
}

TemperatureSimulator.prototype.getConnectCallback = function() {
  var self = this;
  return function(err) {
    if (err) {
      self.connectionStatus = 'failed';
      console.error('['+self.name+'] Could not connect: ' + err.message);
    } else {
      if (self.connectionStatus != 'connecting') return;
      self.connectionStatus = 'connected';

      console.log('['+self.name+'] Client connected');

      self.client.on('message', function (msg) {
        console.log('['+self.name+'] Id: ' + msg.messageId + ' Body: ' + msg.data);
        self.client.complete(msg, printResultFor('completed'));
      });

      self.client.on('error', function (err) {
        console.error('['+self.name+'] '+err.message);
      });

      self.client.on('disconnect', function () {
        clearInterval(self.clock);
        self.connectionStatus = 'disconnected';
        console.log('['+self.name+'] Disconnected.');
        self.client.removeAllListeners();
        self.client.open(connectCallback);
      });
    }
  }
};

TemperatureSimulator.prototype.connect = function() {
  this.connectionStatus = 'connecting';
  console.log(this.connectionString)
  this.client = Client.fromConnectionString(this.connectionString, this.protocol);
  this.client.open(this.getConnectCallback());
};

TemperatureSimulator.prototype.startSending = function() {
  var self = this;
  this.clock = setInterval(function () {
    // Verify if the client is connected yet
    if (self.connectionStatus == 'connecting') {
      console.error('['+self.name+'] The client is not connected yet... ['+self.connectionStatus+']');
      return;
    }

    if (self.connectionStatus != 'connected') {
      console.error('['+self.name+'] The client could not connect ['+self.connectionStatus+']');
      return;
    }

    // Create a random value message and send it to the IoT Hub
    var temp = 32 + (Math.random() * 180); // range: [32, 212]
    var data = JSON.stringify({ value: temp, time: new Date().toISOString() });
    var message = new Message(data);
    console.log('['+self.name+'] Sending message: ' + message.getData());
    self.client.sendEvent(message, self.printResultFor('send', self.name));
  }, self.frequency);
};

TemperatureSimulator.prototype.printResultFor = function(op, name) {
  return function printResult(err, res) {
    if (err) console.log('['+name+'] ' + op + ' error: ' + err.toString());
    if (res) console.log('['+name+'] ' + op + ' status: ' + res.constructor.name);
  };
};

module.exports = TemperatureSimulator;
