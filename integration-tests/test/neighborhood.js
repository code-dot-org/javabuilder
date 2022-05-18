import {neighborhood} from "./lib/sources.js";
import {NEIGHBORHOOD} from "./lib/MiniAppType.js";
import {
  INITIAL_STATUS_MESSAGES,
  EXIT_STATUS_MESSAGE,
  assertMessagesEqual,
  verifyMessages
} from "./helpers/testHelpers.js";
import {expect} from "chai";

describe("Neighborhood", () => {
  it("Paints single square", (done) => {
    const expectedMessages = [
      ...INITIAL_STATUS_MESSAGES,
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
      },
      EXIT_STATUS_MESSAGE
    ];

    const assertOnMessagesReceived = receivedMessages => {
      let expectedPainterId;
      const verifyDetailKey = (key, receivedMessage, expectedMessage) => {
        if (key === 'id') {
          if (receivedMessage.value === "INITIALIZE_PAINTER") {
            expectedPainterId = receivedMessage.detail.id;
          } else {
            expect(receivedMessage.detail.id).to.equal(expectedPainterId);
          }
        } else {
          expect(receivedMessage.detail[key]).to.equal(expectedMessage.detail[key]);
        }
      }

      assertMessagesEqual(receivedMessages, expectedMessages, verifyDetailKey);
    };

    verifyMessages(neighborhood, NEIGHBORHOOD, assertOnMessagesReceived, done);
  }).timeout(20000);
});
