package org.kite9.diagram.functional.thorough;

import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kite9.diagram.AbstractLayoutFunctionalTest;
import org.kite9.diagram.NotAddressed;
import org.kite9.diagram.functional.TestingEngine.ElementsMissingException;

public class Test38XMLFilesNew extends AbstractLayoutFunctionalTest {

	@Test
	public void test_38_1_CantFinishGrouping() throws Exception {
		generate("cant_finish_grouping.xml");
	}

	@Test
	/**
	 * Renders but looks very messy and overlapping
	 */
	@Ignore("Broken in move to Kotlin - but probably not the change")
	public void test_38_2_ADClassDiagram() throws Exception {
		generate("diagramClassHierarchy.xml");
	}

	@Test
	public void test_38_3_ContainerGoesWrong() throws Exception {
		generate("container_goes_wrong.xml");
	}

	@Test
	public void test_38_4_OverlapIssue() throws Exception {
		generate("layout_issue_planarization_diagram.xml");
	}

	/**
	 * Caused by someone adding two labels to the diagram.
	 */
	@Test
	public void test_38_5_DuplicateFieldException() throws Exception {
		generate("duplicate_field_exception.xml");
	}

	@Test
	public void test_38_6_EasyCrash() throws Exception {
		generate("easy_crash.xml");
	}

	@Test
	public void test_38_7_ContainerLayoutProblem() throws Exception {
		generate("container_layout_problem.xml");
	}

	@Test
	public void test_38_8_ContainerLayoutProblem2() throws Exception {
		generate("container_layout_problem_2.xml");
	}

	@Test
	public void test_38_9_CantRouteEdge5() throws Exception {
		generate("cant_route_edge_5.xml");
	}

	/**
	 * Looks like a contradicting element should be a layout element, but
	 * isn't set as such, causing the content check to fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_38_10_CantComplete() throws Exception {
		generate("cant_complete.xml");
	}

	@Test
	public void test_38_11_SizingLayout() throws Exception {
		generate("sizing_layout.xml");
	}

	@Test
	public void test_38_12_AlignedMergeInDirectedContainer() throws Exception {
		generate("aligned_merge_in_directed_container.xml");
	}

	@Test
	public void test_38_13_TextLineFiasco() throws Exception {
		generate("text_line_fiasco.xml");
	}

	@Test
	public void test_38_14_StackOverflow() throws Exception {
		generate("stack_overflow.xml");
	}

	@Test
	public void test_38_15_UnnecessaryBend() throws Exception {
		generate("unnecessary_bend.xml");
	}

	@Test
	public void test_38_16_AlignedMergeInDirectedContainer2() throws Exception {
		generate("aligned_merge_in_directed_container2.xml");
	}

	@Test
	public void test_38_17_NoSuchElementException2() throws Exception {
		generate("no_such_element_exception2.xml");
	}

	@Test
	public void test_38_18_OuterFace() throws Exception {
		generate("outer_face.xml");
	}

	/**
	 * For some reason, id_31 appears inside a container it's not in.
	 * 
	 * @throws Exception
	 */
	@Test
	@NotAddressed
	public void test_38_19_CantCompleteOrth() throws Exception {
		generate("cant_complete_orth.xml");
	}

	@Test
	/**
	 * This is caused by the fact that in the positioning, id_3 is massive.
	 * However, in the end it turns out quite small. Not really worth fixing
	 * unless we see some other examples.
	 */
	public void test_38_21_LongWayRound() throws Exception {
		generate("long_way_round.xml");
	}

	@Test
	public void test_38_22_LongLine() throws Exception {
		generate("long_line.xml");
	}

	@Test
	public void test_38_23_UnnecessaryContradiction() throws Exception {
		generate("unnecessary_contradiction.xml");
	}

	@Test
	public void test_38_24_WonkyCompaction() throws Exception {
		generate("wonky_compaction.xml");
	}

	@Test
	public void test_38_25_AnotherContradiction() throws Exception {
		generate("another_contradiction_issue.xml");
	}

	@Test
	public void test_38_27_CantComplete() throws Exception {
		generate("cant_complete2.xml");
	}

	@Test
	public void test_38_28_SimpleBreak() throws Exception {
		generate("simple_break.xml");
	}

	@Test
	public void test_38_29_Lopsided() throws Exception {
		generate("lopsided.xml");
	}

	@Test
	public void test_38_30_UseCaseDrawing() throws Exception {
		generate("use_case_drawing.xml");
	}

	@Test
	public void test_38_31_NoPointArrow() throws Exception {
		generate("no_point_arrow.xml");
	}

	@Test
	public void test_38_32_InitBug() throws Exception {
        Exception e = Assertions.assertThrows(ElementsMissingException.class, () -> generate("init_bug.xml"));
    }

	@Test
	public void test_38_33_ContainerWrongShape() throws Exception {
		generate("container_wrong_shape.xml");
	}

	@Test
	public void test_38_34_ContainerLayoutError() throws Exception {
		generate("container_layout_error.xml");
	}

	@Test
	public void test_38_35_NonCenteredKey() throws Exception {
		generate("NonCenteredKey.xml");
	}

	@Test
	public void test_38_36_KeyError() throws Exception {
		generate("key_error.xml");
	}

	@Override
	protected boolean checkNoHops() {
		return false;
	}

	@Override
	protected boolean checkMidConnections() {
		return false;
	}

	@Override
	protected boolean checkNoContradictions() {
		return false;
	}

	@Override
	protected boolean checkEverythingStraight() {
		return false;
	}

}
