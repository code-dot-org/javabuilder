import {blockedClassError, compilationError, theaterRuntimeFileNotFound, blankProject} from "./lib/sources.js";
import {CONSOLE, THEATER} from "./lib/MiniAppType.js";
import {
  assertMessagesEqual,
  verifyMessages,
  COMPILING_STATUS_MESSAGE,
  INITIAL_STATUS_MESSAGES,
  EXIT_STATUS_MESSAGE,
} from "./helpers/testHelpers.js";
import {expect} from "chai";
import connectionHelper from "./lib/JavabuilderConnectionHelper.js";

describe("Errors", () => {
  it("Compilation Error", (done) => {
    const expectedMessages = [
      COMPILING_STATUS_MESSAGE,
      { type: "SYSTEM_OUT", value: "/HelloWorld.java:1: error: reached end of file while parsing\npublic class HelloWorld {\n                         ^\n" },
      { type: "EXCEPTION", value: "COMPILER_ERROR" },
      EXIT_STATUS_MESSAGE
    ];

    const assertOnMessagesReceived = receivedMessages => assertMessagesEqual(receivedMessages, expectedMessages);
    verifyMessages(compilationError, CONSOLE, assertOnMessagesReceived, done);
  }).timeout(20000);

  it("Runtime Error", (done) => {
    const expectedMessages = [
      ...INITIAL_STATUS_MESSAGES,
      {
        type: "EXCEPTION",
        value: "FILE_NOT_FOUND",
        detail: {
          causeMessage: "dog.jpeg",
          cause: "Exception message: java.io.FileNotFoundException: dog.jpeg",
          fallbackMessage: "Exception message: java.io.FileNotFoundException: dog.jpeg"
        }
      },
      EXIT_STATUS_MESSAGE
    ];

    const assertOnMessagesReceived = receivedMessages => assertMessagesEqual(receivedMessages, expectedMessages, validateExceptionDetailKey);
    verifyMessages(theaterRuntimeFileNotFound, THEATER, assertOnMessagesReceived, done);
  }).timeout(20000);

  it("Uses blocked classes", (done) => {
    const expectedMessages = [
      ...INITIAL_STATUS_MESSAGES,
      {
        type: "EXCEPTION",
        value: "INVALID_CLASS",
        detail: {
          causeMessage: "java.lang.Thread",
          cause: "Exception message: java.lang.ClassNotFoundException: java.lang.Thread",
          fallbackMessage: "Exception message: java.lang.ClassNotFoundException: java.lang.Thread"
        }
      },
      EXIT_STATUS_MESSAGE
    ];

    const assertOnMessagesReceived = receivedMessages => assertMessagesEqual(receivedMessages, expectedMessages, validateExceptionDetailKey);
    verifyMessages(blockedClassError, CONSOLE, assertOnMessagesReceived, done);
  }).timeout(20000);

  it("Does not have valid token", (done) => {
    // We don't appear to get any more detail than a 500 response when an invalid token is provided.
    const confirmErrorResponse = httpResponse => expect(httpResponse.status).to.equal(500);
    connectionHelper
      .connect(blankProject, CONSOLE, () => {}, () => {}, () => {}, () => {}, () => 'fakeToken', confirmErrorResponse)
      .finally(done)
  }).timeout(20000);
});

const validateExceptionDetailKey = (key, receivedMessage, expectedMessage) => {
  if (key === "causeMessage") {
    expect(receivedMessage.detail.causeMessage).to.equal(expectedMessage.detail.causeMessage);
  } else if (['cause', 'fallbackMessage'].includes(key)) {
    const receivedErrorMessageFirstLine = receivedMessage.detail[key].split("\n")[0];
    expect(receivedErrorMessageFirstLine).to.equal(expectedMessage.detail[key]);
  } else {
    throw new Error(`Unexpected detail key ${key}`);
  }
}
