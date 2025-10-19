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
public class FCFS {
    private Queue readyQueue;
    
    public FCFS (Queue readyQueue){
        this.readyQueue=readyQueue;
    }
    
    public void dispatch (CPU cpu){
        cpu.run((Process) readyQueue.dequeue());
    }

    public Queue getReadyQueue() {
        return readyQueue;
    }

    public void setReadyQueue(Queue readyQueue) {
        this.readyQueue = readyQueue;
    }
    
    
}
