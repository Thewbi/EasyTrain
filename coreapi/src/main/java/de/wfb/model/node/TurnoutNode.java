//package de.wfb.model.node;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//public abstract class TurnoutNode extends BaseNode {
//
//	private static final Logger logger = LogManager.getLogger(TurnoutNode.class);
//
//	private boolean thrown;
//
//	/**
//	 * The id that is used to operate the turnout over the intellibox / controller
//	 * station
//	 */
//	private int protocolTurnoutId;
//
//	public void toggle() {
//
//		logger.info("toggle()");
//
//		thrown = !thrown;
//	}
//
//	@Override
//	public String toString() {
//		return "TurnoutNode(" + getX() + ", " + getY() + ", " + getShapeType() + ")";
//	}
//
//	public int getProtocolTurnoutId() {
//		return protocolTurnoutId;
//	}
//
//	public void setProtocolTurnoutId(final int protocolTurnoutId) {
//		this.protocolTurnoutId = protocolTurnoutId;
//	}
//
//	public boolean isThrown() {
//		return thrown;
//	}
//
//	public void setThrown(final boolean thrown) {
//		this.thrown = thrown;
//	}
//
//}
