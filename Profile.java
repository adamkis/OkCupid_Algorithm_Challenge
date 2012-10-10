package cloverChallenege;

import java.util.HashMap;

/**
 * Reflection of an profile object in the input okCupid input JSON
 * 
 * Contains a HashMap of answers in the format of: QuestionId -> Answer
 * and the id of the person's profile
 * @author adam
 *
 */
public class Profile extends HashMap<Integer, Answer> {


	public int profileId;

	private static final long serialVersionUID = 1L;


	
	
}
