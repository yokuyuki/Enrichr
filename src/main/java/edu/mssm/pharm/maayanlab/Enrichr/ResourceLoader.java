/**
 * Parses XML describing the structure of the gene set libraries.
 * 
 * @author		Edward Y. Chen
 * @since		4/30/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.Gson;

public class ResourceLoader {

	private static final ResourceLoader instance = new ResourceLoader(); // Singleton
	private EnrichmentCategory[] categories;	// Data structure storing java representation of the XML

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
		} catch (JAXBException e) {
			e.printStackTrace();
		}
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
		private GeneSetLibrary[] libraries;

		public String getName() {
			return name;
		}

		public GeneSetLibrary[] getLibraries() {
			return libraries;
		}
	}

	@XmlRootElement(name = "library")
	public static class GeneSetLibrary {

		@XmlAttribute
		private String name;

		@XmlAttribute
		private String format;

		@XmlAttribute
		private boolean hasGrid;

		public String getName() {
			return name;
		}

		public String getFormat() {
			return format;
		}

		public boolean hasGrid() {
			return hasGrid;
		}
	}

}
