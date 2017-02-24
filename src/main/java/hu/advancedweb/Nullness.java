package hu.advancedweb;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * This test measures the performance penalty if an implicit null-check is in place.
 * 
 * Run this test with:
 * mvn clean install; java -jar target/benchmarks.jar "hu.advancedweb.Nullness.*"
 * 
 * To enable diagnostic log about inlining:
 * mvn clean install; java -jar -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining target/benchmarks.jar "hu.advancedweb.Nullness.*"
 * 
 * Note: this is not exactly the test I've measured in the post,
 * but it shows the performance penalty.
 * 
 * @author David Csakvari
 */
public class Nullness {
	
	@State(Scope.Thread)
    public static class MyState {
		volatile public int valueSink; // just store the result somewhere, to avoid dead code removal
        volatile public int state = 0;
        
        @Setup
        public void setup() {
        	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        	executor.schedule(
        			() -> {
        				System.out.println("Deoptimize: 1");
        				state = 1;
        			},
        			25, TimeUnit.SECONDS);
        	executor.schedule(
        			() -> {
        				System.out.println("Deoptimize :0");
        				state = 0;
        			},
        			30, TimeUnit.SECONDS);
        }
    }
	

	@Measurement(iterations = 100, time = 1, timeUnit = TimeUnit.SECONDS)
    @Benchmark
    public void testMethod(MyState s) throws Exception {
		String testInput = getTestValue(s);
		try {
			s.valueSink += safeUpper(testInput).hashCode();
		} catch (Exception e) {
			// Ignore.
		}
	}
	
	
	private String getTestValue(MyState s) {
		if (s.state == 0) {
			return "yep";
		} else {
			return null;
		}
	}
    
//	private String getNull(MyState s) {
//		int i = s.random.nextInt(2);
//		if (i == 0)
//			return "yep";
//		else
//			return null;
//	}
//	
//	private String getNonNull(MyState s) {
//		int i = s.random.nextInt(2);
//		if (i == 0)
//			return "yep";
//		else
//			return "nope";
//	}
	
	public String safeUpper(String s) {
		return s.toLowerCase() + s.toUpperCase();
	}

}
