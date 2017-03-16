package org.kite9.diagram.functional.thorough;

import org.junit.Test;
import org.kite9.diagram.functional.AbstractLayoutFunctionalTest;
import org.kite9.diagram.functional.layout.TestingEngine.ElementsMissingException;
import org.kite9.framework.logging.Kite9Log;

import junit.framework.Assert;


public class Test37XMLFiles extends AbstractLayoutFunctionalTest {

	@Test
	public void test_37_1_LibraryExample() throws Exception {
		generate("library1.xml");
	}

	@Test
	public void test_37_2_OverlapExample() throws Exception {
		generate("overlap.xml");
	}

	@Test
	public void test_37_3_Approaches1() throws Exception {
		generate("approaches1.xml");
	}

	@Test
	public void test_37_4_Approaches2() throws Exception {
		generate("approaches2.xml");
	}

	@Test
	public void test_37_5_Approaches3() throws Exception {
		generate("approaches3.xml");
	}

	@Test
	public void test_37_6_InAndOut() throws Exception {
		generate("in_and_out.xml");
	}

	@Test
	public void test_37_7_ServerError1() throws Exception {
		generate("server_error1.xml");
	}

	@Test
	public void test_37_8_Wonky() throws Exception {
		generate("wonky.xml");
	}
	
	@Test
	public void test_37_9_SlackOptimisationFailure() throws Exception {
		generate("so_failure.xml");
	}
	
	@Test
	public void test_37_10_NudgeFailure() throws Exception {
		generate("nudge_fail.xml");
	}
	
	@Test
	public void test_37_11_Sizings() throws Exception {
		generate("sizings.xml");
	}
	
	@Test
	public void test_37_12_GlyphTooBig() throws Exception {
		generate("glyph_too_big.xml");
	}
	
	@Test
	public void test_37_13_PlanarizationView() throws Exception {
		//MGTPlanarizationBuilder.debug = true;
		generate("planarizationView.xml");
		//MGTPlanarizationBuilder.debug = false;
	}
	

	@Test
	public void test_37_14_PlanarizationDependencyGraph() throws Exception {
		generate("planarizationDependencyGraph.xml");
	}

	@Test
	public void test_37_15_EdgeRouteProblem() throws Exception {
		generate("edge_route_problem.xml");
	}
	
	@Test
	public void test_37_16_WonkyRectangularization() throws Exception {
		generate("wonky_rect.xml");
	}
	
	@Test
	public void test_37_17_CantRouteEdge() throws Exception {
		generate("cant_route_edge.xml");
	}

	@Test
	public void test_37_18_ExcessBendIntroduced() throws Exception {
		generate("excess_bend_introduced.xml");
	}

	@Test
	public void test_37_20_LongLines() throws Exception {
		generate("long_lines.xml");
	}
	
//	@Ignore("Broken in sprint 7")
	@Test
	public void test_37_21_SSPError() throws Exception {
		generate("ssp_error.xml");
	}
	
	@Test
	public void test_37_22_WiggleDistanceNotEqual() throws Exception {
		generate("wiggle_distance_not_equal.xml");
	}
	
	@Test  
	public void test_37_23_EdgesOutAndIn() throws Exception {
		try {
			generate("edges_out_and_in.xml");
		} catch (ElementsMissingException ee) {
			Assert.assertEquals(3, ee.getCountOfMissingElements());
		}
	}
	
	@Test
	public void test_37_24_EdgesOutAndIn2() throws Exception {
		try {
			generate("edges_out_and_in_2.xml");
		} catch (ElementsMissingException ee) {
			Assert.assertEquals(2, ee.getCountOfMissingElements());
		}
	}
	
	@Test
	public void test_37_25_ExtentsNotSame() throws Exception {
		try {
			generate("extents_not_same.xml");
		} catch (ElementsMissingException ee) {
			Assert.assertEquals(2, ee.getCountOfMissingElements());
		}
	}
	
	@Test
	public void test_37_26_SubContainersIssue() throws Exception {
		generate("sub_containers_issue.xml");
	}

	@Test
	public void test_37_27_SitingVertex() throws Exception {
		generate("siting_vertex.xml");
	}
	
	@Test
	public void test_37_28_CantRouteEdge2() throws Exception {
		generate("cant_route_edge_2.xml");
	}
	
	@Test
	public void test_37_29_CantRouteEdge3() throws Exception {
		generate("cant_route_edge_3.xml");
	}
	
	@Test
	public void test_37_30_CantRouteEdge4() throws Exception {
		generate("cant_route_edge_4.xml");
	}
	
	@Test
	public void test_37_31_WrongDirection() throws Exception {
		generate("wrong_direction.xml");
	}
	
	@Test
	public void test_37_32_WrongDirection2() throws Exception {
		generate("wrong_direction_2.xml");
	}
	
	@Test
	public void test_37_33_DirectionalConstraintsIssue() throws Exception {
		generate("directional_constraints_issue.xml");
	}
	
	@Test
	//@Ignore("Broken in sprint 13, no good reason - fix asap")
	public void test_37_34_BrokenContainerFlow() throws Exception {
		generate("broken_container_flow.xml");
	}
	
	@Test
	public void test_37_35_MisplacedGlyph() throws Exception {
		generate("misplaced_glyph.xml");
	}

	@Test
	public void test_37_36_NoFurtherPathPositionRouter() throws Exception {
		generate("no_further_path_position_router.xml");
	}
	
	@Test
	public void test_37_37_BrokenDartFace() throws Exception {
		generate("broken_dart_face.xml");
	}
	
	@Test
	public void test_37_38_BadSpacing_2() throws Exception {
		generate("bad_spacing_2.xml");
	}
	
	@Test
	public void test_37_39_BadSpacing() throws Exception {
		generate("bad_spacing.xml");
	}
	
	@Test
	public void test_37_40_ADLClassHierarchyLayouts() throws Exception {
		generate("ADLClassHierarchyLayouts.xml");
	}
	
	@Override
	protected boolean checkNoHops() {
		return false;
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}
	
	@Override
	public void setLogging() {
		Kite9Log.setLogging(false);
	}
	
	
}
