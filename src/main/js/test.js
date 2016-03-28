#!/usr/local/bin/node
function buildRandomTreeFromCenterRightLeft(s) {
    if (s == '') {
        return null;
    }
    var center = s.substr(0, 1);
    var rightAndLeft = s.substr(1);
    var rightLength = Math.floor(Math.random() * (rightAndLeft.length + 1));
    var right = rightAndLeft.substr(0, rightLength);
    var left = rightAndLeft.substr(rightLength, rightAndLeft.length);
    return {
        center: center,
        left: buildRandomTreeFromCenterRightLeft(left),
        right: buildRandomTreeFromCenterRightLeft(right)
    };
}

function printTreeLeftCenterRight(tree) {
    if (tree == null) {
        return '';
    }
    return printTreeLeftCenterRight(tree.left) + tree.center + printTreeLeftCenterRight(tree.right);
}

function printTreeRightLeftCenter(tree) {
    if (tree == null) {
        return '';
    }
    return printTreeRightLeftCenter(tree.right) + printTreeRightLeftCenter(tree.left) + tree.center;
}

function findEmail(centerRightLeft, leftCenterRight) {
    while (true) {
        var tree = buildRandomTreeFromCenterRightLeft(centerRightLeft);
        if (printTreeLeftCenterRight(tree) == leftCenterRight) {
            var email = printTreeRightLeftCenter(tree);
            console.log(email);
            break;
        }
    }
}

var easyProblem = {
    centerRightLeft: 'damEra@ilh',
    leftCenterRight: 'hal@irdamE'
};
findEmail(easyProblem.centerRightLeft, easyProblem.leftCenterRight);

var hardProblem = {
    centerRightLeft: '.o elPsaec lamei.a sur@h treotrdil0ems.al Pe7  dnse8dco9e:i438f7bfc5224b5d151bdc4ai bc91eamuoy ne r',
    leftCenterRight: 'er muyn oaia1e 9cbi14bdcc55d15b4227fb43f8l:9e8odc7dens  0eaPl s.em.iderotrr@t h.aus c ilaemo saeelP'
};
findEmail(hardProblem.centerRightLeft, hardProblem.leftCenterRight);
