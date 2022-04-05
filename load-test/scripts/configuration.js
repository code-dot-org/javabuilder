// TODO: Update to a load testing instance of Javabuilder
export const UPLOAD_URL = `https://javabuilder-load-test-http.dev-code.org/seedsources/sources.json?Authorization=`;
export const WEBSOCKET_URL = `wss://javabuilder-load-test.dev-code.org?Authorization=`;
const origin = "load-test";
  
export const WEBSOCKET_PARAMS = {
  headers: {
    Origin: origin,
  },
};

export const UPLOAD_PARAMS = {
  headers: {
    Origin: origin,
    "Content-Type": "application/json",
  },
};

// This will be used for generating the JWT token
export const PRIVATE_KEY = null;

// Thresholds for metrics
// Long requests time: we don't want requests to go over this time in the p(95) case
export const LONG_REQUEST_MS = 5000;
// Extra long request time: we never want requests to go over this time.
export const EXTRA_LONG_REQUEST_MS = 14000;

// Mini-app types
export const MiniAppType = {
  CONSOLE: 'console',
  NEIGHBORHOOD: 'neighborhood',
  THEATER: 'theater'
};

export function getTestOptions(maxUserGoal, rampUpTimeMinutes, highLoadTimeMinutes) {
  const maxConcurrentUsers =  Math.floor(maxUserGoal / 30);
  return  {
    scenarios: {
      // ramp up to maxConcurrentUsers VUs over rampUpTimeMinutes
      rampUp: {
        executor: "ramping-vus",
        startVUs: 0,
        stages: [
          {duration: `${rampUpTimeMinutes}m`, target: maxConcurrentUsers}
        ]
      },
      // have maxConcurrentUsers VUs do 3 iterations each minute, for a total of highLoadTimeMinutes * 3
      // iterations per virutal user.
      // Start this after the ramp up time and allow for 1 extra minute in the max duration in case of issues.
      highLoad: {
        executor: "per-vu-iterations",
        vus: maxConcurrentUsers,
        iterations: 3 * highLoadTimeMinutes, // this is iterations per virtual user
        maxDuration: `${highLoadTimeMinutes + 1}m`,
        startTime: `${rampUpTimeMinutes + 1}m`,
      }
    },
    thresholds: {
      exceptions: ["count == 0"],
      errors: ["count == 0"],
      websocket_session_duration_without_sleep: ["p(95) < 5000"],
      long_websocket_sessions: [`count <= ${maxConcurrentUsers}`],
      extra_long_websocket_sessions: ["count == 0"]
    },
    summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(98)", "p(99)"],
  };
}