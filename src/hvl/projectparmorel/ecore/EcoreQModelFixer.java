package hvl.projectparmorel.ecore;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.modelrepair.QModelFixer;

public class EcoreQModelFixer extends QModelFixer {
	private URI uri;
	private ResourceSet resourceSet;
	
	public EcoreQModelFixer() {
		super();
		unsupportedErrorCodes = new HashSet<>();
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		actionExtractor = new EcoreActionExtractor(knowledge);
		errorExtractor = new EcoreErrorExtractor(unsupportedErrorCodes);
		modelProcessor = new EcoreModelProcessor(knowledge, rewardCalculator, unsupportedErrorCodes);
	
		unsupportedErrorCodes.add(4);
		unsupportedErrorCodes.add(6);
	}
	
	public EcoreQModelFixer(List<Integer> preferences) {
		super(preferences);
		unsupportedErrorCodes = new HashSet<>();
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		actionExtractor = new EcoreActionExtractor(knowledge);
		errorExtractor = new EcoreErrorExtractor(unsupportedErrorCodes);
		modelProcessor = new EcoreModelProcessor(knowledge, rewardCalculator, unsupportedErrorCodes);

		unsupportedErrorCodes.add(4);
		unsupportedErrorCodes.add(6);
	}

	@Override
	protected Model initializeModelCopyFromFile() {
		File duplicateFile = createDuplicateFile();
		this.uri = URI.createFileURI(duplicateFile.getAbsolutePath());
		Resource modelResource = getModel(uri);
		
		return new EcoreModel(resourceSet, modelResource, uri);
	}

	private Resource getModel(URI uri) {
		return resourceSet.getResource(uri, true);
	}

	@Override
	protected Model getModel(File model) {
		URI episodeModelUri = URI.createFileURI(model.getAbsolutePath());
		Resource episodeModelResource = getModel(episodeModelUri);
		
		return new EcoreModel(resourceSet, episodeModelResource, uri);
	}

	@Override
	protected void updateRewardCalculator() {
		modelProcessor = new EcoreModelProcessor(knowledge, rewardCalculator, unsupportedErrorCodes);
	}
}