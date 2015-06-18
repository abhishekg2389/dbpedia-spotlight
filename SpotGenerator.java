package dbpedia;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 
 * @author abhishek
 * 
 */
public class SpotGenerator {
	
	private static Set<String> stopwords = Stopwords.stopwords;
	private static Morpha morphologer = new Morpha(System.in);
	
	public SpotGenerator(Set<String> stopwords) {
		SpotGenerator.stopwords = stopwords;
	}
	
	public static String normalizeSF(String sf){
		// Removing punctuations with space and taking care of multiple spaces
		sf = sf.replaceAll("\\W|_|[0-9]", " ");
		sf = sf.replaceAll(" +", " ");
				
		// Removing stopwords 
		List<String> tokens = new ArrayList<String>();
		for (String token:sf.split(" ")){
			if (!stopwords.contains(token))
				tokens.add(token);
		}
		
		// Converting plural to singular
		for (int i=0;i<tokens.size();i++){
			String singular = stem(tokens.get(i));
			tokens.set(i, singular);
		}
		
		StringBuilder sb = new StringBuilder();
		for (String s:tokens){
			sb.append(s);
			sb.append(" ");
		}
		
		return sb.toString().trim();
	}
	
	public static String[] generate(String sf){
		
		String normalizedSF = normalizeSF(sf);
		Set<String> newSFs = new HashSet<String>();
		// sf retained
		newSFs.add(normalizedSF);
		
		// SF Tokens
		String[] tokens = normalizedSF.split(" ");
		StringBuilder sb = null;
		for (int i=0;i<tokens.length;i++){
			sb = new StringBuilder();
			sb.append( tokens[i].substring(0,1) );
		    sb.append( tokens[i].substring(1).toLowerCase() );
		    tokens[i] = sb.toString();
		}
		
		// Initials
		char[] initials = new char[tokens.length];
		for (int i=0;i<tokens.length;i++)
			initials[i] = Character.toUpperCase(tokens[i].charAt(0));
		
		// Considering 3 different cases as spots of length 
		// 2 and 3 are more frequent, hence generating all probable spots
		if (tokens.length==2){
			newSFs.addAll(biTokenSpots(tokens[0], tokens[1]));
		}
		
		if (tokens.length==3){
			newSFs.add(tokens[0]);											// Michael
			newSFs.add(tokens[1]);											// Jeffery
			newSFs.add(tokens[2]);											// Jordan
			newSFs.addAll(biTokenSpots(tokens[0], tokens[2]));				
			newSFs.add(tokens[0]+" "+tokens[1]+" "+initials[2]+".");		// Michael Jeffery J.
			newSFs.add(tokens[0]+" "+tokens[1]+" "+initials[2]);			// Michael Jeffery J
			newSFs.add(tokens[0]+" "+initials[1]+". "+tokens[2]);			// Michael J. Jordan
			newSFs.add(tokens[0]+" "+initials[1]+" "+tokens[2]);			// Michael J Jordan
			newSFs.add(initials[0]+". "+tokens[1]+" "+tokens[2]);			// M. Jeffery Jordan
			newSFs.add(initials[0]+" "+tokens[1]+" "+tokens[2]);			// M Jeffery Jordan
			newSFs.add(initials[0]+"."+initials[1]+". "+tokens[2]);			// M.J. Jordan
			newSFs.add(initials[0]+""+initials[1]+" "+tokens[2]);			// MJ Jordan
			newSFs.add(initials[0]+"."+initials[1]+"."+initials[2]+".");	// M.J.J.
			newSFs.add(initials[0]+""+initials[1]+""+initials[2]);			// MJJ
		}
		
		if (tokens.length>3){
			String newSF = "";
			for (char c:initials){
				newSF += c+".";
			}
			newSF = newSF.trim();
			newSFs.add(newSF);													// S.W.A.T.
			newSFs.add(newSF.replaceAll(".", ""));								// UNESCO
			newSFs.addAll(biTokenSpots(tokens[0], tokens[tokens.length-1]));
		}
		
		for (String newSF:newSFs.toArray(new String[newSFs.size()]))
			newSFs.add("The " + newSF);
		
		// Adding lowercase surfaceforms
		for (String newSF:newSFs.toArray(new String[newSFs.size()]))
			newSFs.add(newSF.toLowerCase());
		
		return newSFs.toArray(new String[newSFs.size()]);
	}
	
	private static Set<String> biTokenSpots(String firstName, String lastName){
		char firstNameIni = Character.toUpperCase(firstName.charAt(0));
		char lastNameIni = Character.toUpperCase(lastName.charAt(0));
		Set<String> newSFs = new HashSet<String>();
		newSFs.add(firstName);								// Michael
		newSFs.add(lastName);								// Jackson
		newSFs.add(firstName+" "+lastNameIni+".");			// Michael J.
		newSFs.add(firstName+" "+lastNameIni);				// Michael J
		newSFs.add(firstNameIni+". "+lastName);				// M. Jackson
		newSFs.add(firstNameIni+" "+lastName);				// M Jackson
		newSFs.add(firstNameIni+"."+lastNameIni+".");		// M.J.
		newSFs.add(firstNameIni+". "+lastNameIni+".");		// M. J.
		newSFs.add(firstNameIni+""+lastNameIni);			// MJ
		
		return newSFs;
	}
	
	private static String stem(String word) {
	    try {
	      morphologer.yyreset(new StringReader(word));
	      morphologer.yybegin(Morpha.any);
	      String wordRes = morphologer.next();
	      return wordRes;
	    } catch (IOException e) {
	      System.err.println("SpotGenerator.stem() had error on word " + word);
	      return word;
	    }
	  }
	
	public static void main(String[] args){
		String [] newSpots = generate("Americans with  Disabilities_Act_of_1990");		// original "Americans_with_Disabilities_Act_of_1990"
		// Output //
		// ad act
		// a.d. act
		// A.D. act
		// the ada
		// disability
		// american disability A
		// the act
		// american
		// The disability
		// the american disability a.
		// The ADA
		// A act
		// act
		// The american D. act
		// american d act
		// A.D.A.
		// american A.
		// a.d.a.
		// the a disability act
		// A disability act
		// ada
		// AA
		// A. disability act
		// aa
		// The american disability A.
		// a disability act
		// The A.D.A.
		// The A act
		// The american A.
		// A. A.
		// The A. act
		// the disability
		// AD act
		// the a.a.
		// american disability A.
		// a act
		// american D act
		// a.a.
		// the a act
		// the american a.
		// the american d act
		// the a. act
		// The american A
		// The american D act
		// american a
		// a. act
		// A.A.
		// american disability act
		// the a. a.
		// The american disability A
		// american disability a.
		// the a. disability act
		// The A disability act
		// the american disability act
		// The american disability act
		// the a.d.a.
		// american A
		// The act
		// the a.d. act
		// the american disability a
		// american disability a
		// The american
		// The A.A.
		// a. a.
		// the ad act
		// The AA
		// the american d. act
		// the aa
		// the american
		// american D. act
		// The A. disability act
		// american a.
		// the american a
		// The A. A.
		// The AD act
		// american d. act
		// A. act
		// a. disability act
		// ADA							// Original Surface Form or Alias Name (as per bold mention on (https://en.wikipedia.org/wiki/Americans_with_Disabilities_Act_of_1990))
		// The A.D. act
	}
}
