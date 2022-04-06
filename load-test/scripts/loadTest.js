import ws from "k6/ws";
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";
import { helloWorld } from "./sources.js";
import {
  LONG_REQUEST_MS,
  EXTRA_LONG_REQUEST_MS,
  MiniAppType,
  UPLOAD_URL,
  UPLOAD_PARAMS,
  WEBSOCKET_URL,
  WEBSOCKET_PARAMS,
  getTestOptions
} from "./configuration.js";
import generateToken from "./generateToken.js";

// Change these options to increase the user goal or time to run the test.
export const options = getTestOptions(
  /* User goal */ 100,
  /* Ramp up time minutes */ 1,
  /* High load time minutes */ 1
);

// Change this to test different code
const sourceToTest = helloWorld;

const exceptionCounter = new Counter("exceptions");
const errorCounter = new Counter("errors");
const connectToCloseTime = new Trend(
  "websocket_session_duration_without_sleep",
  true
);
// websocket sessions > LONG_REQUEST_MS
const longWebsocketSessions = new Counter("long_websocket_sessions");
// websocket sessions > EXTRA_LONG_REQUEST_MS
const extraLongWebsocketSessions = new Counter("extra_long_websocket_sessions");


function isResultSuccess(result) {
  return result && result.status === 200;
}

export default function () {
  const authToken = generateToken(MiniAppType.CONSOLE);
  const uploadResult = http.put(
    UPLOAD_URL + authToken,
    sourceToTest,
    UPLOAD_PARAMS
  );

  check(uploadResult, { "upload status is 200": (r) => isResultSuccess(r)});

  if (isResultSuccess(uploadResult)) {
    const res = ws.connect(WEBSOCKET_URL + authToken, WEBSOCKET_PARAMS, (socket) =>
      onSocketConnect(socket, Date.now())
    );

    check(res, { "websocket status is 101": (r) => r && r.status === 101 });
  }
}

function onSocketConnect(socket, startTime) {
  socket.on("open", () => {});

  socket.on("message", function (data) {
    const parsedData = JSON.parse(data);
    if (parsedData.type === "EXCEPTION") {
      console.log("[EXCEPTION] " + parsedData.value);
      exceptionCounter.add(1);
    }
  });

  socket.on("close", () => {
    const time = Date.now() - startTime;
    connectToCloseTime.add(time);
    if (time > LONG_REQUEST_MS) {
      longWebsocketSessions.add(1);
    }
    if (time > EXTRA_LONG_REQUEST_MS) {
      extraLongWebsocketSessions.add(1);
    }
    const sleepTime = Math.floor(20 - (time / 1000));
    if (sleepTime > 0) {
      sleep(sleepTime);
    }
  });

  socket.on("error", function (e) {
    console.log("[ERROR] " + e.error());
    errorCounter.add(1);
  });
}
