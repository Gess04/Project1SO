/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;
import Process.Process;
import DS.Queue;
import CPU.CPU;

/**
 *
 * @author tomasaraujo
 */
public class SRT implements SchedulingAlgorithm{
    private Queue readyQueue;
    
    public SRT(Queue readyQueue) {
        this.readyQueue = readyQueue;
    }
    
    @Override
    public void reorder() {
        
    }
    
    @Override
    public void dispatch(CPU cpu) {
        
    }

}
