JUIValidationSupport = {}

JUIValidationSupport.isLetter = function(c) {
    return /\p{L}/u.test(String.fromCharCode(c));
}