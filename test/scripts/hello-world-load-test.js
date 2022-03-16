import ws from 'k6/ws';
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';


export const options = {
  vus: 2,
  duration: '30s',
};

const exceptionCounter = new Counter('exceptions');
const errorCounter = new Counter('errors')

const uploadUrl = `https://javabuilder-molly-http.dev-code.org/seedsources/sources.json?Authorization=${__ENV.AUTH_TOKEN}`; 
const url = `wss://javabuilder-molly.dev-code.org?Authorization=${__ENV.AUTH_TOKEN}`;
const origin = __ENV.AUTH_ORIGIN ? __ENV.AUTH_ORIGIN : 'http://localhost-studio.code.org:3000';
const websocketParams = {
  headers: {
    'Origin': origin,
  },
};

const uploadParams = {
  headers: {
    'Origin': origin,
    'Content-Type': 'application/json'
  },
};


const sources = JSON.stringify({
  "sources": {
    "main.json": "{\"source\":{\"HelloWorld.java\":{\"text\":\"public class HelloWorld {\\n  public static void main(String[] args)  {\\n    System.out.println(\\\"Hello World\\\");\\n  }\\n}\",\"isVisible\":true}},\"animations\":{}}"
  },
  "assetUrls": {}
});

export default function () {
  const uploadResult = http.put(uploadUrl, sources, uploadParams);

  const res = ws.connect(url, websocketParams, function (socket) {
    socket.on('open', () => console.log('connected'));

    socket.on('message', function(data) {
      console.log('Message received: ', data);
      const parsedData = JSON.parse(data);
      if (parsedData.type === 'EXCEPTION') {
        console.log('hit an exception ' + parsedData.value);
        exceptionCounter.add(1);
      }
    });

    socket.on('close', () => {
      console.log('disconnected, sleeping for 10 seconds');
      sleep(10);
    });

    socket.on('error', function(e) {
      console.log('error occurred: ' + e.error());
      errorCounter.add(1);
    })
  });

  check(res, 
    { 'websocket status is 101': (r) => r && r.status === 101 }
  );

  check(uploadResult, {'upload status is 200': (r) => r && r.status === 200 });
}