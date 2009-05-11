package org.geworkbench.components.pudge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.structure.PudgeResultSet;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ProteinSequenceAnalysis;

/**
 * Pudge analysis for protein fasta sequence
 * 
 * @author mw2518
 * @version $Id: PudgeAnalysis.java,v 1.2 2009-05-11 20:32:45 jiz Exp $
 */
public class PudgeAnalysis extends AbstractGridAnalysis implements
		ProteinSequenceAnalysis {
	private static final long serialVersionUID = 1L;
	public PudgeConfigPanel pcp;
	String strurl = "http://luna.bioc.columbia.edu/honiglab/pudge/cgi-bin/pipe_int.cgi";
	String req = "--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"dir_name\"\r\n\r\n@\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"domain\"\r\n\r\nnone\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"start\"\r\n\r\nT\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"enterX\"\r\n\r\nselect_methods\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"get_fa\"\r\n\r\n\r\n--AaB03x\r\n"
			+ "content-disposition: form-data; name=\"file\"; filename=\"protein.fasta\"\r\nContent-Type: text/plain\r\n\r\n";

	PudgeAnalysis() {
		setLabel("Pudge Analysis");
		pcp = new PudgeConfigPanel();
		setDefaultPanel(pcp);
	}

	public AlgorithmExecutionResults execute(Object input) {
		if (input == null)
			return new AlgorithmExecutionResults(false, "Invalid input. ", null);
		assert input instanceof DSSequenceSet;

		DSSequenceSet seq = (DSSequenceSet) input;
		File seqfile = seq.getFile();
		File fastafile = seqfile.getAbsoluteFile();
		String resultURL = "";

		try {
			URL url = new URL(strurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(true);
			conn.setAllowUserInteraction(true);
			conn.setRequestProperty("Content-Type",
					"multipart/form-data, boundary=AaB03x");
			conn.setRequestProperty("Content-Transfer-Encoding", "binary");
			conn.connect();

			String jobname = pcp.getjobnameValue() + "_"
					+ new java.sql.Timestamp(new java.util.Date().getTime());
			String request = req
					.replaceFirst("@", jobname.replaceAll(" ", "_"));
			OutputStream out = conn.getOutputStream();
			out.write(request.getBytes());
			FileInputStream fis = new FileInputStream(fastafile);
			byte[] buffer = new byte[4096];
			int bytes_read;
			while ((bytes_read = fis.read(buffer)) != -1) {
				out.write(buffer, 0, bytes_read);
			}
			out.write("\r\n--AaB03x--\r\n".getBytes());
			out.flush();
			out.close();
			fis.close();

			InputStream dat = conn.getInputStream();
			String contenttype = conn.getContentType();

			if (contenttype.toLowerCase().startsWith("text")) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						dat));
				String line;
				while ((line = in.readLine()) != null) {
					if (line.startsWith("window.location")) {
						resultURL = line.substring(19, line.length() - 2);
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (resultURL.length() == 0)
			return new AlgorithmExecutionResults(true,
					"Error in pudge website",
					new PudgeResultSet(seq, resultURL));

		return new AlgorithmExecutionResults(true, "No errors",
				new PudgeResultSet(seq, resultURL));
	}

	public int getAnalysisType() {
		return PUDGE_TYPE;
	}

	public String getType() {
		return "PudgeAnalysis";
	}

	public String getAnalysisName() {
		return "Pudge";
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		return null;
	}

	@Override
	public Class<?> getBisonReturnType() {
		return null;
	}

	@Override
	protected boolean useMicroarraySetView() {
		return false;
	}

	@Override
	protected boolean useOtherDataSet() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet refMASet) {
		// TODO Auto-generated method stub
		return new ParamValidationResults(true, null);
	}
}
