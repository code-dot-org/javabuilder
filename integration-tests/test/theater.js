import { theaterImageAndText } from "./lib/sources.js";
import { THEATER } from "./lib/MiniAppType.js";
import {
  INITIAL_STATUS_MESSAGES,
  EXIT_STATUS_MESSAGE,
  assertMessagesEqual,
  verifyMessages
} from "./helpers/testHelpers.js";
import {JAVABUILDER_BASE_DOMAIN, JAVABUILDER_SUB_DOMAIN} from "./lib/environment.js";
import { expect } from "chai";

describe("Theater", () => {
  it("Runs Theater project with image, text, and sound", (done) => {
    const expectedMessages = [
      ...INITIAL_STATUS_MESSAGES,
      {type: "STATUS", value: "SENDING_VIDEO", detail: {totalTime: "0"}},
      {type: "THEATER", value: "VISUAL_URL", detail: {url: "https://javabuilder-content.code.org/abc-123/theaterImage.gif"}},
      {type: "THEATER", value: "AUDIO_URL", detail: {url: "https://javabuilder-content.code.org/abc-123/theaterAudio.wav"}},
      EXIT_STATUS_MESSAGE
    ];

    const assertOnMessagesReceived = receivedMessages => {
      const verifyDetailKey = (key, receivedMessage, expectedMessage) => {
        if (key === 'url') {
          if (receivedMessage.value === 'VISUAL_URL') {
            verifyMediaURL(receivedMessage.detail.url, 'theaterImage.gif');
          } else if (receivedMessage.value === 'AUDIO_URL') {
            verifyMediaURL(receivedMessage.detail.url, 'theaterAudio.wav');
          } else {
            throw new Error(`Unexpected URL received in detail for message of type ${receivedMessage.type}`);
          }
        } else {
          expect(receivedMessage.detail[key]).to.equal(expectedMessage.detail[key]);
        }
      }

      assertMessagesEqual(receivedMessages, expectedMessages, verifyDetailKey);
    }

    verifyMessages(theaterImageAndText, THEATER, assertOnMessagesReceived, done);
  }).timeout(20000);
});

const verifyMediaURL = (url, filename) => {
  const splitURL = url.split('/');

  expect(splitURL[0]).to.equal('https:');
  expect(splitURL[2]).to.equal(`${JAVABUILDER_SUB_DOMAIN}-content.${JAVABUILDER_BASE_DOMAIN}`);
  expect(splitURL[splitURL.length - 1]).to.equal(filename);
};
