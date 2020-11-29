package org.kite9.diagram.common.elements;

import org.kite9.diagram.model.position.Dimension2D;

public interface Positioned {

	public void setX(double x);

	public void setY(double y);

	public double getX();

	public double getY();

	public Dimension2D getPosition();
}