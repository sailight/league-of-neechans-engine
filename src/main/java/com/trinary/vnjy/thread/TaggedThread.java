/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.trinary.vnjy.thread;

import com.trinary.vnjy.PystRouter;
import com.trinary.vnjy.se.Command;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mmain
 */
public abstract class TaggedThread extends Thread {
    protected boolean running = false;
    
    protected abstract String getTag();
    
    protected Command popQueue() {
        return PystRouter.popCommand(getTag());
    }
    
    protected Command peekQueue() {
        return PystRouter.peekCommand(getTag());
    }
    
    protected Command popIoQueue() {
        return PystRouter.popIoCommand(getTag());
    }
    
    protected Command peekIoQueue() {
        return PystRouter.peekIoCommand(getTag());
    }
    
    protected void pushQueue(Command command) {
        PystRouter.routeCommand(command);
    }
    
    protected void pushIoQueue(Command command) {
        PystRouter.routeIoCommand(command);
    }
    
    @Override
    public void start() {
        running = true;
        super.start();
    }
    
    @Override 
    public void run() {
        while (running) {
            // Peek the i/o queue
            Command ioCommand = peekIoQueue();
            
            // Process io
            if (ioCommand != null) {
                //System.out.println("PROCESSING I/O COMMAND");
                if (ioCommand.isBlocking()) {
                    processIo(ioCommand);
                    popIoQueue();
                } else {
                    popIoQueue();
                    processIo(ioCommand);
                }
                
                continue;
            }
            
            // Peek the command queue
            Command command = peekQueue();
            
            // If no command, then sleep and restart loop
            if (command == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TaggedThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                continue;
            }
            
            // Process command.
            //System.out.println("PROCESSING COMMAND");
            if (command.isBlocking()) {
                processCommand(command);
                popQueue();
            } else {
                popQueue();
                processCommand(command);
            }
        }
    }
    
    public void terminate() {
        running = false;
        stop();
    }
    
    public abstract void processIo(Command command);
    public abstract void processCommand(Command command);
}
