/**
 *
 */
package de.uni_freiburg.informatik.ultimate.plugins.spaceex.automata.hybridsystem;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.plugins.spaceex.automata.HybridModel;
import de.uni_freiburg.informatik.ultimate.plugins.spaceex.parser.generated.ObjectFactory;
import de.uni_freiburg.informatik.ultimate.plugins.spaceex.parser.generated.Sspaceex;
import de.uni_freiburg.informatik.ultimate.test.ConsoleLogger;

/**
 * @author Julian Loeffler (loefflju@informatik.uni-freiburg.de)
 *
 */
public class ParallelCompositionGeneratorTest {
	
	/**
	 * Test method for
	 * {@link de.uni_freiburg.informatik.ultimate.plugins.spaceex.automata.hybridsystem.ParallelCompositionGenerator#computeParallelComposition(de.uni_freiburg.informatik.ultimate.plugins.spaceex.automata.hybridsystem.HybridAutomaton, de.uni_freiburg.informatik.ultimate.plugins.spaceex.automata.hybridsystem.HybridAutomaton)}
	 * .
	 */
	@Test
	public void testComputeParallelComposition() throws Exception {
		final ILogger logger = new ConsoleLogger();
		// TEST1: aut1: 1 location, 0 transitions, aut2: 1 location, 0 transitions
		System.out.println("Starting Test 1..");
		long startTime = System.nanoTime();
		String file =
				"../SpaceExParserTest/src/de/uni_freiburg/informatik/ultimate/plugins/spaceex/automata/hybridsystem/testfiles/test1.xml";
		FileInputStream fis = new FileInputStream(file);
		final JAXBContext jaxContext = JAXBContext.newInstance(ObjectFactory.class);
		final Unmarshaller unmarshaller = jaxContext.createUnmarshaller();
		Sspaceex spaceEx = (Sspaceex) unmarshaller.unmarshal(fis);
		fis.close();
		HybridModel system = new HybridModel(spaceEx, logger);
		Map<String, HybridAutomaton> mergedAutomata = system.getMergedAutomata();
		HybridAutomaton merge = mergedAutomata.get("aut2||aut1");
		System.out.println(merge);
		assertEquals("aut2||aut1", merge.getName());
		assertEquals("[]", merge.getGlobalConstants().toString());
		assertEquals("[x, y]", merge.getGlobalParameters().toString());
		assertEquals("[]", merge.getLocalConstants().toString());
		assertEquals("[]", merge.getLocalParameters().toString());
		assertEquals("{1=loc_1(1), Invariant: y <= 10 && x <= 10, Flow: y' == 10 && x' == 10}",
				merge.getLocations().toString());
		assertEquals("[]", merge.getTransitions().toString());
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("Done in " + estimatedTime / (float) 1000000 + " milliseconds");
		// TEST2: aut1: 1 location, 0 transitions, aut2: 2 locations, 1 transition
		System.out.println("Starting Test 2..");
		startTime = System.nanoTime();
		file = "../SpaceExParserTest/src/de/uni_freiburg/informatik/ultimate/plugins/spaceex/automata/hybridsystem/testfiles/test2.xml";
		fis = new FileInputStream(file);
		spaceEx = (Sspaceex) unmarshaller.unmarshal(fis);
		fis.close();
		system = new HybridModel(spaceEx, logger);
		mergedAutomata = system.getMergedAutomata();
		merge = mergedAutomata.get("aut2||aut1");
		System.out.println(merge);
		assertEquals("aut2||aut1", merge.getName());
		assertEquals("[]", merge.getGlobalConstants().toString());
		assertEquals("[x, y]", merge.getGlobalParameters().toString());
		assertEquals("[]", merge.getLocalConstants().toString());
		assertEquals("[]", merge.getLocalParameters().toString());
		assertEquals("{1=loc_1(1), Invariant: y <= 10 && x <= 10, Flow: y' == 10 && x' == 10,"
				+ " 2=loc_2(2), Invariant: x <= 10, Flow: x' == 10}", merge.getLocations().toString());
		assertEquals("[(1) === (); {} ===> (2)]", merge.getTransitions().toString());
		estimatedTime = System.nanoTime() - startTime;
		System.out.println("Done in " + estimatedTime / (float) 1000000 + " milliseconds");
		// TEST3: aut1: 3 locations, 1 sync transition, 1 discrete transition , aut2: 3 locations, 1 sync transition, 1
		// discrete transition
		System.out.println("Starting Test 3..");
		startTime = System.nanoTime();
		file = "../SpaceExParserTest/src/de/uni_freiburg/informatik/ultimate/plugins/spaceex/automata/hybridsystem/testfiles/test3.xml";
		fis = new FileInputStream(file);
		spaceEx = (Sspaceex) unmarshaller.unmarshal(fis);
		fis.close();
		system = new HybridModel(spaceEx, logger);
		mergedAutomata = system.getMergedAutomata();
		merge = mergedAutomata.get("aut2||aut1");
		System.out.println(merge);
		assertEquals("aut2||aut1", merge.getName());
		assertEquals("[]", merge.getGlobalConstants().toString());
		assertEquals("[x, y]", merge.getGlobalParameters().toString());
		assertEquals("[]", merge.getLocalConstants().toString());
		assertEquals("[]", merge.getLocalParameters().toString());
		assertEquals("[jump1]", merge.getLabels().toString());
		assertEquals(
				"{1=loc_1(1), Invariant: y <= 4 && x <= 4, Flow: y'==1 && x'==1, "
						+ "2=loc_2(2), Invariant: y <= 5 && x <= 5, Flow: y'==1 && x'==1, "
						+ "3=loc_3(3), Invariant: y <= 5 && x <= 6, Flow: y'==1 && x'==1, "
						+ "4=loc_4(4), Invariant: y <= 6 && x <= 5, Flow: y'==1 && x'==1, "
						+ "5=loc_5(5), Invariant: y <= 6 && x <= 6, Flow: y'==1 && x'==1}",
				merge.getLocations().toString());
		assertEquals(
				"[(1) === (); {y:=0 && x:=0}; Label: jump1 ===> (2), " + "(2) === (); {} ===> (3), "
						+ "(2) === (); {} ===> (4), " + "(4) === (); {} ===> (5), " + "(3) === (); {} ===> (5)]",
				merge.getTransitions().toString());
		estimatedTime = System.nanoTime() - startTime;
		System.out.println("Done in " + estimatedTime / (float) 1000000 + " milliseconds");
		// TEST4: aut1: 3 locations, 1 sync transition, 1 discrete transition , aut2: 3 locations, 1 sync transition, 1
		// discrete transition
		System.out.println("Starting Test 4..");
		startTime = System.nanoTime();
		file = "../SpaceExParserTest/src/de/uni_freiburg/informatik/ultimate/plugins/spaceex/automata/hybridsystem/testfiles/test4.xml";
		fis = new FileInputStream(file);
		spaceEx = (Sspaceex) unmarshaller.unmarshal(fis);
		fis.close();
		system = new HybridModel(spaceEx, logger);
		mergedAutomata = system.getMergedAutomata();
		merge = mergedAutomata.get("aut2||aut1");
		System.out.println(merge);
		assertEquals("aut2||aut1", merge.getName());
		assertEquals("[]", merge.getGlobalConstants().toString());
		assertEquals("[x, y]", merge.getGlobalParameters().toString());
		assertEquals("[]", merge.getLocalConstants().toString());
		assertEquals("[]", merge.getLocalParameters().toString());
		assertEquals("[jump1]", merge.getLabels().toString());
		assertEquals(
				"{1=loc_1(1), Invariant: y <= 4 && x <= 4, Flow: y'==1 && x'==1, "
						+ "2=loc_2(2), Invariant: y <= 4 && x <= 5, Flow: y'==1 && x'==1, "
						+ "3=loc_3(3), Invariant: y <= 5 && x <= 4, Flow: y'==1 && x'==1, "
						+ "4=loc_4(4), Invariant: y <= 5 && x <= 6, Flow: y'==1 && x'==1, "
						+ "5=loc_5(5), Invariant: y <= 6 && x <= 6, Flow: y'==1 && x'==1}",
				merge.getLocations().toString());
		assertEquals("[(1) === (); {} ===> (2), " + "(2) === (); {y:=0 && x:=0}; Label: jump1 ===> (4), "
				+ "(4) === (); {} ===> (5)]", merge.getTransitions().toString());
		estimatedTime = System.nanoTime() - startTime;
		System.out.println("Done in " + estimatedTime / (float) 1000000 + " milliseconds");
	}
	
}