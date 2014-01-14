/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.server.services;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.pfgen.axiom.client.services.RunningGenoAnalysisService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.shared.records.RunningGenotypingAnalysis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 *
 * @author eric
 */
public class RunningGenoAnalysisServiceImpl extends RemoteServiceServlet implements RunningGenoAnalysisService {

    private ConnectionPool pool;
    //private Hashtable<String, File> appFiles; 

    //@SuppressWarnings("unchecked")
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        pool = (ConnectionPool) getServletContext().getAttribute("ConnectionPool");
        //appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
    }

    @Override
    public List<RunningGenotypingAnalysis> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {
        List<RunningGenotypingAnalysis> list = (List<RunningGenotypingAnalysis>) getServletContext().getAttribute("runningGenoAnalysisList");
        if (list == null || list.isEmpty()) {
            return new ArrayList<RunningGenotypingAnalysis>();
        } else {
            Collections.sort(list, new Comparator<RunningGenotypingAnalysis>() {
                @Override
                public int compare(RunningGenotypingAnalysis o1, RunningGenotypingAnalysis o2) {
                    return o1.getStartDate().compareTo(o2.getStartDate());
                }
            });
            return list;
        }
    }

    @Override
    public RunningGenotypingAnalysis add(RunningGenotypingAnalysis data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RunningGenotypingAnalysis update(RunningGenotypingAnalysis data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void remove(RunningGenotypingAnalysis data) {
        if (data.getStatus().equals(RunningGenotypingAnalysis.GenoAnaRunningStatus.DONE) && data.getEndDate() != null) {
            //RunningGenotypingAnalysis anaToRemove = new RunningGenotypingAnalysis();
            //anaToRemove.setName(data.getName());
            List<RunningGenotypingAnalysis> list = (List<RunningGenotypingAnalysis>) getServletContext().getAttribute("runningGenoAnalysisList");
            list.remove(data);
            //getServletContext().removeAttribute("runningGenoAnalysisList");
            //getServletContext().setAttribute("runningGenoAnalysisList", list);
        }
    }

    @Override
    public String download(String sortBy, Map<String, String> filterCriteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
