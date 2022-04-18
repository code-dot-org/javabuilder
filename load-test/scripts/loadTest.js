import ws from "k6/ws";
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Trend } from "k6/metrics";
import { scanner } from "./sources.js";
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
  /* User goal */ 30000,
  /* High load time minutes */ 4
);

// Change this to test different code
const SOURCE_TO_TEST = scanner;
// Set this to true to space out requests every REQUEST_TIME_MS milliseconds. Set to
// false to send as many requests as possible.
const SHOULD_SLEEP = false;

const exceptionCounter = new Counter("exceptions");
const errorCounter = new Counter("errors");
const timeoutCounter = new Counter("timeouts");
const totalSessionTime = new Trend("total_session_time", true);
const sessionsOver10Seconds = new Counter("session_over_10_seconds");
const sessionsOver15Seconds = new Counter("session_over_15_seconds");
const sessionsOver20Seconds = new Counter("session_over_20_seconds");
const retryCounters = [new Counter("sessions_with_0_retries"), new Counter("sessions_with_1_retry"), new Counter("sessions_with_2_retries")];
const responseTime = new Trend("response_time", true);
const notSent = new Counter("not_sent");
const noResponse = new Counter("no_response");


function isResultSuccess(result) {
  return result && result.status === 200;
}

export default function () {
  const requestStartTime = Date.now();
  const sessionId = getRandomId();
  const authToken = generateToken(MiniAppType.CONSOLE, sessionId);
  const uploadResult = http.put(
    UPLOAD_URL + authToken,
    SOURCE_TO_TEST,
    UPLOAD_PARAMS
  );

  check(uploadResult, { "upload status is 200": (r) => isResultSuccess(r)});

  if (isResultSuccess(uploadResult)) {
    let res = connectToWebsocketWithRetry(authToken, sessionId, requestStartTime);
    check(res, { "websocket status is 101": (r) => r && r.status === 101 });
  } else {
    console.log(`ERROR upload failed for session id ${sessionId}`);
  }
}

function connectToWebsocketWithRetry(authToken, sessionId, requestStartTime) {
  let res = null;
  let tries = 0;
  let shouldRetry = true;
  while(tries < 3 && shouldRetry) {
    if (tries > 0) {
      // before the first retry sleep for 1 second, before the second retry sleep for 2 seconds.
      console.log(`RETRY ${tries} for session id ${sessionId}`);
      sleep(1 * tries);
    }
    res = connectToWebsocket(authToken, sessionId, requestStartTime);
    if (res != null) {
      shouldRetry = false;
    } 
    tries++;
  }
  retryCounters[tries - 1].add(1);
  return res;
}

function connectToWebsocket(authToken, sessionId, requestStartTime) {
  let res = null;
  let responseTime = null;
  try {
    res = ws.connect(WEBSOCKET_URL + authToken, WEBSOCKET_PARAMS, (socket) =>
      onSocketConnect(socket, requestStartTime, Date.now(), sessionId)
    );
  } catch(error) {
    console.log(`ERROR ${error} for session id ${sessionId}`);
  }
  return res;
}


// pass in onMessage
// need to keep track, per socket, of:
// {sendAt: date, respondedAt: date}
// in a way that onClose can access
// also contain k6 metrics?
function onSocketConnect(socket, requestStartTime, websocketStartTime, sessionId) {
  let sentAt, respondedAt;

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

    if (parsedData.type === "SYSTEM_OUT" && parsedData.value === "What's your name?") {
      const message = JSON.stringify({
        messageType: "SYSTEM_IN",
        message: "Ben"
      });
      socket.send(message);
      sentAt = Date.now();
    }

    if (parsedData.type === "SYSTEM_OUT" && parsedData.value === "Hello Ben!") {
      respondedAt = Date.now();
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

      if (!sentAt) {
        notSent.add(1);
      } else if (!respondedAt) {
        noResponse.add(1);
      } else {
        responseTime.add(respondedAt - sentAt);
      }
    } else {
      console.log(`TIMEOUT detected for session id ${sessionId}`);
      timeoutCounter.add(1);
    }
    
    // Sleep this VU if we are under the max request time. This is so we maintain
    // a reasonable number of total requests across all virtual users.
    if (SHOULD_SLEEP) {
      const sleepTime = Math.floor((REQUEST_TIME_MS - totalTime) / 1000);
      if (sleepTime > 0) {
        sleep(sleepTime);
      }
    }
  });

  socket.on("error", function (e) {
    console.log(`ERROR on websocket request for session id ${sessionId} ` + e.error());
    errorCounter.add(1);
  });
}
