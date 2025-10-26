/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;
import DS.Queue;
import CPU.CPU;
import Clock.ClockManager;
import Process.Process;

/**
 *
 * @author tomasaraujo
 */
public class HRRN implements SchedulingAlgorithm {
    private final Queue readyQueue;
    private final ClockManager clock;

    public HRRN(Queue ready, ClockManager clock) {
        this.readyQueue = ready;
        this.clock = clock;
    }

    @Override
    public void reorder() {
        Process[] arr = readyQueue.getAllElements();
        if (arr == null || arr.length <= 1) return;

        int now = clock.getClockCycles();

        // Precalcular ratios para no recomputar en el sort
        double[] ratio = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            Process p = arr[i];

            int service = (p.getRemainingBurstTime() != null) ? p.getRemainingBurstTime()
                         : (p.getInstructionCount() != null ? p.getInstructionCount() : 1);
            if (service <= 0) service = 1;

            int arrival = p.getArrivaltime();
            int waiting = now - arrival;
            if (waiting < 0) waiting = 0;

            double rr = (waiting + service) / (double) service;
            ratio[i] = rr;
            p.setResponseRatio(rr);
        }

        // Selection sort por ratio. Empates: menor arrivalTime primero.
        for (int i = 0; i < arr.length - 1; i++) {
            int bestPos = i;
            double bestRR = ratio[i];
            int bestArrival = arr[i].getArrivaltime();

            for (int j = i + 1; j < arr.length; j++) {
                double rrj = ratio[j];
                int arrJ = arr[j].getArrivaltime();

                boolean better = false;
                if (rrj > bestRR) {
                    better = true;
                } else if (rrj == bestRR && arrJ < bestArrival) {
                    // desempate por llegada más antigua
                    better = true;
                }

                if (better) {
                    bestPos = j;
                    bestRR = rrj;
                    bestArrival = arrJ;
                }
            }

            if (bestPos != i) {
                Process tmpP = arr[i];
                arr[i] = arr[bestPos];
                arr[bestPos] = tmpP;
                double tmpR = ratio[i];
                ratio[i] = ratio[bestPos];
                ratio[bestPos] = tmpR;
            }
        }

        readyQueue.clear();
        for (int i = 0; i < arr.length; i++) {
            readyQueue.enqueue(arr[i]);
        }
    }

    @Override
    public void dispatch(CPU cpu) {
        // do nothing
    }
    
    @Override
    public void onTick(CPU cpu) {
        
    }
}
