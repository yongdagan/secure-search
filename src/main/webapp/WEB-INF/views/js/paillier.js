paillier = {
    initKey: function(n, g, lambda, mu) {
        this.n = n;
        this.g = g;
        this.lambda = lambda;
        this.mu = mu;
        this.nsquare = n.pow(2);
    },
    decrypt: function(c) {
        if(c.compareTo(bigInt.zero) < 0 || c.compareTo(this.nsquare) >= 0 ||
                bigInt.gcd(c, this.nsquare).notEquals(bigInt.one)) {
            return bigInt.minusOne;
        }
        // L(x) = (x-1)/n
        // m = L(c^lambda mod nsquare) * mu mod n
        var x = c.modPow(this.lambda, this.nsquare);
        var l = x.minus(bigInt.one).divide(this.n);
        return l.multiply(this.mu).mod(this.n);
    }
}
