/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CPU;

import Clock.ClockManager;
import DS.ProcessList;
import DS.Queue;
import Process.Process;
import Scheduler.Scheduler;

/**
 *
 * @author Gabriel Flores
 */
public class CPU {
    private String runningProcess;
    private Integer PC;
    private Integer MAR;
    private Process process;
    private Scheduler scheduler;
    private ClockManager clockManager;
    private Queue readyQueue;
    private Queue blockedQueue;
    private ProcessList exitList;
    private String processName;

    public CPU(String runningProcess, Integer PC, Integer MAR, Process process, ClockManager clockManager, Queue readyQueue, Queue blockedQueue, ProcessList exitList, String processName) {
        this.runningProcess = "OS";
        this.PC = 0;
        this.MAR = 0;
        this.process = process;
        this.clockManager = clockManager;
        this.readyQueue = readyQueue;
        this.blockedQueue = blockedQueue;
        this.exitList = exitList;
        this.processName = "";
    }

    public String getRunningProcess() {
        return runningProcess;
    }

    public void setRunningProcess(String runningProcess) {
        this.runningProcess = runningProcess;
    }

    public Integer getPC() {
        return PC;
    }

    public void setPC(Integer PC) {
        this.PC = PC;
    }

    public Integer getMAR() {
        return MAR;
    }

    public void setMAR(Integer MAR) {
        this.MAR = MAR;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Queue getReadyQueue() {
        return readyQueue;
    }

    public void setReadyQueue(Queue readyQueue) {
        this.readyQueue = readyQueue;
    }
    
    
    public void run (Process process) {
        setProcess(process);
        setProcessName(process.getProcessName());
        setRunningProcess("p"+process.getID());
        new Thread(() -> {
            int startCycle = clockManager.getClockCycles();
            int targetCycle;    
        }).start();
    }
    
    
    
    
}
