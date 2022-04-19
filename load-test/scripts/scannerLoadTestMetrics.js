import { Counter, Trend } from "k6/metrics";

const metrics = {
    responseTime: new Trend("response_time", true),
    notSent: new Counter("not_sent"),
    noResponse: new Counter("no_response")
};
const metricsData = {
    sentAt: null,
    respondedAt: null
};

function onMessage(socket, parsedData) {
    if (parsedData.type === "SYSTEM_OUT" && parsedData.value === "What's your name?") {
        const message = JSON.stringify({
            messageType: "SYSTEM_IN",
            message: "Ben"
        });
        socket.send(message);
        metricData.sentAt = Date.now();
    }

    if (parsedData.type === "SYSTEM_OUT" && parsedData.value === "Hello Ben!") {
        metricData.respondedAt = Date.now();
    }
}

function onClose() {
    const {responseTime, notSent, noResponse} = metrics;
    const {sentAt, respondedAt} = metricData;

    if (!metricData.sentAt) {
        metrics.notSent.add(1);
    } else if (!metricData.respondedAt) {
        metrics.noResponse.add(1);
    } else {
        metrics.responseTime.add(respondedAt - sentAt);
    }
}

const metricsReporter = {onMessage, onClose};
export default metricsReporter;
