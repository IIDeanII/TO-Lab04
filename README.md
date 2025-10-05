Notas Java

Compilar: javac TrapezoidParallel.java

Ejecutar: java TrapezoidParallel

Ajusta EPS o maxN si se quiere más precisión o más iteraciones.

Pool: Executors.newFixedThreadPool(...) es adecuado para CPU-bound en arquitecturas 64-bit, usando availableProcessors().
________________________________________________________________________
Notas C++

Compilar: g++ -std=c++17 -O2 -pthread TrapezoideParalelo.cpp -o trapezoid

Ejecutar: ./trapezoid

Aquí usamos new/delete para partial (memoria dinámica) y std::thread.

No se uso un thread pool estándar; para C++ moderno se puede implementar un pool o usar bibliotecas (Boost, ThreadPool libs). La versión presentada crea tasks threads por evaluación n.
_________________________________________________________________________
Notas Go

Compilar: go build TrapecioParalelo.go 

Worker pool: usamos ranges channel para distribuir sub-rangos y results para recolectar sumas.

runtime.NumCPU() y GOMAXPROCS para alinear con núcleos físicos/logical en sistemas 64-bit.
