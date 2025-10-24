import { helloWorld, scanner, theaterImageAndText } from "./lib/sources.js";
import * as MiniAppType from "./lib/MiniAppType.js";
import connectionHelper from "./lib/JavabuilderConnectionHelper.js";

/**
 * Global setup hook that runs once before all tests.
 * This warms up the Lambda functions for all mini-app types to prevent cold start timeouts.
 */
export const mochaHooks = {
  beforeAll(done) {
    // Configuration for each mini-app type to be warmed up.
    const warmupConfigs = [
      { source: helloWorld, type: MiniAppType.NEIGHBORHOOD, name: "NEIGHBORHOOD" },
      { source: helloWorld, type: MiniAppType.CONSOLE, name: "CONSOLE" },
      { source: theaterImageAndText, type: MiniAppType.THEATER, name: "THEATER" }
    ];

    this.timeout(60000); // 60 second timeout for cold start of all types

    console.info("\n🔥 Warming up Lambda functions for all mini-app types...\n");

    let completedWarmups = 0;
    const totalWarmups = warmupConfigs.length;

    const checkComplete = () => {
      completedWarmups++;
      if (completedWarmups === totalWarmups) {
        console.info("✅ Lambda warmup complete for all mini-app types!\n");
        done();
      }
    };

    const warmupConnection = (sources, miniAppType, typeName) => {
      const startTime = Date.now();

      const onMessage = () => {};
      const onError = (error) => {
        const duration = ((Date.now() - startTime) / 1000).toFixed(2);
        console.warn(`Warmup warning for ${typeName} (non-fatal, ${duration}s):`, error.message);
        checkComplete();
      };
      const onClose = () => {
        const duration = ((Date.now() - startTime) / 1000).toFixed(2);
        console.info(`  ✓ ${typeName} warmed up (${duration}s)`);
        checkComplete();
      };

      connectionHelper
        .connect(sources, miniAppType, () => {}, onMessage, onError, onClose)
        .catch((error) => {
          const duration = ((Date.now() - startTime) / 1000).toFixed(2);
          console.warn(`Warmup warning for ${typeName} (non-fatal, ${duration}s):`, error.message);
          checkComplete();
        });
    };

    // Warm up all mini-app types in parallel
    warmupConfigs.forEach(config => {
      warmupConnection(config.source, config.type, config.name);
    });
  }
};
