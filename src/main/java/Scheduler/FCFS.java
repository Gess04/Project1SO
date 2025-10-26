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
    
    
    private int compare(Process a, Process b) {
        int ta = a.getArrivaltime();
        int tb = b.getArrivaltime();
        if (ta != tb) return ta - tb;
        return a.getID().compareTo(b.getID());
    }
    
    @Override
    public void reorder() {
        Process[] arr = readyQueue.getAllElements();
        if (arr == null || arr.length <= 1) return;

        // insertion sort por (arrivalTime, ID)
        for (int i = 1; i < arr.length; i++) {
            Process key = arr[i];
            int j = i - 1;
            while (j >= 0 && compare(arr[j], key) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }

        readyQueue.clear();
        for (int k = 0; k < arr.length; k++) readyQueue.enqueue(arr[k]); 
    }
    
    @Override
    public void dispatch(CPU cpu) {
        // do nothing
    }
    
    @Override
    public void onTick(CPU cpu) {
        // do nothing
    }
    
}
