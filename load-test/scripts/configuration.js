export const basicTestOptions = {
  scenarios: {
    // ramp up to 5 VUs over 30 seconds
    rampUp: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        {duration: '30s', target: 5}
      ]
    },
    // have 5 VUs do 3 iterations each, up to a max of 2 minutes. Start this after
    // the ramp up time.
    highLoad: {
      executor: "per-vu-iterations",
      vus: 5,
      iterations: 3,
      maxDuration: "2m",
      startTime: '30s',
    }
  },
  thresholds: {
    exceptions: ["count == 0"],
    errors: ["count == 0"],
    websocket_session_duration_without_sleep: ["p(95) < 5000"],
    long_websocket_sessions: ["count <= 1"],
    extra_long_websocket_sessions: ["count == 0"]
  },
  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(98)", "p(99)"],
};

// TODO: Update to a load testing instance of Javabuilder
export const uploadUrl = `https://javabuilder-molly-http.dev-code.org/seedsources/sources.json?Authorization=`;
export const url = `wss://javabuilder-molly.dev-code.org?Authorization=`;
// TODO: update to 'load-test' once we have load test auth set up
const origin = "http://localhost-studio.code.org:3000";
  
export const websocketParams = {
  headers: {
    Origin: origin,
  },
};

export const uploadParams = {
  headers: {
    Origin: origin,
    "Content-Type": "application/json",
  },
};

// These will be used for generating the JWT token
export const PRIVATE_KEY = __ENV.PRIVATE_KEY;
export const PRIVATE_KEY_PASSWORD = __ENV.PRIVATE_KEY_PASSWORD;

// Thresholds for metrics
export const LONG_REQUEST_MS = 5000;
export const EXTRA_LONG_REQUEST_MS = 10000;
