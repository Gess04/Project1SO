/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import CPU.CPU;
import DS.Queue;
import Process.Process;

/**
 *
 * @author tomasaraujo y Gabriel Flores
 */
public class Scheduler {
    private Queue<Process> readyQueue;
    private SchedulingAlgorithm algorithm;
    
    public Scheduler(SchedulingAlgorithm algorithm, Queue<Process> readyQueue) {
        this.algorithm = algorithm;
        this.readyQueue = readyQueue;
    }

    // Expose the ready queue if needed
    public Queue<Process> getReadyQueue() {
        return readyQueue;
    }

    // Reorder the queue based on the selected scheduling algorithm
    public void reorder() {
        algorithm.reorder();  // Reorder based on the specific algorithm
    }

    // Dispatch the process to the CPU for execution
    public void dispatch(CPU cpu) {
        reorder();  // Reorder the queue before dispatching
        Process process = readyQueue.dequeue();  // Get the next process
//        cpu.run(process);  // Let the CPU execute the process
    }
}
