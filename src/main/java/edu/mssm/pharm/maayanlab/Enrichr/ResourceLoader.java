/**
 * Parses XML describing the structure of the gene set libraries.
 * 
 * @author		Edward Y. Chen
 * @since		4/30/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.Gson;

import edu.mssm.pharm.maayanlab.common.bio.FuzzyGeneSetLibrary;
import edu.mssm.pharm.maayanlab.common.bio.GeneSetLibrary;
import edu.mssm.pharm.maayanlab.common.core.FileUtils;

public class ResourceLoader {

	private static final ResourceLoader instance = new ResourceLoader(); // Singleton
	private EnrichmentCategory[] categories;	// Data structure storing java representation of the XML
	private HashMap<String, GeneSetLibrary> geneSetLibraries = new HashMap<String, GeneSetLibrary>();

	public static void main(String args[]) {
		ResourceLoader loader = ResourceLoader.getInstance();
		System.out.println(new Gson().toJson(loader.getCategories()));
	}

	public static ResourceLoader getInstance() {
		return instance;
	}

	private ResourceLoader() {
		InputStream is = ResourceLoader.class.getClassLoader()
				.getResourceAsStream("gene_set_libraries.xml");
		try {
			// Use JAXB to parse XML
			JAXBContext jaxbContext = JAXBContext.newInstance(EnrichmentCategories.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			EnrichmentCategories root = (EnrichmentCategories) jaxbUnmarshaller.unmarshal(is);
			categories = root.getCategories();
			loadLibraries();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private void loadLibraries() {
		for (EnrichmentCategory category : categories) {
			for (EnrichmentLibrary library : category.getLibraries()) {
				// TODO: parallelize loading using new threads
				String libraryName = library.getName();
				Collection<String> backgroundLines = FileUtils.readResource(libraryName + ".gmt");				
				
				GeneSetLibrary newLibrary;
				if (FileUtils.resourceExists(libraryName + "_ranks.txt")) {
					Collection<String> rankLines = FileUtils.readResource(libraryName + "_ranks.txt");
					if (library.isFuzzy()) {
						newLibrary = new FuzzyGeneSetLibrary(backgroundLines, rankLines);
					}
					else {
						newLibrary = new GeneSetLibrary(backgroundLines, rankLines);
					}					
				}
				else {
					if (library.isFuzzy()) {
						newLibrary = new FuzzyGeneSetLibrary(backgroundLines);
					}
					else {
						newLibrary = new GeneSetLibrary(backgroundLines);
					}
				}
				geneSetLibraries.put(libraryName, newLibrary);
			}
		}
	}
	
	public GeneSetLibrary getLibrary(String libraryName) {
		return geneSetLibraries.get(libraryName);
	}

	public EnrichmentCategory[] getCategories() {
		return categories;
	}

	@XmlRootElement(name = "categories")
	private static class EnrichmentCategories {

		@XmlElement(name = "category")
		private EnrichmentCategory[] categories;

		public EnrichmentCategory[] getCategories() {
			return categories;
		}
	}

	@XmlRootElement(name = "category")
	public static class EnrichmentCategory {

		@XmlAttribute
		private String name;

		@XmlElement(name = "library")
		private EnrichmentLibrary[] libraries;

		public String getName() {
			return name;
		}

		public EnrichmentLibrary[] getLibraries() {
			return libraries;
		}
	}

	@XmlRootElement(name = "library")
	public static class EnrichmentLibrary {

		@XmlAttribute
		private String name;

		@XmlAttribute
		private String format;

		@XmlAttribute
		private boolean hasGrid;
		
		@XmlAttribute
		private boolean isFuzzy;

		public String getName() {
			return name;
		}

		public String getFormat() {
			return format;
		}

		public boolean hasGrid() {
			return hasGrid;
		}
		
		public boolean isFuzzy() {
			return isFuzzy;
		}
	}

}
