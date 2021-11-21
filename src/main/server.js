const http = require('http'),
    fs = require('fs'),
    url = require('url'),
    game = require('./lib/blackjack.js').newGame();

const Server = {};
let io;
let clients = [];
let ready = [];
let deck = null;

Server.restart = function () {
    console.log('restart');
    clients = [];
    ready = [];
    deck = null;
}

Server.deal = function () {
    console.log('deal');
    game.newGame(clients.length, deck);
    ready = [];
    io.sockets.emit('deal', game.toJson());
}

Server.hit = function () {
    console.log('hit');
    game.hit();
    io.sockets.emit('hit', game.toJson());
}

Server.stand = function () {
    console.log('stand');
    game.stand();
    io.sockets.emit('stand', game.toJson());
}

Server.registerSocketIO = function (io) {
    io.sockets.on('connection', function (socket) {
        console.log('User connected');
        if (clients.length <= 1) {
            let id = clients.length + 1;
            clients.push(id)
            socket.emit('id', id);
        }

        socket.on('deal', function (data) {
            if (!ready.includes(data))
                ready.push(data)

            if (ready.length === clients.length)
                Server.deal(socket);
        });

        socket.on('hit', function () {
            Server.hit();
        });

        socket.on('stand', function () {
            Server.stand();
        });

        socket.on('disconnect', function () {
            console.log('User disconnected');
            if (clients.length === 2) {
                Server.restart();
            }
        });
    });
}

Server.init = function () {
    const httpServer = http.createServer(async function (req, res) {
        let path = url.parse(req.url).pathname;
        console.log(path);
        let contentType = 'text/html';
        if (path === '/') {
            path = '/index.html';
        } else if (path.indexOf('.css')) {
            contentType = 'text/css';
        } else if (path.indexOf('.svg')) {
            contentType = 'image/svg+xml';
        }
        const buffers = [];

        for await (const chunk of req) {
            buffers.push(chunk);
        }

        deck = Buffer.concat(buffers).toString();

        fs.readFile(__dirname + path, function (error, data) {
            res.writeHead(200, {'Content-Type': contentType});
            res.end(data, 'utf-8');
        });
    }).listen(3000);

    io = require('socket.io').listen(httpServer);
    Server.registerSocketIO(io);
}

Server.init();


