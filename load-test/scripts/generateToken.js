import crypto from "k6/crypto";
import encoding from "k6/encoding";
import {
  uuidv4,
} from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';
import { PRIVATE_KEY } from "./configuration.js";

const algToHash = {
    HS256: "sha256",
    HS384: "sha384",
    HS512: "sha512"
};

export default function generateToken(miniAppType) {
  const issuedAtTime = (Date.now() / 1000) - 3;
  const expirationTime = issuedAtTime + 600;
  const payload = {
    iat: issuedAtTime,
    iss: "load-test-dev",
    exp: expirationTime,
    uid: uuidv4(),
    level_id: "none",
    execution_type: "RUN",
    mini_app_type: miniAppType,
    verified_teachers: uuidv4(),
    options: "{}",
    sid: uuidv4()
  };
  let token = encode(payload);
  console.log("token: " + token);
  return token;
}

function sign(data) {
    let hash = crypto.hmac('sha256', PRIVATE_KEY, data, 'base64rawurl');
    return hash;
}

function encode(payload) {
    let algorithm = "HS256";
    let header = encoding.b64encode(JSON.stringify({ typ: "JWT", alg: algorithm }), "rawurl");
    let payloadEncoded = encoding.b64encode(JSON.stringify(payload), "rawurl");
    let contentToEncode = header + "." + payloadEncoded;
    let sig = sign(contentToEncode, algToHash[algorithm]);
    return [header, payloadEncoded, sig].join(".");
}
