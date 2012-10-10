package cloverChallenege;

import java.util.LinkedList;

/**
 * 
 * Reflection of an answer object in the input okCupid input JSON
 * Contains:
 * 		- The id of the question
 * 		- The answer of the person
 * 		- The set of answers he accepts from answers to this question
 * 		- The importance representing how important it is to him how the other answers
 * 
 * @author adam
 *
 */
public class Answer {

	final int[] IMPORTANCE_POINTS = new int[]{0, 1, 10, 50, 250};
	
	// Importance code 0
	static final int IMPORTANCE_0 = 0;
	// Importance code 1
	static final int IMPORTANCE_1 = 1;
	// Importance code 2
	static final int IMPORTANCE_2 = 10;
	// Importance code 3
	static final int IMPORTANCE_3 = 50;
	// Importance code 4
	static final int IMPORTANCE_4 = 250;

	public int 					questionId;
	public int 					answerCode;
	public LinkedList<Integer> 	acceptableAnswers;
	public int 					importanceValue;
	
	public Answer(){}
	
	public Answer( int questionId, int answerCode, LinkedList<Integer> acceptableAnswers, int importanceCode ){
		
		this.questionId			= questionId;
		this.answerCode 		= answerCode;
		
		this.acceptableAnswers	= acceptableAnswers;
		try {
			this.setImportance(importanceCode);
		} catch (CannotParseImportanceValueException e) {
			e.printStackTrace();
		}
	}
	
	public void setImportance(int importanceCode) throws CannotParseImportanceValueException{
		
		if( importanceCode == 0 )
			this.importanceValue = IMPORTANCE_0;
		else if( importanceCode == 1 )
			this.importanceValue = IMPORTANCE_1;
		else if( importanceCode == 2 )
			this.importanceValue = IMPORTANCE_2;
		else if( importanceCode == 3 )
			this.importanceValue = IMPORTANCE_3;
		else if( importanceCode == 4 )
			this.importanceValue = IMPORTANCE_4;
		else
			throw new CannotParseImportanceValueException("Importance value cannot be parsed");
		
	}
	

	
	
	public String toString(){
		return "Answer Contains: QuestionID:" + Integer.toString(questionId) + 
				" AnswerCode" + Integer.toString(answerCode) +
				" AcceptedAnswers" + this.acceptableAnswers.toString() +
				" Importance:" + Integer.toString(importanceValue);
	}
	
	public class CannotParseImportanceValueException extends Exception {
	    
		private static final long serialVersionUID = 1L;

		public CannotParseImportanceValueException(String message) {
	        super(message);
	    }
	}

}
