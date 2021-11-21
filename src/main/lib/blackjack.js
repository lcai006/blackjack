let cards = require('./cards');

// Blackjack game.
function BlackjackGame () {
    this.dealerHand = new BlackjackHand();
    this.playerHand = new BlackjackHand();
    this.aiHand = new BlackjackHand();
    this.player2Hand = new BlackjackHand();
    this.result = 'None';
    this.cards = cards.createPlayingCards();
    this.players = 0;
    this.current = 0;
    this.wait = [];
    this.status = {};
}

BlackjackGame.prototype.newGame = function (players, deck) {
    this.players = players;
    this.cards = cards.createPlayingCards();
    this.current = 1;
    this.wait = [];
    this.dealerHand = new BlackjackHand();
    this.playerHand = new BlackjackHand();
    this.aiHand = new BlackjackHand();
    this.player2Hand = new BlackjackHand();
    this.status['dealer'] = 'normal';
    this.status['ai'] = 'normal';
    this.status['player1'] = 'normal';
    if (this.players === 2)
        this.status['player2'] = 'normal';
    else
        this.status['player2'] = 'Empty';
    if (deck !== null) {
        this.cards.setDeck(deck);
    }

    this.dealCards();
    this.dealCards();

    this.result = 'None';
}

BlackjackGame.prototype.dealCards = function() {
    this.playerHand.addCard(this.cards.dealNextCard());
    if (this.players > 1)
        this.player2Hand.addCard(this.cards.dealNextCard());
    this.aiHand.addCard(this.cards.dealNextCard());
    this.dealerHand.addCard(this.cards.dealNextCard());
}

BlackjackGame.prototype.toJson = function () {
    let data = {
        dealer: {
            cards: this.dealerHand.getCards(),
            score: this.dealerHand.getScore(),
            visibleScore: this.dealerHand.getVisibleScore(),
            status: this.status['dealer']
        },
        player1: {
            cards: this.playerHand.getCards(),
            score: this.playerHand.getScore(),
            visibleScore: this.playerHand.getVisibleScore(),
            status: this.status['player1']
        },
        ai: {
            cards: this.aiHand.getCards(),
            score: this.aiHand.getScore(),
            visibleScore: this.aiHand.getVisibleScore(),
            status: this.status['ai']
        },
        current: this.current,
        result: this.result
    };
    if (this.players > 1) {
        data['player2'] = {
            cards: this.player2Hand.getCards(),
            score: this.player2Hand.getScore(),
            visibleScore: this.player2Hand.getVisibleScore(),
            status: this.status['player2']
        };
    }
    return data;
}

// any player can still play
BlackjackGame.prototype.isGameInProgress = function () {
    return this.status['player1'] === 'normal' || this.status['player2'] === 'normal' || this.status['dealer'] === 'normal' || this.status['ai'] === 'normal';
}

BlackjackGame.prototype.hit = function () {
    if (this.isGameInProgress()) {
        if (this.current === 1) {
            this.playerHand.addCard(this.cards.dealNextCard());
            if (this.playerHand.isBust()) {
                this.wait.push(1);
                this.status['player1'] = 'Bust';
            } else if (this.playerHand.numOfCards() === 7) {
                this.status['player1'] = '7-card Charlie';
            }
        } else {
            this.player2Hand.addCard(this.cards.dealNextCard());
            if (this.player2Hand.isBust()) {
                this.wait.push(2);
                this.status['player2'] = 'Bust';
            } else if (this.player2Hand.numOfCards() === 7) {
                this.status['player2'] = '7-card Charlie';
            }
        }

        this.aiDecisions();
        this.getResult();
    }


    if (this.wait.length < this.players) {
        this.next();
    }
}

BlackjackGame.prototype.getResult = function () {
    let winner = {'Player 1': this.playerHand.getScore(), 'Player 2': this.player2Hand.getScore(), 'AI Player': this.aiHand.getScore(), 'Dealer': this.dealerHand.getScore()};

    if (!this.isGameInProgress()) {
        if (this.players < 2) {
            delete winner['Player 2'];
        } else {
            if (winner['Player 2'] > 21)
                delete winner['Player 2'];
        }

        if (winner['Player 1'] > 21)
            delete winner['Player 1'];
        if (winner['AI Player'] > 21)
            delete winner['AI Player'];
        if (winner['Dealer'] > 21)
            delete winner['Dealer'];

        if (Object.keys(winner).length === 0) {
            this.result = 'All Bust';
        } else {
            const num = Object.values(winner);
            const max = Math.max(...num);
            const key = Object.keys(winner).filter(key => winner[key] === max);
            this.result = 'Winner ' + key.join(' ');
        }
    } else {
        this.result = 'None';
    }
}

BlackjackGame.prototype.stand = function () {
    this.wait.push(this.current);
    if (this.current === 1) {
        this.status['player1'] = 'Stand';
    } else {
        this.status['player2'] = 'Stand';
    }

    if (this.isGameInProgress()) {
        this.aiDecisions();
        this.getResult();
    }

    if (this.wait.length < this.players) {
        this.next();
    }

}

BlackjackGame.prototype.next = function () {
    if (this.wait.length === 1 && !this.wait.includes(this.current))
        return;

    this.current += 1;
    if (this.current > this.players) {
        this.current = 1;
    }
}

