import ws from "k6/ws";
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";
import { helloWorld } from "./sources.js";
import { getRandomId, generateToken } from "./tokenHelpers.js";
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

// Change these options to increase the user goal or time to run the test.
export const options = getTestOptions(
  /* User goal */ 1000,
  /* Ramp up time minutes */ 5,
  /* High load time minutes */ 10
);

// Change this to test different code
const sourceToTest = helloWorld;
// Timeout is we go greater than the max request time to ensure we stay
// close to our concurrent user goal.
const MAX_REQUEST_TIME_MS = 20000;

const exceptionCounter = new Counter("exceptions");
const errorCounter = new Counter("errors");
const timeoutCounter = new Counter("timeouts");
const totalRequestTime = new Trend("total_request_time", true);
// websocket sessions > LONG_REQUEST_MS
const longWebsocketSessions = new Counter("long_websocket_sessions");
// websocket sessions > EXTRA_LONG_REQUEST_MS
const extraLongWebsocketSessions = new Counter("extra_long_websocket_sessions");


function isResultSuccess(result) {
  return result && result.status === 200;
}

export default function () {
  const requestStartTime = Date.now();
  const sessionId = getRandomId();
  const authToken = generateToken(MiniAppType.CONSOLE, sessionId);
  const uploadResult = http.put(
    UPLOAD_URL + authToken,
    sourceToTest,
    UPLOAD_PARAMS
  );

  check(uploadResult, { "upload status is 200": (r) => isResultSuccess(r)});

  if (isResultSuccess(uploadResult)) {
    const res = ws.connect(WEBSOCKET_URL + authToken, WEBSOCKET_PARAMS, (socket) =>
      onSocketConnect(socket, requestStartTime, Date.now(), sessionId)
    );

    check(res, { "websocket status is 101": (r) => r && r.status === 101 });
  } else {
    console.log(`ERROR upload failed for session id ${sessionId}`);
  }
}

function onSocketConnect(socket, requestStartTime, websocketStartTime, sessionId) {
  socket.on("open", () => {
    socket.setTimeout(() => {
      console.log(`Triggering TIMEOUT for session id ${sessionId}, request has gone longer than ${MAX_REQUEST_TIME_MS} ms.`);
      socket.close();
    }, MAX_REQUEST_TIME_MS);
  });

  socket.on("message", function (data) {
    const parsedData = JSON.parse(data);
    if (parsedData.type === "EXCEPTION") {
      console.log(`EXCEPTION for session id ${sessionId} ` + parsedData.value);
      exceptionCounter.add(1);
    }
  });

  socket.on("close", () => {
    const websocketTime = Date.now() - websocketStartTime;
    const totalTime = Date.now() - requestStartTime;
    if (websocketTime < MAX_REQUEST_TIME_MS) {
      // only log requests that didn't time out, as timeouts are a separate metric.
      totalRequestTime.add(totalTime);
      if (totalTime > EXTRA_LONG_REQUEST_MS) {
        console.log(`EXTRA LONG REQUEST Session id ${sessionId} had a request time of ${totalTime} ms.`);
        extraLongWebsocketSessions.add(1);
      } else if (totalTime > LONG_REQUEST_MS) {
        console.log(`LONG REQUEST Session id ${sessionId} had a request time of ${totalTime} ms.`);
        longWebsocketSessions.add(1);
      }
    } else {
      console.log(`TIMEOUT detected for session id ${sessionId}`);
      timeoutCounter.add(1);
    }
    
    const sleepTime = Math.floor((MAX_REQUEST_TIME_MS - totalTime) / 1000);
    if (sleepTime > 0) {
      sleep(sleepTime);
    }
  });

  socket.on("error", function (e) {
    console.log(`ERROR on websocket request for session id ${sessionId} ` + e.error());
    errorCounter.add(1);
  });
}
