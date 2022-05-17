import connectionHelper from "../lib/JavabuilderConnectionHelper.js";
import {expect} from "chai";

/**
 * Helper for verifying the basic case that set of messages was received
 * from Javabuilder. Fails if there are any errors connecting.
 *
 * @param {*} sourcesJson sources (see sources.js)
 * @param {*} miniAppType mini-app type (see MiniAppType.js)
 * @param {*} assertOnMessagesReceived a verification callback to be called once all messages have been received
 * @param {*} doneCallback Mocha's 'done' callback
 */
export const verifyMessagesReceived = (
  sourcesJson,
  miniAppType,
  assertOnMessagesReceived,
  doneCallback
) => {
  const receivedMessages = [];

  const onMessage = (event) => {
    receivedMessages.push(JSON.parse(event.data));
  };
  const onError = (error) => {
    doneCallback(new Error(error.message));
  };
  const onClose = (event) => {
    expect(event.wasClean).to.be.true;
    assertOnMessagesReceived(receivedMessages);
    doneCallback();
  };

  connectionHelper
    .connect(sourcesJson, miniAppType, () => {}, onMessage, onError, onClose)
    .catch((err) => {
      doneCallback(err);
    });
};

export const assertMessagesEqual = (receivedMessages, expectedMessages, verifyDetailKey) => {
  expect(receivedMessages.length).to.equal(expectedMessages.length);
  expectedMessages.forEach((expectedMessage, index) => {
    const receivedMessage = receivedMessages[index];
    expect(receivedMessage.type).to.equal(expectedMessage.type);
    expect(receivedMessage.value).to.equal(expectedMessage.value);

    if (verifyDetailKey && expectedMessage.detail) {
      Object.keys(expectedMessage.detail).forEach(key => verifyDetailKey(key, receivedMessage, expectedMessage));
    }
  });
}

export const INITIAL_STATUS_MESSAGES = [
  {type: "STATUS", value: "COMPILING"},
  {type: "STATUS", value: "COMPILATION_SUCCESSFUL"},
  {type: "STATUS", value: "RUNNING"}
];

export const EXIT_STATUS_MESSAGE = {type: "STATUS", value: "EXITED"};
