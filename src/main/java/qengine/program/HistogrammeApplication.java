package qengine.program;

import fr.boreal.model.logicalElements.api.Substitution;
import fr.boreal.model.query.api.Query;
import fr.boreal.storage.natives.SimpleInMemoryGraphStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import qengine.model.RDFAtom;
import qengine.model.StarQuery;
import qengine.parser.RDFAtomParser;
import qengine.parser.StarQuerySparQLParser;
import qengine.storage.RDFHexaStore;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HistogrammeApplication {

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

		for (RDFAtom atom : rdfAtoms) {
			store.add(atom); // Stocker chaque RDFAtom dans le store
		}


		HashMap<String, Integer> map = new HashMap<>();
		HashMap<Integer, List<StarQuery>> goodMap = new HashMap<>();
		int nb = 0;

		long start = System.nanoTime();
		int i = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb_with_indice = new StringBuilder();
		for (StarQuery starQuery : starQueries) {
			sb.append(i);
			sb.append(" ");
			Iterator<Substitution> iterator = store.match(starQuery);
			ArrayList<Substitution> subList = new ArrayList<>();
			iterator.forEachRemaining(subList::add);
			sb.append(subList.size());
			int nKey = subList.size()/10;
			sb.append(" "+nKey);
			map.computeIfAbsent(""+nKey, s -> 0);
			map.put(""+nKey, map.get(""+nKey)+1);
			sb.append("\n");
			if (subList.isEmpty()) {
				nb++;
			}

			goodMap.computeIfAbsent(subList.size(), k -> new ArrayList<>());
			goodMap.get(subList.size()).add(starQuery);

			i++;
		}
//		System.out.println(sb);
		goodMap.remove(0);


		long finish = System.nanoTime();
		System.currentTimeMillis();
		long timeElapsed = finish - start;
		double d = timeElapsed/1000000000d;
//		System.out.println("t="+d);
		DecimalFormat df = new DecimalFormat("#.###");
		String arrondi = df.format(d);
		System.out.println(arrondi);

		System.out.println("\n\n"+nb+" requêtes sans réponses");

		HashMap<Integer, List<StarQuery>> readyToMatchMapBonneClass = new HashMap<>();
		int nbParClasses = 30;
//		int tailleClass = 5;
		for (Integer nbReponse : goodMap.keySet().stream().sorted().filter(x -> !x.equals(0)).toList()) {
			int res = nbReponse/10;
			readyToMatchMapBonneClass.computeIfAbsent(res, k -> new ArrayList<>());
			readyToMatchMapBonneClass.get(res).addAll(goodMap.get(nbReponse));

		}
		for (Integer nbReponse : readyToMatchMapBonneClass.keySet().stream().sorted().toList()) {
//			System.out.println(nbReponse+" "+readyToMatchMapBonneClass.get(nbReponse).size());
			readyToMatchMapBonneClass.get(nbReponse);
			Collections.shuffle(readyToMatchMapBonneClass.get(nbReponse));
			List<StarQuery> list = readyToMatchMapBonneClass.get(nbReponse).stream().limit(nbParClasses).toList();

			readyToMatchMapBonneClass.put(nbReponse, list);
		}

		//System.out.println(readyToMatchMap.keySet().stream().map(x -> readyToMatchMap.get(x).size()).mapToInt(Integer::intValue).sum()+" querries after filter");

		System.out.println("class #Requete "+nbParClasses+" par classe");
		List<StarQuery> allQueryFiltered = new ArrayList<>();
		for (Integer nbReponse : readyToMatchMapBonneClass.keySet().stream().sorted().toList()) {
			allQueryFiltered.addAll(readyToMatchMapBonneClass.get(nbReponse));
			System.out.println(nbReponse+" "+readyToMatchMapBonneClass.get(nbReponse).size());
		}
		Collections.shuffle(allQueryFiltered);
		for (StarQuery starQuery : allQueryFiltered) {
			System.out.println(starQuery.getLabel());
		}
		System.out.println(allQueryFiltered.size()+" queries in total after filtration into classes");

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



//		PrintStream originalOut = System.out;

		// Création d'un ByteArrayOutputStream pour capturer les données
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		PrintStream newOut = new PrintStream(baos);

		// Redirige System.out vers notre PrintStream
//		System.setOut(newOut);

		// Code dont la sortie doit être capturée
//		System.out.println("Ceci est capturé dans le flux !");




		try (RDFAtomParser rdfAtomParser = new RDFAtomParser(rdfFile, RDFFormat.NTRIPLES)) {
			int count = 0;
			while (rdfAtomParser.hasNext()) {
				RDFAtom atom = rdfAtomParser.next();
				rdfAtoms.add(atom);  // Stocker l'atome dans la collection
//				System.out.println("RDF Atom #" + (++count) + ": " + atom);
			}
//			System.out.println("Total RDF Atoms parsed: " + count);
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
				System.err.println("path="+path);
				throw new RuntimeException(e);
			}
		}


//		System.out.println("Total Queries parsed: " + starQueries.size());

		return starQueries;
	}
}
