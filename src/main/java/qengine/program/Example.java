package qengine.program;

import fr.boreal.model.query.api.Query;
import org.eclipse.rdf4j.rio.RDFFormat;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine.parser.RDFAtomParser;
import qengine.parser.StarQuerySparQLParser;

import java.io.FileReader;
import java.io.IOException;

/**
 * Exemple d'utilisation des parsers RDF et SparQL.
 * Ce programme parse les fichiers RDF et les requêtes SparQL, puis affiche les résultats.
 */
public final class Example {

	private static final String WORKING_DIR = "data/";
	private static final String SAMPLE_DATA_FILE = WORKING_DIR + "sample_data.nt";
	private static final String SAMPLE_QUERY_FILE = WORKING_DIR + "sample_query.queryset";

	public static void main(String[] args) throws IOException {
		System.out.println("=== Parsing RDF Data ===");
		parseRDFData(SAMPLE_DATA_FILE);

		System.out.println("\n=== Parsing Sample Queries ===");
		parseSparQLQueries(SAMPLE_QUERY_FILE);
	}

	/**
	 * Parse et affiche le contenu d'un fichier RDF.
	 *
	 * @param rdfFilePath Chemin vers le fichier RDF à parser
	 */
	private static void parseRDFData(String rdfFilePath) throws IOException {
		FileReader rdfFile = new FileReader(rdfFilePath);

		try (RDFAtomParser rdfAtomParser = new RDFAtomParser(rdfFile, RDFFormat.NTRIPLES)) {
			int count = 0;
			while (rdfAtomParser.hasNext()) {
				RDFAtom atom = rdfAtomParser.next();
				System.out.println("RDF Atom #" + (++count) + ": " + atom);
			}
			System.out.println("Total RDF Atoms parsed: " + count);
		}
	}

	/**
	 * Parse et affiche le contenu d'un fichier de requêtes SparQL.
	 *
	 * @param queryFilePath Chemin vers le fichier de requêtes SparQL
	 */
	private static void parseSparQLQueries(String queryFilePath) throws IOException {
		try (StarQuerySparQLParser queryParser = new StarQuerySparQLParser(queryFilePath)) {
			int queryCount = 0;

			while (queryParser.hasNext()) {
				Query query = queryParser.next();
				if (query instanceof StarQuery starQuery) {
					System.out.println("Star Query #" + (++queryCount) + ":");
					System.out.println("  Central Variable: " + starQuery.getCentralVariable().label());
					System.out.println("  RDF Atoms:");
					starQuery.getRdfAtoms().forEach(atom -> System.out.println("    " + atom));
				} else {
					System.err.println("Requête inconnue ignorée.");
				}
			}
			System.out.println("Total Queries parsed: " + queryCount);
		}
	}
}
