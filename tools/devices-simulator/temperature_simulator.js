// Copyright (c) Microsoft. All rights reserved.

"use strict";

var AbstractSimulator = require("./abstract_simulator.js");

// Inheritance
var TemperatureSimulator = function () {
    AbstractSimulator.apply(this, arguments);
    this.messageType = "temperature";

    // (15-25) .. (15+25) => -10 .. 40
    this.mid = 15;
    this.range = 25;
    this.peak = this.range * Math.random();
};

TemperatureSimulator.prototype = Object.create(AbstractSimulator.prototype);
TemperatureSimulator.prototype.constructor = TemperatureSimulator;

// Implement generateData abstract
TemperatureSimulator.prototype.generateData = function () {

    var d = new Date();
    var h = d.getHours();
    var m = d.getMinutes();
    var s = d.getSeconds();
    var sec = (((h * 60) + m) * 60 + s) % 360;

    // Trend changes when Math.sin() = 0
    if (sec === 0 || sec === 180) {
        this.peak = this.range * Math.random();
    }

    // Sinusoidal values
    var rad = sec * Math.PI / 180;
    var measure = this.mid + Math.sin(rad) * this.peak;

    // Truncate after 1st decimal
    measure = Math.floor(measure * 10) / 10;

    // Convert to Fahrenheit
    // measure = measure * 1.8 + 32

    return JSON.stringify({
        value: measure,
        time: new Date().toISOString()
    });
};

module.exports = TemperatureSimulator;
