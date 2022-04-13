// TODO: Update to a load testing instance of Javabuilder
export const UPLOAD_URL = `https://javabuilder-high-load-http.code.org/seedsources/sources.json?Authorization=`;
export const WEBSOCKET_URL = `wss://javabuilder-high-load.code.org?Authorization=`;
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

// Time per request--if a request takes under this time, sleep until we have
// reached this time. This is so we do not issue too many requests.
export const REQUEST_TIME_MS = 20000;
// Time after which to timeout a request
export const TIMEOUT_MS = 40000;

// Mini-app types
export const MiniAppType = {
  CONSOLE: 'console',
  NEIGHBORHOOD: 'neighborhood',
  THEATER: 'theater'
};

export function getTestOptions(maxUserGoal, highLoadTimeMinutes) {
  const maxConcurrentUsers =  Math.floor(maxUserGoal / 30);
  return  {
    scenarios: {
      halfConcurrency: {
        executor: "constant-vus",
        vus: Math.ceil(maxConcurrentUsers / 2),
        duration: '1m',
      },
      initialHighConcurrency: {
        executor: "constant-vus",
        vus: maxConcurrentUsers,
        duration: '1m',
        startTime: '1m'
      },
      // maintain maxConcurrentUsers VUs for highLoadTimeMinutes minutes. Start this after first two
      // scenarios have completed.
      highLoad: {
        executor: "constant-vus",
        vus: maxConcurrentUsers,
        duration: `${highLoadTimeMinutes}m`,
        startTime: '2m',
      }
    },
    thresholds: {
      exceptions: ["count == 0"],
      errors: ["count == 0"],
      timeouts: ["count == 0"],
      total_session_time: ["p(95) < 5000"],
      dropped_iterations: ["count == 0"]
    },
    summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(98)", "p(99)"],
  };
}
