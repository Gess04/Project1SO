/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Process;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Gabriel Flores
 */
public class Process extends Thread {

    private Integer ID;
    private String processName;
    private Integer instructionCount;
    private Integer raminingBurstTime;
    private boolean CPUbound;
    private boolean IObound;
    private Integer cyclesToExcept;
    private Integer cyclesToCompleteRequest;
    private String status;
    private Integer PC;
    private Integer MAR;
    private Integer priority;
    private Semaphore mutex;
    private int arrivaltime;
    private double responseRatio;
    
    public Process(int ID, String processName, int instructionCount, Integer raminingBurstTime, boolean CPUbound, boolean IObound, Integer cyclesToExcept, Integer cyclesToCompleteRequest, String status, Integer PC, Integer MAR, Integer priority, Semaphore mutex, int arrivaltime, double responseRatio) {
        this.ID = ID;
        this.processName = processName;
        this.instructionCount = instructionCount;
        this.raminingBurstTime = raminingBurstTime;
        this.CPUbound = CPUbound;
        this.IObound = IObound;
        this.cyclesToExcept = cyclesToExcept;
        this.cyclesToCompleteRequest = cyclesToCompleteRequest;
        this.status = "Ready";
        this.PC = 0;
        this.MAR = 0;
        this.priority = priority;
        this.mutex = mutex;
        this.arrivaltime = arrivaltime;
        this.responseRatio = 0;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Integer getInstructionCount() {
        return instructionCount;
    }

    public void setInstructionCount(Integer instructionCount) {
        this.instructionCount = instructionCount;
    }

    public Integer getRaminingBurstTime() {
        return raminingBurstTime;
    }

    public void setRaminingBurstTime(Integer raminingBurstTime) {
        this.raminingBurstTime = raminingBurstTime;
    }

    public boolean isCPUbound() {
        return CPUbound;
    }

    public void setCPUbound(boolean CPUbound) {
        this.CPUbound = CPUbound;
    }

    public boolean isIObound() {
        return IObound;
    }

    public void setIObound(boolean IObound) {
        this.IObound = IObound;
    }

    public Integer getCyclesToExcept() {
        return cyclesToExcept;
    }

    public void setCyclesToExcept(Integer cyclesToExcept) {
        this.cyclesToExcept = cyclesToExcept;
    }

    public Integer getCyclesToCompleteRequest() {
        return cyclesToCompleteRequest;
    }

    public void setCyclesToCompleteRequest(Integer cyclesToCompleteRequest) {
        this.cyclesToCompleteRequest = cyclesToCompleteRequest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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



    public Semaphore getMutex() {
        return mutex;
    }

    public void setMutex(Semaphore mutex) {
        this.mutex = mutex;
    }

    public int getArrivaltime() {
        return arrivaltime;
    }

    public void setArrivaltime(int arrivaltime) {
        this.arrivaltime = arrivaltime;
    }

    public double getResponseRatio() {
        return responseRatio;
    }

    public void setResponseRatio(double responseRatio) {
        this.responseRatio = responseRatio;
    }
  
    
}
