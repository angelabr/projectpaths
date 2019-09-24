package hvl.projectparmorel.knowledge;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class ActionMap {
	private final String XML_NODE_NAME = "action";
	private final String XML_ID_NAME = "id";
	/**
	 * A map containing the actions for the given context.
	 */
	private Map<Integer, Action> actions;

	protected ActionMap() {
		actions = new HashMap<>();
	}

	protected ActionMap(Integer actionId, Action action) {
		this();
		actions.put(actionId, action);
	}

	protected ActionMap(Element context) throws IOException {
		this();
		NodeList actionList = context.getElementsByTagName(XML_NODE_NAME);
		for(int i = 0; i < actionList.getLength(); i++) {
			Node actionNode = actionList.item(i);
			if(actionNode.getNodeType() == Node.ELEMENT_NODE) {
				Element actionElement = (Element) actionNode;
				Integer actionId = Integer.parseInt(actionElement.getAttribute(XML_ID_NAME));
				Action action = new Action(actionElement);
				actions.put(actionId, action);
			} else {
				throw new IOException("Could not instantiate action map from node " + actionNode.getNodeName());
			}
		}
	}

	/**
	 * Checks that the action map contains a given action id.
	 * 
	 * @param actionId
	 * @return true if the action ID exists, false otherwise.
	 */
	protected boolean containsValue(int actionId) {
		return actions.containsKey(actionId);
	}

	/**
	 * Gets the key for optimal action
	 * 
	 * @return the highest value in the action map. If two are equal, one of them is
	 *         returned. If the set is empty, null is returned.
	 */
	protected Integer getBestActionKey() {
		Set<Integer> actionIdSet = actions.keySet();
		Integer[] actionIds = new Integer[actionIdSet.size()];
		actionIds = actionIdSet.toArray(actionIds);
		if (actionIds.length > 0) {
			Integer optimalActionId = actionIds[0];

			for (int i = 1; i < actionIds.length; i++) {
				Action optimalAction = actions.get(optimalActionId);
				Action action = actions.get(actionIds[i]);
				if (action.compareTo(optimalAction) > 0) {
					optimalActionId = actionIds[i];
				}
			}
			return optimalActionId;
		}
		return null;
	}

	/**
	 * Gets a random action
	 * 
	 * @return a random action
	 */
	protected Action getRandomAction() {
		Random randomGenerator = new Random();
		Integer[] actionIds = new Integer[actions.keySet().size()];
		actionIds = actions.keySet().toArray(actionIds);
		int randomActionIndex = randomGenerator.nextInt(actionIds.length);
		return actions.get(actionIds[randomActionIndex]);
	}
	
	/**
	 * Sets the action for the specified action id. If the action is not in the
	 * hierarchy, it will be added.
	 * 
	 * @param actionId
	 * @param action
	 */
	protected void setAction(Integer actionId, Action action) {
		actions.put(actionId, action);
	}
	
	/**
	 * Gets the value for the specified action id
	 * 
	 * @param actionId
	 * @return the action for the specified action
	 */
	protected Action getAction(Integer actionId) {
		return actions.get(actionId);
	}

	/**
	 * Saves content to the document under the context element
	 * 
	 * @param document
	 * @param context
	 */
	protected void saveTo(Document document, Element context) {
		for(Integer key : actions.keySet()) {
            Element action = document.createElement(XML_NODE_NAME);
            
            Attr contextId = document.createAttribute(XML_ID_NAME);
            contextId.setValue("" + key);
            action.setAttributeNode(contextId);
            
            actions.get(key).saveTo(document, action);
            context.appendChild(action);
		}
	}

	/**
	 * Sets all the weights to zero.
	 */
	protected void clearWeights() {
		for(Action action : actions.values()) {
			action.setWeight(0);
		}
	}

	/**
	 * Influences the action weights from the preferences and previous learning by the specified factor.
	 * 
	 * @param factor
	 * @param preferences 
	 */
	protected void influenceWeightsFromPreferencesBy(double factor, List<Integer> preferences) {
		for(Action action : actions.values()) {
			action.influenceWeightFromPreferencesBy(factor, preferences);
		}
	}
}
