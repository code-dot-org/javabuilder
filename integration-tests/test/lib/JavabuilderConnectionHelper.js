import { v4 as uuidv4 } from "uuid";
import fetch from "node-fetch";
import jwt from "jsonwebtoken";
import crypto from "crypto";
import WebSocket from "ws";
import {
  JAVABUILDER_HTTP_URL,
  JAVABUILDER_WEBSOCKET_URL,
  PASSWORD,
  PRIVATE_KEY,
} from "./environment.js";

const INTEGRATION_TESTS_ORIGIN = "integration-tests";
const INTEGRATION_TESTS_SESSION_ID_PREFIX = "integrationTests-";

/**
 * Helper class for facilitating a connection to a Javabuilder instance
 */
class JavabuilderConnectionHelper {
  async connect(sourcesJson, miniAppType, onOpen, onMessage, onError, onClose, generateToken = generateJavabuilderToken, httpErrorOverride) {
    const sessionId = INTEGRATION_TESTS_SESSION_ID_PREFIX + getRandomId();
    const token = generateToken(miniAppType, sessionId);
    console.info(
      `
      Connecting to Javabuilder...
      HTTP URL: ${JAVABUILDER_HTTP_URL}
      WebSocket URL: ${JAVABUILDER_WEBSOCKET_URL}
      Session ID: ${sessionId}
      `
    );

    const httpResponse = await fetch(
      `${JAVABUILDER_HTTP_URL}?Authorization=${token}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Origin: INTEGRATION_TESTS_ORIGIN,
        },
        body: sourcesJson,
      }
    );

    if (!httpResponse.ok) {
      if (httpErrorOverride) {
        httpErrorOverride(httpResponse);
      } else {
        throw new Error(`HTTP API error: ${httpResponse.statusText}`);
      }
    }

    const socket = new WebSocket(
      `${JAVABUILDER_WEBSOCKET_URL}?Authorization=${token}`,
      {
        origin: INTEGRATION_TESTS_ORIGIN,
      }
    );

    const logOnOpen = () => {
      console.info("Connected!");
      onOpen();
    };

    const onMessageWrapper = event => onMessage(event, socket);

    socket.onopen = logOnOpen;
    socket.onmessage = onMessageWrapper;
    socket.onclose = onClose;
    socket.onerror = onError;
  }
}

const getRandomId = () => uuidv4();

function generateJavabuilderToken(miniAppType, sessionId) {
  const issuedAtTime = Date.now() / 1000 - 3;
  const expirationTime = issuedAtTime + 63;
  const payload = {
    iat: issuedAtTime,
    iss: "integration-tests",
    exp: expirationTime,
    uid: getRandomId(),
    level_id: "none",
    execution_type: "RUN",
    mini_app_type: miniAppType,
    verified_teachers: getRandomId(),
    options: "{}",
    sid: sessionId,
    can_access_dashboard_assets: false
  };

  const key = crypto.createPrivateKey({
    key: PRIVATE_KEY,
    passphrase: PASSWORD,
  });

  return jwt.sign(payload, key, {
    algorithm: "RS256",
  });
}

const connectionHelper = new JavabuilderConnectionHelper();
export default connectionHelper;
