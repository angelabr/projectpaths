package hvl.projectparmorel.knowledge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hvl.projectparmorel.general.Action;

class ActionMapTest {
	
	private ActionMap actionMap;
	
	private Action action1;
	private Action action2;
	private Action action3;
	private Action action4;
	private Action action5;
	
	@BeforeEach
	public void setUp() {
		actionMap = new ActionMap();
		action1 = new Action();
		action2 = new Action();
		action3 = new Action();
		action4 = new Action();
		action5 = new Action();
		action1.setId(1);
		action2.setId(2);
		action3.setId(3);
		action4.setId(4);
		action5.setId(5);
		actionMap.addAction(action1);
		actionMap.addAction(action2);
		actionMap.addAction(action3);
		actionMap.addAction(action4);
		actionMap.addAction(action5);
	}

	@Test
	void actionWithHighestWeightIsFoundWhenLookingForTheBestOption() {
		action1.setWeight(100);
		action2.setWeight(230);
		action3.setWeight(901);
		action4.setWeight(0);
		action5.setWeight(-1000);
		assertEquals(3, actionMap.getBestActionKey());
	}
}
