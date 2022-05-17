import { helloWorld } from "./lib/sources.js";
import { NEIGHBORHOOD } from "./lib/MiniAppType.js";
import {assertMessagesEqual, verifyMessagesReceived} from "./helpers/testHelpers.js";
import { expect } from "chai";

describe("Hello World", () => {
  it("Runs Hello World project", (done) => {
    const expectedMessages = [
      { type: "STATUS", value: "COMPILING" },
      { type: "STATUS", value: "COMPILATION_SUCCESSFUL" },
      { type: "STATUS", value: "RUNNING" },
      { type: "SYSTEM_OUT", value: "Hello World" },
      { type: "SYSTEM_OUT", value: "\n" },
      { type: "STATUS", value: "EXITED" },
    ];
    const assertOnMessagesReceived = receivedMessages => assertMessagesEqual(receivedMessages, expectedMessages);

    verifyMessagesReceived(helloWorld, NEIGHBORHOOD, assertOnMessagesReceived, done);
  }).timeout(20000);
});
