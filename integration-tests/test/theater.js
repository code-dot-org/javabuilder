// import { helloWorld } from "./lib/sources.js";
// import { NEIGHBORHOOD } from "./lib/MiniAppType.js";
// import {
//   assertOnExitStatusMessage,
//   assertOnInitialStatusMessages,
//   verifyMessagesReceived
// } from "./helpers/testHelpers.js";
// import { expect } from "chai";
//
// describe("Theater", () => {
//   it("Runs Theater project", (done) => {
//     const expectedRuntimeMessages = [
//       {type: "STATUS", value: "SENDING_VIDEO", detail: {totalTime: "0"}},
//       {type: "THEATER", value: "VISUAL_URL", detail: {url: "https://javabuilder-content.code.org/abc-123/theaterImage.gif"}},
//       {type: "THEATER", value:"AUDIO_URL", detail: {url: "https://javabuilder-content.code.org/abc-123/theaterAudio.wav"}}
//     ];
//
//     const assertOnMessagesReceived = receivedMessages => {
//       expect(receivedMessages.length).to.equal(7);
//       const initialStatusMessages = receivedMessages.slice(0,3);
//       const runtimeMessages = receivedMessages.slice(3,6);
//       const exitStatusMessage = receivedMessages[6];
//
//       assertOnInitialStatusMessages(initialStatusMessages);
//       runtimeMessages.forEach((receivedMessage, index) => {
//         const expectedMessage = expectedRuntimeMessages[index];
//
//         expect(receivedMessage.type).to.equal(expectedMessage.type);
//         expect(receivedMessage.value).to.equal(expectedMessage.value);
//
//         const detail = receivedMessage.detail;
//         Object.keys(detail).forEach(key => {
//           if (key === 'url') {
//             if (receivedMessage.value === 'VISUAL_URL') {
//               verifyImageURL(detail.url);
//             } else if (receivedMessage.value === 'AUDIO_URL') {
//               verifyAudioURL(detail.url);
//             } else {
//               throw new Error(`Unexpected URL received in detail for message of type ${receivedMessage.type}`);
//             }
//           } else {
//             expect(receivedMessage.detail[key]).to.equal(expectedMessage.detail[key]);
//           }
//         });
//       });
//
//       assertOnExitStatusMessage(exitStatusMessage);
//     }
//
//     verifyMessagesReceived(helloWorld, NEIGHBORHOOD, assertOnMessagesReceived, done);
//   }).timeout(20000);
// });
//
// const verifyImageURL = url => {
//   expect(url.contains("content.code.org")).to.be.true;
//   expect(url.split('/')[-1]).to.equal("theaterImage.gif");
// };
//
// const verifyAudioURL = url => {
//   expect(url.contains("content.code.org")).to.be.true;
//   expect(url.split('/')[-1]).to.equal("theaterAudio.wav");
// };
//
