import connectionHelper from "../lib/JavabuilderConnectionHelper.js";
import {expect} from "chai";

/**
 * Helper for verifying that a set of messages was received (and possibly sent)
 * from Javabuilder. Fails if there are any errors connecting.
 *
 * @param {*} sourcesJson sources (see sources.js)
 * @param {*} miniAppType mini-app type (see MiniAppType.js)
 * @param {*} assertOnMessagesObserved a verification callback to be called once all messages have been sent/received
 * @param {*} doneCallback Mocha's 'done' callback
 * @param {*} onMessageCallback a callback executed after each received message that can be used to respond to messages
 */
export const verifyMessages = (
  sourcesJson,
  miniAppType,
  assertOnMessagesObserved,
  doneCallback,
  onMessageCallback
) => {
  const allMessages = [];

  const onMessage = (event, socket) => {
    const parsedData = JSON.parse(event.data);
    allMessages.push(parsedData);

    if (onMessageCallback) {
      onMessageCallback(parsedData, socket, allMessages);
    }
  }

  const onError = (error) => {
    doneCallback(new Error(error.message));
  };
  const onClose = (event) => {
    expect(event.wasClean).to.be.true;
    assertOnMessagesObserved(allMessages);
    doneCallback();
  };

  connectionHelper
    .connect(sourcesJson, miniAppType, () => {}, onMessage, onError, onClose)
    .catch((err) => {
      doneCallback(err);
    });
};

export const assertMessagesEqual = (observedMessages, expectedMessages, verifyDetailKey) => {
  expect(observedMessages.length).to.equal(expectedMessages.length);
  expectedMessages.forEach((expectedMessage, index) => {
    const observedMessage = observedMessages[index];
    expect(observedMessage.type).to.equal(expectedMessage.type);
    expect(observedMessage.value).to.equal(expectedMessage.value);

    if (verifyDetailKey && expectedMessage.detail) {
      Object.keys(expectedMessage.detail).forEach(key => verifyDetailKey(key, observedMessage, expectedMessage));
    }
  });
};

export const INITIAL_STATUS_MESSAGES = [
  {type: "STATUS", value: "COMPILING"},
  {type: "STATUS", value: "COMPILATION_SUCCESSFUL"},
  {type: "STATUS", value: "RUNNING"}
];

export const EXIT_STATUS_MESSAGE = {type: "STATUS", value: "EXITED"};
