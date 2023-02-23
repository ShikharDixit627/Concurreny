package com.shikharD;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.shikharD.Main.EOF;

public class Main {
    public static final String EOF = "EOF";

    public static void main(String[] args) {
        List<String> buffer = new ArrayList<>();
        ReentrantLock bufferLock = new ReentrantLock();

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Producer producer = new Producer(buffer,ThreadColor.ANSI_GREEN,bufferLock);
        MyConsumer consumer1 = new MyConsumer(buffer,ThreadColor.ANSI_PURPLE,bufferLock);
        MyConsumer consumer2 = new MyConsumer(buffer,ThreadColor.ANSI_CYAN,bufferLock);

        executorService.execute(producer);
        executorService.execute(consumer1);
        executorService.execute(consumer2);
        Future<String> future = executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println(ThreadColor.ANSI_CYAN+"I'm printed for callable class for ");
                return "The result for Callable class";
            }
        });
        try{
            System.out.println(future.get());
        }catch (ExecutionException e){
            System.out.println("Execution error detected");
        }catch (InterruptedException e){
            System.out.println("Thread running the task was interrupted");
        }

        executorService.shutdown();
    }
}

class Producer implements Runnable{

    private List<String> buffer;
    private String color;
    private ReentrantLock bufferLock;

    public Producer(List<String> buffer, String color, ReentrantLock bufferLock){
        this.buffer = buffer;
        this.color = color;
        this.bufferLock = bufferLock;
    }
    public void run(){
        Random random = new Random();
        String[] nums = {"1","2","3","4","5"};

        for(String num:nums){
            try {
                System.out.println(color + "Adding .." + num);
                bufferLock.lock();
                try{
                    buffer.add(num);
                } finally {
                    bufferLock.unlock();
                }
                Thread.sleep(random.nextInt(1000));
            }catch (InterruptedException e){
                System.out.println("the producer was interrupted");
            }
        }

        System.out.println(color+"Adding EOF and exiting...");
        bufferLock.lock();
        try{
            buffer.add("EOF");
        }finally {
            bufferLock.unlock();
        }
    }
}
class MyConsumer implements Runnable{
    private List<String> buffer;
    private String color;
    private ReentrantLock bufferLock;

    public MyConsumer(List<String> buffer, String color, ReentrantLock bufferLock){
        this.buffer = buffer;
        this.color = color;
        this.bufferLock = bufferLock;
    }
    public void run(){

        int counter = 0;
        while(true){
            if(bufferLock.tryLock()){
                try {
                    if (buffer.isEmpty()) {
                        continue;
                    }
                    System.out.println(color+"the Counter = "+counter);
                    counter = 0;

                    if (buffer.get(0).equals(EOF)) {
                        System.out.println(color + "Exiting.. ");

                        break;
                    } else {
                        System.out.println(color + "Removed " + buffer.remove(0));
                    }
                }finally {
                    bufferLock.unlock();
                }
            }else{
                counter++;
            }


        }
    }

}




