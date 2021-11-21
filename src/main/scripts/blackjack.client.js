const App = {};
let uid = 0;
let firstHide = true;

App.deal = function () {
    firstHide = true;
    App.socket.emit('deal', uid);
}

App.hit = function () {
    App.socket.emit('hit');
}

App.stand = function () {
    App.socket.emit('stand');
}

App.getRankHtml = function (rank) {
    if (rank === 1) {
        return 'A';
    } else if (rank === 11) {
        return 'J';
    } else if (rank === 12) {
        return 'Q';
    } else if (rank === 13) {
        return 'K';
    }
    return rank;
}

App.getCardsHtml = function (cards, hide) {
    const htmlEntities = {
        'H': '&#9829;',
        'D': '&#9830;',
        'C': '&#9827;',
        'S': '&#9824;'
    };

    let html = '';
    for (let i = 0; i < cards.length; i++) {
        const card = cards[i];
        let rank = App.getRankHtml(card.rank);
        if (i === 0 && hide) {
            html += `
			<div class="card back">
			</div>
		    `;
        } else {
            html += `
			<div class="card ` + card.suit + `">
				<div class="top rank">` + App.getRankHtml(rank) + `</div>
				<div class="suit">` + htmlEntities[card.suit] + `</div>
				<div class="bottom rank">` + App.getRankHtml(rank) + `</div>
			</div>
		    `;
        }
    }
    return html;
}

App.updatePlayer = function (player) {
    const html = App.getCardsHtml(player.cards);
    $('#playerCards').html(html);
    $('#playerScore').text('Score: ' + player.score);
    console.log(player.status);
    if (player.status !== 'normal')
        $('#playerStatus').text(player.status);
    else
        $('#playerStatus').text('');
}

App.updateDealer = function (dealer) {
    let html = '';
    if (firstHide && dealer.status !== 'Bust') {
        html = App.getCardsHtml(dealer.cards, true);
        $('#dealerScore').text('Score: ' + dealer.visibleScore);
    } else {
        html = App.getCardsHtml(dealer.cards, false);
        $('#dealerScore').text('Score: ' + dealer.score);
    }
    $('#dealerCards').html(html);
    if (dealer.status !== 'normal')
        $('#dealerStatus').text(dealer.status);
    else
        $('#dealerStatus').text('');
}

App.updateOtherPlayer = function (player) {
    let html = '';
    if (firstHide && player.status !== 'Bust') {
        html = App.getCardsHtml(player.cards, true);
        $('#otherScore').text('Score: ' + player.visibleScore);
    } else {
        html = App.getCardsHtml(player.cards, false);
        $('#otherScore').text('Score: ' + player.score);
    }
    $('#otherCards').html(html);
    if (player.status !== 'normal')
        $('#otherStatus').text(player.status);
    else
        $('#otherStatus').text('');
}

App.updateAi = function (ai) {
    let html;
    if (firstHide && ai.status !== 'Bust') {
        html = App.getCardsHtml(ai.cards, true);
        $('#aiScore').text('Score: ' + ai.visibleScore);
    } else {
        html = App.getCardsHtml(ai.cards, false);
        $('#aiScore').text('Score: ' + ai.score);
    }
    $('#aiCards').html(html);
    if (ai.status !== 'normal')
        $('#aiStatus').text(ai.status);
    else
        $('#aiStatus').text('');
}

App.updateResult = function (result) {
    let displayResult = result;
    if (result === 'None') {
        displayResult = '';
    }
    $('#result').text(displayResult);
}

App.disableButton = function (id) {
    $(id).attr('disabled', 'disabled');
}

App.enableButton = function (id) {
    $(id).removeAttr('disabled');
}

App.disableDeal = function () {
    App.disableButton('#deal');
    App.enableButton('#hit');
    App.enableButton('#stand');
}

App.enableDeal = function () {
    App.enableButton('#deal');
    App.disableButton('#hit');
    App.disableButton('#stand');
}

App.disableAll =  function () {
    App.disableButton('#deal');
    App.disableButton('#hit');
    App.disableButton('#stand');
}

App.enableDealIfGameFinished = function (result) {
    if (result !== 'None') {
        App.enableDeal();
    }
}

App.dealResult = function (game) {
    if (game.current === uid)
        App.disableDeal();
    App.updateView(game);
}

App.updateGame = function (game) {
    if (game.current === uid)
        App.disableDeal();
    App.updateView(game);
    App.enableDealIfGameFinished(game.result);
}

App.updateView = function (game) {
    if (game.result !== 'None') {
        firstHide = false;
    }
    App.updateDealer(game.dealer);
    if (uid === 1) {
        App.updatePlayer(game.player1);
        if (game.hasOwnProperty('player2'))
            App.updateOtherPlayer(game.player2);
    } else if (uid === 2) {
        App.updatePlayer(game.player2);
        App.updateOtherPlayer(game.player1);
    }
    App.updateAi(game.ai);
    App.updateResult(game.result);
}

App.socket = {}

App.registerClientActions = function () {
    
    $('#deal').on("click", function () {
        App.disableAll();
        App.deal();
    });

    $('#hit').on("click", function () {
        App.disableAll();
        App.hit();
    });

    $('#stand').on("click", function () {
        App.disableAll();
        App.stand();
    });
}

App.registerServerActions = function () {
    App.socket.on('id', function (id) {
        uid = id;
        if (id > 1) {
            $('#otherPlayer').text("Player 1");
        }
    });
    App.socket.on('stand', function (game) {
        App.updateGame(game);
    });
    App.socket.on('deal', function (game) {
        App.dealResult(game);
    });
    App.socket.on('hit', function (game) {
        App.updateGame(game);
    });
}

App.init = function () {
    App.socket = io.connect('http://localhost:3000');
    App.registerClientActions();
    App.registerServerActions();
    App.enableDeal();
}

$(function () {
    App.init();
});