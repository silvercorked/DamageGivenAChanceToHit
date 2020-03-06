import java.math.BigDecimal;
import java.util.concurrent.CopyOnWriteArrayList;

public class OneIndexOfList implements Runnable {

	private int index = 0;
	private CopyOnWriteArrayList<Roller> rollers;
	private CopyOnWriteArrayList<BigDecimal> hits;
	private CopyOnWriteArrayList<BigDecimal> attempts;
	private CopyOnWriteArrayList<BigDecimal> cumulativeDamage;
	private boolean canPrint;
	private BigDecimal MAX_TRIES;
	
	public OneIndexOfList(int index, CopyOnWriteArrayList<Roller> rollers
			, CopyOnWriteArrayList<BigDecimal> hits, CopyOnWriteArrayList<BigDecimal> attempts
			, CopyOnWriteArrayList<BigDecimal> cumulativeDamage, BigDecimal MAX_TRIES
			, boolean canPrint) {
		this.index = index;
		this.rollers = rollers;
		this.hits = hits;
		this.attempts = attempts;
		this.cumulativeDamage = cumulativeDamage;
		this.MAX_TRIES = MAX_TRIES;
		this.canPrint = canPrint;
	}
	
	@Override
	public void run() {
		for (; attempts.get(index).compareTo(MAX_TRIES) < 0; attempts.set(index, attempts.get(index).add(BigDecimal.ONE))) {
			if (rollers.get(index).tryToHit()) {
				hits.set(index, hits.get(index).add(BigDecimal.ONE));
				cumulativeDamage.set(index, cumulativeDamage.get(index).add(new BigDecimal(rollers.get(index).rollDamage())));
				int val = attempts.get(index).intValue();
				if (val == 0 && this.canPrint) {
					System.out.printf("Thread %d is starting!", index);
					System.out.println();
				}
				if (this.canPrint && val != 0 && val % (MAX_TRIES.intValue() / 8) == 0) {
					System.out.printf("Thread %d : attempts: %s, hits: %s, damage: %s"
							, index, attempts.toString(), hits.toString(), cumulativeDamage.toString());
					System.out.println();
				}
//				try {
//					System.out.printf(", hitChanceEstimate: %s, damagePerStrike: %s"
//							, hits.get(index).divide(attempts.get(index)), cumulativeDamage.get(index).divide(attempts.get(index)));
//				} catch(ArithmeticException e) {
//					System.err.println("Thread " + index + " no exact representable decimal result");
//				}
				//System.out.println();
			}
		}
		BigDecimal goingSum = BigDecimal.ZERO;
		BigDecimal threads = BigDecimal.ZERO;
		double threadsDouble = 0;
		double totalThreadsDouble = attempts.size();
		BigDecimal totalThreads = new BigDecimal(attempts.size());
		for (BigDecimal attempt : attempts) {
			if (attempt.compareTo(MAX_TRIES) == 0) {
				threads = threads.add(BigDecimal.ONE);
				threadsDouble++;
			}
			goingSum = goingSum.add(attempt);
		}
		
		try {
			System.err.println("Thread handling index " + index + " has finished!! Completed " + threads.toString() + " threads of " + totalThreads.toString() + " total. "
					+ ((threadsDouble / totalThreadsDouble) * 100) + "% of threads have completed. " + (goingSum.divide(MAX_TRIES.multiply(totalThreads))) + "% done.");
		} catch (ArithmeticException e) {
			System.err.println("Thread handling index " + index + " has finished!! Completed " + threads.toString() + " threads of " + totalThreads.toString() + " total. "
					+ ((threadsDouble / totalThreadsDouble) * 100) + "% of threads have completed. ");
		}
	}

}
