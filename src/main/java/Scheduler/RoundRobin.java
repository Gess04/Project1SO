/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Scheduler;

import CPU.CPU;
import DS.Queue;
import Process.Process;
import java.util.concurrent.Semaphore;

/**
 *
 * @author tomasaraujo
 */
public class RoundRobin implements SchedulingAlgorithm {
    private final Queue<Process> ready;
    private final Semaphore readyLock;
    private final int quantum;      // en ciclos de reloj

    private int     remainingSlice = 0;
    private Integer lastPid        = null;
    
     private int lastClockSeen = -1;

    public RoundRobin(Queue<Process> ready, Semaphore readyLock, int quantum) {
        this.ready     = ready;
        this.readyLock = readyLock;
        this.quantum   = (quantum <= 0) ? 1 : quantum;
    }

    @Override public void reorder() { 
        // do nothing 
    }

    @Override
    public void dispatch(CPU cpu) {
        if (cpu.getProcess() != null) return;
        try {
            readyLock.acquire();
            if (!ready.isEmpty()) {
                Process nxt = ready.dequeue();
                cpu.setProcess(nxt);
                cpu.setProcessName(nxt.getProcessName());
                cpu.setRunningProcess("P" + nxt.getID());
                nxt.setStatus(Process.Status.Running);
                lastPid        = nxt.getID();
                remainingSlice = quantum;
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            readyLock.release();
        }
    }
    
    private void preemptToReady(CPU cpu) {
        Process running = cpu.getProcess();
        if (running == null) return;
        if (running.getStatus() != Process.Status.Running) return;

        running.setStatus(Process.Status.Ready);

        try {
            readyLock.acquire();
            ready.enqueue(running);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            readyLock.release();
        }

        cpu.setProcess(null);
        cpu.setRunningProcess("OS");
        cpu.setProcessName("");

        lastPid        = null;
        remainingSlice = 0;
    }

    @Override
    public void onTick(CPU cpu) {
        
        int clk = cpu.getClockManager().getClockCycles();
        if (clk == lastClockSeen) {
            return;
        }
        lastClockSeen = clk;
        Process running = cpu.getProcess();

        if (running == null) {
            dispatch(cpu);
            return;
        }

        // si cambió el proceso, arranca nuevo quantum
        if (lastPid == null || !lastPid.equals(running.getID())) {
            lastPid        = running.getID();
            remainingSlice = quantum;
            return;
        }

        // descontar 1 por tick si sigue Running
        if (running.getStatus() == Process.Status.Running) {
            remainingSlice--;
        }

        // terminó o se bloqueó
        if ((running.getRemainingBurstTime() != null && running.getRemainingBurstTime() <= 0)
            || running.getStatus() == Process.Status.Blocked) {
            lastPid = null;
            remainingSlice = 0;
            return;
        }

        // se agotó el quantum
        if (remainingSlice <= 0) {
            preemptToReady(cpu);
            dispatch(cpu); // carga el siguiente si hay
        }
    }

}