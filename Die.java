import java.util.Random;

/**
 * presumes the chance of landing on any face is evenly distributed.
 * @author Alex
 *
 */
public class Die {
	private int faces;
	private int[] faceValues;
	private Random rand;
	
	public Die() {
		this.faces = 6;
		int[] defaultDie = {1, 2, 3, 4, 5, 6};
		this.faceValues = defaultDie;
		rand = new Random();
	}
	
	public Die(int faces, int[] faceValues) {
		this.faces = faces;
		this.faceValues = faceValues;
		rand = new Random();
	}
	
	public int roll() {
		return this.getFaceValues()[(int) (this.rand.nextDouble() * this.getFaceValues().length)];
	}
	
	public int getFaces() {
		return this.faces;
	}
	public int[] getFaceValues() {
		return this.faceValues;
	}
	
}
