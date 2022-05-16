import { helloWorld } from "./lib/sources.js";
import { NEIGHBORHOOD } from "./lib/MiniAppType.js";
import { verifyMessagesReceived } from "./helpers/testHelpers.js";
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
    const assertOnMessagesReceived = receivedMessages => expect(expectedMessages).to.deep.equal(receivedMessages);

    verifyMessagesReceived(helloWorld, NEIGHBORHOOD, assertOnMessagesReceived, done);
  }).timeout(20000);
});
