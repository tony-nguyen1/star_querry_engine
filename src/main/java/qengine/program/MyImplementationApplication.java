package qengine.program;

import fr.boreal.model.formula.api.FOFormula;
import fr.boreal.model.formula.api.FOFormulaConjunction;
import fr.boreal.model.kb.api.FactBase;
import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.query.api.FOQuery;
import fr.boreal.model.query.api.Query;
import fr.boreal.model.queryEvaluation.api.FOQueryEvaluator;
import fr.boreal.query_evaluation.generic.GenericFOQueryEvaluator;
import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine.parser.RDFAtomParser;
import qengine.parser.StarQuerySparQLParser;
import qengine.storage.RDFHexaStore;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_EVEN;

public final class MyImplementationApplication {

//	private static final String WORKING_DIR = "watdiv-mini-projet-partie-2/";
//	private static final String SAMPLE_DATA_FILE = WORKING_DIR + "100K.nt";
//	private static final String SAMPLE_QUERY_DIR = WORKING_DIR + "data2M/";

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			throw new IllegalArgumentException("usage : ./abc.jar path/to/data path/to/querry mode");
		}
//		for (String s : args) {
//			System.out.println(s);
//		}

		String pathDataFile;
		String pathQueryDir;
		pathDataFile = args[0];
		pathQueryDir = args[1];
//		System.out.println("\n"+SAMPLE_DATA_FILE+"\n"+SAMPLE_QUERY_DIR);

		int moteurInt;
		moteurInt = Integer.parseInt(args[2]);
		/*
		 * Exemple d'utilisation des deux parsers
		 */
//		System.out.println("=== Parsing RDF Data ===");
		List<RDFAtom> rdfAtoms = parseRDFData(pathDataFile);

//		System.out.println("\n=== Parsing Sample Queries ===");
		List<StarQuery> starQueries = parseDirQuerries(pathQueryDir);//parseSparQLQueries(SAMPLE_QUERY_FILE);


//
//		/*
//		 * Exemple d'utilisation de l'évaluation de requetes par Integraal avec les objets parsés
//		 */
//		System.out.println("\n=== Executing the queries with Integraal ===");
		Store store = null;
		switch (moteurInt) {
			case 0:
				store = new WrapperIntegraal(new SimpleInMemoryGraphStore());
				break;
			case 1:
				store = new RDFHexaStore();
		}
		System.out.println(store);

		for (RDFAtom atom : rdfAtoms) {
			store.add(atom); // Stocker chaque RDFAtom dans le store
		}


		HashMap<String, Integer> map = new HashMap<>();
		//TODO


		HashMap<Integer, List<StarQuery>> goodMap = new HashMap<>();
		long start = System.nanoTime();
		int i = 0;
		int nbReponsesVides = 0;
		StringBuilder sb = new StringBuilder();
		for (StarQuery starQuery : starQueries) {
			sb.append(i);
			sb.append(" ");
			Iterator<Substitution> iterator = store.match(starQuery);
			ArrayList<Substitution> subList = new ArrayList<>();
			iterator.forEachRemaining(subList::add);
			if (subList.isEmpty()) {
				nbReponsesVides++;
			}
			sb.append(subList.size());
			int nKey = subList.size()/10;
			sb.append(" "+nKey);
			map.computeIfAbsent(""+nKey, s -> 0);
			map.put(""+nKey, map.get(""+nKey)+1);
			sb.append("\n");
			i++;
//			System.out.println(starQuery.getLabel());

			int res = subList.size()/10;
//			System.out.println(nbReponse+"                  "+res);
			goodMap.computeIfAbsent(res, k -> new ArrayList<>());
			goodMap.get(res).add(starQuery);
		}
//		System.out.println(sb);



		long finish = System.nanoTime();
		System.currentTimeMillis();
		long timeElapsed = finish - start;
		double d = timeElapsed/1000000000d;
		DecimalFormat df = new DecimalFormat("#.###");
		String arrondi = df.format(d);
		System.out.println("t="+arrondi);

		System.out.println("class #Requete");
		for (Integer nbReponse : goodMap.keySet().stream().sorted().toList()) {
			System.out.println(nbReponse+" "+goodMap.get(nbReponse).size());
		}
		System.out.println(nbReponsesVides+" réponses vides");
	}

	/**
	 * Parse et affiche le contenu d'un fichier RDF.
	 *
	 * @param rdfFilePath Chemin vers le fichier RDF à parser
	 * @return Liste des RDFAtoms parsés
	 */
	private static List<RDFAtom> parseRDFData(String rdfFilePath) throws IOException {
		FileReader rdfFile = new FileReader(rdfFilePath);
		List<RDFAtom> rdfAtoms = new ArrayList<>();



		PrintStream originalOut = System.out;

		// Création d'un ByteArrayOutputStream pour capturer les données
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream newOut = new PrintStream(baos);

		// Redirige System.out vers notre PrintStream
//		System.setOut(newOut);

		// Code dont la sortie doit être capturée
		System.out.println("Ceci est capturé dans le flux !");




		try (RDFAtomParser rdfAtomParser = new RDFAtomParser(rdfFile, RDFFormat.NTRIPLES)) {
			int count = 0;
			while (rdfAtomParser.hasNext()) {
				RDFAtom atom = rdfAtomParser.next();
				rdfAtoms.add(atom);  // Stocker l'atome dans la collection
				++count;
//				System.out.println("RDF Atom #" + (count) + ": " + atom);
			}
			System.out.println("Total RDF Atoms parsed: " + count);
		}


		// Restauration de la sortie standard
//		System.setOut(originalOut);

		// Affichage du contenu capturé
//		baos.reset();
//		System.out.println("Sortie capturée : " + baos.toString());
		return rdfAtoms;
	}

	/**
	 * Parse et affiche le contenu d'un fichier de requêtes SparQL.
	 *
	 * @param queryFilePath Chemin vers le fichier de requêtes SparQL
	 * @return Liste des StarQueries parsées
	 */
	private static List<StarQuery> parseSparQLQueries(String queryFilePath) throws IOException {
		List<StarQuery> starQueries = new ArrayList<>();

		try (StarQuerySparQLParser queryParser = new StarQuerySparQLParser(queryFilePath)) {
			int queryCount = 0;

			while (queryParser.hasNext()) {
				Query query = queryParser.next();
				if (query instanceof StarQuery starQuery) {
					starQueries.add(starQuery);  // Stocker la requête dans la collection
//					System.out.println("Star Query #" + (++queryCount) + ":");
//					System.out.println("  Central Variable: " + starQuery.getCentralVariable().label());
//					System.out.println("  RDF Atoms:");
//					starQuery.getRdfAtoms().forEach(atom -> System.out.println("    " + atom));
				} else {
//					System.err.println("Requête inconnue ignorée.");
				}
			}
//			System.out.println("Total Queries parsed: " + starQueries.size());
		}
		return starQueries;
	}

	private static List<StarQuery> parseDirQuerries(String querryDirPath) throws IOException {
		List<StarQuery> starQueries = new ArrayList<>();

		Set<String> fileNameSet = Stream.of(new File(querryDirPath).listFiles())
				.filter(file -> !file.isDirectory())
				.map(file -> querryDirPath+file.getName())
				.collect(Collectors.toSet());

//		System.out.println(fileNameSet);
		for (String path : fileNameSet) {
			try {
				starQueries.addAll(parseSparQLQueries(path));

			} catch (Exception e) {
//				System.err.println("path="+path);
				throw new RuntimeException(e);
			}
		}


		System.out.println("Total Queries parsed: " + starQueries.size());

		return starQueries;
	}
}
