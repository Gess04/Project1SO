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
import java.util.concurrent.Semaphore;

/**
 *
 * @author Gabriel Flores
 */
public class CPU {

    private String runningProcess = "OS";
    private Integer PC = 0;
    private Integer MAR = 0;
    private String processName = "";
    private Process process = null;

    private ClockManager clockManager;
    private Queue readyQueue;
    private Queue blockedQueue;
    private ProcessList exitList;

    private Semaphore readyLock;
    private Semaphore blockedLock;
    private Semaphore exitLock;
    private Semaphore ioDevice;

    private volatile boolean running = false;
    private Thread worker;
    private int lastCycleRef = 0;
    
    private volatile boolean preemptRequested = false;
    private volatile Process preemptCandidate = null;
    
    public CPU(ClockManager clockManager,
               Queue readyQueue, Queue blockedQueue, ProcessList exitList,
               Semaphore readyLock, Semaphore blockedLock, Semaphore exitLock,
               Semaphore ioDevice) {
        this.clockManager = clockManager;
        this.readyQueue = readyQueue;
        this.blockedQueue = blockedQueue;
        this.exitList = exitList;
        this.readyLock = readyLock;
        this.blockedLock = blockedLock;
        this.exitLock = exitLock;
        this.ioDevice = ioDevice;
    }
    
    
        /**
     * @return the runningProcess
     */
    public String getRunningProcess() {
        return runningProcess;
    }

    /**
     * @param runningProcess the runningProcess to set
     */
    public void setRunningProcess(String runningProcess) {
        this.runningProcess = runningProcess;
    }

    /**
     * @return the PC
     */
    public Integer getPC() {
        return PC;
    }

    /**
     * @param PC the PC to set
     */
    public void setPC(Integer PC) {
        this.PC = PC;
    }

    /**
     * @return the MAR
     */
    public Integer getMAR() {
        return MAR;
    }

    /**
     * @param MAR the MAR to set
     */
    public void setMAR(Integer MAR) {
        this.MAR = MAR;
    }

    /**
     * @return the processName
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * @param processName the processName to set
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /**
     * @return the process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * @return the clockManager
     */
    public ClockManager getClockManager() {
        return clockManager;
    }

