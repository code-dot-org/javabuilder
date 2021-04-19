const SLACK_TOKEN = process.env.SLACK_TOKEN;
const SLACK_CHANNEL = process.env.SLACK_CHANNEL;
const STACK_NAME = process.env.STACK_NAME;

const AWS = require('aws-sdk');
const codePipeline = new AWS.CodePipeline();
const cloudFormation = new AWS.CloudFormation();
const slackApi = require('./slackApi');

/**
 * Lambda function for creating a CI-approval Slack Message Button in response to
 * a CodePipeline Manual Approval Notification SNS message.
 *
 * AWS APIs invoked:
 * - codepipeline:GetPipelineState
 * - codepipeline:GetPipelineExecution
 * - cloudformation:DescribeStacks
 *
 * @see https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/CodePipeline.html#getPipelineState-property
 * @see https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/CodePipeline.html#getPipelineExecution-property
 * @see https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/CloudFormation.html#describeStacks-property
 * @see https://docs.aws.amazon.com/codepipeline/latest/userguide/approvals-json-format.html
 * @see https://api.slack.com/methods/chat.postMessage
 */
module.exports.requestApproval = async function (event) {
  console.log(`Request received: ${JSON.stringify(event)}`);

  const data = JSON.parse(event.Records[0].Sns.Message);
  const token = data.approval.token;
  const pipelineName = data.approval.pipelineName;
  const stage = data.approval.stageName;
  const action = data.approval.actionName;

  // Lookup the commit URL and ID from the pipeline execution state.
  const state = await codePipeline.getPipelineState({name: pipelineName}).promise();
  const stageState = state.stageStates.find((state) => state.stageName === stage);
  const executionId = stageState.latestExecution.pipelineExecutionId;
  const execution = await codePipeline.getPipelineExecution({
    pipelineName: pipelineName,
    pipelineExecutionId: executionId
  }).promise();
  const artifact = execution.pipelineExecution.artifactRevisions[0];

  const revisionUrl = artifact.revisionUrl;
  const revisionId = artifact.revisionId.substring(0, 7);

  // Lookup the previous and current stage URLs from the CloudFormation stack outputs.
  const stack = await cloudFormation.describeStacks({StackName: STACK_NAME}).promise();
  const currentUrl = stack.Stacks[0].Outputs.find((o) => o.OutputKey === 'ApiUrl').OutputValue;

  const emoji = {
    true: ':rocket:',
    false: ':exclamation:'
  };

  const payload = {
    true: {
      approve: true,
      approvalText: emoji[true],
      codePipelineToken: token,
      codePipelineName: pipelineName,
      stage: stage,
      action: action
    },
    false: {
      approve: false,
      approvalText: emoji[false],
      codePipelineToken: token,
      codePipelineName: pipelineName,
      stage: stage,
      action: action
    }
  };

  const slackMessage = {
    username: STACK_NAME,
    channel: SLACK_CHANNEL,
    attachments: [
      {
        text: `Build <${revisionUrl}|\`${revisionId}\`> awaiting approval to <${currentUrl}|${stage}>.`,
        mrkdwn_in: ["text"],
        fallback: "Unable to approve from fallback",
        callback_id: "ci_approval",
        color: "#3AA3E3",
        attachment_type: "default",
        actions: [
          {
            name: action,
            text: `${emoji[true]} Approve`,
            style: "primary",
            type: "button",
            value: JSON.stringify(payload[true]),
            confirm: {
              title: "Are you sure?",
              text: `Approve to ${stage} ${emoji[true]}`,
              ok_text: "Yes",
              dismiss_text: "No"
            }
          },
          {
            name: action,
            text: `${emoji[false]} Reject`,
            type: "button",
            value: JSON.stringify(payload[false])
          }
        ]
      }
    ]
  };

  await slackApi('chat.postMessage', slackMessage, SLACK_TOKEN);
};
