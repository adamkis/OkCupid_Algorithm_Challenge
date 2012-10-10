package cloverChallenege;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of OkCupid's matching algorithm. Given a JSON input it calculates how much the
 * people match each other.
 * 
 * The input JSON contains an array of profiles. A profile represents a person, who answers questions
 * and defines how he wants others to answer that. Besides that, he defines how important is it for him.
 * A sample of input is at the end of this class in comment.
 * 
 * The output JSON represents each profiles scores towards any other profile
 * 
 * @author adam
 *
 */
public class OkCupid {
	

	/**
	 * Given two profile's answers, calculates the matching score of the two people
	 * The algorithm is descibed here: http://www.okcupid.com/help/match-percentages 
	 * @param myAnswers - One of the two profile's set of answers which we want to use for calculating
	 * 						the matching score to the other profile
	 * @param herAnswers - The other profile's set of answers to calculate the matching score
	 * @return - The match score in float. Basically represents how the two people answered a set of questions
	 * 				and how they expected others to answer them
	 */
	private float calculateScore(Profile myAnswers, Profile herAnswers){
		
		int totalAnswerPointsForMe = 0;
		int rightAnswerPointsForMe = 0;
		for( int questionId : myAnswers.keySet() ){

			Answer herAnswer	= herAnswers.get(questionId);
			Answer myAnswer		= myAnswers.get(questionId);

			totalAnswerPointsForMe += myAnswer.importanceValue;
			if ( herAnswer != null )
				if( myAnswer.acceptableAnswers.contains(herAnswer.answerCode) ){
					rightAnswerPointsForMe += myAnswer.importanceValue;
				}
			
		}
		
		
		int totalAnswerPointsForHer = 0;
		int rightAnswerPointsForHer = 0;
		for( int questionId : herAnswers.keySet() ){

			Answer herAnswer	= herAnswers.get(questionId);
			Answer myAnswer		= myAnswers.get(questionId);
			
			totalAnswerPointsForHer += herAnswer.importanceValue;
			if ( myAnswer != null )
				if( herAnswer.acceptableAnswers.contains(myAnswer.answerCode) ){
					rightAnswerPointsForHer += herAnswer.importanceValue;
				}

		}
		
		return 
				(float)Math.sqrt(
							( (float)rightAnswerPointsForMe  / (float)totalAnswerPointsForMe)
							*
							( (float)rightAnswerPointsForHer / (float)totalAnswerPointsForHer )
						);
	}
	
