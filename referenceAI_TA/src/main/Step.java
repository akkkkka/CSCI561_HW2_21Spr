/**
 *
 */
package main;

import java.util.LinkedList;

/**
 * @author daniellink
 *
 */
public class Step {

	private int xFrom, yFrom, xTo, yTo;

	/**
	 * @return the yTo
	 */
	public int getyTo() {
		return yTo;
	}

	/**
	 * @param yTo the yTo to set
	 */
	public void setyTo(final int yTo) {
		this.yTo = yTo;
	}

	/**
	 * @return the xTo
	 */
	public int getxTo() {
		return xTo;
	}

	/**
	 * @param xTo the xTo to set
	 */
	public void setxTo(final int xTo) {
		this.xTo = xTo;
	}

	/**
	 * @return the yFrom
	 */
	public int getyFrom() {
		return yFrom;
	}

	/**
	 * @param yFrom the yFrom to set
	 */
	public void setyFrom(final int yFrom) {
		this.yFrom = yFrom;
	}

	/**
	 * @return the xFrom
	 */
	public int getxFrom() {
		return xFrom;
	}

	/**
	 * @param xFrom the xFrom to set
	 */
	public void setxFrom(final int xFrom) {
		this.xFrom = xFrom;
	}

	// private int number;
	private final LinkedList<Step> children;

	public enum Type {
		SHOVE, JUMP
	}

	private Type kind;

	public Type getKind() {
		return kind;
	}

	public Step(final Type t, final int xFrom_, final int yFrom_, final int xTo_, final int yTo_) {
		xFrom = xFrom_;
		yFrom = yFrom_;
		xTo = xTo_;
		yTo = yTo_;
		kind = t;
		children = new LinkedList<>();
	}

	public Step(String s) {
		final String[] parts = s.split("\\s+");
		if (parts.length != 3) {
			System.err.println("Step in a move needs to have 3 sections, found " + parts.length + " in '" + s + "'.");
			System.exit(-1);
		}
		kind = null;
		switch (parts[0]) {
		case "e" -> kind = Type.SHOVE;
		case "j" -> kind = Type.JUMP;
		default -> System.err.println("Cannot find valid step type in string '" + s + "'.");
		}
		xFrom = parts[1].charAt(0) - 'a';
		yFrom = '8' - parts[1].charAt(1);
		xTo = parts[2].charAt(0) - 'a';
		yTo = '8' - parts[2].charAt(1);
		children = new LinkedList<>();
	}

	public void addChild(final Step s) {
		children.add(s);
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public LinkedList<Step> toLongestStepSequence() {
		final LinkedList<Step> stepSequence = new LinkedList<>();
		Step currentStep = this;
		stepSequence.add(currentStep);
		while (!currentStep.children.isEmpty()) {
			final Step previousStep = currentStep;
			currentStep = currentStep.children.get(0);
			stepSequence.add(currentStep);
			if (currentStep.children.isEmpty()) {
				previousStep.children.remove(0);
			}
		}
		return stepSequence;
	}

	@Override
	public String toString() {
		final String fromString = Board.positionToNotation(xFrom, yFrom);
		final String toString = Board.positionToNotation(xTo, yTo);
		String output = fromString + " " + toString + "\n";
		if (children.size() > 0) {
			output += "\nChildren:\n";
			for (final Step c : children) {
				output += " " + c.toString();
			}
		}
		return output;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + xFrom;
		result = prime * result + xTo;
		result = prime * result + yFrom;
		result = prime * result + yTo;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Step other = (Step) obj;
		if (kind != other.getKind()) {
			return false;
		}
		if (xFrom != other.xFrom) {
			return false;
		}
		if (xTo != other.xTo) {
			return false;
		}
		if (yFrom != other.yFrom) {
			return false;
		}
		if (yTo != other.yTo) {
			return false;
		}
		return true;
	}

}
