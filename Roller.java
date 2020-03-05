package damageGivenAChanceToHit;

import java.util.Random;

public class Roller {
	private Random rand;
	private int toHitModifier;
	private int abilityScoreBonus;
	private int enemyAC;
	private String diceInstantiationString;
	private int faces;
	private boolean advantage;
	private int howManyRolls;
	private Die[] dice;
	
	public Roller() {
		this.rand = new Random();
		this.toHitModifier = 2;
		this.abilityScoreBonus = 0;
		this.enemyAC = 10;
		this.diceInstantiationString = "1d6:1,2,3,4,5,6";
		this.advantage = true;
		this.howManyRolls = 1;
		this.initializeDice(this.diceInstantiationString);
	}
	
	public Roller(int mod, int enAC, int asb, String dice, boolean advantage, int howManyRolls) {
		this.rand = new Random();
		this.toHitModifier = mod;
		this.abilityScoreBonus = asb;
		this.enemyAC = enAC;
		this.diceInstantiationString = dice;
		this.advantage = advantage;
		this.howManyRolls = howManyRolls;
		this.initializeDice(this.diceInstantiationString);
	}
	
	private void initializeDice(String creationString) {
		int amount = Integer.parseInt(creationString.substring(0, creationString.indexOf('d')), 10);
		int dieFaces = Integer.parseInt(creationString.substring(creationString.indexOf('d') + 1, creationString.indexOf(':')), 10);
		int faceValues[] = new int[dieFaces];
		//i = findNextDecimal(i, creationString)
		for (int i = creationString.indexOf(':') + 1, arrayIndex = 0; arrayIndex < faceValues.length; arrayIndex++) {
			int start = i;
			while (Character.isDigit(creationString.charAt(i))) {
				if (++i >= creationString.length())
					break;
			}
			faceValues[arrayIndex] = Integer.parseInt(creationString.substring(start, i++), 10);
		}
		this.dice = new Die[amount]; 
		for (int i = 0; i < amount; i++) {
			this.dice[i] = new Die(dieFaces, faceValues);
		}
		this.faces = dieFaces;
	}
	
	/**
	 * Tries to hit with the provided parameters
	 * @return whether they hit or not.
	 */
	public boolean tryToHit() {
		int currentRecord = this.advantage ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		for (int i = 0; i < this.howManyRolls + 1; i++) {
			int possibleRecord = this.rand.nextInt(20) + 1 + this.toHitModifier;
			if (this.advantage ? (possibleRecord > currentRecord) : (possibleRecord < currentRecord))
				currentRecord = possibleRecord;
		}
		return currentRecord >= this.enemyAC;
	}
	
	public int[] rollDice() {
		int[] result = new int[this.dice.length];
		for (int i = 0; i < this.dice.length; i++) {
			result[i] = this.dice[i].roll();
		}
		return result;
	}
	
	public int rollDamage() {
		int sum = 0;
		int values[] = this.rollDice();
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		return sum + this.abilityScoreBonus;
	}
	public int getToHitMod() {
		return this.toHitModifier;
	}
	public int getAbilityScoreBonus() {
		return this.abilityScoreBonus;
	}
	public int getEnemyAC() {
		return this.enemyAC;
	}
	public int getFaces() {
		return this.faces;
	}
	public int getAmountOfDice() {
		return this.dice.length;
	}
	public boolean getAdvantage() {
		return this.advantage;
	}
	public int getd20sRolled() {
		return this.howManyRolls;
	}
	public String getSituation() {
		StringBuilder situation = new StringBuilder();
		situation.append(this.howManyRolls == 0
				? "Standard"
				: this.howManyRolls == 1
					? "Double"
					: this.howManyRolls == 2
						? "Triple"
						: this.howManyRolls == 3
							? "Quad"
							: "shouldn't be possible");
		situation.append(this.howManyRolls == 0
				? " Roll"
				: this.advantage
					? " Advantage"
					: " Disadvantage");
		return situation.toString();
	}
}
