const SLACK_SIGNING_SECRET = process.env.SLACK_SIGNING_SECRET;

const crypto = require('crypto');
const querystring = require('querystring');
const AWS = require('aws-sdk');
const codePipeline = new AWS.CodePipeline();

/**
 * Lambda function for responding to a CI-approval Slack Message Button event.
 * Calls codepipeline:PutApprovalResult, and returns the updated Slack message in the response.
 * @see https://api.slack.com/docs/message-buttons#responding_to_message_actions
 * @see https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/CodePipeline.html#putApprovalResult-property
 */
module.exports.respondApproval = async function(event, context, callback) {
  console.log(`Request received: ${JSON.stringify(event)}`);
  verifyMessage(event.headers, event.body, callback);
  const payload = JSON.parse(querystring.parse(event.body).payload);
  const actionDetails = JSON.parse(payload.actions[0].value);
  const user = payload.user;
  const status = actionDetails.approve ? "Approved" : "Rejected";
  await codePipeline.putApprovalResult({
    pipelineName: actionDetails.codePipelineName,
    stageName: actionDetails.stage,
    actionName: actionDetails.action,
    result: {
      summary: `${status} by @${user.name} via Slack`,
      status: status
    },
    token: actionDetails.codePipelineToken
  }).promise();
  const message = payload.original_message;
  const attachment = message.attachments[0];
  Object.assign(attachment, {
    text: attachment.text.replace('awaiting approval', status) + ' ' + actionDetails.approvalText,
    color: actionDetails.approve ? "good" : "danger",
    footer: `<@${user.id}>`,
    ts: payload.action_ts,
    actions: []
  });
  callback(null, {statusCode: 200, body: JSON.stringify(message)});
};

/**
 * Verify Slack message using signed secret.
 * @see https://api.slack.com/docs/verifying-requests-from-slack
 */
function verifyMessage(headers, body, callback) {
  const baseString = `v0:${headers['X-Slack-Request-Timestamp']}:${body}`;
  const hash = 'v0=' + crypto.createHmac('sha256', SLACK_SIGNING_SECRET)
    .update(baseString)
    .digest('hex');
  const retrievedSignature = headers['X-Slack-Signature'];
  if (hash !== retrievedSignature) {
    callback(null, {
      statusCode: 401,
      body: 'Signature verification failed, Ignoring message'
    });
  }
}
