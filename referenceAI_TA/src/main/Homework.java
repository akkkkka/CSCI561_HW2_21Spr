/**
 *
 */
package main;

/**
 * @author daniellink
 *
 */
public class Homework {

	/**
	 *
	 */
	public Homework() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		System.out.println("CS 561 Spring 2021 HW2 Reference Solution");
		Input.readInputFile();
		switch (Input.mode) {
		case Single -> {
			Game.makeOneMove();
			System.exit(0);
		}
		case Game -> {
			Game.makeOneMove();
			System.exit(0);
		}
		case Self -> {
			Board.setDebug(true);
			Game.play();
			System.exit(0);
		}
		default -> {
			System.err.println("Unknown game mode: '" + Input.getGameModeString() + "'");
			System.exit(-1);
		}
		}
	}
}
