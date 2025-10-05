#include <iostream>
#include <vector>
#include <thread>
#include <cmath>
#include <functional>
#include <limits>

using namespace std;

double f(double x) {
    return 2.0*x*x + 3.0*x + 0.5;
}

double integrateForN(int n, double a, double b, int numThreads) {
    if (n == 0) return 0.0;
    double h = (b - a) / n;
    double sum = 0.5 * (f(a) + f(b));
    int interior = max(0, n - 1);
    if (interior > 0) {
        int tasks = min(numThreads, interior);
        // crear array dinamico para sumas parciales
        double* partial = new double[tasks];
        for (int i = 0; i < tasks; ++i) partial[i] = 0.0;

        vector<thread> threads;
        int chunk = interior / tasks;
        int rem = interior % tasks;
        int start = 1;
        for (int t = 0; t < tasks; ++t) {
            int len = chunk + (t < rem ? 1 : 0);
            int s = start;
            int e = start + len - 1;
            start = e + 1;
            threads.emplace_back([=, &partial]() {
                double local = 0.0;
                for (int i = s; i <= e; ++i) {
                    double x = a + i * h;
                    local += f(x);
                }
                partial[t] = local;
            });
        }
        for (auto &th : threads) th.join();
        for (int t = 0; t < tasks; ++t) sum += partial[t];
        delete[] partial;
    }
    return h * sum;
}

int main() {
    double a = 2.0, b = 20.0;
    int maxThreads = thread::hardware_concurrency();
    if (maxThreads == 0) maxThreads = 4;
    int maxN = 1000000;
    double EPS = 1e-12;
    double prev = numeric_limits<double>::quiet_NaN();

    for (int n = 1; n <= maxN; ++n) {
        double val = integrateForN(n, a, b, maxThreads);
        cout.setf(std::ios::fixed); cout.precision(12);
        cout << "n=" << n << " -> I=" << val << "\n";
        if (!isnan(prev)) {
            if (val == prev || fabs(val - prev) < EPS) {
                cout << "Se repite en n=" << n << " (I=" << val << "). Deteniendo.\n";
                break;
            }
        }
        prev = val;
    }
    return 0;
}
