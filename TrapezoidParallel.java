import java.util.concurrent.*;
import java.util.*;

public class TrapezoidParallel {
    public interface Function {
        double f(double x);
    }

    private final Function func;
    private final double a, b;
    private final int maxThreads;
    private final double EPS = 1e-12;

    public TrapezoidParallel(Function func, double a, double b, int maxThreads) {
        this.func = func;
        this.a = a;
        this.b = b;
        this.maxThreads = Math.max(1, maxThreads);
    }

    public double integrateForN(int n, ExecutorService pool) throws Exception {
        if (n == 0) return 0.0;
        double h = (b - a) / n;
        // suma de extremos
        double sum = 0.5 * (func.f(a) + func.f(b));

        // distribuir i = 1..n-1 entre tareas
        int interior = Math.max(0, n - 1);
        if (interior > 0) {
            int tasks = Math.min(maxThreads, interior);
            List<Future<Double>> futures = new ArrayList<>();
            int chunk = interior / tasks;
            int rem = interior % tasks;
            int startIndex = 1;
            for (int t = 0; t < tasks; t++) {
                int len = chunk + (t < rem ? 1 : 0);
                int s = startIndex;
                int e = startIndex + len - 1;
                startIndex = e + 1;
                final int fs = s, fe = e;
                Callable<Double> job = () -> {
                    double local = 0.0;
                    for (int i = fs; i <= fe; i++) {
                        double x = a + i * h;
                        local += func.f(x);
                    }
                    return local;
                };
                futures.add(pool.submit(job));
            }
            for (Future<Double> f : futures) {
                sum += f.get();
            }
        }
        return h * sum;
    }

    public void runUntilRepeat(int maxN) throws Exception {
        int n = 1;
        double prev = Double.NaN;
        int available = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(maxThreads, available));
        try {
            while (n <= maxN) {
                double val = integrateForN(n, pool);
                System.out.printf("n=%d -> I=%.12f%n", n, val);
                if (!Double.isNaN(prev)) {
                    if (val == prev || Math.abs(val - prev) < EPS) {
                        System.out.printf("Se repite en n=%d (I=%.12f). Deteniendo.%n", n, val);
                        break;
                    }
                }
                prev = val;
                n++;
            }
            if (n > maxN) System.out.println("Alcanzado maxN sin repetición.");
        } finally {
            pool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        Function f = x -> 2.0*x*x + 3.0*x + 0.5;
        double a = 2.0, b = 20.0;
        int maxThreads = Runtime.getRuntime().availableProcessors(); // pool size recomendado
        TrapezoidParallel solver = new TrapezoidParallel(f, a, b, maxThreads);
        int maxN = 1_000_000; // límite de seguridad
        solver.runUntilRepeat(maxN);
    }
}
