import {neighborhood} from "./lib/sources.js";
import {NEIGHBORHOOD} from "./lib/MiniAppType.js";
import {
  assertOnExitStatusMessage,
  assertOnInitialStatusMessages,
  verifyMessagesReceived
} from "./helpers/testHelpers.js";
import {expect} from "chai";

describe("Neighborhood", () => {
  it("Paints single square", (done) => {
    const expectedRuntimeMessages = [
      {
        type: "NEIGHBORHOOD",
        value: "INITIALIZE_PAINTER",
        detail: {
          x: "0",
          y: "0",
          paint: "1",
          direction: "east",
          id: "painter-1"
        }
      },
      {
        type: "NEIGHBORHOOD",
        value: "PAINT",
        detail: {
          color: "blue",
          id: "painter-1"
        }
      }
    ];

    const assertOnMessagesReceived = receivedMessages => {
      expect(receivedMessages.length).to.equal(6);

      const initialStatusMessages = receivedMessages.slice(0,3);
      const runtimeMessages = receivedMessages.slice(3,5);
      const exitStatusMessage = receivedMessages[5];

      assertOnInitialStatusMessages(initialStatusMessages);

      let expectedPainterId;
      runtimeMessages.forEach((receivedMessage, index) => {
        const expectedMessage = expectedRuntimeMessages[index];

        const messageType = receivedMessage.type;
        expect(receivedMessage.type).to.equal(expectedMessage.type);
        expect(receivedMessage.value).to.equal(expectedMessage.value);

        // Verify details object is as expected.
        // The painter ID varies across executions,
        // so assert that the intialized painter ID
        // persists in other painter messages.
        const detail = receivedMessage.detail;
        Object.keys(detail).forEach(key => {
          if (key === 'id') {
            if (receivedMessage.value === "INITIALIZE_PAINTER") {
              expectedPainterId = detail.id;
            } else {
              expect(detail.id).to.equal(expectedPainterId);
            }
          } else {
            expect(receivedMessage.detail[key]).to.equal(expectedMessage.detail[key]);
          }
        })
      });

      assertOnExitStatusMessage(exitStatusMessage);
    };

    verifyMessagesReceived(neighborhood, NEIGHBORHOOD, assertOnMessagesReceived, done);
  }).timeout(20000);
});
