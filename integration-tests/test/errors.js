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
import {uploadSources} from "./lib/JavabuilderConnectionHelper.js";

describe("Error States", () => {
  it("Responds with compilation error when given malformed student code", (done) => {
    const expectedMessages = [
      COMPILING_STATUS_MESSAGE,
      { type: "SYSTEM_OUT", value: "/HelloWorld.java:1: error: reached end of file while parsing\npublic class HelloWorld {\n                         ^\n" },
      { type: "EXCEPTION", value: "COMPILER_ERROR" },
      EXIT_STATUS_MESSAGE
    ];

    const assertOnMessagesReceived = receivedMessages => assertMessagesEqual(receivedMessages, expectedMessages);
    verifyMessages(compilationError, CONSOLE, assertOnMessagesReceived, done);
  }).timeout(20000);

  it("Responds with FileNotFound exception when unknown file is used", (done) => {
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

  it("Throws exception when blocked class used in student code", (done) => {
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

  it("Does not accept malformed token", async () => {
    const httpResponse = await uploadSources(blankProject, 'aBadToken');
    expect(httpResponse.status).to.equal(500);
  }).timeout(2000);
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
