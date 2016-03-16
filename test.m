function [y, delta, j] = test(theta)
data = load("out.csv");
y = data(:, 1);
X = data(:, [2:size(data, 2)]);
X = [ones(size(X, 1), 1) X];
x = X*theta;
t = 1:size(y, 1);
plot(t, y, "xr", t, x, "g");
delta = y-x;
j = sum(delta.^2);
end