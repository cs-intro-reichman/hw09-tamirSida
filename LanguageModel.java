import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        String window = "";
        In in = new In(fileName);
        // Reads just enough characters to form the first window
        while (window.length() < windowLength && !in.isEmpty()) {
            window += in.readChar();
        }
        
        // Processes the entire text, one character at a time
        while (!in.isEmpty() && window.length() == windowLength) {
            char c = in.isEmpty() ? ' ' : in.readChar(); // Handle file end or read next char
            List probs = CharDataMap.getOrDefault(window, new List());
            probs.update(c);
            CharDataMap.put(window, probs);
            
            // Update window for the next iteration
            window = window.substring(1) + c;
        }
        
        // Compute probabilities after training
        CharDataMap.values().forEach(this::calculateProbabilities);
    }
    

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
    public void calculateProbabilities(List probs) {
        double sum = 0;
        ListIterator listIt = probs.listIterator(0);
        while (listIt.hasNext()) {
            CharData data = listIt.next();
            sum += data.count;
        }
        if (sum == 0) return; // Prevent division by zero
        
        double cumulativeProbability = 0;
        listIt = probs.listIterator(0); // Reset the iterator
        while (listIt.hasNext()) {
            CharData data = listIt.next();
            data.p = data.count / sum;
            cumulativeProbability += data.p;
            data.cp = cumulativeProbability;
        }
    }
    
    
    

    // Returns a random character from the given probabilities list.
    public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble(); // Use the class's Random instance
        ListIterator listIt = probs.listIterator(0);
        while (listIt.hasNext()) {
            CharData data = listIt.next();
            if (data.cp > r) {
                return data.chr;
            }
        }
        return '_'; // Fallback or handle this case as needed
    }
    
    

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
    public String generate(String initialText, int textLength) {
        if (initialText.length() < windowLength) return initialText;
        
        StringBuilder generatedText = new StringBuilder(initialText);
        while (generatedText.length() < textLength) {
            String currentWindow = generatedText.substring(generatedText.length() - windowLength);
            List probs = CharDataMap.get(currentWindow);
            if (probs == null) break;
            
            char nextChar = getRandomChar(probs);
            generatedText.append(nextChar);
        }
        
        return generatedText.toString();
    }
    
    

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
