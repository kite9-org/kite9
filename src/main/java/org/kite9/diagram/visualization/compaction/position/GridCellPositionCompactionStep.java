package org.kite9.diagram.visualization.compaction.position;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;

/**
 * Sets the details of the cell positions in the parent container (Layout=GRID).
 */
public class GridCellPositionCompactionStep implements CompactionStep {

	public void compact(Compaction c, Embedding r, Compactor cr) {
		if (r.isTopEmbedding()) {
			Set<Container> gridContainers = createTopElementSet(c.getOrthogonalization());
			
			for (Container de : gridContainers) {
				RectangleRenderingInformation rri = de.getRenderingInformation();
				setPostions(rri, de.getContents());
			}
		}
	}
	
	private void setPostions(RectangleRenderingInformation pri, List<DiagramElement> contents) {
		if (contents == null) {
			pri.setCellXPositions(new double[] {});
			pri.setCellYPositions(new double[] {});
		} else {
			Dimension2D parent = pri.getPosition();
			Set<Double> xs = new HashSet<>();
			Set<Double> ys = new HashSet<>();
			contents.stream()
				.filter(c -> c instanceof Connected)
				.map(c -> c.getRenderingInformation()).forEach(rri -> {
				Dimension2D p = rri.getPosition();
				Dimension2D s = rri.getSize();
				xs.add(p.getWidth() - parent.getWidth());
				xs.add(p.getWidth() + s.getWidth() - parent.getWidth());
				ys.add(p.getHeight() - parent.getHeight());
				ys.add(p.getHeight() + s.getHeight() - parent.getHeight());
			});
			double[] xPositions = xs.stream().mapToDouble(d -> (double) d).sorted().toArray();
			double[] yPositions = ys.stream().mapToDouble(d -> (double) d).sorted().toArray();
			pri.setCellXPositions(xPositions);
			pri.setCellYPositions(yPositions);
		}
	}

	private Set<Container> createTopElementSet(Orthogonalization c) {
		Set<Container> out  = new LinkedHashSet<Container>();
		for (Dart e : c.getAllDarts()) {
			for (DiagramElement de : e.getDiagramElements().keySet()) {
				if (de instanceof Container) {
					if (((Container)de).getLayout() == Layout.GRID) {
						out.add((Container) de);
					}
				}
			}
		}	
		return out;
	}

}
