/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Gabriel Flores
 */
public class ClockManager {
    private int clockCycles = 0;
    private double instructionDuration;
    private ScheduledExecutorService scheduler;
    private Runnable clockTask;
    
    public ClockManager(double instructionDuration){
        this.instructionDuration = instructionDuration;
        startClock();
    }
    
    private void startClock(){
        scheduler = Executors.newScheduledThreadPool(1);
        clockTask = () -> {
            synchronized (this) {
            clockCycles++;
        }
        };
        long intialDelay = 0;
        long period = (long) (instructionDuration*1000);
        scheduler.scheduleAtFixedRate(clockTask, intialDelay, period, TimeUnit.MILLISECONDS);
    }
    
    public synchronized void updateInstructionDuration (double newDuration) {
        if (newDuration == instructionDuration) return;
        this.instructionDuration = newDuration;
        restartClockWithNewRate();
    }
    
    private synchronized void restartClockWithNewRate(){
        if (scheduler != null && ! scheduler.isShutdown()){    //reinicia un scheduler (programador de tareas) con una nueva tasa/temporizador
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(500, TimeUnit.MILLISECONDS)){
                    scheduler.shutdownNow();}
            } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    }
        }
        startClock(); //nuevo tiempo de intervalo
    }
    
    public double getInstructionDuration(){
        return instructionDuration;
    }
    
    public void setInstructionDuration(double instructionDuration){
        this.instructionDuration=instructionDuration;
    }
    
    public synchronized int getClockCycles(){
        return clockCycles;
    }
        
}
