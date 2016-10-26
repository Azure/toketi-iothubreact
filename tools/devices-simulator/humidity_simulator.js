// Copyright (c) Microsoft. All rights reserved.

"use strict";

var AbstractSimulator = require("./abstract_simulator.js");

// Inheritance
var HumiditySimulator = function () {
    AbstractSimulator.apply(this, arguments);
    this.model = "humidity";

    // (70-30) .. (70+30) => 40 .. 100
    this.mid = 70;
    this.range = 30;
    this.peak = this.range * Math.random();
};

HumiditySimulator.prototype = Object.create(AbstractSimulator.prototype);
HumiditySimulator.prototype.constructor = HumiditySimulator;

// Implement generateData abstract
HumiditySimulator.prototype.generateData = function () {

    var d = new Date();
    var h = d.getHours();
    var m = d.getMinutes();
    var s = d.getSeconds();
    var sec = ((((h * 60) + m) * 60 + s) + 180) % 360;

    // Trend changes when Math.sin() = 0
    if (sec === 0 || sec === 180) {
        this.peak = this.range * Math.random();
    }

    // Sinusoidal values
    var rad = sec * Math.PI / 180;
    var measure = this.mid + Math.sin(rad) * this.peak;

    // Truncate after 1st decimal
    measure = Math.floor(measure * 10) / 10;

    return JSON.stringify({
        value: measure,
        time: new Date().toISOString()
    });
};

module.exports = HumiditySimulator;
