package hu.advancedweb;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * This test measures the performance differences between cases
 * whether or not there is a dominant branch.
 * 
 * Run this test with:
 * mvn clean install; java -jar target/benchmarks.jar "hu.advancedweb.Branching.*"
 * 
 * To enable diagnostic log about inlining:
 * mvn clean install; java -jar -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining target/benchmarks.jar "hu.advancedweb.Branching.*"
 * 
 * @author David Csakvari
 */
public class Branching {
	
	@State(Scope.Thread)
    public static class MyState {
        public int valueSink; // just store the result somewhere, to avoid dead code removal
        public double chanceOfNegativeNumber = 0.0;
    	Random random = new Random();
        
        @Setup
        public void setup() {
        	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        	
        	executor.schedule(
        			() -> {
           				System.out.println("50%");
        				chanceOfNegativeNumber = 0.5d;
        			},
        			20, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
           				System.out.println("40%");
        				chanceOfNegativeNumber = 0.4d;
        			},
        			25, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
           				System.out.println("30%");
        				chanceOfNegativeNumber = 0.3d;
        			},
        			28, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
           				System.out.println("20%");
        				chanceOfNegativeNumber = 0.2d;
        			},
        			31, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
           				System.out.println("10%");
        				chanceOfNegativeNumber = 0.1d;
        			},
        			34, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
           				System.out.println("5%");
        				chanceOfNegativeNumber = 0.05d;
        			},
        			37, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
           				System.out.println("1%");
           				chanceOfNegativeNumber = 0.01d;
        			},
        			40, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
        				System.out.println("0");
        				chanceOfNegativeNumber = 0.00d;
        			},
        			43, TimeUnit.SECONDS);

        	executor.schedule(
        			() -> {
        				System.out.println("neg");
        				chanceOfNegativeNumber = 1.0d;
        			},
        			46, TimeUnit.SECONDS);

        }
    }
	
	@Measurement(iterations=100)
    @Benchmark
    public void testMethod(MyState s) throws Exception {
    	int i = getRandomNumber(s.random, s.chanceOfNegativeNumber);
    	doSomeBranching(s, i);
    }
	
	private int getRandomNumber(Random random, double chanceOfNegativeNumber) {
		return random.nextInt(Integer.MAX_VALUE) - ((int)(Integer.MAX_VALUE * chanceOfNegativeNumber));
	}
    
	private void doSomeBranching(MyState s, int i) {
    	if (i >= 0) {
    		s.valueSink += 2;
    	} else {
    		s.valueSink += 1;
    	}
    }

}
