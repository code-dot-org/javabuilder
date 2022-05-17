import { helloWorld } from "./lib/sources.js";
import { NEIGHBORHOOD } from "./lib/MiniAppType.js";
import {
  assertMessagesEqual,
  verifyMessagesReceived,
  INITIAL_STATUS_MESSAGES,
  EXIT_STATUS_MESSAGE,
} from "./helpers/testHelpers.js";

describe("Hello World", () => {
  it("Runs Hello World project", (done) => {
    const expectedMessages = [
      ...INITIAL_STATUS_MESSAGES,
      { type: "SYSTEM_OUT", value: "Hello World" },
      { type: "SYSTEM_OUT", value: "\n" },
      EXIT_STATUS_MESSAGE
    ];
    const assertOnMessagesReceived = receivedMessages => assertMessagesEqual(receivedMessages, expectedMessages);

    verifyMessagesReceived(helloWorld, NEIGHBORHOOD, assertOnMessagesReceived, done);
  }).timeout(20000);
});
