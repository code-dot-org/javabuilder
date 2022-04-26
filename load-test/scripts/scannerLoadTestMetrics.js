import { Counter, Trend } from "k6/metrics";

const responseTime = new Trend("response_time", true);
const notSent = new Counter("not_sent");
const noResponse =  new Counter("no_response");
let sentAt, respondedAt;

function onMessage(socket, parsedData) {
    if (parsedData.type === "SYSTEM_OUT" && parsedData.value === "What's your name?") {
        const message = JSON.stringify({
            messageType: "SYSTEM_IN",
            message: "Ben"
        });
        socket.send(message);
        sentAt = Date.now();
    }

    if (parsedData.type === "SYSTEM_OUT" && parsedData.value === "Hello Ben!") {
        respondedAt = Date.now();
    }
}

function onClose() {
    if (!sentAt) {
        notSent.add(1);
    } else if (!respondedAt) {
        noResponse.add(1);
    } else {
        responseTime.add(respondedAt - sentAt);
    }
}

const metricsReporter = {onMessage, onClose};
export default metricsReporter;