	/**
	 * Reads input JSON file to and converts it to the easy-to-handle reflection of it.
	 * The reflection is:
	 * 		- An ArrayList of Profiles
	 * 		- A Profile represents a person, consists of its id and a HashMap of Answers
	 * 		- An Answer represents a Question, the persons answer, the expected set of answers and the
	 * 			importance
	 * 
	 * @param inputPath - The path of the input JSON file
	 * @return - The reflection
	 */
	private ArrayList<Profile> convertJSON(String inputPath){
		
		ArrayList<Profile> profiles = new ArrayList<Profile>();
		
		// Good visualizer http://visualizer.json2html.com/
		try {
			
			// Reading input JSON file
			String line;
			String inputJSONtext = "";
			FileReader fr = new FileReader(inputPath);
			BufferedReader br = new BufferedReader(fr);
			System.out.println("Reading file...");
			while ( (line = br.readLine()) != null ){
				inputJSONtext += line;
			}
			
			
			Profile profile = new Profile();
			
			JSONObject inputJSON = new JSONObject(inputJSONtext);
			
			//Goes through profiles in JSON adding them to an ArrayList reflection "profiles"
			JSONArray profilesJSON = inputJSON.getJSONArray("profiles");
			for( int i = 0 ; i< profilesJSON.length() ; i++ ){
				
				profile = new Profile();
				JSONObject profileJSON = profilesJSON.getJSONObject(i);
				profile.profileId = profileJSON.getInt("id");

				//Goes through all answers in the profile
				JSONArray answersJSON =profileJSON.getJSONArray("answers");
				for( int j = 0 ; j< answersJSON.length() ; j++ ){
					
					try {
						JSONObject answerJSON = answersJSON.getJSONObject(j);
						LinkedList<Integer> acceptableAnswerList = new LinkedList<Integer>();
						JSONArray acceptableAnswersJSON =answerJSON.getJSONArray("acceptableAnswers");
						
						for( int k = 0 ; k< acceptableAnswersJSON.length() ; k++ ){
							acceptableAnswerList.add(acceptableAnswersJSON.getInt(k));
						}
						
						Answer answer = new Answer(	answerJSON.getInt("questionId"), 
												answerJSON.getInt("answer"), 
												acceptableAnswerList, 
												answerJSON.getInt("importance")
											);
						profile.put(answer.questionId, answer);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				profiles.add(profile);
			}
			
			fr.close();
			br.close();
			return profiles;
	
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	/**
	 * Calculates the scores from all person to every other.
	 * The output is a HashMap:
	 * 		- The Key is a vector. It's first element is the profileId of the first person, the second
	 * 			is the profileId of the second person
	 * 		- The values is the score in float
	 * 
	 * @param profiles - The ArrayList reflection of profiles(from the input JSON). Originally
	 * 						the 'convertJSON' method produces it
	 * @return - The HashMap of scores
	 */
	private HashMap<Vector<Integer>, Float> generateResult(ArrayList<Profile> profiles){

		HashMap<Vector<Integer>, Float> results = new HashMap<Vector<Integer>, Float>();
		
		for( int i=0 ; i<profiles.size() ; i++ )
			for( int j = 0 ; j<profiles.size() ; j++ ){
				
				if ( i!=j ){
					
					float score = calculateScore(profiles.get(i), profiles.get(j));
					Vector<Integer> keys = new Vector<Integer>();
					keys.add(profiles.get(i).profileId);
					keys.add(profiles.get(j).profileId);
					results.put(keys, score);
				}
			}
		
		return results;
		
	}
	
	/**
	 * Generates the output JSON representing the results. 
	 * Contains profiles representing an originally answering person. A profile contains:
	 * 		- The profileID
	 * 		- All the scores to other answering persons
	 * Prints the result to the console.
	 * 
	 * @param results
	 * @return - The JSON object representing the the results
	 */
	private JSONObject generateJSONresult( HashMap<Vector<Integer>, Float> results ){
		
		JSONObject outputJSON = new JSONObject();
		try {

			JSONArray resultsArrayJSON = new JSONArray();
			outputJSON.put("results", resultsArrayJSON);
			
			// Goes through all ProfileId - ProfileId pairs, insterting the scores
			for ( Vector<Integer> keyPair : results.keySet() ){
				
				JSONObject resultJSON = new JSONObject();
				
				// Goes through the whole JSON array of results, to search if there is already a
				// JSON object for that profile
				boolean addNewProfile = true;	
				for( int i = 0 ; i< resultsArrayJSON.length() ; i++ ){
					
					JSONObject iteratingResultJSON = resultsArrayJSON.getJSONObject(i);
					// The profile is already present in the array of results
					if( iteratingResultJSON.getInt("profileId") == keyPair.get(0) ){
						
						resultJSON = iteratingResultJSON;
						JSONArray scoresArrayJSON = resultJSON.getJSONArray("scores");
						JSONObject score = new JSONObject();
						scoresArrayJSON.put(score);
						
						score.put("profileId", keyPair.get(1));
						score.put("score", (double)results.get(keyPair));
						// Should not add the profile twice
						addNewProfile = false;
					}
					
				}
				// The profile was not in the array of results
				if( addNewProfile ){
					
					resultJSON = new JSONObject();
					resultsArrayJSON.put(resultJSON);
					resultJSON.put("profileId", keyPair.get(0));
					JSONArray scoresArrayJSON = new JSONArray();
					resultJSON.put("scores", scoresArrayJSON);

					JSONObject score = new JSONObject();
					scoresArrayJSON.put(0, score);
					score.put("profileId", keyPair.get(1));
					score.put("score", (double)results.get(keyPair));
					
				}

			}
			// Printing the generated JSON to console
			System.out.println("\n" + outputJSON.toString() );
		
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return outputJSON;
		
	}
	
	
	public static void main(String[] args) {
		
		OkCupid okc = new OkCupid();
		//ArrayList<AnswerMap> allProfileAnswers = okc.convertJSON("input_json.txt");
		ArrayList<Profile> profiles = okc.convertJSON("input2.json");
		//ArrayList<AnswerMap> allProfileAnswers = okc.convertJSON("temp.json");
		
		HashMap<Vector<Integer>, Float> results = okc.generateResult(profiles);
		okc.generateJSONresult(results);
		
//		for( AnswerMap awm : allProfileAnswers ){
//			for( Answer answer : awm.values() ){
//				System.out.println(answer.toString());
//			}
//		}

	

	}

}


//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
////////////// Example of the input JSON
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
/*

{
  "profiles": [
    {
      "id": 0,
      "answers": [
        {
          "questionId": 0,
          "answer": 1,
          "acceptableAnswers": [0,3],
          "importance": 0
        },
        {
          "questionId": 1,
          "answer": 2,
          "acceptableAnswers": [0,2],
          "importance": 2
        }
      ]
    },
    {
      "id": 1,
      "answers": [
        {
          "questionId": 0,
          "answer": 2,
          "acceptableAnswers": [1,2,3],
          "importance": 4
        },
        {
          "questionId": 1,
          "answer": 3,
          "acceptableAnswers": [3],
          "importance": 3
        }
      ]
    }
  ]
}


 */
