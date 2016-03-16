function [y, delta] = test(theta)
data = load("out.csv");
y = data(:, 1);
X = data(:, [2:14]);
X = [ones(size(X, 1), 1) X];
x = X*theta;
t = 1:size(y, 1);
plot(t, y, "xr", t, x, "g");
delta = y-x;
end