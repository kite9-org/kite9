package org.kite9.diagram.model.position;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.objects.OPair;

public class RectangleRenderingInformationImpl extends AbstractRenderingInformationImpl implements RectangleRenderingInformation {
	
	private OPair<BigFraction> gx, gy;
	private int sx, sy;
	private double[] xc, yc;
	
	public RectangleRenderingInformationImpl() {
		super();
	}
	
	public RectangleRenderingInformationImpl(Dimension2D pos, Dimension2D size, Direction orientation, boolean rendered) {
		setPosition(pos);
		setSize(size);
		setRendered(rendered);
	}

	@Override
	public OPair<BigFraction> gridXPosition() {
		return gx;
	}

	@Override
	public OPair<BigFraction> gridYPosition() {
		return gy;
	}

	public void setGridXPosition(OPair<BigFraction> gx) {
		this.gx = gx;
	}
	
	public void setGridYPosition(OPair<BigFraction> gy) {
		this.gy = gy;
	}

	@Override
	public int gridXSize() {
		return sx;
	}

	@Override
	public int gridYSize() {
		return sy;
	}

	@Override
	public void setGridXSize(int x) {
		this.sx = x;
	}

	@Override
	public void setGridYSize(int y) {
		this.sy = y;
	}

	@Override
	public double[] getCellXPositions() {
		return xc;
	}

	@Override
	public double[] getCellYPositions() {
		return yc;
	}

	@Override
	public void setCellXPositions(double[] xs) {
		this.xc = xs;
	}

	@Override
	public void setCellYPositions(double[] ys) {
		this.yc = ys;
	}
}
