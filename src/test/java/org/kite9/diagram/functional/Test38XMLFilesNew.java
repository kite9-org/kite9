package org.kite9.diagram.functional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.kite9.diagram.functional.TestingEngine.ElementsMissingException;
import org.kite9.framework.common.RepositoryHelp;
import org.kite9.framework.common.TestingHelp;


public class Test38XMLFilesNew extends AbstractFunctionalTest {

	@Test
	public void test_38_1_CantFinishGrouping() throws IOException {
		generate("cant_finish_grouping.xml");
	}
	
	@Test
	/**
	 * Renders but looks very messy and overlapping
	 */
	public void test_38_2_ADClassDiagram() throws IOException {
		generate("diagramClassHierarchy.xml");
	}
	
	@Test
	public void test_38_3_ContainerGoesWrong() throws IOException {
		generate("container_goes_wrong.xml");
	}
	
	@Test
	public void test_38_4_OverlapIssue() throws IOException {
		generate("layout_issue_planarization_diagram.xml");
	}
	
	/**
	 * Caused by someone adding two labels to the diagram.
	 */
	@Test
	@Ignore
	public void test_38_5_DuplicateFieldException() throws IOException {
		generate("duplicate_field_exception.xml");
	}
	
	@Test
	public void test_38_6_EasyCrash() throws IOException {
		generate("easy_crash.xml");
	}
	
	@Test
	public void test_38_7_ContainerLayoutProblem() throws IOException {
		generate("container_layout_problem.xml");
	}
	
	@Test
	public void test_38_8_ContainerLayoutProblem2() throws IOException {
		generate("container_layout_problem_2.xml");
	}
	
	@Test
	public void test_38_9_CantRouteEdge5() throws IOException {
		generate("cant_route_edge_5.xml");
	}
	
	/**
	 * Looks like a contradicting element should be a layout element, but 
	 * isn't set as such, causing the content check to fail.
	 * @throws IOException
	 */
	@Test
	public void test_38_10_CantComplete() throws IOException {
		generate("cant_complete.xml");
	}
	
	@Test
	public void test_38_11_SizingLayout() throws IOException {
		generate("sizing_layout.xml");
	}
	

	@Test
	public void test_38_12_AlignedMergeInDirectedContainer() throws IOException {
		generate("aligned_merge_in_directed_container.xml");
	}
	
	@Test
	public void test_38_13_TextLineFiasco() throws IOException {
		generate("text_line_fiasco.xml");
	}
	
	@Test
	public void test_38_14_StackOverflow() throws IOException {
		generate("stack_overflow.xml");
	}
	
	@Test
	public void test_38_15_UnnecessaryBend() throws IOException {
		generate("unnecessary_bend.xml");
	}
	
	@Test
	public void test_38_16_AlignedMergeInDirectedContainer2() throws IOException {
		generate("aligned_merge_in_directed_container2.xml");
	}
	
	@Test
	public void test_38_17_NoSuchElementException2() throws IOException {
		generate("no_such_element_exception2.xml");
	}
	
	@Test
	public void test_38_18_OuterFace() throws IOException {
		generate("outer_face.xml");
	}
	
	@Test
	public void test_38_19_CantCompleteOrth() throws IOException {
		generate("cant_complete_orth.xml");
	}
	
	
	@Test
	// not addressed
	/** 
	 * This is caused by the fact that in the positioning, id_3 is massive.  
	 * However, in the end it turns out quite small.  Not really worth fixing 
	 * unless we see some other examples.
	 */
	public void test_38_21_LongWayRound() throws IOException {
		generate("long_way_round.xml");
	}
	
	@Test
	public void test_38_22_LongLine() throws IOException {
		generate("long_line.xml");
	}
	
	@Test
	public void test_38_23_UnnecessaryContradiction() throws IOException {
		generate("unnecessary_contradiction.xml");
	}
	
	@Test
	// not addressed
	public void test_38_24_WonkyCompaction() throws IOException {
		generate("wonky_compaction.xml");
	}
	
	@Test
	// fixed
	public void test_38_25_AnotherContradiction() throws IOException {
		generate("another_contradiction_issue.xml");
	}
	
	@Test
	public void test_38_26_Palettes() throws IOException {
		String[] palettes=new String[] {
				"palette_uml1",
				"palette_uml2", 
				"palette_adl",
				"palette_flowchart1", 
				"palette_flowchart2",
				"bonnie",
				"empty",
				"init"
		};
		
		for (String string : palettes) {
			String file = string+".xml";
			try {
				generate(file);
			} catch (ElementsMissingException eme) {
			}
			try {
				generateSizes(file);
			} catch (ElementsMissingException eme) {
			}
			File fin = TestingHelp.prepareFileName(this.getClass(), "test_38_26_Palettes", "sizes.xml");
			File fout = TestingHelp.prepareFileName(this.getClass(), "test_38_26_Palettes", string+".sxml");
			FileInputStream fis = new FileInputStream(fin);
			FileOutputStream fos = new FileOutputStream(fout);
			RepositoryHelp.streamCopy(fis, fos, true);
		}
	}
	
	
	@Test
	public void test_38_27_CantComplete() throws IOException {
		generate("cant_complete2.xml");
	}
	
	@Test
	public void test_38_28_SimpleBreak() throws IOException {
		generate("simple_break.xml");
	}
	
	@Test
	public void test_38_29_Lopsided() throws IOException {
		generate("lopsided.xml");
	}
	
	@Test
	public void test_38_30_UseCaseDrawing() throws IOException {
		generate("use_case_drawing.xml");
	}
	
	@Test
	public void test_38_31_NoPointArrow() throws IOException {
		generateNoRename("no_point_arrow.xml");
	}
	
	@Test
	@Ignore
	// not addressed
	public void test_38_32_InitBug() throws IOException {
		generateNoRename("init_bug.xml");
	}
	
	@Test
	public void test_38_33_ContainerWrongShape() throws IOException {
		generateNoRename("container_wrong_shape.xml");
	}
	
	@Test
	public void test_38_34_ContainerLayoutError() throws IOException {
		generateNoRename("container_layout_error.xml");
	}
	
	@Test
	public void test_38_35_NonCenteredKey() throws IOException {
		generate("NonCenteredKey.xml");
	}
	
	@Test
	public void test_38_36_KeyError() throws IOException {
		generate("key_error.xml");
	}
	
	
	@Override
	protected boolean checkNoHops() {
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
