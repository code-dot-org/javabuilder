import ws from "k6/ws";
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";
import { helloWorld } from "./sources.js";

export const options = {
  scenarios: {
    // ramp up to 5 VUs over 30 seconds
    rampUp: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        {duration: '30s', target: 5}
      ]
    },
    // have 5 VUs do 9 iterations each, up to a max of 3 minutes. Start this after
    // the ramp up time.
    highLoad: {
      executor: "per-vu-iterations",
      vus: 5,
      iterations: 9,
      maxDuration: "3m",
      startTime: '31s'
    }
  },
  thresholds: {
    exceptions: ["count == 0"],
    errors: ["count == 0"],
    websocket_session_duration_without_sleep: ["p(95) < 5000"],
  },
  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(98)", "p(99)"],
};

const exceptionCounter = new Counter("exceptions");
const errorCounter = new Counter("errors");
const connectToCloseTime = new Trend(
  "websocket_session_duration_without_sleep",
  true
);

const uploadUrl = `https://javabuilder-molly-http.dev-code.org/seedsources/sources.json?Authorization=${__ENV.AUTH_TOKEN}`;
const url = `wss://javabuilder-molly.dev-code.org?Authorization=${__ENV.AUTH_TOKEN}`;
const origin = __ENV.AUTH_ORIGIN
  ? __ENV.AUTH_ORIGIN
  : "http://localhost-studio.code.org:3000";
  
const websocketParams = {
  headers: {
    Origin: origin,
  },
};

const uploadParams = {
  headers: {
    Origin: origin,
    "Content-Type": "application/json",
  },
};

export default function () {
  const uploadResult = http.put(uploadUrl, helloWorld, uploadParams);
  const res = ws.connect(url, websocketParams, (socket) =>
    onSocketConnect(socket, Date.now())
  );

  check(res,
    { 'websocket status is 101': (r) => r && r.status === 101 }
  );

  check(uploadResult, { "upload status is 200": (r) => r && r.status === 200 });
}

function onSocketConnect(socket, startTime) {
  socket.on("open", () => {});

  socket.on("message", function (data) {
    const parsedData = JSON.parse(data);
    if (parsedData.type === "EXCEPTION") {
      console.log("hit an exception " + parsedData.value);
      exceptionCounter.add(1);
    }
  });

  socket.on("close", () => {
    connectToCloseTime.add(Date.now() - startTime);
    sleep(10);
  });

  socket.on("error", function (e) {
    console.log("error occurred: " + e.error());
    errorCounter.add(1);
  });
}
