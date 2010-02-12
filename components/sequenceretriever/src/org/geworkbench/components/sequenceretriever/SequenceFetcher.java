package org.geworkbench.components.sequenceretriever;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.components.parsers.RMAExpressFileFormat;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.sequences.GeneChromosomeMatcher;

/**
 * <p>Title: </p>
 * <p/>
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author XZ
 * @version 1.0
 */
public class SequenceFetcher {
	static Log log = LogFactory.getLog(RMAExpressFileFormat.class);
    public static final String UCSC = "UCSC";
    private static SequenceFetcher theSequenceFetcher = new SequenceFetcher();
    private final static String chiptyemapfilename = "chiptypeDatabaseMap.txt";
    private static HashMap<String, String> chiptypeMap = new HashMap<String, String>();
    public static final String newline = System.getProperty("line.separator");
    public static String UCSCDATABASEURL = "jdbc:mysql://genome-mysql.cse.ucsc.edu:3306/";
    public static final String EBIURL = "http://www.ebi.ac.uk/ws/services/Dbfetch";
    public static int UPSTREAM = 2000;
    public static int DOWNSTREAM = 2000;

    private static String genomeAssembly = "";

    private static CSSequenceSet<CSSequence> cachedSequences = null;
	private static ArrayList<String> allDBs = new ArrayList<String>();
	private static ArrayList<String> recentDBs = new ArrayList<String>();

	private static ArrayList <String> displayList = new ArrayList<String>();
	private static String defaultChipChoice = "Select a genome";	
	private static Object selectedValue = null;
	
    @SuppressWarnings("unchecked")
	private static void populateSequenceCache() {
        File file = new File( FilePathnameUtils.getTemporaryFilesDirectoryPath() +
                "sequences" + File.separator +
                "cachedSequences" );
        if (cachedSequences == null) {
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    cachedSequences = (CSSequenceSet<CSSequence>) ois.readObject();
                    ois.close();
                    fis.close();
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                }
            } else {
                URL url =  SequenceFetcher.class.getResource(
                        "All.NC.-2k+2k.txt");
                File downloadedFile =
                        new File(FilePathnameUtils.getTemporaryFilesDirectoryPath() +
                                "sequences" + File.separator +
                                "downloadedSequences");
                try {
                    if (!downloadedFile.exists()) {
                        downloadedFile.getParentFile().mkdirs();
                        downloadedFile.createNewFile();
                        url = new URL(System.getProperty("data.download.site") +
                                "All.NC.-2k+2k.txt");
                        BufferedReader br = new BufferedReader(new
                                InputStreamReader(url.openStream()));
                        BufferedWriter bw = new BufferedWriter(new FileWriter(
                                downloadedFile));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            bw.write(line);
                            bw.write("\n");
                        }
                        bw.flush();
                        br.close();
                        bw.close();
                    }
                } catch (MalformedURLException mfe) {
                    mfe.printStackTrace();
                } catch (FileNotFoundException fnfe) {

                } catch (IOException ioe) {

                }
                try {
                    cachedSequences = CSSequenceSet.getSequenceDB(
                            downloadedFile);
                    cachedSequences.parseMarkers();
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file.
                            getAbsolutePath());
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(cachedSequences);
                    oos.flush();
                    oos.close();
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    static CSSequence getCachedPromoterSequence(DSGeneMarker marker,
                                                       int upstream, int fromStart) {
        if (cachedSequences == null) {
            populateSequenceCache();
        }
        if (cachedSequences != null) {
            CSSequence sequence = (CSSequence) cachedSequences.get(marker.
                    getLabel());
            if (sequence != null) {
                return sequence.getSubSequence(UPSTREAM - upstream - 1,
                        sequence.length() - DOWNSTREAM +
                                fromStart - 1);
            }
        }
        return null;
    }

