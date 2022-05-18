function getSecretString(payload) {
  if (!payload) {
    return "";
  }

  const json = JSON.parse(payload);
  return json["SecretString"];
}

export const PRIVATE_KEY = getSecretString(process.env.JAVABUILDER_PRIVATE_KEY);
export const PASSWORD = getSecretString(process.env.JAVABUILDER_PASSWORD);

export const JAVABUILDER_HTTP_URL = process.env.JAVABUILDER_HTTP_URL;
export const JAVABUILDER_WEBSOCKET_URL = process.env.JAVABUILDER_WEBSOCKET_URL;

export const JAVABUILDER_BASE_DOMAIN = process.env.JAVABUILDER_BASE_DOMAIN;
export const JAVABUILDER_SUB_DOMAIN = process.env.JAVABUILDER_SUB_DOMAIN;
