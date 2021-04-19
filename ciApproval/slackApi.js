const https = require('https');
const url = require('url');
const querystring = require('querystring');

/**
 * Call the specified Slack API method.
 * @param method Slack API method to call
 * @param {Object} args arguments to pass to method
 * @param {String} slackToken Slack OAuth token
 * @returns {Promise} response body
 */
module.exports = function(method, args, slackToken) {
  const requestOptions = url.parse(`https://slack.com/api/${method}`);
  if (method.match('chat')) {
    // chat.* API calls allow JSON POST requests.
    requestOptions.method = 'POST';
    requestOptions.headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${slackToken}`
    };
    return request(requestOptions, JSON.stringify(args));
  } else {
    // Other API calls require urlencoded GET requests.
    requestOptions.method = 'GET';
    requestOptions.headers = {
      'Content-Type': 'application/x-www-form-urlencoded',
    };
    args.token = slackToken;
    requestOptions.path += `?${querystring.stringify(args)}`;
    return request(requestOptions, null);
  }
};

function request(options, body) {
  return new Promise((resolve, reject) => {
    const req = https.request(options, (response) => {
      if (response.statusCode < 200 || response.statusCode > 299) {
        reject(new Error('Error, status code: ' + response.statusCode));
      }
      const body = [];
      response.on('data', chunk => body.push(chunk));
      response.on('end', () => resolve(body.join('')));
    });
    req.on('error', err => reject(err));
    if (body) {
      req.write(body);
    }
    req.end();
  })
}
