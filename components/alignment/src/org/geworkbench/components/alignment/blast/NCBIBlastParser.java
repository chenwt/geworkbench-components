package org.geworkbench.components.alignment.blast;

/**
 *
 * @version $Id$
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.sequence.BlastObj;

/**
 * BlastParser is a class that reads in from a text file generated by
 * RemoteBlast of Blast results. It parses out the Blast hits into a Vector of
 * BlastObj objects.
 */
public class NCBIBlastParser {
	Log log = LogFactory.getLog(NCBIBlastParser.class);

	private int totalSequenceNum = 0;
	private String filename;

	/**
	 * Creates a new BlastParser with querySeq and filename set to specified
	 * ProtSeq and String value. Also creates a new hits Vector.
	 * 
	 * @param the
	 *            ProtSeq to set querySeq to.
	 * @param the
	 *            String to set filename to.
	 */
	public NCBIBlastParser(final int totalSequenceNum, final String filename) {
		this.totalSequenceNum = totalSequenceNum;
		this.filename = filename;
	}

	final private static String NEWLINESIGN = "<BR>";
	final private static int HIT_NUMBER_LIMIT = 250;

	private int totalHitCount = 0;
	private boolean hitOverLimit = false;

	/**
	 * Reads in Blast results from file and parses data into BlastObj objects.
	 */
	public ArrayList<Vector<BlastObj>> parseResults() {
		/**
		 * The new BlastDataSet Array
		 */
		ArrayList<Vector<BlastObj>> blastDataset = new ArrayList<Vector<BlastObj>>(
				10);

		totalHitCount = 0;
		StringTokenizer st;
		BlastObj each;
		int index = 0;

		File file = new File(filename);
		// server failure
		if (file.length() < 600) {
			log.warn("No hit found. try again.");
			return null;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			int count = 0;
			do {
				// A Vector of Strings representing each Blast hit by Accession
				// number.
				Vector<BlastObj> hits = new Vector<BlastObj>();
				boolean hitsFound = false;
				// loop to proceed to beginning of hit list from Blast output
				// file
				while (line != null) {

					if (line
							.startsWith("Sequences producing significant alignments:")) {
						hitsFound = true;
						break;
					}
					if (line.contains("No significant similarity found.")) {
						hitsFound = false;
						break;
					}
					line = br.readLine();
				}
				if (!hitsFound) {
					count++;
					line = br.readLine();
					continue;
				}

				/* parsing section of the blast Hit info text */
				line = br.readLine();
				line = br.readLine();
				int hitCount = 0;
				while (line != null && !line.trim().startsWith("</pre>")) {
					hitCount++;
					totalHitCount++;
					if (hitCount > HIT_NUMBER_LIMIT) {
						hitOverLimit = true;
						line = br.readLine();
						continue; // skip further parsing the summary section
					}
					
					Pattern p = Pattern.compile(".+?href=\"(http:.+?)\".+"); // first URL
					Matcher m = p.matcher(line);
					String firstUrl = null;
					if (m.matches()) {
						firstUrl = m.group(1);
					}

					String id = null;
					String name = null;
					String description = null;
					String score = null;
					String evalue = null;
					
					String[] tagSeparated = line.split("\\<(/?[^\\>]+)\\>"); // separated by HTML tag
					if(tagSeparated.length==5) { // for most databases
						String[] fields = tagSeparated[1].split("\\|");
						id = fields[0];
						name = fields[1]; // next field is fact the second of the name, ignored in the previous/current behavior
						description = tagSeparated[2].trim();
						score = tagSeparated[3].trim();
						evalue = tagSeparated[4].trim();
						String[] tokens=evalue.split("\\s");
						evalue=tokens[0];
					} else if(tagSeparated.length==3) { // for database alu (without HTML links)
						String[] fields = tagSeparated[0].split("\\|");
						id = fields[0];
						int firstSpace = fields[2].indexOf(" ");
						name = fields[1]+":"+fields[2].substring(0, firstSpace);
						description = fields[2].trim().substring(firstSpace);
						score = tagSeparated[1].trim();
						evalue = tagSeparated[2].trim();
					} else {
						log.error("unexcepted HTML tag count " + tagSeparated.length);
						line = br.readLine();
						continue;
					}

					each = new BlastObj(true, id, name, description, score,
							evalue); // create new BlastObj for hit

					try {
						each.setInfoURL(new URL(firstUrl));
						String s = firstUrl.replaceAll("GenPept", "fasta");
						s = s.replaceAll("GenBank", "fasta");
						each.setSeqURL(new URL(s));
					} catch (MalformedURLException e) {
						// ignore if URL is valid, e.g. null or the reason
					}

					hits.add(each);

					line = br.readLine();
				} // end of processing summary.

				index = 0;

				boolean endofResult = false;
				final String ALU_DETAIL_LEADING = "<pre><script src=\"blastResult.js\"></script>>";
				while (line != null) {
					line = line.trim();
					if (line.startsWith("Database") || line.startsWith(">")
							|| line.startsWith(ALU_DETAIL_LEADING)) {
						break;
					}
					line = br.readLine();
				}

				if (line.startsWith(ALU_DETAIL_LEADING)) {
					line = line.substring(ALU_DETAIL_LEADING.length()-1);
				}

				/* parsing detailed alignments Each has <PRE></PRE> */
				while (line != null && line.trim().startsWith(">")
						|| line.trim().startsWith("Database")) {

					if (line.trim().startsWith("Database")) {
						endofResult = true;
						break;
					}

					StringBuffer detaillines = new StringBuffer("<PRE>").append(line);
					line = br.readLine().trim();

					boolean additionalDetail = false;
					if (line.trim().startsWith("Score")) {
						index--;
						additionalDetail = true;

					}
					// get BlastObj hit for which alignment is for
					each = hits.get(index);
					// skip the beginning description
					boolean getStartPoint = true;
					StringBuffer subject = new StringBuffer();
					int endPoint = 0;
					while (!(line.trim().startsWith(">"))) {

						if (line.startsWith("</form>")) {
							// end of the useful information for one blast.
							endofResult = true;
							break;
						}

						if (line.startsWith("Length=")) {
							String[] lengthVal = line.split("=");
							each.setLength(new Integer(lengthVal[1].trim())
									.intValue());
						}
						if (line.startsWith("Identities = ")) {
							/*
							 * TODO use Match pattern later.
							 */
							String alignmentLengthString = line.substring("Identities = ".length(), line.indexOf("/"));
							int alignmentLength = Integer.parseInt(alignmentLengthString);
							if(0==each.getAlignmentLength())
								each.setAlignmentLength(alignmentLength);
							StringTokenizer st1 = new StringTokenizer(line, "(");
							st1.nextToken();
							String identity = st1.nextToken();
							String[] s = identity.split("%");
							if(0==each.getPercentAligned())
								each
									.setPercentAligned(new Integer(s[0])
											.intValue());

						}
						// get the start point, end point and length

						if (line.trim().startsWith("Sbjct")) {
							st = new StringTokenizer(line);
							st.nextToken();
							if (getStartPoint) {
								if(0==each.getStartPoint())
									each.setStartPoint(Integer.valueOf(
										st.nextToken()).intValue());
								getStartPoint = false;
							} else {
								st.nextToken();
							}
							// concat the aligned parts and get rid of "-"
							subject = subject.append(st.nextToken().replaceAll(
									"-", ""));

							endPoint = Integer.valueOf(st.nextToken())
									.intValue();
						}

						String s = br.readLine();
						line = s.trim();
						if (!line.startsWith(">")) {
							detaillines.append(s).append(NEWLINESIGN);
						}
					}
					each.setEndPoint(endPoint);
//					each.setAlignmentLength(Math.abs(each.getStartPoint()
//							- each.getEndPoint()) + 1);
					each.setSubject(subject.toString());

					detaillines.append("</PRE>");

					if (additionalDetail) {
						String previousDetail = each.getDetailedAlignment();
						detaillines =  detaillines.insert(0, previousDetail);
					}
					each.setDetailedAlignment(detaillines.toString());

					index++;
					if (endofResult || index>=hits.size()) {
						endofResult = false;
						break;
					}

				}
				line = br.readLine();

				blastDataset.add(hits);
				count++;
			} while (count < totalSequenceNum);
			return blastDataset;
		} catch (FileNotFoundException e) {
			log.error("file "+filename+"not found.");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * getSummary
	 * 
	 * @return String
	 */
	public String getSummary() {
		if (hitOverLimit) {
			return "Some sequences have more than 250 hits, only the first "
					+ HIT_NUMBER_LIMIT + " hits are displayed. Total hits: "
					+ totalHitCount + ".";
		}
		return "Total hits for all sequences: " + totalHitCount + ".";
	}
	
	public int getHitCount(){
		return totalHitCount;
	}

}
