package hvl.projectparmorel.reward;

import java.util.ArrayList;
import java.util.List;

import hvl.projectparmorel.modelrepair.Preferences;
import hvl.projectparmorel.modelrepair.Solution;
import hvl.projectparmorel.general.Action;
import hvl.projectparmorel.general.AppliedAction;
import hvl.projectparmorel.general.Error;
import hvl.projectparmorel.general.Model;
import hvl.projectparmorel.knowledge.Knowledge;
import hvl.projectparmorel.knowledge.QTable;

public class RewardCalculator {
	private Knowledge knowledge;
	private List<Preference> preferences;
	private List<PreferenceOption> preferenceOptions;

	public RewardCalculator(Knowledge knowledge, List<PreferenceOption> preferences) {
		this.knowledge = knowledge;
		this.preferenceOptions = preferences;
		this.preferences = initializeFrom(preferences);
	}

	private List<Preference> initializeFrom(List<PreferenceOption> preferences) {
		Preferences filePreferences = new Preferences();
		
		List<Preference> prefs = new ArrayList<>();
		for (PreferenceOption preference : preferences) {
			switch (preference) {
			case SHORT_SEQUENCES_OF_ACTIONS:
				prefs.add(new PreferShortSequencesOfActions(filePreferences.getWeightRewardShorterSequencesOfActions()));
				break;
			case LONG_SEQUENCES_OF_ACTIONS:
				prefs.add(new PreferLongSequencesOfActions(filePreferences.getWeightRewardLongerSequencesOfActions()));
				break;
			case PUNISH_DELETION:
				prefs.add(new PunishDeletionPreference(filePreferences.getWeightPunishDeletion()));
				break;
			case REPAIR_HIGH_IN_CONTEXT_HIERARCHY:
				prefs.add(new PreferRepairingHighInContextHierarchyPreference(filePreferences.getWeightRewardRepairingHighInErrorHierarchies()));
				break;
			case REPAIR_LOW_IN_CONTEXT_HIERARCHY:
				prefs.add(new PreferRepairingLowInContextHierarchyPreference(filePreferences.getWeightRewardRepairingLowInErrorHierarchies()));
				break;
			case PUNISH_MODIFICATION_OF_MODEL:
				prefs.add(new PunishModificationOfModelPreference(filePreferences.getWeightPunishModificationOfTheOriginalModel()));
				break;
			case REWARD_MODIFICATION_OF_MODEL:
				prefs.add(new RewardModificationOfModelPreference(filePreferences.getWeightRewardModificationOfTheOriginalModel()));
				break;
			default:
				throw new UnsupportedOperationException("This operation is not yet implemented.");
			}
		}
		filePreferences.saveToFile();
		return prefs;
	}

	/**
	 * Some preferences compare aspects of the model pre and post applying an
	 * action. This call allows the preferences to store the required information
	 * before choosing action.
	 * 
	 * @param model
	 */
	public void initializePreferencesBeforeChoosingAction(Model model) {
		for (Preference preference : preferences) {
			if (preference instanceof ResultBasedPreference) {
				ResultBasedPreference pref = (ResultBasedPreference) preference;
				pref.initializeBeforeApplyingAction(model);
			}
		}
	}

	/**
	 * Calculates the reward based on the result from applying the specified action
	 * to the specified error to fix.
	 * 
	 * @param currentErrorToFix
	 * @param action
	 * @return the reward
	 */
	public int calculateRewardFor(Model model, Error currentErrorToFix, Action action) {
		int reward = 0;

		int contextId = action.getHierarchy();
		for (Preference preference : preferences) {
			int rewardFromPreference = preference.rewardActionForError(model, currentErrorToFix, action);
			if (rewardFromPreference != 0) {
				addTagMap(currentErrorToFix, contextId, action, preference.getPreferenceOption().id,
						rewardFromPreference);
			}
			reward += rewardFromPreference;
		}
		
		

		if (!preferenceOptions.contains(PreferenceOption.REPAIR_HIGH_IN_CONTEXT_HIERARCHY) && !preferenceOptions.contains(PreferenceOption.REPAIR_LOW_IN_CONTEXT_HIERARCHY) && !preferenceOptions.contains(PreferenceOption.PUNISH_DELETION)) {
			reward += 30;
		}

		return reward;
	}

	/**
	 * Sets the tag map for the error, context and action to the specified tagId and
	 * value
	 * 
	 * @param error
	 * @param contextId
	 * @param action
	 * @param tagId
	 * @param value
	 */
	private void addTagMap(Error error, int contextId, Action action, int tagId, int value) {
		QTable qTable = knowledge.getQTable();
		qTable.setTagValueInTagDictionary(error.getCode(), contextId, action.getCode(), tagId, value);
	}

	/**
	 * Calculates rewards that compare the different solutions to each other.
	 * 
	 * @param possibleSolutions
	 */
	public void rewardPostRepair(List<Solution> possibleSolutions) {
		for (Preference preference : preferences) {
			if (preference instanceof PostRepairPreference) {
				PostRepairPreference comparingPreference = (PostRepairPreference) preference;
				comparingPreference.rewardPostRepair(possibleSolutions, knowledge);
			}
		}
	}

	/**
	 * Rewards the specified sequence.
	 * 
	 * @param solution
	 * @param preferenceId
	 */
	public void rewardSolution(Solution solution) {
		QTable qTable = knowledge.getQTable();
		for (AppliedAction appliedAction : solution.getSequence()) {
			int contextId = appliedAction.getAction().getHierarchy();
			int errorCode = appliedAction.getError().getCode();
			int actionId = appliedAction.getAction().getCode();
			double oldWeight = qTable.getWeight(errorCode, contextId, actionId);

			qTable.setWeight(errorCode, contextId, actionId, oldWeight + 300);
			qTable.updateReward(appliedAction, contextId);
		}
	}

	/**
	 * Rewards the specified sequence. Saves the knowledge afterwards if the
	 * shouldSave-variable is set to true.
	 * 
	 * @param solution
	 * @param preferenceId
	 * @param shouldSave
	 */
	public void rewardSolution(Solution solution, boolean shouldSave) {
		rewardSolution(solution);
		if (shouldSave) {
			knowledge.save();
		}
	}

	public List<PreferenceOption> getPreferences() {
		return preferenceOptions;
	}

	public void influenceWeightsFromPreferencesBy(double factor) {
		knowledge.influenceWeightsFromPreferencesBy(factor, preferenceOptions);
	}
}
