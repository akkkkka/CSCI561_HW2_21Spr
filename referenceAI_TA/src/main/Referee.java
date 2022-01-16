/**
 *
 */
package main;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author daniellink
 *
 */
public class Referee {

	static boolean materialChange = false; // Whether a piece was removed or promoted to king
	static LinkedList<Step> stepsInOutput;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println("CS 561 Spring 2021 HW2 Reference Solution -- Referee Mode");
		Reffiles.createDataDir();
		Reffiles.writeDefaultNext();

		// Read input.txt
		Input.readInputFile();
		Input.inputBoard.generateAllMoves(0);

		// Read output.txt
		stepsInOutput = Input.readOutputFile();

		Step.Type firstStepType = null;

		for (final Step s : stepsInOutput) {
			if (firstStepType == null) {
				firstStepType = s.getKind();
				if (firstStepType == Step.Type.JUMP) {
					materialChange = true;
				}
				continue;
			}
			if (firstStepType == Step.Type.SHOVE) {
				exitOnInvalidMove("A simple move can only have one step");
			}
			if (s.getKind() != firstStepType) {
				exitOnInvalidMove("All steps in a move need to be of the same type!");
			}
		}

		// Compare the steps in the output to all generated moves
		boolean moveFound = false;

		final ArrayList<Move> possibleMoves = Input.inputBoard.getPossibleMoves();
		if (possibleMoves.isEmpty()) {
			System.err.println("Could not find any valid moves to make for player " + Input.inputBoard.getPlayerUp());
			System.out.println("*** Winner: " + Board.getOtherColor(Input.inputBoard.getPlayerUp()) + " ***");
			Reffiles.writeToNext("LOSE");
			System.exit(0);
		}

		for (final Move m : Input.inputBoard.getPossibleMoves()) {
			if (m.hasEqualSteps(stepsInOutput)) {
				moveFound = true;
				break;
			}
		}

		if (!moveFound) {
			System.err.println("Move is not valid");
			System.exit(0);
		}

		System.out.println("Move is valid");

		if (Reffiles.findThreepeatedPositions()) {
			System.out.println("Seen this position 3 times");
			System.out.println("*** Game ends in draw ***");
			Reffiles.writeToNext("DRAW");
			System.exit(0);
		}

		if (Input.inputBoard.executeStepList(stepsInOutput)) {
			materialChange = true;
			Reffiles.cleanDataDirectory();
			// Update ply count of last material change
		}

		Input.inputBoard.otherPlayerUp();
		Input.inputBoard.generateAllMoves(0);
		if (Input.inputBoard.getPossibleMoves().isEmpty()) {
			System.out.println("After this move, player " + Input.inputBoard.getPlayerUp() + " cannot make any moves");
			System.out.println("*** Winner: " + Board.getOtherColor(Input.inputBoard.getPlayerUp()) + " ***");
			Reffiles.writeToNext("WIN");
			System.exit(0);
		}
		Reffiles.writeNextWithContent();
		if (Reffiles.increasePlyCountFile(materialChange)) {
			System.out.println("50 plys without any material changes");
			System.out.println("*** Game ends in draw ***");
			Reffiles.writeToNext("DRAW");
			System.exit(0);
		}

		final String dateTime = String.valueOf(System.currentTimeMillis());

		Reffiles.writeStringToFile(Input.inputBoard.toString(),
				Reffiles.dataDirName + FileSystems.getDefault().getSeparator() + "refboard_" + dateTime);

	}

	private static void exitOnInvalidMove(String error) {
		System.err.println("Move " + stepsInOutput + " is invalid");
		System.err.println(error);
		System.exit(0);
	}

}