	/*
	 * Get databases from the USCS MySQL server 
	 * which have the highest version number.
	 * 
	 * retrieves databases of the form:
	 * anoCar1, anoGam1, apiMel1, apiMel2, bosTau2, bosTau3, bosTau4
	 * and stores only the most recent version in the variable recentDBs.
	 * 
	 */
    static {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                SequenceFetcher.class.getResourceAsStream(chiptyemapfilename)));
        try {
            String str = br.readLine();
            while (str != null && str.contains(",")) {
                String[] data = str.split(",");
                chiptypeMap.put(data[0].trim(), data[1].trim());
                str = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    	Statement statement ;
		try {
		Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			JOptionPane.showMessageDialog(null, ClassNotFoundException.class
					.getSimpleName()
					+ " for jdbc driver.", "Have you installed jdbc driver?",
					JOptionPane.ERROR_MESSAGE);
			log.error(cnfe, cnfe);
		}
			
		String dburl = UCSCDATABASEURL;
		Connection connection;
		try {
			connection = DriverManager.getConnection(dburl, "genome", "");
			statement = connection.createStatement();
			statement.execute("show databases");
			ResultSet resultSet = statement .getResultSet();
			while ( resultSet.next() ){
				allDBs.add( resultSet.getString(1));
			}
		    Collections.sort(allDBs);
		} catch (Exception e) {
			log.error(e);
		}

		Pattern stringPattern = Pattern.compile("\\D+"); 
		String mostRecentDBandVer = allDBs.get(0);
		Matcher matcher = stringPattern.matcher(mostRecentDBandVer);
		boolean foundDB = matcher.find();
		String currentDB = "";
		if (foundDB){
			currentDB = matcher.group();
		}

		String challenger = "";
		String challengersDB = "";
		for (int i=0; i< allDBs.size(); i++){
			challenger = allDBs.get(i);
			matcher = stringPattern.matcher(challenger);
			foundDB = matcher.find();
			if (foundDB){
				challengersDB = matcher.group();
			}

			if(!challengersDB.equals(currentDB)){
				recentDBs.add(mostRecentDBandVer);

				mostRecentDBandVer = allDBs.get(i);
				currentDB = challengersDB;
				continue;
			}
			
			if (mostRecentDBandVer.length() == challenger.length() ){
				if(mostRecentDBandVer.compareTo(challenger)<0){
					mostRecentDBandVer = challenger;
				}
			}

			if(mostRecentDBandVer.length() < challenger.length()){
				mostRecentDBandVer = challenger;
			}
		}
    }


    private SequenceFetcher() {
    }

    static SequenceFetcher getSequenceFetcher() {
        return theSequenceFetcher;
    }

    public String translateFromAffIDtoRefSeqID(String affID) {
        return null;
    }

    static CSSequenceSet<CSSequence> getAffyProteinSequences(String affyid) {
        CSSequenceSet<CSSequence> sequenceSet = new CSSequenceSet<CSSequence>();
        try {
            Call call = (Call) new Service().createCall();
            call.setTargetEndpointAddress(new java.net.URL(EBIURL));
            call.setOperationName(new QName("urn:Dbfetch", "fetchData"));
            call.addParameter("query", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("format", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("style", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.SOAP_ARRAY);
            String[] uniprotids = AnnotationParser.getInfo(affyid,
                    AnnotationParser.SWISSPROT);
            if (uniprotids != null) {
                for (int i = 0; i < uniprotids.length; i++) {
                    if (uniprotids[i] != null &&
                            !uniprotids[i].trim().equals("")) {
                        String[] result = (String[]) call.invoke(new Object[]{
                                "uniprot:" + uniprotids[i].trim(), "fasta",
                                "raw"});

                        if (result.length == 0) {
                            System.out.println("hmm...something wrong :-(\n");
                        } else {
                            if (result[0].trim().startsWith("<html>")) {
                                //retrieved some error message. skip this sequence.
                                continue;
                            }
                            CSSequence sequence = new CSSequence();
                            String label = affyid + "_" + uniprotids[i];
                            String seqStr = "";
                            for (int count = 1; count < result.length; count++) {
                               // seqStr += result[count] + newline;
                                      seqStr += result[count];//new line causes troubles when blastp directly. xz
                            }
                            sequence = new CSSequence(label, seqStr);
                            sequenceSet.addASequence(sequence);
                        }
                    }
                }
            }
        } catch (Exception e) {
        	if ( e.getMessage().contains("DbfNoEntryFoundException"))
        	 	 log.info("No result found for affyid " + affyid );
        	else
                 e.printStackTrace();
        }
        return sequenceSet;
    }

    static CSSequence[] getSequences(String geneName, String source) {
        if (source.equals(UCSC)) {
            return getSequences(geneName);
        }
        return null;
    }

    static String matchChipType(String chipId, String annotationFileName) {

		displayList.clear();
    	displayList.add("Select a genome");
		String db = "";
		Iterator<String> recentDBsIterator = recentDBs.iterator();
		while (recentDBsIterator.hasNext()) {
			db = (String) recentDBsIterator.next();
			// get display, such as "Human", from chiptypeDatabseMap.txt

			String displayString = chiptypeMap.get(db);
			String dbNoVersion = stringBegining(db);
			boolean foundOne = false;
			if (displayString!=null){
				displayString += " - ("+ db + ")";
				foundOne = true;
			}else {
				
				displayString = chiptypeMap.get(dbNoVersion);
				if (displayString!=null){
					displayString += " - ("+ db + ")";
					foundOne = true;
				}
			}

			if (foundOne){
				displayList.add(displayString);
				
				if(annotationFileName.contains("HG_") && dbNoVersion.equals("hg")){
					defaultChipChoice = displayString; 	
				}else if(annotationFileName.contains("Mouse") && dbNoVersion.equals("mm")){
					defaultChipChoice = displayString; 	
				}else if(annotationFileName.contains("Rat") && dbNoVersion.equals("rn")){
					defaultChipChoice = displayString; 	
				}
			}
		}
		showGenomeDialog();
		
		String database = null;
		if (selectedValue != null && !selectedValue.equals("Select a genome")) {
			genomeAssembly = (String) selectedValue;
			String choice = (String) selectedValue;
			database = choice.substring(choice.indexOf("(")+1, choice.indexOf(")"));
		}
		
		return database;
	}

    private static void showGenomeDialog() {
	    Runnable showInputDialog = new Runnable() {
	        public void run() {
	        	selectedValue = JOptionPane.showInputDialog(null,
	    				"Please select a species.\nIts latest genome version\nfrom UCSC will be used.", "Confirm Genome Version",
	    				JOptionPane.QUESTION_MESSAGE, null, displayList.toArray(),
	    				defaultChipChoice);		        }
	    };
	    try {
			SwingUtilities.invokeAndWait(showInputDialog);
		} catch (Exception e) {
			log.error(e);
		}
	}
    
    /**
     * getSequences
     *
     * @param geneName String
     * @return CSSequence[]
     */
    private static CSSequence[] getSequences(String geneName) {
        return null;
    }

    /**
	 * getGeneChromosomeMatchers
	 *
	 * @param geneName
	 *            String
	 * @param database
	 *            String
	 * @return any[]
     * @throws SQLException
	 */
	static Vector<GeneChromosomeMatcher> getGeneChromosomeMatchers(String geneName,
			String database) throws SQLException {
		if (database == null) {
			return null;
		}
		String[] columnName = { "chrom", "strand", "txStart", "txEnd" };
		Vector<GeneChromosomeMatcher> vector = new Vector<GeneChromosomeMatcher>();
		try {
			Statement stmt;
			Class.forName("com.mysql.jdbc.Driver");

			String url = UCSCDATABASEURL + database.trim()
					+ "?autoReconnect=true";
			Connection con = DriverManager.getConnection(url, "genome", "");
			stmt = con.createStatement();
			boolean success = stmt
					.execute("select known.chrom, known.strand, known.txStart, known.txEnd, kg.refseq from knownGene as known, kgXref as kg  where kg.refseq = '"
							+ geneName + "' and kg.kgID = known.name ");
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					String chrom = rs.getString(columnName[0]);
					boolean positiveStrand = true;
					if (rs.getString(columnName[1]).equalsIgnoreCase("-")) {
						positiveStrand = false;
					}
					int txStart = new Integer(rs.getString(columnName[2]))
							.intValue();
					int txEnd = new Integer(rs.getString(columnName[3]))
							.intValue();
					GeneChromosomeMatcher geneMatcher = new GeneChromosomeMatcher(
							positiveStrand, chrom, txStart, txEnd, database);
					geneMatcher.setName(geneName);
					vector.add(geneMatcher);
				}
			}
		} catch (SQLException sqle) {
			throw sqle;
		} catch (ClassNotFoundException cnfe) {
			JOptionPane.showMessageDialog(null, ClassNotFoundException.class
					.getSimpleName()
					+ " for jdbc driver.", "Have you installed jdbc driver?",
					JOptionPane.ERROR_MESSAGE);
			log.error(cnfe, cnfe);
		} catch (Exception e) {
			log.error(e);
		}
		return vector;
	}

    /**
	 * Retrieve sequences based on the chromosome position of the gene.
	 *
	 * @param geneChromosomeMatcher
	 * @param upstreamRegion
	 * @param downstreamRegion
	 * @return
	 */
    CSSequence getSequences(GeneChromosomeMatcher
            geneChromosomeMatcher,
                                   int upstreamRegion,
                                   int downstreamRegion) {
        if (geneChromosomeMatcher == null) {
            return null;
        }
        CSSequence sequence = null;
        if (geneChromosomeMatcher != null) {
            int upStartPoint = geneChromosomeMatcher.getStartPoint();
            int downStartPoint = geneChromosomeMatcher.getEndPoint();
            int startPoint = upStartPoint - upstreamRegion;
            int endPoint = upStartPoint + downstreamRegion - 1;
            if (!geneChromosomeMatcher.isPositiveStrandDirection()) {
                startPoint = downStartPoint - downstreamRegion + 1;
                endPoint = downStartPoint + upstreamRegion;
            }
            sequence = getSequence(geneChromosomeMatcher.
                    getGenomeBuildNumber(),
                    geneChromosomeMatcher.getChr(),
                    startPoint, endPoint,
                    geneChromosomeMatcher.
                            isPositiveStrandDirection());
        }
        return sequence;
    }

    /**
     * Contact UCSC to get the sequence back. Real workhorse.
     *
     * @param genomeBuilderName
     * @param chromosomeName
     * @param startPoint
     * @param endPoint
     * @param isPositiveStrand
     * @return
     */
    private CSSequence getSequence(String genomeBuilderName,
                                   String chromosomeName,
                                   int startPoint, int endPoint,
                                   boolean isPositiveStrand) {

        int maxSize = 1000000;
        String request = "http://genome.cse.ucsc.edu/cgi-bin/das/" +
                genomeBuilderName + "/dna?segment=" +
                chromosomeName +
                ":" + startPoint + ":" + endPoint;
        ;

        try {
            InputStream uin = new URL(request).openStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    uin));
            String line;
            StringBuffer sequenceContent = new StringBuffer();
            while ((line = in.readLine()) != null) {
                int size = 0;
                if (line.trim().startsWith("<DNA")) {
                    String[] str = line.split(">");
                    if (str.length > 1) {
                        sequenceContent.append(str[1]);
                        size = str[1].length();
                    }

                    String label = "request";

                    while ((line = in.readLine()) != null &&
                            !line.trim().endsWith("DNA>")) {
                        size += line.length();
                        if (size >= maxSize) {

                        }
                        sequenceContent.append(line);
                    }
                    String content = sequenceContent.toString();
                    if (!isPositiveStrand) {
                        content = CSSequence.reverseString(content);
                    }
                    CSSequence seq = new CSSequence(label,
                            content);
                    return seq;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    static String getGenomeAssembly()
    {
    	return genomeAssembly;
    }

    /*
     * Take a String like hg19 and return hg
     */
    private static String stringBegining(String StringAndInt){
		Pattern stringPattern = Pattern.compile("\\D+"); 
		Matcher matcher = stringPattern.matcher(StringAndInt);
		boolean foundOne = matcher.find();
		String justString = "";
		if (foundOne){
			justString = matcher.group();
		}
    	return justString;
    }
    
}