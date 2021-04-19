const SLACK_TOKEN = process.env.SLACK_TOKEN;
const SLACK_CHANNEL = process.env.SLACK_CHANNEL;
const slackApi = require('./slackApi');

const AWS = require('aws-sdk');
const codePipeline = new AWS.CodePipeline();

/**
 * Lambda function for forwarding a CloudWatch Event SNS message to Slack.
 */
exports.handler = async function (event) {
  console.log(`Request received: ${JSON.stringify(event)}`);
  let message;
  switch (event.source) {
    case "aws.codepipeline":
      message = await codePipelineMessage(event.detail, event['detail-type']);
      break;
    default:
      message = {};
      break;
  }
  const method = message.ts ? 'chat.update' : 'chat.postMessage';
  await slackApi(method, message, SLACK_TOKEN);
};

/**
 * Formats a Slack-message JSON object from a CodePipeline SNS-detail object.
 * @see https://docs.aws.amazon.com/codepipeline/latest/userguide/detect-state-changes-cloudwatch-events.html#create-cloudwatch-notifications
 * @param detail JSON object with event details. Example:
   {
      "pipeline": "myPipeline",
      "version": "1",
      "execution-id": 'execution_Id',
      "stage": "Prod",
      "state": "STARTED",
      "type": {
        "owner": "AWS",
        "category": "Deploy",
        "provider": "CodeDeploy",
        "version": 1
      }
    }
 * @param type detail-type string.
 */
async function codePipelineMessage(detail, type) {
  const executionId = detail['execution-id'];
  const pipeline = detail.pipeline;
  const execution = await codePipeline.getPipelineExecution({
    pipelineName: pipeline,
    pipelineExecutionId: executionId
  }).promise();
  const artifact = execution.pipelineExecution.artifactRevisions[0];
  let revisionUrl = '';
  let revisionId = 'unknown';
  if (artifact) {
    revisionUrl = artifact.revisionUrl;
    revisionId = artifact.revisionId.substring(0, 7);
  }

  const stateColors = {
    STARTED: 'warning',
    SUCCEEDED: 'good',
    FAILED: 'danger',
    CANCELED: 'danger'
  };

  const message = {
    username: pipeline,
    channel: SLACK_CHANNEL,
    attachments: [{
      title: `[<${revisionUrl}|@${revisionId}>] ${detail.stage} ${detail.state.toLowerCase()}`,
      fallback: executionId,
      color: stateColors[detail.state],
    }]
  };

  if (detail.state !== 'STARTED') {
    // Search Slack channel history for previously-started stage's original message,
    // so we can update instead of posting a new message.
    const historyJson = await slackApi('channels.history', {
      channel: SLACK_CHANNEL,
      oldest: artifact.created
    }, SLACK_TOKEN);
    const history = JSON.parse(historyJson);
    if (history.ok) {
      const oldMessage = history.messages.filter(message =>
        message.attachments && message.attachments.some(attachment =>
        attachment.fallback === executionId &&
        attachment.title.match(detail.stage)
        )).pop();
      if (oldMessage) {
        // Update the original message by reusing its timestamp.
        message.ts = oldMessage.ts;

        // Append the stage-execution duration to the message.
        const elapsed = Date.now() - message.ts * 1000;
        const elapsedString = new Date(elapsed).toUTCString().split(' ')[4];
        message.attachments[0].title += ` (${elapsedString})`;
        message.attachments[0].ts = Date.now() / 1000;
      }
    }
  }

  return message;
}