BlackjackGame.prototype.aiDecisions = function () {
    // human players done
    if (this.wait.length === this.players) {
        while (this.isGameInProgress()) {
            if (this.status['ai'] === 'normal')
                this.aiAction();
            if (this.status['dealer'] === 'normal')
                this.dealerAction();
        }
    } else if (this.wait.length === 1) {
        if (!this.wait.includes(this.current) || this.current !== 1) {
            if (this.current === this.players) {
                if (this.status['ai'] === 'normal')
                    this.aiAction();
                if (this.status['dealer'] === 'normal')
                    this.dealerAction();
            }
        }
    } else {
        if (this.current === this.players) {
            if (this.status['ai'] === 'normal')
                this.aiAction();
            if (this.status['dealer'] === 'normal')
                this.dealerAction();
        }
    }
}

BlackjackGame.prototype.aiAction = function () {
    let s = this.aiHand.getScore();
    if (s === 21) {
        this.status['ai'] = 'Stand';
    }else if (this.assume21()) {
        this.aiHand.addCard(this.cards.dealNextCard());
    } else if (s >= 18 && s <= 20) {
        if (this.dealerHand.getVisibleScore() > s - 10 || this.playerHand.getVisibleScore() > s - 10 || this.player2Hand.getVisibleScore() > s - 10) {
            this.aiHand.addCard(this.cards.dealNextCard());
        } else {
            this.status['ai'] = 'Stand';
        }
    } else {
        this.aiHand.addCard(this.cards.dealNextCard());
    }

    if (this.aiHand.isBust())
        this.status['ai'] = 'Bust';
    else if (this.aiHand.numOfCards() === 7)
        this.status['ai'] = '7-card Charlie';
}

BlackjackGame.prototype.dealerAction = function () {
    if (this.dealerHand.getScore() < 17) {
        this.dealerHand.addCard(this.cards.dealNextCard());
    } else if (this.dealerHand.getScore() === 17 && this.dealerHand.containsAce()){
        this.dealerHand.addCard(this.cards.dealNextCard());
    } else {
        this.status['dealer'] = 'Stand';
    }

    if (this.dealerHand.isBust())
        this.status['dealer'] = 'Bust';
    else if (this.dealerHand.numOfCards() === 7)
        this.status['dealer'] = '7-card Charlie';
}

// Blackjack hand.
function BlackjackHand() {
    this.cards = [];
}

BlackjackHand.prototype.hasCards = function () {
    return this.cards.length > 0;
}

BlackjackHand.prototype.numOfCards = function () {
    return this.cards.length;
}

BlackjackHand.prototype.addCard = function (card) {
    this.cards.push(card);
}

BlackjackHand.prototype.numberToSuit = function (number) {
    let suits = ['C', 'D', 'H', 'S'];
    let index = Math.floor(number / 13);
    return suits[index];
}

BlackjackHand.prototype.numberToCard = function (number) {
  return {
    rank: (number % 13) + 1,
    suit: this.numberToSuit(number)
  };
}

BlackjackHand.prototype.getCards = function () {
    let convertedCards = [];
    for (let i = 0; i < this.cards.length; i++) {
        let number = this.cards[i];
        convertedCards[i] = this.numberToCard(number);
    }
    return convertedCards;
}

BlackjackHand.prototype.getCardScore = function (card) {
    if (card.rank === 1) {
        return 11;
    } else if (card.rank >= 11) {
        return 10;
    }
    return card.rank;
}

BlackjackHand.prototype.containsAce = function () {
    let cards = this.getCards();

    for (let i = 0; i < cards.length; ++i) {
        let card = cards[i];
        if (card.rank === 1)
            return true;
    }

    return false;
}

BlackjackGame.prototype.assume21 = function () {
    if (this.playerHand.numOfCards() === 2 && (this.playerHand.getVisibleScore() === 10 || this.playerHand.getVisibleScore() === 11))
        return true;
    else return this.player2Hand.numOfCards() === 2 && (this.player2Hand.getVisibleScore() === 10 || this.player2Hand.getVisibleScore() === 11);
}

BlackjackHand.prototype.getScore = function () {
    let score = 0;
    let cards = this.getCards();
    let aces = [];

    // Sum all cards excluding aces.
    for (let i = 0; i < cards.length; ++i) {
        let card = cards[i];
        if (card.rank === 1) {
            aces.push(card);
        } else {
            score = score + this.getCardScore(card);
        }
    }

    // Add aces.
    if (aces.length > 0) {
        let acesScore = aces.length * 11;
        let acesLeft = aces.length;
        while ((acesLeft > 0) && (acesScore + score) > 21) {
            acesLeft = acesLeft - 1;
            acesScore = acesScore - 10;
        }
        score = score + acesScore;
    }

    return score;
}

BlackjackHand.prototype.getVisibleScore = function () {
    let score = 0;
    let cards = this.getCards();
    let aces = [];

    // Skip 1st card
    for (let i = 1; i < cards.length; ++i) {
        let card = cards[i];
        if (card.rank === 1) {
            aces.push(card);
        } else {
            score = score + this.getCardScore(card);
        }
    }

    // Add aces.
    if (aces.length > 0) {
        let acesScore = aces.length * 11;
        let acesLeft = aces.length;
        while ((acesLeft > 0) && (acesScore + score) > 21) {
            acesLeft = acesLeft - 1;
            acesScore = acesScore - 10;
        }
        score = score + acesScore;
    }

    return score;
}

BlackjackHand.prototype.isBust = function () {
    return this.getScore() > 21;
}

// Exports.
function newGame () {
    return new BlackjackGame();
}

exports.newGame = newGame