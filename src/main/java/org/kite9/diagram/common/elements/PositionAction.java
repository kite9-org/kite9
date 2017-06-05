/**
 * 
 */
package org.kite9.diagram.common.elements;



public interface PositionAction {
	
	public static final PositionAction XAction = new PositionAction() {
		
		public double value(Positioned s) {
			return s.getX();
		}
		
		public void add(Positioned s, double amount) {
			s.setX(s.getX()+  amount);
		}
	
		public void set(Positioned s, double amount) {
			s.setX(amount);
		}
		
		public String toString() {
			return "V";
		}
		
	};
	public static PositionAction YAction = new PositionAction() {
		
		public double value(Positioned s) {
			return s.getY();
		}
		
		public void add(Positioned s, double amount) {
			s.setY(s.getY() + amount);
		}
		
		public void set(Positioned s, double amount) {
			s.setY(amount);
		}
		
		public String toString() {
			return "H";
		}
	};

	public double value(Positioned s);
	
	public void add(Positioned s, double amount);
	
	public void set(Positioned s, double amount);
	
	
}