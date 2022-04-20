import crypto from "k6/crypto";
import encoding from "k6/encoding";
import {
  uuidv4,
} from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';
import { PRIVATE_KEY } from "./configuration.js";

// Generate a JWT token for the given mini app type, with random
// user id, teacher id and session id. The token has a time to live of 1 minute.
export function generateToken(miniAppType, sessionId) {
  const issuedAtTime = (Date.now() / 1000) - 3;
  const expirationTime = issuedAtTime + 63;
  const payload = {
    iat: issuedAtTime,
    iss: "load-test",
    exp: expirationTime,
    uid: getRandomId(),
    level_id: "none",
    execution_type: "RUN",
    mini_app_type: miniAppType,
    verified_teachers: getRandomId(),
    options: "{}",
    sid: sessionId
  };
  return encodeAsJWT(payload);
}

export function getRandomId() {
  return uuidv4();
}

// Generate a JWT using the HS256 algorithm, which relies on a shared secret
// between the sender and receiver. 
// To keep the load test script small we can't use npm packages that do this for us,
// so we need to generate the token ourselves using k6's crypto package.
// Logic is modified from: https://gist.github.com/robingustafsson/7dd6463d85efdddbb0e4bcd3ecc706e1
// JWT details: https://jwt.io/introduction
function encodeAsJWT(payload) {
    let algorithm = "HS256";
    let header = encoding.b64encode(JSON.stringify({ typ: "JWT", alg: algorithm }), "rawurl");
    let payloadEncoded = encoding.b64encode(JSON.stringify(payload), "rawurl");
    let contentToEncode = header + "." + payloadEncoded;
    let signature = sign(contentToEncode, "sha256");
    return [header, payloadEncoded, signature].join(".");
}

// Sign the given data using the HMAC SHA256 algorithm.
function sign(data) {
  return crypto.hmac('sha256', PRIVATE_KEY, data, 'base64rawurl');
}
