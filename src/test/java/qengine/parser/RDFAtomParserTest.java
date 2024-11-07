package qengine.parser;

import org.junit.jupiter.api.Test;
import qengine.model.RDFAtom;
import qengine.parser.RDFAtomParser;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe RDFAtomParser.
 */
class RDFAtomParserTest {

    @Test
    void testParseValidRDFAtoms() throws Exception {
        File rdfFile = new File("src/test/resources/sample_data.nt");
        try (RDFAtomParser parser = new RDFAtomParser(rdfFile)) {
            assertTrue(parser.hasNext(), "Le parser devrait trouver des triplets RDF.");

            RDFAtom atom1 = parser.next();
            assertEquals("http://example.org/subject1", atom1.getTerms()[0].label(), "Sujet incorrect.");
            assertEquals("http://example.org/predicate1", atom1.getTerms()[1].label(), "Prédicat incorrect.");
            assertEquals("http://example.org/object1", atom1.getTerms()[2].label(), "Objet incorrect.");

            RDFAtom atom2 = parser.next();
            assertEquals("http://example.org/subject2", atom2.getTerms()[0].label(), "Sujet incorrect.");
            assertEquals("http://example.org/predicate2", atom2.getTerms()[1].label(), "Prédicat incorrect.");
            assertEquals("http://example.org/object2", atom2.getTerms()[2].label(), "Objet incorrect.");

            assertFalse(parser.hasNext(), "Le parser ne devrait plus avoir de triplets RDF.");
        }
    }

    @Test
    void testParseEmptyFile() throws Exception {
        File emptyFile = new File("src/test/resources/empty.nt");
        try (RDFAtomParser parser = new RDFAtomParser(emptyFile)) {
            assertFalse(parser.hasNext(), "Le parser ne devrait pas trouver de triplets dans un fichier vide.");
        }
    }

    @Test
    void testMultipleCloseCalls() throws Exception {
        File rdfFile = new File("src/test/resources/sample_data.nt");
        try (RDFAtomParser parser = new RDFAtomParser(rdfFile)) {
            assertTrue(parser.hasNext(), "Le parser devrait trouver des triplets RDF.");
            parser.close();
            parser.close(); // Appel supplémentaire pour tester la gestion des fermetures multiples
        }
    }

    @Test
    void testNextWithoutHasNext() throws Exception {
        File rdfFile = new File("src/test/resources/sample_data.nt");
        try (RDFAtomParser parser = new RDFAtomParser(rdfFile)) {
            RDFAtom atom1 = parser.next();
            assertEquals("http://example.org/subject1", atom1.getTerms()[0].label(), "Sujet incorrect.");
        }
    }

    @Test
    void testParserExhaustion() throws Exception {
        File rdfFile = new File("src/test/resources/sample_data.nt");
        try (RDFAtomParser parser = new RDFAtomParser(rdfFile)) {
            while (parser.hasNext()) {
                parser.next();
            }
            assertFalse(parser.hasNext(), "Le parser ne devrait pas indiquer de triplets restants.");
            assertThrows(IllegalArgumentException.class, parser::next, "Un appel à next() sans triplets restants devrait lever une exception.");
        }
    }
}