    /**
     * @param clockManager the clockManager to set
     */
    public void setClockManager(ClockManager clockManager) {
        this.clockManager = clockManager;
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

    /**
     * @return the blockedQueue
     */
    public Queue getBlockedQueue() {
        return blockedQueue;
    }

    /**
     * @param blockedQueue the blockedQueue to set
     */
    public void setBlockedQueue(Queue blockedQueue) {
        this.blockedQueue = blockedQueue;
    }

    /**
     * @return the exitList
     */
    public ProcessList getExitList() {
        return exitList;
    }

    /**
     * @param exitList the exitList to set
     */
    public void setExitList(ProcessList exitList) {
        this.exitList = exitList;
    }

    /**
     * @return the readyLock
     */
    public Semaphore getReadyLock() {
        return readyLock;
    }

    /**
     * @param readyLock the readyLock to set
     */
    public void setReadyLock(Semaphore readyLock) {
        this.readyLock = readyLock;
    }

    /**
     * @return the blockedLock
     */
    public Semaphore getBlockedLock() {
        return blockedLock;
    }

    /**
     * @param blockedLock the blockedLock to set
     */
    public void setBlockedLock(Semaphore blockedLock) {
        this.blockedLock = blockedLock;
    }

    /**
     * @return the exitLock
     */
    public Semaphore getExitLock() {
        return exitLock;
    }

    /**
     * @param exitLock the exitLock to set
     */
    public void setExitLock(Semaphore exitLock) {
        this.exitLock = exitLock;
    }

    /**
     * @return the ioDevice
     */
    public Semaphore getIoDevice() {
        return ioDevice;
    }

    /**
     * @param ioDevice the ioDevice to set
     */
    public void setIoDevice(Semaphore ioDevice) {
        this.ioDevice = ioDevice;
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * @return the worker
     */
    public Thread getWorker() {
        return worker;
    }

    /**
     * @param worker the worker to set
     */
    public void setWorker(Thread worker) {
        this.worker = worker;
    }

    /**
     * @return the lastCycleRef
     */
    public int getLastCycleRef() {
        return lastCycleRef;
    }

    /**
     * @param lastCycleRef the lastCycleRef to set
     */
    public void setLastCycleRef(int lastCycleRef) {
        this.lastCycleRef = lastCycleRef;
    }
    
    

    public void start() {
        if (isRunning()) return;
        setRunning(true);
        setWorker(new Thread(this::run, "CPU-Worker"));
        getWorker().start();
    }

    public void stop() {
        setRunning(false);
        if (getWorker() != null) getWorker().interrupt();
    }
    
    public void requestPreemption(Process candidate) {
        this.preemptCandidate = candidate;
        this.preemptRequested = true;
    }
    
    private void run() {
        setLastCycleRef(getClockManager().getClockCycles());
        while (isRunning()) {
            try {
                int now = getClockManager().getClockCycles();

                if (getProcess() == null) {
                    dispatchFromReady();
                    setLastCycleRef(now);
                }

                if (getProcess() != null && now > getLastCycleRef()) {
                    if (preemptRequested) {
                        preemptRequested = false;
                        try {
                            getReadyLock().acquire();
                            // Devuelve el proceso actual a la cola si aún no terminó
                            if (process != null) {
                                process.setStatus(Process.Status.Ready);
                                readyQueue.enqueue(process);
                            }

                            // Carga el nuevo proceso corto o prioritario
                            process = preemptCandidate;
                            preemptCandidate = null;

                            if (process != null) {
                                process.setStatus(Process.Status.Running);
                                setRunningProcess("P" + process.getID());
                                setProcessName(process.getProcessName());
                                setPC(process.getPC());
                                setMAR(process.getMAR());
                            } else {
                                setRunningProcess("OS");
                                setProcessName("");
                                setPC(getClockManager().getClockCycles());
                                setMAR(getClockManager().getClockCycles());
                            }
                        } finally {
                            getReadyLock().release();
                        }
                    }
                    stepOneInstruction(getProcess());
                    setLastCycleRef(now);
                }

                Thread.sleep(5);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void dispatchFromReady() {
        try {
            getReadyLock().acquire();
            if (!readyQueue.isEmpty()) {
                Process process = (Process) getReadyQueue().dequeue();
                setProcess(process);
                setProcessName(process.getProcessName());
                setRunningProcess("P" + process.getID());
                process.setStatus(Process.Status.Running);
                // sincroniza contadores visibles
                setPC(process.getPC());
                setMAR(process.getMAR());
            } else {
                setRunningProcess("OS");
                setProcessName("");
                setPC(getClockManager().getClockCycles());
                setMAR(getClockManager().getClockCycles());
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            getReadyLock().release();
        }
    }

    private void stepOneInstruction(Process process) {
        process.setPC(process.getPC() + 1);
        process.setMAR(process.getMAR() + 1);
        process.setRemainingBurstTime(process.getRemainingBurstTime() - 1);

        setPC(process.getPC());
        setMAR(process.getMAR());

        // ¿E/S?
        if (process.isIObound()
            && process.getCyclesToExcept() != null
            && process.getCyclesToCompleteRequest() != null
            && process.getPC().equals(process.getCyclesToExcept())) {

            process.setStatus(Process.Status.Blocked);
            try {
                getBlockedLock().acquire();
                getBlockedQueue().enqueue(process);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                getBlockedLock().release();
            }

            int wakeCycle = getClockManager().getClockCycles() + process.getCyclesToCompleteRequest();
            scheduleUnblock(process, wakeCycle);

            // libera CPU
            setProcess(null);
            setRunningProcess("OS");
            setProcessName("");
            return;
        }

        // ¿Terminó?
        if (process.getRemainingBurstTime() != null && process.getRemainingBurstTime() <= 0) {
            process.setStatus(Process.Status.Exit);
            try {
                getExitLock().acquire();
                getExitList().add(process);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                getExitLock().release();
            }
            setProcess(null);
            setRunningProcess("OS");
            setProcessName("");
        }
    }

    // Hilo que simula espera de E/S y reencola a Ready
    private void scheduleUnblock(Process process, int wakeCycle) {
        new Thread(() -> {
            try {
                getIoDevice().acquire(); // ocupa dispositivo

                while (getClockManager().getClockCycles() < wakeCycle) {
                    Thread.sleep(10);
                }

                try {
                    getBlockedLock().acquire();
                    getBlockedQueue().dequeueById(process.getID());
                } finally {
                    getBlockedLock().release();
                }

                process.setStatus(Process.Status.Ready);

                try {
                    getReadyLock().acquire();
                    getReadyQueue().enqueue(process);
                } finally {
                    getReadyLock().release();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                getIoDevice().release(); // libera dispositivo
            }
        }, "IO-Wait-P" + process.getID()).start();
    }
}