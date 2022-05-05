import { helloWorld } from "./lib/sources.js";
import { NEIGHBORHOOD } from "./lib/MiniAppType.js";
import { verifyMessagesRecevied } from "./helpers/TestHelpers.js";

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

    verifyMessagesRecevied(helloWorld, NEIGHBORHOOD, expectedMessages, done);
  }).timeout(20000);
});
