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
 * @author tomasaraujo
 */
public class FCFS implements SchedulingAlgorithm {
    private Queue readyQueue;
    
    public FCFS (Queue readyQueue){
        this.readyQueue=readyQueue;
    }
    
    public Queue getReadyQueue() {
        return readyQueue;
    }

    public void setReadyQueue(Queue readyQueue) {
        this.readyQueue = readyQueue;
    }
    
    @Override
    public void reorder() {
        // do nothing
    }
    
    @Override
    public void dispatch(CPU cpu) {
        // do nothing
    }
    
}
