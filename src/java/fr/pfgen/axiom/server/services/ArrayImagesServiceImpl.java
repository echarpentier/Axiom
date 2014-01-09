package fr.pfgen.axiom.server.services;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import fr.pfgen.axiom.client.services.ArrayImagesService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.SamplesTable;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.ArrayImageRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class ArrayImagesServiceImpl extends RemoteServiceServlet implements ArrayImagesService {

    private ConnectionPool pool;
    private Hashtable<String, File> appFiles;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        pool = (ConnectionPool) getServletContext().getAttribute("ConnectionPool");
        appFiles = (Hashtable<String, File>) getServletContext().getAttribute("ApplicationFiles");
    }

    @Override
    public List<ArrayImageRecord> fetch(Integer startRow, Integer endRow, final String sortBy, Map<String, String> filterCriteria) {

        GenericGwtRpcList<ArrayImageRecord> outList = new GenericGwtRpcList<ArrayImageRecord>();
        String arrayImageFolder = appFiles.get("arrayImageFile").getAbsolutePath();

        List<ArrayImageRecord> out = SamplesTable.getArrayImageRecord(pool, arrayImageFolder, startRow, endRow, sortBy, filterCriteria);

        outList.addAll(out);
        outList.setTotalRows(out.size());

        return outList;
    }

    @Override
    public String downloadThumbnailsPdf(String plateName) {
        String arrayImageFolder = appFiles.get("arrayImageFile").getAbsolutePath();
        Map<String, String> crits = new Hashtable<String, String>();
        crits.put("plateName", plateName);
        List<ArrayImageRecord> sampleList = SamplesTable.getArrayImageRecord(pool, arrayImageFolder, null, null, null, crits);
        File tmpFolder = appFiles.get("temporaryFolder");

        Document pdfDoc = new Document(PageSize.A4.rotate(), 0, 0, 5, 0);
        File doc = new File(tmpFolder, "Thumbs_" + plateName + ".pdf");

        try {
            PdfWriter.getInstance(pdfDoc, new FileOutputStream(doc));
            pdfDoc.open();
            //pdfDoc.setPageSize(PageSize.A4.rotate());
            pdfDoc.setPageCount(1);

            PdfPTable pdfTable = new PdfPTable(13);
            pdfTable.setKeepTogether(true);

            String[] header = {"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

            for (String s : header) {
                FontSelector fontselector = new FontSelector();
                Font font = new Font();
                font.setSize(10);
                font.setFamily(FontFamily.TIMES_ROMAN.toString());
                font.setColor(BaseColor.RED);
                fontselector.addFont(font);

                pdfTable.addCell(fontselector.process(s));
            }
            int i = 0;
            String letters = "ABCDEFGH";
            for (ArrayImageRecord record : sampleList) {
                if ((i % 13) == 0) {
                    FontSelector fontselector = new FontSelector();
                    Font font = new Font();
                    font.setSize(10);
                    font.setFamily(FontFamily.TIMES_ROMAN.toString());
                    font.setColor(BaseColor.RED);
                    fontselector.addFont(font);
                    pdfTable.addCell(fontselector.process(letters.substring((i / 13), (i / 13) + 1)));
                    i++;
                }
                FontSelector fontselector = new FontSelector();
                Font font = new Font();
                font.setSize(8);
                font.setFamily(FontFamily.TIMES_ROMAN.toString());
                font.setColor(BaseColor.BLUE);
                fontselector.addFont(font);

                Image img = Image.getInstance(record.getThumbnailPath());
                PdfPTable celTable = new PdfPTable(1);
                celTable.addCell(fontselector.process(record.getName()));
                celTable.addCell(img);

                PdfPCell cell = new PdfPCell(celTable);
                pdfTable.addCell(cell);
                i++;
            }
            pdfTable.setHeaderRows(1);
            pdfDoc.add(pdfTable);

            pdfDoc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return doc.getAbsolutePath();
    }
}