/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;
import DS.Queue;
import Process.Process;
import CPU.CPU;

/**
 *
 * @author tomasaraujo
 */
public class SPN implements SchedulingAlgorithm {
    private Queue readyQueue;
    
    public SPN(Queue readyQueue) {
        this.readyQueue = readyQueue;
    }

    /**
     * @return the readyQueue
     */
    public Queue getReadyQueue() {
        return readyQueue;
    }

    /**
     * @param readyQueue the readyQueue to set
     */
    public void setReadyQueue(Queue readyQueue) {
        this.readyQueue = readyQueue;
    }
    
    @Override
    public void reorder() {
        Process[] processes = readyQueue.getAllElements();
        if (processes == null || processes.length <= 1) return;

        for (int i = 0; i < processes.length - 1; i++) {
            int minIndex = i;
            int minBurst = (processes[i].getRemainingBurstTime() != null) ? processes[i].getRemainingBurstTime() : Integer.MAX_VALUE;

            for (int j = i + 1; j < processes.length; j++) {
                int burstJ = (processes[j].getRemainingBurstTime() != null) ? processes[j].getRemainingBurstTime() : Integer.MAX_VALUE;

                if (burstJ < minBurst) {
                    minBurst = burstJ;
                    minIndex = j;
                }
            }

            if (minIndex != i) {
                Process temp = processes[i];
                processes[i] = processes[minIndex];
                processes[minIndex] = temp;
            }
        }

        readyQueue.clear();
        for (int i = 0; i < processes.length; i++) {
            readyQueue.enqueue(processes[i]);
        }
        

    }
    
    @Override
    public void dispatch(CPU cpu) {
        
    }
}
