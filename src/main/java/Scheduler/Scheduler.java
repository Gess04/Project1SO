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

    public void reorder() {
        algorithm.reorder();
    }

    public void dispatch(CPU cpu) {
        reorder();
        Process process = readyQueue.dequeue();
    }
    
    public void onTick(CPU cpu) {
        if (algorithm == null) return;

        algorithm.onTick(cpu);

        algorithm.reorder();

        if (cpu.getProcess() == null) {
            algorithm.dispatch(cpu);
        }
    }
}
