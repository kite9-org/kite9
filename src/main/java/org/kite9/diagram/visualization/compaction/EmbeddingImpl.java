package org.kite9.diagram.visualization.compaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;

public class EmbeddingImpl implements Embedding {
	
	final List<DartFace> faces;
	Set<Embedding> innerEmbeddings;
	private final int number;
	private boolean topEmbedding;

	public EmbeddingImpl(int number, Set<DartFace> faces) {
		this.faces = new ArrayList<>(faces);
		this.number = number;
	}

	@Override
	public String toString() {
		return "EmbeddingImpl [number=" + number +  ", innerEmbeddings=" + (innerEmbeddings == null ? 0 : innerEmbeddings.size()) + ", faces=" + (faces == null ? 0 : faces.size()) + "]";
	}

	public void setInnerEmbeddings(Set<Embedding> inside) {
		this.innerEmbeddings = inside;
	}

	@Override
	public List<DartFace> getDartFaces() {
		return faces;
	}

	@Override
	public Set<Embedding> getInnerEmbeddings() {
		return innerEmbeddings;
	}
	
	@Override
	public Set<Segment> getVerticalSegments(Compaction c) {
		return facesToSegments(c, EmbeddingImpl::isVerticalDart);
	}
	
	@Override
	public Set<Segment> getHorizontalSegments(Compaction c) {
		return facesToSegments(c, EmbeddingImpl::isHorizontalDart);
	}

	public boolean isTopEmbedding() {
		return topEmbedding;
	}

	public void setTopEmbedding(boolean topEmbedding) {
		this.topEmbedding = topEmbedding;
	}
	
	private Set<Segment> facesToSegments(Compaction c, Predicate<? super Dart> p) {
		return faces.stream()
			.flatMap(f -> f.getDartsInFace().stream())
			.map(dd -> dd.getDart())
			.filter(p)
			.map(d -> c.getSegmentForDart(d))
			.collect(Collectors.toSet());
	}

	public static boolean isVerticalDart(Dart d) {
		return Direction.isVertical(d.getDrawDirection());
	}

	public static boolean isHorizontalDart(Dart d) {
		return Direction.isHorizontal(d.getDrawDirection());
	}
	
}
