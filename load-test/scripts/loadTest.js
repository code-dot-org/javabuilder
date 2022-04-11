import ws from "k6/ws";
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";
import { helloWorld } from "./sources.js";
import { getRandomId, generateToken } from "./tokenHelpers.js";
import {
  MiniAppType,
  UPLOAD_URL,
  UPLOAD_PARAMS,
  WEBSOCKET_URL,
  WEBSOCKET_PARAMS,
  getTestOptions,
  TIMEOUT_MS,
  REQUEST_TIME_MS
} from "./configuration.js";

// Change these options to increase the user goal or time to run the test.
export const options = getTestOptions(
  /* User goal */ 1000,
  /* High load time minutes */ 15
);

// Change this to test different code
const sourceToTest = helloWorld;

const exceptionCounter = new Counter("exceptions");
const errorCounter = new Counter("errors");
const timeoutCounter = new Counter("timeouts");
const totalSessionTime = new Trend("total_session_time", true);
const sessionsOver10Seconds = new Counter("session_over_10_seconds");
const sessionsOver15Seconds = new Counter("session_over_15_seconds");
const sessionsOver20Seconds = new Counter("session_over_20_seconds");


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
      console.log(`Triggering TIMEOUT for session id ${sessionId}, request has gone longer than ${TIMEOUT_MS} ms.`);
      socket.close();
    }, TIMEOUT_MS);
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
    if (websocketTime < TIMEOUT_MS) {
      // only log requests that didn't time out, as timeouts are a separate metric.
      totalSessionTime.add(totalTime);
      if (totalTime > 20000) {
        console.log(`OVER 20 SECONDS Session id ${sessionId} had a request time of ${totalTime} ms.`);
        sessionsOver20Seconds.add(1);
      } else if (totalTime > 15000) {
        console.log(`OVER 15 SECONDS Session id ${sessionId} had a request time of ${totalTime} ms.`);
        sessionsOver15Seconds.add(1);
      } else if (totalTime > 10000) {
        console.log(`OVER 10 SECONDS Session id ${sessionId} had a request time of ${totalTime} ms.`);
        sessionsOver10Seconds.add(1);
      }
    } else {
      console.log(`TIMEOUT detected for session id ${sessionId}`);
      timeoutCounter.add(1);
    }
    
    // Sleep this VU if we are under the max request time. This is so we maintain
    // a reasonable number of total requests across all virtual users.
    const sleepTime = Math.floor((REQUEST_TIME_MS - totalTime) / 1000);
    if (sleepTime > 0) {
      sleep(sleepTime);
    }
  });

  socket.on("error", function (e) {
    console.log(`ERROR on websocket request for session id ${sessionId} ` + e.error());
    errorCounter.add(1);
  });
}
