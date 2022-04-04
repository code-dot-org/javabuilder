import ws from "k6/ws";
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";
import { helloWorld } from "./sources.js";
import {
  LONG_REQUEST_MS,
  EXTRA_LONG_REQUEST_MS,
  BASIC_TEST_OPTIONS,
  MiniAppType,
  UPLOAD_URL,
  UPLOAD_PARAMS,
  WEBSOCKET_URL,
  WEBSOCKET_PARAMS
} from "./configuration.js";
import generateToken from "./generateToken.js";

export const options = BASIC_TEST_OPTIONS;

const exceptionCounter = new Counter("exceptions");
const errorCounter = new Counter("errors");
const connectToCloseTime = new Trend(
  "websocket_session_duration_without_sleep",
  true
);
// websocket sessions > 5 seconds
const longWebsocketSessions = new Counter("long_websocket_sessions");
// websocket sessions > 10 seconds
const extraLongWebsocketSessions = new Counter("extra_long_websocket_sessions");

export default function () {
  const authToken = generateToken(MiniAppType.CONSOLE);
  const uploadResult = http.put(
    UPLOAD_URL + authToken,
    helloWorld,
    UPLOAD_PARAMS
  );
  const res = ws.connect(WEBSOCKET_URL + authToken, WEBSOCKET_PARAMS, (socket) =>
    onSocketConnect(socket, Date.now())
  );

  check(res, { "websocket status is 101": (r) => r && r.status === 101 });

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
    if (time > LONG_REQUEST_MS) {
      longWebsocketSessions.add(1);
    }
    if (time > EXTRA_LONG_REQUEST_MS) {
      extraLongWebsocketSessions.add(1);
    }
    sleep(15);
  });

  socket.on("error", function (e) {
    console.log("error occurred: " + e.error());
    errorCounter.add(1);
  });
}
