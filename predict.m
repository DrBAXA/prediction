function theta = predict()
data = load("out.csv");
y = data(:, 1);
X = data(:, [2:size(data, 2)]);
X = [ones(size(X, 1), 1) X];
theta = pinv(X'*X, 300)*X'*y;
end