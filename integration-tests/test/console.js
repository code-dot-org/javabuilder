import { scanner } from "./lib/sources.js";
import { CONSOLE } from "./lib/MiniAppType.js";
import {
  INITIAL_STATUS_MESSAGES,
  EXIT_STATUS_MESSAGE,
  assertMessagesEqual,
  verifyMessages
} from "./helpers/testHelpers.js";

describe("Console", () => {
  it("Runs simple interactive console project", (done) => {
    const expectedMessages = [
      ...INITIAL_STATUS_MESSAGES,
      {type: "SYSTEM_OUT", value: "What's your name?"},
      {type: "SYSTEM_OUT", value: "\n"},
      {messageType: "SYSTEM_IN", message: "Ben"},
      {type: "SYSTEM_OUT", value: "Hello Ben!"},
      {type: "SYSTEM_OUT", value: "\n"},
      EXIT_STATUS_MESSAGE
    ];
    const assertOnMessagesObserved = observedMessages => assertMessagesEqual(observedMessages, expectedMessages);

    let hasReceivedMessage, hasReceivedNewLineAfterMessage;
    const onMessageCallback = (parsedData, socket, allMessages) => {
      // Confirm receipt of "What's your name" and new line messages back to back.
      if (parsedData.type === "SYSTEM_OUT" && parsedData.value === "What's your name?") {
        hasReceivedMessage = true;
      } else if (hasReceivedMessage && (parsedData.type === "SYSTEM_OUT" && parsedData.value === "\n")) {
        hasReceivedNewLineAfterMessage = true;
      } else {
        hasReceivedNewLineAfterMessage = false;
        hasReceivedMessage = false;
      }

      if (hasReceivedMessage && hasReceivedNewLineAfterMessage) {
        const message = {
          messageType: "SYSTEM_IN",
          message: "Ben"
        };
        socket.send(JSON.stringify(message));
        allMessages.push(message);
      }
    };

    verifyMessages(scanner, CONSOLE, assertOnMessagesObserved, done, onMessageCallback);
  }).timeout(20000);
});
