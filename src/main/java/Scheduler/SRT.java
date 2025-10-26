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
public class SRT implements SchedulingAlgorithm {
    private final Queue reayQueue;

    public SRT(Queue readyQueue) {
        this.reayQueue = readyQueue;
    }

    
    private int rem(Process process) {
        Integer r = process.getRemainingBurstTime();
        return (r == null || r < 0) ? Integer.MAX_VALUE : r;
    }
    
    @Override
    public void reorder() {
        Process[] src = reayQueue.getAllElements();
        if (src == null || src.length <= 1) return;

        for (int i = 0; i < src.length - 1; i++) {
            int best = i;
            for (int j = i + 1; j < src.length; j++) {
                if (rem(src[j]) < rem(src[best])) {
                    best = j;
                }
            }
            if (best != i) {
                Process tmp = src[i];
                src[i] = src[best];
                src[best] = tmp;
            }
        }

        reayQueue.clear();
        for (int k = 0; k < src.length; k++) {
            reayQueue.enqueue(src[k]);
        }
    }

    // SRT es preventivo: si hay uno más corto en READY que el que corre, preempt.
    @Override
    public void onTick(CPU cpu) {
        Process cur = cpu.getProcess();
        if (cur == null) return;       
        Process head = (Process) reayQueue.peek();  

        if (head != null && rem(head) < rem(cur)) {
            cpu.preemptRunningToReady();
        }
    }

    @Override
    public void dispatch(CPU cpu) { 
        // do nothing
    }

}
