import connectionHelper from "../lib/JavabuilderConnectionHelper.js";
import {expect} from "chai";

/**
 * Helper for verifying the basic case that set of messages was received
 * from Javabuilder. Fails if there are any errors connecting.
 *
 * @param {*} sourcesJson sources (see sources.js)
 * @param {*} miniAppType mini-app type (see MiniAppType.js)
 * @param {*} expectedMessages a list of expected messages from Javabuilder
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

export const assertOnInitialStatusMessages = receivedInitialStatusMessages => {
  const expectedInitialStatusMessages = [
    {type: "STATUS", value: "COMPILING"},
    {type: "STATUS", value: "COMPILATION_SUCCESSFUL"},
    {type: "STATUS", value: "RUNNING"}
  ];

  assertMessagesEqual(receivedInitialStatusMessages, expectedInitialStatusMessages);
}

export const assertOnExitStatusMessage = receivedExitStatusMessage => {
  const expectedExitStatusMessage = {type: "STATUS", value: "EXITED"};
  expect(receivedExitStatusMessage).to.deep.equal(expectedExitStatusMessage);
}

export const assertMessagesEqual = (receivedMessages, expectedMessages) => {
  expect(receivedMessages).to.deep.equal(expectedMessages);
}
