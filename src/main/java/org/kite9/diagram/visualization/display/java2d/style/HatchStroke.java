/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.kite9.diagram.visualization.display.java2d.style;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.visualization.display.java2d.adl_basic.StrokeWithWidth;

public class HatchStroke implements StrokeWithWidth {
    private BasicStroke stroke;
    private BasicStroke hatch;
    float gap;
    float angle;

    public HatchStroke( BasicStroke stroke, BasicStroke hatch, float gap, float angle ) {
        this.stroke = stroke;
        this.hatch = hatch;
        this.gap = gap;
        this.angle = angle;
	}
	
	public float getLineWidth() {
		return stroke.getLineWidth();
	}

	public Shape createStrokedShape( Shape shape ) {
		shape = stroke.createStrokedShape(shape);
		
		Area mask = new Area(shape);
	
		Rectangle2D r = shape.getBounds();
		double m = angle;

		GeneralPath result = new GeneralPath();
		double sy = 0;
		double x = r.getMinX();
		
		// create hatch of rectangle
		double limit = r.getMaxX() + (((float)(r.getMaxY() - r.getMinY())) / m);
		while (x < limit) {
			sy = r.getMinY() + m * (x - r.getMinX());
			result.moveTo(r.getMinX(), sy);
			result.lineTo(x, r.getMinY());
			x += gap;
			
		}
		
		result.lineTo(limit, r.getMaxY());
		
		Shape lines =  hatch.createStrokedShape(result);
		Area fill = new Area(lines);
		
		fill.intersect(mask);
		
		return fill;

	}

}
