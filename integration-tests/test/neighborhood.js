import {neighborhood} from "./lib/sources.js";
import {NEIGHBORHOOD} from "./lib/MiniAppType.js";
import {verifyMessagesReceived} from "./helpers/testHelpers.js";
import {expect} from "chai";

describe("Neighborhood", () => {
  it("Paints single square", (done) => {
    const expectedMessages = [
      {type: "STATUS", value: "COMPILING"},
      {type: "STATUS", value: "COMPILATION_SUCCESSFUL"},
      {type: "STATUS", value: "RUNNING"},
      {
        type: "NEIGHBORHOOD",
        value: "INITIALIZE_PAINTER",
        detail: {
          x: "0",
          y: "0",
          paint: "1",
          direction: "east"
        }
      },
      {
        type: "NEIGHBORHOOD",
        value: "PAINT",
        detail: {
          color: "blue"
        }
      },
      {type: "STATUS", value: "EXITED"}
    ];

    const assertOnMessagesReceived = receivedMessages => {
      expect(receivedMessages.length).to.equal(expectedMessages.length);

      let expectedPainterId;
      receivedMessages.forEach((receivedMessage, index) => {
        const expectedMessage = expectedMessages[index];

        const messageType = receivedMessage.type;
        if (messageType === "STATUS") {
          expect(receivedMessage).to.deep.equal(expectedMessage);
        } else if (messageType === "NEIGHBORHOOD") {
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
        } else {
          throw new Error(`Unexpected message type received: ${messageType}`);
        }
      });
    };

    verifyMessagesReceived(neighborhood, NEIGHBORHOOD, assertOnMessagesReceived, done);
  }).timeout(20000);
});
