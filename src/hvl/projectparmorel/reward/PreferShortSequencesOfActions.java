package hvl.projectparmorel.reward;

import java.util.List;
import java.util.logging.Logger;

import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;
import hvl.projectparmorel.modelrepair.QModelFixer;
import hvl.projectparmorel.modelrepair.Solution;

class PreferShortSequencesOfActions extends Preference implements SolutionComparingPreference {

	private Logger logger;

	PreferShortSequencesOfActions(int weight) {
		super(weight, PreferenceValue.SHORT_SEQUENCES_OF_ACTIONS);
		logger = Logger.getLogger(QModelFixer.LOGGER_NAME);
	}

	@Override
	public void rewardPostRepair(List<Solution> possibleSolutions, Knowledge knowledge) {
		Solution optimalSequence = null;
		int smallestSequenceSize = 9999;

		for (Solution sequence : possibleSolutions) {
			if (sequence.getSequence().size() < smallestSequenceSize && sequence.getWeight() > 0) {
				smallestSequenceSize = sequence.getSequence().size();
				optimalSequence = sequence;
			} else if (sequence.getSequence().size() == smallestSequenceSize) {
				if (sequence.getWeight() > optimalSequence.getWeight()) {
					optimalSequence = sequence;
				}
			}
		}
		if (optimalSequence != null) {
			optimalSequence.setWeight(optimalSequence.getWeight() + weight);
			rewardSolution(optimalSequence, knowledge);
			logger.info("Rewarded solution " + optimalSequence.getId() + " with a weight of " + weight
					+ " because of preferences to reward shorter sequences.");
		}
	}

	/**
	 * Rewards the specified sequence.
	 * 
	 * @param solution
	 * @param knowledge
	 */
	private void rewardSolution(Solution solution, Knowledge knowledge) {
		QTable qTable = knowledge.getQTable();
		for (AppliedAction appliedAction : solution.getSequence()) {
			int contextId = appliedAction.getAction().getHierarchy();
			int errorCode = appliedAction.getError().getCode();
			int actionId = appliedAction.getAction().getCode();
			double oldWeight = qTable.getWeight(errorCode, contextId, actionId);

			qTable.setWeight(errorCode, contextId, actionId, oldWeight + 300);
			if (qTable.getTagDictionaryForAction(errorCode, contextId, actionId).contains(value.id)) {
				int oldTagValue = qTable.getTagDictionaryForAction(errorCode, contextId, actionId)
						.getWeightFor(value.id);
				qTable.setTagValueInTagDictionary(errorCode, contextId, actionId, value.id, oldTagValue + 500);
			} else {
				qTable.setTagValueInTagDictionary(errorCode, contextId, actionId, value.id, 500);
			}
			qTable.updateReward(appliedAction, contextId);
		}
	}

	@Override
	int rewardActionForError(Model model, Error error, Action action) {
		return 0;
	}

}
