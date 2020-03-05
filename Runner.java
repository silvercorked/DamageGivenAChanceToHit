package damageGivenAChanceToHit;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Runner {

	public static void main(String[] args) {
		Instant start = Instant.now();
		// to hit mod, enemy AC, ability score, die
		CopyOnWriteArrayList<Roller> rollers = new CopyOnWriteArrayList<Roller>();
		for (int n = 0; n < 4; n++) { // how many d20's to roll
			for (int m = 0; m < (n == 0 ? 1 : 2); m++) { //advantage
				for (int l = 1; l < 2; l++) { // num of dice
					for (int k = 0; k < 20; k++) { // to hit
						for (int j = 0; j < (k - 2); j++) { // ability score mod (minus 2 becuase base prof bonus is 2, thus to hit must always be at least 2 greater than the ability score)
							for (int i = 10; i < 30; i++) { // enemyAC
								if (k + 20 >= i) {
									rollers.add(new Roller(k, i, j, "" + l + "d4:1,2,3,4", m == 0, n));
									rollers.add(new Roller(k, i, j, "" + l + "d6:1,2,3,4,5,6", m == 0, n));
									rollers.add(new Roller(k, i, j, "" + (l + 1) + "d6:1,2,3,4,5,6", m == 0, n));
									rollers.add(new Roller(k, i, j, "" + l + "d8:1,2,3,4,5,6,7,8", m == 0, n));
									rollers.add(new Roller(k, i, j, "" + l + "d10:1,2,3,4,5,6,7,8,9,10", m == 0, n));
									rollers.add(new Roller(k, i, j, "" + l + "d12:1,2,3,4,5,6,7,8,9,10,11,12", m == 0, n));
								}
							}
						}
					}
				}
			}
		}
		CopyOnWriteArrayList<BigDecimal> hits = new CopyOnWriteArrayList<BigDecimal>();
		CopyOnWriteArrayList<BigDecimal> attempts = new CopyOnWriteArrayList<BigDecimal>();
		CopyOnWriteArrayList<BigDecimal> cumulativeDamage = new CopyOnWriteArrayList<BigDecimal>();
		for (int i = 0; i < rollers.size(); i++) {
			hits.add(BigDecimal.ZERO);
			attempts.add(BigDecimal.ZERO);
			cumulativeDamage.add(BigDecimal.ZERO);
		}
		final BigDecimal MAX_TRIES = new BigDecimal("1000");
		Thread threads[] = new Thread[rollers.size()];
		for (int i = 0; i < rollers.size(); i++) {
			threads[i] = new Thread(new OneIndexOfList(i, rollers, hits, attempts, cumulativeDamage, MAX_TRIES, i % 10000 == 0));
			threads[i].start();
		}
		boolean done = false;
		do {
			done = true;
			for (int i = 0; i < threads.length; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while (!done);
		//all threads have returned
		ArrayList<Result> results = new ArrayList<Result>();
		for (int i = 0; i < rollers.size(); i++) {
			Result temp = new Result();
			temp.roller = rollers.get(i);
			temp.hits = hits.get(i);
			temp.attempt = attempts.get(i);
			temp.damage = cumulativeDamage.get(i);
			temp.hitChanceEstimate = hits.get(i).divide(attempts.get(i));
			temp.damagePerStrike = cumulativeDamage.get(i).divide(attempts.get(i));
			results.add(temp);
		}
		Collections.sort(results, new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				return o1.damagePerStrike.compareTo(o2.damagePerStrike);
			}
		});
		List<String> lines = new ArrayList<String>();
		lines.add("d20's rolled, situation, Sides, Amount of Damage Dice, toHitMod, abilityScoreMod, enemyAC, attempts, hits, damage, hitChanceEstimate, damagePerStrike");
		for (int i = 0; i < results.size(); i++) {
			results.get(i).hitChanceEstimate = results.get(i).hitChanceEstimate.setScale(50).stripTrailingZeros();
			results.get(i).damagePerStrike = results.get(i).damagePerStrike.setScale(50).stripTrailingZeros();
			
			System.out.printf("d20s rolled: %2d, advantage: %1d, How Many Sides: %2d, amount of dice: %2d, toHitMod: %2d, abilityScoredMod: %2d, enemyAC: %2d ", results.get(i).roller.getd20sRolled(), results.get(i).roller.getAdvantage() ? 1 : 0, results.get(i).roller.getFaces(), results.get(i).roller.getAmountOfDice(), results.get(i).roller.getToHitMod(), results.get(i).roller.getAbilityScoreBonus(), results.get(i).roller.getEnemyAC());
			System.out.printf("attempts: %10s, hits: %10s, damage: %15s"
					, results.get(i).attempt.toString(), results.get(i).hits.toString(), results.get(i).damage.toString());
			System.out.printf(", hitChanceEstimate: %15s, damagePerStrike: %15s"
					, results.get(i).hitChanceEstimate, results.get(i).damagePerStrike);
			System.out.println();
			lines.add(String.format("%d, %s, %d, %d, %d, %d, %d, %s, %s, %s, %s, %s"
					, results.get(i).roller.getd20sRolled(), results.get(i).roller.getSituation()
					, results.get(i).roller.getFaces(), results.get(i).roller.getAmountOfDice()
					, results.get(i).roller.getToHitMod(), results.get(i).roller.getAbilityScoreBonus()
					, results.get(i).roller.getEnemyAC(), results.get(i).attempt.toString()
					, results.get(i).hits.toString(), results.get(i).damage.toString()
					, results.get(i).hitChanceEstimate, results.get(i).damagePerStrike));
		}
		Path file = Paths.get("C:/Users/Alex/Desktop/dndStats.csv");
		try {
			Files.write(file,  lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Instant end = Instant.now();
		long elapsed = Duration.between(start, end).toMinutes();
		System.out.println("Elapsed Time: " + elapsed); //minutes
	}
}
