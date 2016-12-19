package org.kite9.diagram.functional;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.functional.TestingEngine.ElementsMissingException;
import org.kite9.framework.logging.Kite9Log;

import junit.framework.Assert;


public class Test37XMLFiles extends AbstractFunctionalTest {

	@Test
	public void test_37_1_LibraryExample() throws IOException {
		generate("library1.xml");
	}

	@Test
	public void test_37_2_OverlapExample() throws IOException {
		generate("overlap.xml");
	}

	@Test
	public void test_37_3_Approaches1() throws IOException {
		generate("approaches1.xml");
	}

	@Test
	public void test_37_4_Approaches2() throws IOException {
		generate("approaches2.xml");
	}

	@Test
	public void test_37_5_Approaches3() throws IOException {
		generate("approaches3.xml");
	}

	@Test
	public void test_37_6_InAndOut() throws IOException {
		generate("in_and_out.xml");
	}

	@Test
	public void test_37_7_ServerError1() throws IOException {
		generate("server_error1.xml");
	}

	@Test
	public void test_37_8_Wonky() throws IOException {
		generate("wonky.xml");
	}
	
	@Test
	public void test_37_9_SlackOptimisationFailure() throws IOException {
		generate("so_failure.xml");
	}
	
	@Test
	public void test_37_10_NudgeFailure() throws IOException {
		generate("nudge_fail.xml");
	}
	
	@Test
	public void test_37_11_Sizings() throws IOException {
		generate("sizings.xml");
	}
	
	@Test
	public void test_37_12_GlyphTooBig() throws IOException {
		generate("glyph_too_big.xml");
	}
	
	@Test
	public void test_37_13_PlanarizationView() throws IOException {
		//MGTPlanarizationBuilder.debug = true;
		generate("planarizationView.xml");
		//MGTPlanarizationBuilder.debug = false;
	}
	

	@Test
	public void test_37_14_PlanarizationDependencyGraph() throws IOException {
		generate("planarizationDependencyGraph.xml");
	}

	@Test
	public void test_37_15_EdgeRouteProblem() throws IOException {
		generate("edge_route_problem.xml");
	}
	
	@Test
	public void test_37_16_WonkyRectangularization() throws IOException {
		generate("wonky_rect.xml");
	}
	
	@Test
	public void test_37_17_CantRouteEdge() throws IOException {
		generate("cant_route_edge.xml");
	}

	@Test
	public void test_37_18_ExcessBendIntroduced() throws IOException {
		generate("excess_bend_introduced.xml");
	}

	@Test
	public void test_37_20_LongLines() throws IOException {
		generate("long_lines.xml");
	}
	
//	@Ignore("Broken in sprint 7")
	@Test
	public void test_37_21_SSPError() throws IOException {
		generate("ssp_error.xml");
	}
	
	@Test
	public void test_37_22_WiggleDistanceNotEqual() throws IOException {
		generate("wiggle_distance_not_equal.xml");
	}
	
	@Test  
	public void test_37_23_EdgesOutAndIn() throws IOException {
		try {
			generate("edges_out_and_in.xml");
		} catch (ElementsMissingException ee) {
			Assert.assertEquals(3, ee.getCountOfMissingElements());
		}
	}
	
	@Test
	public void test_37_24_EdgesOutAndIn2() throws IOException {
		try {
			generate("edges_out_and_in_2.xml");
		} catch (ElementsMissingException ee) {
			Assert.assertEquals(2, ee.getCountOfMissingElements());
		}
	}
	
	@Test
	public void test_37_25_ExtentsNotSame() throws IOException {
		try {
			generate("extents_not_same.xml");
		} catch (ElementsMissingException ee) {
			Assert.assertEquals(2, ee.getCountOfMissingElements());
		}
	}
	
	@Test
	public void test_37_26_SubContainersIssue() throws IOException {
		generate("sub_containers_issue.xml");
	}

	@Test
	public void test_37_27_SitingVertex() throws IOException {
		generate("siting_vertex.xml");
	}
	
	@Test
	public void test_37_28_CantRouteEdge2() throws IOException {
		generate("cant_route_edge_2.xml");
	}
	
	@Test
	public void test_37_29_CantRouteEdge3() throws IOException {
		generate("cant_route_edge_3.xml");
	}
	
	@Test
	public void test_37_30_CantRouteEdge4() throws IOException {
		generate("cant_route_edge_4.xml");
	}
	
	@Test
	public void test_37_31_WrongDirection() throws IOException {
		generate("wrong_direction.xml");
	}
	
	@Test
	public void test_37_32_WrongDirection2() throws IOException {
		generate("wrong_direction_2.xml");
	}
	
	@Test
	public void test_37_33_DirectionalConstraintsIssue() throws IOException {
		generate("directional_constraints_issue.xml");
	}
	
	@Test
	//@Ignore("Broken in sprint 13, no good reason - fix asap")
	public void test_37_34_BrokenContainerFlow() throws IOException {
		generate("broken_container_flow.xml");
	}
	
	@Test
	public void test_37_35_MisplacedGlyph() throws IOException {
		generate("misplaced_glyph.xml");
	}

	@Test
	public void test_37_36_NoFurtherPathPositionRouter() throws IOException {
		generate("no_further_path_position_router.xml");
	}
	
	@Test
	public void test_37_37_BrokenDartFace() throws IOException {
		generate("broken_dart_face.xml");
	}
	
	@Test
	public void test_37_38_BadSpacing_2() throws IOException {
		generate("bad_spacing_2.xml");
	}
	
	@Test
	public void test_37_39_BadSpacing() throws IOException {
		generate("bad_spacing.xml");
	}
	
	@Test
	public void test_37_40_ADLClassHierarchyLayouts() throws IOException {
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
