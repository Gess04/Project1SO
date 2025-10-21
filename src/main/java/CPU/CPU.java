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
                Process p = (Process) getReadyQueue().dequeue();
                setProcess(p);
                setProcessName(p.getProcessName());
                setRunningProcess("P" + p.getID());
                p.setStatus(Process.Status.Running);
                // sincroniza contadores visibles
                setPC(p.getPC());
                setMAR(p.getMAR());
            } else {
                setRunningProcess("OS");
                setProcessName("");
                setPC((Integer) 0);
                setMAR((Integer) 0);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            getReadyLock().release();
        }
    }

    private void stepOneInstruction(Process p) {
        p.setPC(p.getPC() + 1);
        p.setMAR(p.getMAR() + 1);
        p.setRemainingBurstTime(p.getRemainingBurstTime() - 1);

        setPC(p.getPC());
        setMAR(p.getMAR());

        // ¿E/S?
        if (p.isIObound()
            && p.getCyclesToExcept() != null
            && p.getCyclesToCompleteRequest() != null
            && p.getPC().equals(p.getCyclesToExcept())) {

            p.setStatus(Process.Status.Blocked);
            try {
                getBlockedLock().acquire();
                getBlockedQueue().enqueue(p);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                getBlockedLock().release();
            }

            int wakeCycle = getClockManager().getClockCycles() + p.getCyclesToCompleteRequest();
            scheduleUnblock(p, wakeCycle);

            // libera CPU
            setProcess(null);
            setRunningProcess("OS");
            setProcessName("");
            return;
        }

        // ¿Terminó?
        if (p.getRemainingBurstTime() != null && p.getRemainingBurstTime() <= 0) {
            p.setStatus(Process.Status.Exit);
            try {
                getExitLock().acquire();
                getExitList().add(p);
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
    private void scheduleUnblock(Process p, int wakeCycle) {
        new Thread(() -> {
            try {
                getIoDevice().acquire(); // ocupa dispositivo

                while (getClockManager().getClockCycles() < wakeCycle) {
                    Thread.sleep(10);
                }

                try {
                    getBlockedLock().acquire();
                    getBlockedQueue().dequeueById(p.getID());
                } finally {
                    getBlockedLock().release();
                }

                p.setStatus(Process.Status.Ready);

                try {
                    getReadyLock().acquire();
                    getReadyQueue().enqueue(p);
                } finally {
                    getReadyLock().release();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                getIoDevice().release(); // libera dispositivo
            }
        }, "IO-Wait-P" + p.getID()).start();
    }
}