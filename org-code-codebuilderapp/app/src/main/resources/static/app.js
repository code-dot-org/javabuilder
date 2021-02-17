var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#console").show();
    }
    else {
        $("#console").hide();
    }
    $("#output").html("");
}

function connect() {
    var socket = new SockJS('/codebuilder');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/topic/output', function (userOutput) {
            showOutput(JSON.parse(userOutput.body).output);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendFileName() {
    stompClient.send("/app/execute", {}, JSON.stringify({'fileName': $("#fileName").val(), 'code': $("#code").val()}));
}

function showOutput(message) {
    $("#output").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendFileName(); });
});