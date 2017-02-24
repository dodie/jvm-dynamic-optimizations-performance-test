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
 * This test measures the performance differences between monomorphic,
 * bimorphic, and megamorphic call sites caused by JVM inlining policy.
 * 
 * Run this test with:
 * mvn clean install; java -jar target/benchmarks.jar "hu.advancedweb.NMorphicInlinig.*"
 * 
 * To enable diagnostic log about inlining:
 * mvn clean install; java -jar -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining target/benchmarks.jar "hu.advancedweb.NMorphicInlinig.*"
 * 
 * @author David Csakvari
 */
public class NMorphicInlinig {
	
	@State(Scope.Thread)
    public static class TestState {
		
        public int state;
        public int valueSink; // just store the result somewhere, to avoid dead code removal
        
		public Calculator simple = new SimpleCalculator();
		public Calculator marvelous = new MarvelousCalculator();
		public Calculator magnificent = new MagnificentCalculator();
		public Calculator shiny = new ShinyCalculator();
		public Calculator x = new XCalculator();
        
		/**
		 * This method modifies the test state.
		 * @see NMorphicInlinig#getCalculator(TestState)
		 */
        @Setup
        public void setup() {
        	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        	
        	executor.schedule(
        			() -> {
        				System.out.println("Iteration Deoptimize: 1");
        				state = 1;
        			},
        			25, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
        				System.out.println("Iteration Deoptimize: 2");
        				state = 2;
        			},
        			35, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
        				System.out.println("Iteration Deoptimize: 3");
        				state = 3;
        			},
        			160, TimeUnit.SECONDS);
        	
        	executor.schedule(
        			() -> {
        				System.out.println("Iteration Deoptimize: 4");
        				state = 4;
        			},
        			180, TimeUnit.SECONDS);
        	
        }
    }
	
	@Measurement(iterations = 20000, time = 1, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public void testMethod(TestState s) throws Exception {
    	Calculator c = getCalculator(s);
		s.valueSink = s.valueSink + handleRequest(c, 1312, 2435);
    }
	
	private int handleRequest(Calculator c, int a, int b) {
    	return c.doSomeCalculation(a, b);
    }
	
	/**
	 * A static method as a baseline.
	 * Change the method above to use this method instead of the polymorphic call.
	 */
    private static int doSomeCalculation(int startValue, int step) {
		return startValue + step;
	}
	
	static final Random r = new Random();
	
	/**
	 * Returns a calculator implementation, depending on the state of the test.
	 * The state is modified by scheduled tasks.
	 * @see TestState
	 */
	private Calculator getCalculator(TestState s) {
		if (s.state == 0) {
			return getCalculatorMono(s);
		} else if (s.state == 1) {
			return getCalculatorMonoRare(s);
		} else if (s.state == 2) {
			return getCalculatorEvenTwo(s);
		} else if (s.state == 3) {
			return getCalculatorEven(s);
		} else {
			return getCalculatorMono(s);
		}
	}
	
	private Calculator getCalculatorEven(TestState s) {
		int c = r.nextInt(5);
		s.valueSink += c;
		if (c == 0) {
			return s.shiny;
		} else if (c  == 1) {
			return s.magnificent;
		} else if (c == 2){
			return s.simple;
		} else if (c == 3) {
			return s.marvelous;
		} else {
			return s.x ;
		}
	}
	
	private Calculator getCalculatorEvenTwo(TestState s) {
		int c = r.nextInt(2);
		s.valueSink += c;
		if (c == 0) {
			return s.marvelous;
		} else {
			return s.simple;
		}
	}
	
	private Calculator getCalculatorEvenThree(TestState s) {
		int c = r.nextInt(4);
		s.valueSink += c;
		if (c == 0) {
			return s.marvelous;
		} else if(c == 1) {
			return s.simple;
		} else {
			return s.shiny;
		}
	}
	
	private Calculator getCalculatorEvenFour(TestState s) {
		int c = r.nextInt(4);
		s.valueSink += c;
		if (c == 0) {
			return s.marvelous;
		} else if(c == 1) {
			return s.simple;
		} else if (c==2){
			return s.magnificent;
		} else {
			return s.shiny;
		}
	}
	
	private Calculator getCalculatorMono(TestState s) {
		int c = r.nextInt(5);
		s.valueSink += c;
		if (c == 0) {
			return s.marvelous;
		} else if (c  == 1) {
			return s.marvelous;
		} else if (c == 2){
			return s.marvelous;
		} else if (c == 3) {
			return s.marvelous;
		} else {
			return s.marvelous;
		}
	}
	
	private Calculator getCalculatorMonoRare(TestState s) {
		int c = r.nextInt(Integer.MAX_VALUE);
		s.valueSink += c;
		if (c == 0) {
			System.out.println("Oh, a rare SimpleCalculator!");
			return s.simple;
		}  
		else if (c == 1) {
			System.out.println("Oh, a rare ShinyCalculator!");
			return s.shiny;
		} else if (c == 2) {
			System.out.println("Oh, a rare XCalculator!");
			return s.x;
		} else if (c == 3) {
			System.out.println("Oh, a rare MagnificentCalculator!");
			return s.magnificent;
		} else {
			return s.marvelous;
		}
	}
	
    /*
     * Different Calculator implementations.
     */

	interface Calculator {
		int doSomeCalculation(int a, int b);
	}    

	static class SimpleCalculator implements Calculator {
		public int doSomeCalculation(int startValue, int step) {
			return startValue + step;
		}
	}

	static class ShinyCalculator implements Calculator {
    	public int doSomeCalculation(int startValue, int step) {
			return startValue + step;
    	}
    }
    
	static class MagnificentCalculator implements Calculator {
    	public int doSomeCalculation(int startValue, int step) {
			return startValue + step;
    	}
    }
    
	static class MarvelousCalculator implements Calculator {
    	public int doSomeCalculation(int startValue, int step) {
			return startValue + step;
    	}
    }
	
	static class XCalculator implements Calculator {
    	public int doSomeCalculation(int startValue, int step) {
			return startValue + step;
    	}
    }
    
}
