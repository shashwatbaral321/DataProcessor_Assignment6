package main

import (
	"fmt"
	"sync"
	"time"
)

type Task struct {
	ID int
}

type Result struct {
	TaskID   int
	WorkerID int
	Output   string
}

func worker(id int, tasks <-chan Task, results chan<- Result, wg *sync.WaitGroup) {
	defer wg.Done()
	for task := range tasks {
		fmt.Printf("Worker %d started Task-%d\n", id, task.ID)
		time.Sleep(300 * time.Millisecond) // simulate work
		fmt.Printf("Worker %d completed Task-%d\n", id, task.ID)
		results <- Result{
			TaskID:   task.ID,
			WorkerID: id,
			Output:   fmt.Sprintf("Result of Task-%d by Worker-%d", task.ID, id),
		}
	}
}

func main() {
	numWorkers := 4
	numTasks := 10

	tasks := make(chan Task, numTasks)
	results := make(chan Result, numTasks)

	var wg sync.WaitGroup

	// Start workers
	for w := 0; w < numWorkers; w++ {
		wg.Add(1)
		go worker(w, tasks, results, &wg)
	}

	// Send tasks
	for t := 1; t <= numTasks; t++ {
		tasks <- Task{ID: t}
	}
	close(tasks)

	wg.Wait()
	close(results)

	// Print results
	fmt.Println("\nFinal Results:")
	for result := range results {
		fmt.Println(result.Output)
	}
}
