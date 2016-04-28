package org.kite9.diagram.suites;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.kite9.diagram.functional.Test15ContainerLinking;
import org.kite9.diagram.functional.Test17TwoContainerLinking;
import org.kite9.diagram.functional.Test18DirectedPlanarization;
import org.kite9.diagram.functional.Test22DirectedEdgeInsertion;
import org.kite9.diagram.functional.Test25NLinkedContainers;
import org.kite9.diagram.functional.Test27VertexOrderingDirected;
import org.kite9.diagram.functional.Test28VertexOrderingMixed;
import org.kite9.diagram.functional.Test30VertexOrderingContained;
import org.kite9.diagram.functional.Test33Contradictions;
import org.kite9.diagram.functional.Test36LayoutChoices;
import org.kite9.diagram.functional.Test38XMLFilesNew;

@RunWith(Suite.class)
@Ignore
@SuiteClasses({ 
		Test18DirectedPlanarization.class,
		Test25NLinkedContainers.class, 
		Test27VertexOrderingDirected.class, 
		Test22DirectedEdgeInsertion.class,
		Test28VertexOrderingMixed.class, 
		Test30VertexOrderingContained.class, 
		Test15ContainerLinking.class, 
		Test17TwoContainerLinking.class,
		Test38XMLFilesNew.class,
		Test33Contradictions.class,
		Test36LayoutChoices.class
		})
public class BuddyMergeTestSuite {

}
