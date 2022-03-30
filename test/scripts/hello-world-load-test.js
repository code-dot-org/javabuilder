import ws from "k6/ws";
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";
import { helloWorld } from "./sources.js";

export const options = {
  scenarios: {
    // ramp up to 5 VUs over 30 seconds
    // rampUp: {
    //   executor: "ramping-vus",
    //   startVUs: 0,
    //   stages: [
    //     {duration: '1m', target: 5}
    //   ]
    // },
    // have 5 VUs do 9 iterations each, up to a max of 3 minutes. Start this after
    // the ramp up time.
    highLoad: {
      executor: "per-vu-iterations",
      vus: 1,
      iterations: 3,
      maxDuration: "3m"
    }
  },
  thresholds: {
    exceptions: ["count == 0"],
    errors: ["count == 0"],
    websocket_session_duration_without_sleep: ["p(95) < 5000"],
    long_websocket_sessions: ["count <= 1"],
    extra_long_websocket_sessions: ["count == 0"]
  },
  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(98)", "p(99)"],
};

const exceptionCounter = new Counter("exceptions");
const errorCounter = new Counter("errors");
const connectToCloseTime = new Trend(
  "websocket_session_duration_without_sleep",
  true
);
const longWebsocketSessions = new Counter("long_websocket_sessions");
const extraLongWebsocketSessions = new Counter("extra_long_websocket_sessions");

const uploadUrl = `https://javabuilder-molly-http.dev-code.org/seedsources/sources.json?Authorization=`;
const url = `wss://javabuilder-molly.dev-code.org?Authorization=`;
const origin = "http://localhost-studio.code.org:3000";
  
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

const privateKey = __ENV.AUTH_KEY;

export default function () {
  if (privateKey != null) {
    console.log("found private key!");
  } else {
    console.log("did not find private key!");
  }
  const authToken = "placeholder";
  const uploadResult = http.put(uploadUrl + authToken, helloWorld, uploadParams);
  const res = ws.connect(url + authToken, websocketParams, (socket) =>
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
    const time = Date.now() - startTime;
    connectToCloseTime.add(time);
    if (time > 5000) {
      longWebsocketSessions.add(1);
    }
    if (time > 10000) {
      extraLongWebsocketSessions.add(1);
    }
    sleep(15);
  });

  socket.on("error", function (e) {
    console.log("error occurred: " + e.error());
    errorCounter.add(1);
  });
}
