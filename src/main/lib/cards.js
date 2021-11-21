
function PlayingCards() {
    this.cards = this.getShuffledPack();
    this.currentPackLocation = 0;
    this.deck = null;
}

PlayingCards.prototype.getRandomInt = function (max) {
    return Math.floor(Math.random() * (max + 1));
}

PlayingCards.prototype.getShuffledPack = function () {
    let cards = [];
    cards[0] = 0;
    for (let i = 1; i < 52; i++) {
        let j = this.getRandomInt(i);
        cards[i] = cards[j];
        cards[j] = i;        
    }

    if (this.deck !== null && this.deck !== undefined) {
        for (const n of this.deck) {
            cards = cards.filter(function(e){
                return e !== n;
            });
        }

        cards = this.deck.concat(cards)
    }

    return cards;
}

PlayingCards.prototype.dealNextCard = function () {
    
    console.log("currentPackLocation: " + this.currentPackLocation);

    if (this.currentPackLocation >= this.cards.length) {
        this.cards = this.getShuffledPack();
        this.currentPackLocation = 0;
        console.log("Created new pack");
    }

    let cardNumber = this.cards[this.currentPackLocation];
    this.currentPackLocation = this.currentPackLocation + 1;
    return cardNumber;
}

PlayingCards.prototype.setDeck = function (deck) {
    if (typeof(deck) == "string") {
        let list = deck.split(',');
        this.deck = list.map((i) => Number(i));
    }
    this.cards = this.getShuffledPack();
}

function createPlayingCards () {
    return new PlayingCards();
}

exports.createPlayingCards = createPlayingCards