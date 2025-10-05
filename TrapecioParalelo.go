package main

import (
	"fmt"
	"math"
	"runtime"
	"sync"
)

func f(x float64) float64 {
	return 2.0*x*x + 3.0*x + 0.5
}

type Range struct{ s, e int }

func integrateForN(n int, a, b float64, workers int) float64 {
	if n == 0 {
		return 0.0
	}
	h := (b - a) / float64(n)
	sum := 0.5 * (f(a) + f(b))
	interior := n - 1
	if interior > 0 {
		tasks := workers
		if tasks > interior {
			tasks = interior
		}
		ranges := make(chan Range, tasks)
		results := make(chan float64, tasks)
		var wg sync.WaitGroup

		
		for w := 0; w < tasks; w++ {
			wg.Add(1)
			go func() {
				defer wg.Done()
				for r := range ranges {
					local := 0.0
					for i := r.s; i <= r.e; i++ {
						x := a + float64(i)*h
						local += f(x)
					}
					results <- local
				}
			}()
		}

		chunk := interior / tasks
		rem := interior % tasks
		start := 1
		for t := 0; t < tasks; t++ {
			len := chunk
			if t < rem {
				len++
			}
			if len == 0 {
				results <- 0.0
				continue
			}
			s := start
			e := start + len - 1
			start = e + 1
			ranges <- Range{s: s, e: e}
		}
		close(ranges)

		go func() {
			wg.Wait()
			close(results)
		}()

		for r := range results {
			sum += r
		}
	}
	return h * sum
}

func main() {
	a, b := 2.0, 20.0
	maxWorkers := runtime.NumCPU()
	runtime.GOMAXPROCS(maxWorkers)
	maxN := 1000000
	EPS := 1e-12
	prev := math.NaN()

	for n := 1; n <= maxN; n++ {
		val := integrateForN(n, a, b, maxWorkers)
		fmt.Printf("n=%d -> I=%.12f\n", n, val)
		if !math.IsNaN(prev) {
			if val == prev || math.Abs(val-prev) < EPS {
				fmt.Printf("Se repite en n=%d (I=%.12f). Deteniendo.\n", n, val)
				break
			}
		}
		prev = val
	}
}
